package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;

import java.util.ArrayList;
import java.util.List;

import org.gwtproject.xml.client.Element;
import org.gwtproject.xml.client.Node;
import org.gwtproject.xml.client.NodeList;

public class XMLUtils {
    /**
     * Liefert den Text eines LocalisedText-Elementes zurück gemäss gewünschter
     * Sprache. Wird die Sprache nicht gefunden, wird das erste Element, das
     * gefunden wird, zurückgeliefert.
     * Achtung: LanguageCode muss nicht einmal vorhanden sein.
     * 
     * @param element
     * @param language
     */
    public static String getLocalisedTextByLanguage(Element element, String language) {
        List<Element> textElementList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(element, "Text/LocalisedText", textElementList);

        String firstFoundText = null;
        
        for (int i = 0; i < textElementList.size(); i++) {
            Element el = (Element) textElementList.get(i);
            
            String code = XMLUtils.getElementValueByPath(el, "Language");
            String text = XMLUtils.getElementValueByPath(el, "Text");
                        
            if (firstFoundText == null) {
                firstFoundText = text;
            }
            
            if (code != null && code.equalsIgnoreCase(language)) {
                return text;
            }
        }

        return firstFoundText;
    }

    public static String getElementValueByPath(Element root, String path, String ret) {
        if (root == null || path == null) return null;
        
        //console.log("root:"+root.getNodeName());
        //console.log("path:"+path);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        String[] pathElements = path.split("/");
        int pathElementsLength = pathElements.length;

        // TODO: "*" && length() == 1
        String pathElement = pathElements[0];
        if (pathElement.endsWith("*") && pathElement.length() > 1) {
            pathElement = pathElement.substring(0,pathElement.length()-1);
        }
        
        NodeList childNodes = root.getChildNodes();
        for (int i=0; i<childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                String nodeName = childElement.getNodeName();
                if (nodeName.contains(":"+pathElement)) {
                    if (pathElementsLength == 1) {
                        return childElement.getFirstChild().getNodeValue();
                    } else {
                        return getElementValueByPath(childElement, path.substring(path.indexOf("/")+1), ret);
                    }
                }
            }
        }
        return ret;
    }
    
    
    // TODO: https://www.geeksforgeeks.org/wildcard-character-matching/
    public static String getElementValueByPath(Element root, String path) {
        return getElementValueByPath(root, path, null);
    }
    
    // Path beginnt mit dem ersten Kindelement.
    public static void getElementsByPath(Element root, String path, List<Element> list) {
        //console.log("root:"+root.getNodeName());
        //console.log("path:"+path);
        
        if (root == null || path == null) return;

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        
        String[] pathElements = path.split("/");
        int pathElementsLength = pathElements.length;
        //console.log(pathElementsLength);

        String pathElement = pathElements[0];
        if (pathElement.endsWith("*") && pathElement.length() > 1) {
            pathElement = pathElement.substring(0,pathElement.length()-1);
        }

        NodeList childNodes = root.getChildNodes();
        for (int i=0; i<childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                //console.log(childElement.getNodeName());
                String nodeName = childElement.getNodeName();
                if (nodeName.contains(":"+pathElement) || pathElement.equals("*")) {
                    //console.log("children found");
                    if (pathElementsLength == 1) {
                        //console.log("abbruch");
                        list.add(childElement);
                    } else {
                        getElementsByPath(childElement, path.substring(path.indexOf("/")+1), list);
                    }
                }
            }
        }
        return;
    }
}

