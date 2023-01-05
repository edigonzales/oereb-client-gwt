package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dominokit.domino.ui.badges.Badge;
import org.dominokit.domino.ui.breadcrumbs.Breadcrumb;
import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.infoboxes.InfoBox;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.tabs.Tab;
import org.dominokit.domino.ui.tabs.TabsPanel;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.TextNode;

import com.google.gwt.core.client.GWT;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.gwtproject.xml.client.XMLParser;
import org.gwtproject.xml.client.Element;
import org.gwtproject.xml.client.NodeList;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.i18n.client.DateTimeFormat;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import elemental2.dom.AbortController;
import elemental2.dom.CSSProperties;
import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Headers;
import elemental2.dom.Location;
import elemental2.dom.RequestInit;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.AtPixelOptions;
import ol.Extent;
import ol.Map;
import ol.MapBrowserEvent;
import ol.OLFactory;
import ol.Pixel;
import ol.events.condition.Condition;
import ol.Feature;
import ol.FeatureAtPixelFunction;
import ol.format.GeoJson;
import ol.interaction.Select;
import ol.interaction.SelectOptions;
import ol.layer.Base;
import ol.layer.Layer;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.style.Fill;
import ol.style.Stroke;
import ol.style.Style;
import proj4.Proj4;

public class App implements EntryPoint {
    // Internationalization
    private MyMessages messages = GWT.create(MyMessages.class);

    // Application settings
    private String myVar;
    private String SEARCH_SERVICE_URL;
    private String CANTON_SERVICE_URL;
    private JsPropertyMap<Object> OEREB_SERVICE_URLS;

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    // Projection
    // private static final String EPSG_2056 = "EPSG:2056";
    // private static final String EPSG_4326 = "EPSG:4326";
    // private Projection projection;

    // Browser url components
    // private Location location;
    // private String pathname;
    private UrlComponents urlComponents;

    // ol3 map
    private String MAP_DIV_ID = "map";
    private Map map;
    private HTMLElement mapElement;
    private ol.Overlay realEstatePopup;

    public void onModuleLoad() {
        // Change Domino UI color scheme.
        Theme theme = new Theme(ColorScheme.BLUE);
        theme.apply();

        // Get url from browser (client) to find out the correct location for resources.
        Location location = DomGlobal.window.location;
        String pathname = location.pathname;

        if (pathname.contains("index.html")) {
            pathname = pathname.replace("index.html", "");
        }

        urlComponents = new UrlComponents(location, pathname);

        // Get settings with a synchronous request.
        XMLHttpRequest httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", pathname + "settings", false);
        httpRequest.onload = event -> {
            if (Arrays.asList(200, 201, 204).contains(httpRequest.status)) {
                String responseText = httpRequest.responseText;
                try {
                    JsPropertyMap<Object> propertiesMap = Js.asPropertyMap(Global.JSON.parse(responseText));
                    SEARCH_SERVICE_URL = propertiesMap.getAsAny("searchServiceUrl").asString();
                    CANTON_SERVICE_URL = propertiesMap.getAsAny("cantonServiceUrl").asString();
                    OEREB_SERVICE_URLS = propertiesMap.getAsAny("oerebServiceUrls").asPropertyMap();
                } catch (Exception e) {
                    DomGlobal.window.alert("Error loading settings!");
                    DomGlobal.console.error("Error loading settings!", e);
                }
            } else {
                DomGlobal.window.alert("Error loading settings!" + httpRequest.status);
            }

        };
        httpRequest.addEventListener("error", event -> {
            DomGlobal.window
                    .alert("Error loading settings! Error: " + httpRequest.status + " " + httpRequest.statusText);
        });
        httpRequest.send();

        // Initialize site
        init();
    }

