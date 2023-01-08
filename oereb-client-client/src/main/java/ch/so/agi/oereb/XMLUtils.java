package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;

import java.util.ArrayList;
import java.util.List;

import org.gwtproject.xml.client.Element;
import org.gwtproject.xml.client.Node;
import org.gwtproject.xml.client.NodeList;

import ol.Coordinate;
import ol.OLFactory;

public class XMLUtils {
    /**
     * Erstellt eine Liste aus Grundstueck-Pojo aus einem GetEgridResponse-Element.
     * 
     * @param Element
     * @return
     */
    public static List<Grundstueck> createGrundstuecke(Element root, String language) {
        List<Grundstueck> grundstueckeList = new ArrayList<Grundstueck>();

        NodeList childNodes = root.getChildNodes();
        Grundstueck grundstueck = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                String nodeName = childElement.getNodeName();

                if (nodeName.contains("egrid")) {
                    grundstueck = new Grundstueck();
                    grundstueck.setEgrid(childElement.getFirstChild().getNodeValue());
                }

                if (nodeName.contains("number")) {
                    grundstueck.setNummer(childElement.getFirstChild().getNodeValue());
                }

                if (nodeName.contains("identDN")) {
                    grundstueck.setNbident(childElement.getFirstChild().getNodeValue());
                }

                if (nodeName.contains("type")) {
                    String art = XMLUtils.getLocalisedTextByLanguage((Element)childElement.getElementsByTagName("Text").item(0), language);
                    grundstueck.setArt(art);
                }

                if (nodeName.contains("limit")) {
                    ol.geom.Geometry geom = XMLUtils.createGeometry(childElement);
                    grundstueck.setGeometrie(geom);
                    grundstueckeList.add(grundstueck);
                }
            }
        }
        return grundstueckeList;
    }
    
   /**
    * Erstellt aus einem XML-Element mit INTERLIS-Kodierung
    * ein Openlayers Polygon. Es wird nur der Geometrietyp
    * 'Polygon' unterstützt.
    * 
    * @param Element
    */
    public static ol.geom.Geometry createGeometry(Element element) {
        List<ol.Coordinate[]> ringList = new ArrayList<ol.Coordinate[]>();

        // Exterior ring
        List<Element> exteriorElementList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(element, "surface/exterior/polyline", exteriorElementList);

        if (exteriorElementList.size() == 0) {
            return null;
        }

        // There can only be only one exterior polyline.
        Element exteriorPolyline = exteriorElementList.get(0);

        List<ol.Coordinate> exteriorCoordList = new ArrayList<ol.Coordinate>();
        NodeList exteriorChildNodes = exteriorPolyline.getChildNodes();
        for (int i = 0; i < exteriorChildNodes.getLength(); i++) {
            if (exteriorChildNodes.item(i) instanceof Element) {
                Element childElement = (Element) exteriorChildNodes.item(i);

                String c1 = XMLUtils.getElementValueByPath(childElement, "c1");
                String c2 = XMLUtils.getElementValueByPath(childElement, "c2");

                ol.Coordinate coord = OLFactory.createCoordinate(Double.valueOf(c1), Double.valueOf(c2));
                exteriorCoordList.add(coord);
            }
        }

        ringList.add((Coordinate[]) exteriorCoordList.toArray());

        // Interior rings
        List<Element> interiorElementList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(element, "surface/interior/polyline", interiorElementList);

        if (interiorElementList.size() > 0) {
            for (Element interiorPolyline : interiorElementList) {
                List<ol.Coordinate> interiorCoordList = new ArrayList<ol.Coordinate>();
                NodeList interiorChildNodes = interiorPolyline.getChildNodes();
                for (int i = 0; i < interiorChildNodes.getLength(); i++) {
                    if (interiorChildNodes.item(i) instanceof Element) {
                        Element childElement = (Element) interiorChildNodes.item(i);

                        String c1 = XMLUtils.getElementValueByPath(childElement, "c1");
                        String c2 = XMLUtils.getElementValueByPath(childElement, "c2");

                        ol.Coordinate coord = OLFactory.createCoordinate(Double.valueOf(c1), Double.valueOf(c2));
                        interiorCoordList.add(coord);
                    }
                }
                ringList.add((Coordinate[]) interiorCoordList.toArray());
            }
        }
        
        ol.geom.Polygon polygon = new ol.geom.Polygon((Coordinate[][]) ringList.toArray());
        return polygon;
    }

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
        XMLUtils.getElementsByPath(element, "LocalisedText", textElementList);

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
        console.log("root:"+root.getNodeName());
        console.log("path:"+path);
        
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

