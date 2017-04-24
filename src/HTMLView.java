import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import org.jsoup.nodes.Element;

/**
 * <pre class="doc_header">
 * <p>
 * </pre>
 *
 * @author kelmore5
 * @custom.date 4/21/17
 */
public class HTMLView extends JFrame implements Runnable {
    @SuppressWarnings("FieldCanBeLocal")
    private final String title = "HTML Tag Generator";
    private HTMLController controller;
    private HashMap<String, ArrayList<Element>> tags;
    private HTMLDisplayPane displayPane;

    private HTMLView() {
        controller = new HTMLController();
        displayPane = new HTMLDisplayPane();

        this.createFileMenu();
        displayPane.setBackground(Color.BLACK);
        this.getContentPane().add(displayPane);
    }

    private void createFileMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem enter = new JMenuItem("Enter URL");
        enter.addActionListener(e -> {
            String url = JOptionPane.showInputDialog("Enter a full URL for processing (including 'http://www.xx'):");
            tags = controller.processDocument(controller.getJSoupDoc(url));
            displayPane.processHTMLTags(url, tags);
        });

        JMenu autoSearch = new JMenu("Get Tags for...");
        JMenuItem google = new JMenuItem("Google");
        google.addActionListener(e-> {
            String url = "http://www.google.com";
            tags = controller.processDocument(controller.getJSoupDoc(url));
            displayPane.processHTMLTags(url, tags);
        });

        JMenu searchBy = new JMenu("Search By");
        JMenuItem searchByTag = new JMenuItem("Tag");
        JMenuItem searchByID = new JMenuItem("ID");
        JMenuItem searchByClass = new JMenuItem("Class");
        searchByTag.addActionListener(e-> displayPane.searchBy(HTMLDisplayPane.TAG));
        searchByID.addActionListener(e-> displayPane.searchBy(HTMLDisplayPane.ID));
        searchByClass.addActionListener(e-> displayPane.searchBy(HTMLDisplayPane.CLASS));

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e-> System.exit(0));

        JMenu viewMenu = new JMenu("View");
        JMenu sortBy = new JMenu("Sort By");
        JMenuItem sortByTag = new JMenuItem("Tag");
        JMenuItem sortByID = new JMenuItem("ID");
        JMenuItem sortByClass = new JMenuItem("Class");

        sortByTag.addActionListener(e -> displayPane.sortTags(HTMLDisplayPane.TAG));
        sortByID.addActionListener(e -> displayPane.sortTags(HTMLDisplayPane.ID));
        sortByClass.addActionListener(e -> displayPane.sortTags(HTMLDisplayPane.CLASS));

        searchBy.add(searchByTag);
        searchBy.add(searchByID);
        searchBy.add(searchByClass);

        autoSearch.add(google);

        sortBy.add(sortByTag);
        sortBy.add(sortByID);
        sortBy.add(sortByClass);

        fileMenu.add(enter);
        fileMenu.add(autoSearch);
        fileMenu.add(exit);
        viewMenu.add(sortBy);
        viewMenu.add(searchBy);

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        this.setJMenuBar(menuBar);
    }

    public void run() {
        setSize(800,800);
        setTitle(title);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        HTMLView grabber = new HTMLView();
        javax.swing.SwingUtilities.invokeLater(grabber);
    }
}