    public void init() {
        // Add and create ol3 map element and object.
        mapElement = div().id(MAP_DIV_ID).element();
        body().add(mapElement);
        map = new GeodiensteMapPreset().getMap(MAP_DIV_ID);
        map.addSingleClickListener(new MapSingleClickListener());

        // Search box
        SearchBox searchBox = new SearchBox(urlComponents, messages, SEARCH_SERVICE_URL, CANTON_SERVICE_URL);
        body().add(searchBox);

        body().element().addEventListener("location_found", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                console.log("FUUUUUUBAR2");

                CustomEvent customEvent = ((CustomEvent) evt);
                SearchResult searchResult = (SearchResult) customEvent.detail;
                console.log(searchResult);

                getEgrid(searchResult, true);
            }
        });
    }

    public void getEgrid(SearchResult searchResult, boolean limit) {
        String oerebServiceUrl = ((JsString) OEREB_SERVICE_URLS.get(searchResult.getCanton())).normalize();
        String coord = searchResult.getCoordinate().toStringXY(3).replace(" ", "");

        DomGlobal.fetch(oerebServiceUrl + "getegrid/xml/?GEOMETRY=true&EN=" + coord).then(response -> {
            if (!response.ok) {
                DomGlobal.window.alert("!response.ok...");
                return null;
            }
            if (response.status == 204) {
                DomGlobal.window.alert("No EGRID found. Response code: 204.");
                return null;
            }
            return response.text();
        }).then(xml -> {
//            console.log(xml.toString());
            org.gwtproject.xml.client.Document doc = XMLParser.parse(xml);

            List<Grundstueck> grundstueckeList = createGrundstuecke(doc.getDocumentElement());

//            List<Element> egridElementList = new ArrayList<Element>();
//            XMLUtils.getElementsByPath(doc.getDocumentElement(), "egrid", egridElementList);
//            
//            if (egridElementList.size() == 0) {
//                DomGlobal.window.alert("No EGRID found.");
//                return null;
//            }

            // limit==1: Aufruf aus Suche.
//            if (limit) {
//                Element el = (Element) egridElementList.get(0);
//                Grundstueck grundstueck = createGrundstueck(el);
//                

//                
//
//            } else {
//                List<Grundstueck> grundstueckList = new ArrayList<Grundstueck>();
//                for (int i=0;i<egridElementList.size();i++) {
//                    Element el = (Element) egridElementList.get(i);
//                    Grundstueck grundstueck = createGrundstueck(el);                    
//                    grundstueckList.add(grundstueck);
//                }
//                
//                // if > 1 -> GUI-Auswahl
//                selectEgrid(grundstueckList); 
//                
//                
//            }

            // dispatch? getExtract?

            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });
    }

    private List<Grundstueck> createGrundstuecke(Element root) {
        List<Grundstueck> grundstueckeList = new ArrayList<Grundstueck>();

        NodeList childNodes = root.getChildNodes();
        Grundstueck grundstueck = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                String nodeName = childElement.getNodeName();

//                console.log("i: " + i);
//                console.log("nodeName: " + nodeName);

                if (nodeName.contains("egrid")) {
                    grundstueck = new Grundstueck();
                    grundstueck.setEgrid(childElement.getFirstChild().getNodeValue());

//                    console.log(childElement.getNextSibling().getNodeType());
//                    Element numberElement = (Element) childElement.getNextSibling();
//                    grundstueck.setNummer(numberElement.getFirstChild().getNodeValue());
//                                       
//                    Element identdnElement = (Element) numberElement.getNextSibling();
//                    grundstueck.setNbident(identdnElement.getFirstChild().getNodeValue());
//                               
//                    Element typeElement = (Element) identdnElement.getNextSibling();
//                    getLocalisedTextByLanguage(typeElement, "de");
////                    grundstueck.setArt(nodeName);
//                    
//                    Element limitElement = (Element) typeElement.getNextSibling();
//                    grundstueck.setGeometrie(null);

                }

                if (nodeName.contains("number")) {
                    grundstueck.setNummer(childElement.getFirstChild().getNodeValue());
                }

                if (nodeName.contains("identDN")) {
                    grundstueck.setNbident(childElement.getFirstChild().getNodeValue());
                }

                if (nodeName.contains("type")) {
                    String art = XMLUtils.getLocalisedTextByLanguage(childElement, "de");
                    grundstueck.setArt(art);
                }

                if (nodeName.contains("limit")) {
                    // auch wieder whitespaces... aber zuerst gehts ums Grundstueckelement.
//                    console.log(childElement.getFirstChild().getNodeName());
//                    ol.geom.Geometry = 
                    createGeometry(childElement);
                    grundstueckeList.add(grundstueck);
                }

            }
        }

//        console.log(grundstueckList);

        return grundstueckeList;
    }

    public ol.geom.Geometry createGeometry(Element element) {
        List<Element> exteriorList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(element, "surface/exterior/polyline", exteriorList);

        if (exteriorList.size() == 0) {
            return null;
        }

        // There can only be one exterior polyline.
        Element exteriorPolyline = exteriorList.get(0);
        console.log(exteriorPolyline);

        NodeList childNodes = exteriorPolyline.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            if (childNodes.item(i) instanceof Element) {
                Element childElement = (Element) childNodes.item(i);
                String nodeName = childElement.getNodeName();

                console.log(nodeName);

                String c1 = XMLUtils.getElementValueByPath(childElement, "c1");
                String c2 = XMLUtils.getElementValueByPath(childElement, "c2");

                console.log(c1 + " " + c2);

            }
        }

        return null;

        
        
        
        
    }

    private void selectEgrid(List<Grundstueck> egridList) {
        console.log("select egrid in gui");

    }

    private final class MapSingleClickListener implements ol.event.EventListener<MapBrowserEvent> {
        @Override
        public void onEvent(MapBrowserEvent event) {
//            mapElement.style.pointerEvents = "none";
            ol.Coordinate coordinate = event.getCoordinate();

            SearchResult result = new SearchResult();
            result.setCoordinate(coordinate);

            String coord = coordinate.toStringXY(3).replace(" ", "");

            DomGlobal.fetch(CANTON_SERVICE_URL + coord).then(response -> {
                if (!response.ok) {
                    return null;
                }
                return response.text();
            }).then(json -> {
                JsPropertyMap<?> parsed = Js.cast(Global.JSON.parse(json));
                JsArray<?> results = Js.cast(parsed.get("results"));
                if (results.length > 0) {
                    JsPropertyMap<?> resultObj = Js.cast(results.getAt(0));
                    if (resultObj.has("properties")) {
                        JsPropertyMap properties = (JsPropertyMap) resultObj.get("properties");
                        String canton = ((JsString) properties.get("ak")).normalize();
                        result.setCanton(canton);

                        getEgrid(result, false);
                    }
                }
                return null;
            }).catch_(error -> {
                console.log(error);
                return null;
            });
        }
    }

}