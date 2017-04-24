import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * <pre class="doc_header">
 * <p>
 * </pre>
 *
 * @author kelmore5
 * @custom.date 4/23/17
 */
class HTMLController {
    private HashMap<String, ArrayList<Element>> tags;

    HTMLController() {
        tags = new HashMap<>();
    }

    Document getJSoupDoc(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch(IOException ex) {
            System.out.println("Jsoup crashed...\n");
            ex.printStackTrace();
        }

        return doc;
    }

    HashMap<String, ArrayList<Element>> processDocument(Document doc) {
        Element body = doc.body();
        for(Element element: body.getAllElements()) {
            ArrayList<Element> elements = tags.computeIfAbsent(element.tagName(), k -> new ArrayList<>());
            elements.add(element);
        }
        return tags;
    }
}
