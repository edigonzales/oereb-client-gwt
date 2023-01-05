package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;

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
import com.google.gwt.dom.client.DivElement;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.gwtproject.xml.client.XMLParser;
import org.jboss.elemento.HtmlContentBuilder;
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
import ol.Coordinate;
import ol.Extent;
import ol.Map;
import ol.MapBrowserEvent;
import ol.OLFactory;
import ol.Overlay;
import ol.OverlayOptions;
import ol.Pixel;
import ol.View;
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

    // Browser url components
    private UrlComponents urlComponents;

    // ol3 map
    private static final String MAP_DIV_ID = "map";
    private static final String ID_ATTR_NAME = "id";
    private static final String HIGHLIGHT_VECTOR_LAYER_ID = "highlight_vector_layer";
    private static final String HIGHLIGHT_VECTOR_FEATURE_ID = "highlight_fid";

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

        // Event wird in via textueller Suche (Search box) dispatched.
        body().element().addEventListener("location_found", new EventListener() {
            @Override
            public void handleEvent(Event evt) {
                CustomEvent customEvent = ((CustomEvent) evt);
                SearchResult searchResult = (SearchResult) customEvent.detail;
                getEgrid(searchResult, true);
            }
        });
    }
    
    private void getEgrid(SearchResult searchResult, boolean limit) {
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
            org.gwtproject.xml.client.Document doc = XMLParser.parse(xml);

            List<Grundstueck> grundstueckeList = XMLUtils.createGrundstuecke(doc.getDocumentElement());
            
            if (grundstueckeList.size() == 0) {
                DomGlobal.window.alert("No EGRID found.");
                return null;
            }

            Grundstueck grundstueck = null;
            // limit=true: Aufruf aus Suche (nicht via Klick in Karte).
            if (limit) {
                grundstueck = grundstueckeList.get(0);
            } else {
                if (grundstueckeList.size() > 1) {
                    selectEgrid(grundstueckeList, searchResult.getCoordinate(), searchResult.getCanton());
                } else {
                    grundstueck = grundstueckeList.get(0);
                }
            }
                        
            if (grundstueck != null) {
                Feature f = new Feature();
                f.setGeometry(grundstueck.getGeometrie());
                Feature[] fs = new Feature[] {f};
                addFeaturesToHighlightingVectorLayer(fs);
                
                grundstueck.setCanton(searchResult.getCanton());
                getExtract(grundstueck);                
            }
            
            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });
    }
    
    private void getExtract(Grundstueck grundstueck) {
        console.log("get extract");
        console.log(grundstueck.getCanton());
        
        Extent extent = grundstueck.getGeometrie().getExtent();
        View view = map.getView();
        double resolution = view.getResolutionForExtent(extent);
        view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 1);
        double x = extent.getLowerLeftX() + extent.getWidth() / 2;
        double y = extent.getLowerLeftY() + extent.getHeight() / 2;
        view.setCenter(new Coordinate(x,y));
        
        // FIXME
        // - extent verkleinern
        // was ist imagesize?
        // extent und imagesize weg und tolerance = 0.
        // https://api3.geo.admin.ch/rest/services/all/MapServer/identify?geometryFormat=geojson&geometryType=esriGeometryPoint&imageDisplay=1624,616,96&lang=en&layers=all:ch.swisstopo.swissboundaries3d-kanton-flaeche.fill&limit=1&mapExtent=2468000,1068500,2844000,1316500&returnGeometry=false&sr=2056&tolerance=1&geometry=2599558.846,1216956.994
        // -> BE statt SO

        
        
        DomGlobal.fetch("proxy/extract/xml/?CANTON="+grundstueck.getCanton()+"&GEOMETRY=false&EGRID=" + grundstueck.getEgrid()).then(response -> {
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
            console.log(xml);
            
//            org.gwtproject.xml.client.Document doc = XMLParser.parse(xml);
//
//            List<Grundstueck> grundstueckeList = XMLUtils.createGrundstuecke(doc.getDocumentElement());
//            
//            if (grundstueckeList.size() == 0) {
//                DomGlobal.window.alert("No EGRID found.");
//                return null;
//            }
//
//            Grundstueck grundstueck = null;
//            // limit=true: Aufruf aus Suche (nicht via Klick in Karte).
//            if (limit) {
//                grundstueck = grundstueckeList.get(0);
//            } else {
//                if (grundstueckeList.size() > 1) {
//                    selectEgrid(grundstueckeList, searchResult.getCoordinate(), searchResult.getCanton());
//                } else {
//                    grundstueck = grundstueckeList.get(0);
//                }
//            }
//                        
//            if (grundstueck != null) {
//                grundstueck.setCanton(searchResult.getCanton());
//                getExtract(grundstueck);                
//            }
            
            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });

        
    }
    
    
    
    /*
     * Selektiert via GUI aus einer Liste mit mehreren Grundstücken ein Grundstück.
     */
    private void selectEgrid(List<Grundstueck> grundstueckeList, Coordinate coord, String canton) {
        console.log("select egrid in gui");
        if (realEstatePopup != null) {
            map.removeOverlay(realEstatePopup);
        }
        
        HTMLElement closeButton = span().add(Icons.ALL.close()).element(); 
        closeButton.style.cursor = "pointer";

        HtmlContentBuilder<HTMLDivElement> popupBuilder = div().id("realestate-popup");
        popupBuilder.add(
                div().id("realestate-popup-header")
                .add(span().textContent(messages.map_popup_title()))
                .add(span().id("realestate-popup-close").add(closeButton))
                ); 

        HashMap<String,Grundstueck> grundstueckeMap = new HashMap<String,Grundstueck>();
        for (Grundstueck grundstueck : grundstueckeList) {
            String egrid = grundstueck.getEgrid();
            String number = grundstueck.getNummer();
            String type = grundstueck.getArt();

            String label = new String("GB-Nr.: " + number + " ("+type+")");
            HTMLDivElement row = div().id(egrid).css("realestate-popup-row")
                    .add(span().textContent(label)).element();

            grundstueckeMap.put(egrid, grundstueck);
            
            bind(row, mouseover, evt -> {
                row.style.backgroundColor = "#efefef";
                row.style.cursor = "pointer";
                Feature feature = OLFactory.createFeature();
                feature.setGeometry(grundstueck.getGeometrie());
                Feature[] fs = new Feature[] {feature};
                addFeaturesToHighlightingVectorLayer(fs);
            });

            bind(row, mouseout, evt -> {
                row.style.backgroundColor = "white";
            });

            bind(row, click, evt -> {
                map.removeOverlay(realEstatePopup);
                Grundstueck g = grundstueckeMap.get(row.getAttribute("id"));
                g.setCanton(canton);
                Feature f = OLFactory.createFeature();
                f.setGeometry(g.getGeometrie());
                Feature[] fs = new Feature[] {f};
                addFeaturesToHighlightingVectorLayer(fs);  
                
                getExtract(g);
            });
            
            popupBuilder.add(row);
        }
        
        HTMLElement popupElement = popupBuilder.element();     
        bind(closeButton, click, evt -> {
            removeHighlightVectorLayer();
            map.removeOverlay(realEstatePopup);
        });
        
        DivElement overlay = Js.cast(popupElement);
        OverlayOptions overlayOptions = OLFactory.createOptions();
        overlayOptions.setElement(overlay);
        overlayOptions.setPosition(coord);
        overlayOptions.setOffset(OLFactory.createPixel(0, 0));
        realEstatePopup = new Overlay(overlayOptions);
        map.addOverlay(realEstatePopup);    
    }

    private final class MapSingleClickListener implements ol.event.EventListener<MapBrowserEvent> {
        @Override
        public void onEvent(MapBrowserEvent event) {
            reset();
            
            
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
    
    private void reset() {
        removeHighlightVectorLayer();
//        avElement.reset();
//        grundbuchElement.reset();
//        oerebElement.reset();
    }
    
    private void addFeaturesToHighlightingVectorLayer(Feature[] features) {
        ol.layer.Vector vectorLayer = (ol.layer.Vector) getMapLayerById(HIGHLIGHT_VECTOR_LAYER_ID);
        if (vectorLayer == null) {
            vectorLayer = createHighlightVectorLayer();
        }
        Vector vectorSource = vectorLayer.getSource();
        vectorSource.clear(false);
        vectorSource.addFeatures(features);
    }
    
    private ol.layer.Vector createHighlightVectorLayer() {
        Style style = new Style();
        Stroke stroke = new Stroke();
        stroke.setWidth(6);
        //stroke.setColor(new ol.color.Color(249, 128, 0, 1.0));
        stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));
        style.setStroke(stroke);

        VectorOptions vectorSourceOptions = OLFactory.createOptions();
        Vector vectorSource = new Vector(vectorSourceOptions);
        
        VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
        vectorLayerOptions.setSource(vectorSource);
        vectorLayerOptions.setStyle(style);
        ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
        vectorLayer.set(ID_ATTR_NAME, HIGHLIGHT_VECTOR_LAYER_ID);
        vectorLayer.setZIndex(10000);
        map.addLayer(vectorLayer);
        return vectorLayer;
    }

    private void removeHighlightVectorLayer() {
        Base vlayer = getMapLayerById(HIGHLIGHT_VECTOR_LAYER_ID);
        map.removeLayer(vlayer);
    }

    private Base getMapLayerById(String id) {
        ol.Collection<Base> layers = map.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);
            try {
                String layerId = item.get(ID_ATTR_NAME);
                if (layerId == null) {
                    continue;
                }
                if (layerId.equalsIgnoreCase(id)) {
                    return item;
                }
            } catch (Exception e) {
                console.log(e.getMessage());
                console.log("should not reach here");
            }
        }
        return null;
    }   

}