import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.Position;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import org.jsoup.nodes.Element;

/**
 * <pre class="doc_header">
 * <p>
 * </pre>
 *
 * @author kelmore5
 * @custom.date 4/23/17
 */
class HTMLDisplayPane extends JSplitPane implements TreeSelectionListener {
    static final String TAG = "Tag", ID = "ID", CLASS = "Class";
    private JSplitPane topPane;
    private HashMap<String, JTree> trees;
    private JTree currentTree;
    private JLabel tagInfo;
    private JTextArea htmlView;
    private String currentURL;

    HTMLDisplayPane() {
        trees = new HashMap<>();
        topPane = new JSplitPane();
        tagInfo = new JLabel();
        trees.put(TAG, new JTree(new DefaultMutableTreeNode("Tags")));
        trees.put(ID, new JTree());
        trees.put(CLASS, new JTree());
        currentTree = trees.get(TAG);
        htmlView = new JTextArea();
        currentURL = "";

        setupPanel();
    }

    private void setupPanel() {
        this.setOrientation(JSplitPane.VERTICAL_SPLIT);

        topPane.setLeftComponent(new JScrollPane(currentTree));
        topPane.setRightComponent(tagInfo);

        htmlView.setEditable(false);
        htmlView.setWrapStyleWord(true);

        this.setLeftComponent(topPane);
        this.setRightComponent(new JScrollPane(htmlView));
    }

    void processHTMLTags(String url, HashMap<String, ArrayList<Element>> tags) {
        this.currentURL = url;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.currentURL);
        DefaultMutableTreeNode sortBy = new DefaultMutableTreeNode("Tags");
        root.add(sortBy);
        JTree htmlTreeByTag = new JTree(root);

        String[] keys = tags.keySet().toArray(new String[] {});
        Arrays.sort(keys);

        for(String key: keys) {
            DefaultMutableTreeNode keyRoot = new DefaultMutableTreeNode(key);
            ArrayList<Element> elements = tags.get(key);
            elements.sort(new ElementIDComparator());

            for(Element element: elements) {
                String nodeString = key;
                nodeString += element.id().isEmpty() ? "" : " " + element.id();
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeString);
                newNode.add(new DefaultMutableTreeNode(element));
                keyRoot.add(newNode);
            }

            sortBy.add(keyRoot);
        }

        htmlTreeByTag.addTreeSelectionListener(this);
        swapHTMLTree(htmlTreeByTag);

        trees.put(TAG, htmlTreeByTag);
        trees.put(ID, createIDTree(tags));
        trees.put(CLASS, createClassTree(tags));
    }

    private JTree createIDTree(HashMap<String, ArrayList<Element>> tags) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.currentURL);
        DefaultMutableTreeNode sortBy = new DefaultMutableTreeNode("IDs");
        root.add(sortBy);

        JTree htmlTreeByID = new JTree(root);

        ArrayList<Element> temp = new ArrayList<>();
        for(ArrayList<Element> elements: tags.values()) {
            temp.addAll(elements);
        }

        temp.sort(new ElementIDComparator());

        for(Element e: temp) {
            DefaultMutableTreeNode idRoot = new DefaultMutableTreeNode(e.id().isEmpty() ?
                    "*blank ID* (" + e.tagName() + ")"  : e.id() + " (" + e.tagName() + ")");
            DefaultMutableTreeNode htmlNode = new DefaultMutableTreeNode(e);

            idRoot.add(htmlNode);
            sortBy.add(idRoot);
        }

        htmlTreeByID.addTreeSelectionListener(this);
        return htmlTreeByID;
    }

    private JTree createClassTree(HashMap<String, ArrayList<Element>> tags) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this.currentURL);
        DefaultMutableTreeNode sortBy = new DefaultMutableTreeNode("Class");
        root.add(sortBy);
        JTree htmlTreeByClass = new JTree(root);

        HashMap<String, ArrayList<Element>> newTagList = new HashMap<>();
        for(ArrayList<Element> elements: tags.values()) {
            for(Element element: elements) {
                ArrayList<Element> elements_new = newTagList.computeIfAbsent(element.className(), k -> new ArrayList<>());
                elements_new.add(element);
            }
        }

        String[] keys = newTagList.keySet().toArray(new String[] {});
        Arrays.sort(keys);

        for(String key: keys) {
            DefaultMutableTreeNode keyRoot = new DefaultMutableTreeNode(key);
            ArrayList<Element> elements = newTagList.get(key);
            elements.sort(new ElementIDComparator());

            for(Element element: elements) {
                String nodeString = key;
                nodeString += element.id().isEmpty() ? "" : " " + element.id();
                nodeString += " (" + element.tagName() + ")";
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(nodeString);
                newNode.add(new DefaultMutableTreeNode(element));
                keyRoot.add(newNode);
            }

            sortBy.add(keyRoot);
        }

        htmlTreeByClass.addTreeSelectionListener(this);
        return htmlTreeByClass;
    }

    private void swapHTMLTree(JTree tree) {
        currentTree = tree;
        topPane.remove(topPane.getLeftComponent());
        topPane.setLeftComponent(new JScrollPane(currentTree));
        currentTree.expandPath(currentTree.getPathForRow(1));
    }

    void sortTags(String sortType) {
        JTree temp = trees.get(sortType);

        if(currentTree != temp)
            swapHTMLTree(temp);
    }

    void searchBy(String searchType) {
        String searchTerm = JOptionPane.showInputDialog("Enter the " + searchType + " to search for: ");
        JTree temp = trees.get(searchType);

        if(currentTree != temp)
            swapHTMLTree(temp);

        TreePath path = currentTree.getNextMatch(searchTerm, 0, Position.Bias.Forward);
        currentTree.scrollPathToVisible(path);
        currentTree.setSelectionPath(path);
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentTree.getLastSelectedPathComponent();

        if(node == null)
            return;

        if(!node.isLeaf()) {
            if(node.getFirstChild().isLeaf())
                node = node.getFirstLeaf();
            else {
                htmlView.setText("");
                tagInfo.setText("");
                return;
            }
        }

        Element nodeInfo = (Element) node.getUserObject();
        htmlView.setText(nodeInfo.toString());
        fillTagInfo(nodeInfo);
    }

    private void fillTagInfo(Element e) {
        String output = "<html><ul>";
        output += "<li>Tag: " + e.tagName() + "</li>";
        output += "<li>ID: " + e.id() + "</li>";
        output += "<li>Class: " + e.className() + "</li>";
        output += "</ul></html>";
        tagInfo.setText(output);
    }

    private class ElementIDComparator implements Comparator<Element> {
        @Override
        public int compare(Element o1, Element o2) {
            return o1.id().compareTo(o2.id());
        }
    }
}
