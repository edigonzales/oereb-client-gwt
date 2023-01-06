package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;
import static org.dominokit.domino.ui.style.Unit.px;

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
import org.dominokit.domino.ui.button.ButtonSize;
import org.dominokit.domino.ui.datatable.ColumnConfig;
import org.dominokit.domino.ui.datatable.DataTable;
import org.dominokit.domino.ui.datatable.TableConfig;
import org.dominokit.domino.ui.datatable.store.LocalListDataStore;
import org.dominokit.domino.ui.dialogs.MessageDialog;
import org.dominokit.domino.ui.forms.TextBox;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.infoboxes.InfoBox;
import org.dominokit.domino.ui.modals.ModalDialog;
import org.dominokit.domino.ui.notifications.Notification;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.StyleType;
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

import ch.so.agi.oereb.model.ConcernedTheme;

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
import jsinterop.base.Any;
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
    private List<String> NOT_SUPPORTED_CANTONS;
    private String RESULT_CARD_HEIGHT = "calc(100% - 215px)";
    private String LANGUAGE = "de";

    // Not supported cantons
//    private List<String> notSupportedCantons = new ArrayList<>(Arrays.asList("GE", "LU", "SZ", "VD", "VS", "LI"));

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

    private HTMLElement resultDiv;
    private HTMLElement resultCard;    
    private HTMLElement resultCardContent;    
    private HTMLDivElement headerRow;
    
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
                    
                    Any[] notSupportedCantonsArray = propertiesMap.getAsAny("notSupportedCantons").asArray();
                    NOT_SUPPORTED_CANTONS = new ArrayList<>();
                    for (int i=0;i<notSupportedCantonsArray.length;i++) {
                        NOT_SUPPORTED_CANTONS.add(notSupportedCantonsArray[i].asString());
                    }
                                        
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
        
        // Div for results
        resultCard = div().id("result-card").element();
        resultCardContent = div().id("result-card-content").element(); // wegen fadeout
        resultCard.appendChild(resultCardContent);
        
        HTMLDivElement fadeoutBottomDiv = div().id("fadeout-bottom-div").element();
        resultCard.appendChild(fadeoutBottomDiv);
        
        body().add(resultCard);
    }
    
    private void getEgrid(SearchResult searchResult, boolean limit) {
        String canton = searchResult.getCanton();
        
        if (NOT_SUPPORTED_CANTONS.contains(canton)) {
            MessageDialog errorMessage = MessageDialog
                    .createMessage(messages.error_message_not_supported_canton_title(), messages.error_message_not_supported_canton_detail(canton))
                    .error();
            errorMessage.open();
            return;
        }

        String coord = searchResult.getCoordinate().toStringXY(3).replace(" ", "");

        DomGlobal.fetch("proxy/getegrid/xml/?CANTON="+canton+"&GEOMETRY=true&EN=" + coord).then(response -> {
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

            List<Grundstueck> grundstueckeList = XMLUtils.createGrundstuecke(doc.getDocumentElement(), LANGUAGE);
            
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
            //console.log(xml);
            
            resultDiv = div().id("result-div").element();

            Button closeBtn = Button.createPrimary(Icons.ALL.clear())
            .circle()
            .setSize(ButtonSize.SMALL)
            .setButtonType(StyleType.PRIMARY)
            .setTooltip(messages.result_header_button_tooltip_remove())
            .style()
            .setMargin(px.of(3)).setBackgroundColor("#2196F3").get();
            
            closeBtn.addClickListener(event -> {
               reset();
            });

            Button expandBtn = Button.createPrimary(Icons.ALL.remove())
            .circle()
            .setSize(ButtonSize.SMALL)
            .setButtonType(StyleType.PRIMARY)
            .setTooltip(messages.result_header_button_tooltip_minimize())                
            .style()
            .setMargin(px.of(3)).setBackgroundColor("#2196F3").get();
                            
            expandBtn.addClickListener(event -> {
                if (resultCard.offsetHeight > headerRow.offsetHeight) {
                    expandBtn.setIcon(Icons.ALL.add());
                    expandBtn.setTooltip(messages.result_header_button_tooltip_maximize());

                    resultCard.style.overflow = "hidden";
                    resultCard.style.height = CSSProperties.HeightUnionType.of(String.valueOf(headerRow.offsetHeight) + "px");
                    resultDiv.style.visibility = "hidden";
                    
                } else {
                    expandBtn.setIcon(Icons.ALL.remove());
                    expandBtn.setTooltip(messages.result_header_button_tooltip_maximize());

                    resultDiv.style.visibility = "visible";
                    resultCard.style.overflow = "auto";
                    resultCard.style.height = CSSProperties.HeightUnionType.of(RESULT_CARD_HEIGHT);
                }
            });

            headerRow = div().id("result-header-row").element(); 
            HTMLElement resultParcelSpan  = span().id("result-parcel-span").textContent(messages.result_header_real_estate(grundstueck.getNummer())).element();
            
            HTMLElement resultButtonSpan = span().id("result-button-span").element();
            resultButtonSpan.appendChild(expandBtn.element());
            resultButtonSpan.appendChild(closeBtn.element());
            
            headerRow.appendChild(resultParcelSpan);
            headerRow.appendChild(resultButtonSpan);

            resultCardContent.appendChild(headerRow);

            //TODO: zuerst parsen, dann alles rendern.
            
            parseResponse(xml);

            
            
            
            // Show result
            resultCard.style.height = CSSProperties.HeightUnionType.of(RESULT_CARD_HEIGHT);
            resultCard.style.overflow = "auto";
            resultCard.style.visibility = "visible";

            return null;
        }).catch_(error -> {
            console.log(error);
            return null;
        });

        
    }
    
    private void parseResponse(String xml) {
        org.gwtproject.xml.client.Document doc = XMLParser.parse(xml);
        
        // Concerned themes
        HashMap<String,ConcernedTheme> concernedThemesMap = new HashMap<>();
        List<Element> restrictionOnLandownershipList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(doc.getDocumentElement(), "Extract/RealEstate/RestrictionOnLandownership", restrictionOnLandownershipList);
        
        if (restrictionOnLandownershipList.size() == 0) {
            Window.alert("should not reach here");
        }
        
        for (Element restrictionOnLandownershipElement : restrictionOnLandownershipList) {
            List<Element> themeTextList = new ArrayList<Element>();
            XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "Theme/Text", themeTextList);
            // Es gibt jeweils nur ein Element pro Restriction
            Element themeTextElement = themeTextList.get(0);
            String localisedThemeName = XMLUtils.getLocalisedTextByLanguage(themeTextElement, LANGUAGE);
            console.log(localisedThemeName);
            
            if (!concernedThemesMap.containsKey(localisedThemeName)) {
                ConcernedTheme theme = new ConcernedTheme();
                theme.setCode(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Theme/Code"));
                theme.setName(localisedThemeName);
                theme.setSubtheme(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "SubTheme"));                

                // WMS
                String layerOpacity = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Map/layerOpacity");
                String layerIndex = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Map/layerIndex");

                // TODO: 
                // - map-Param
                // - port 
                // - params: case insensitive. mit entries() loopen
                List<Element> referenceWmsList = new ArrayList<Element>();
                XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "Map/ReferenceWMS", referenceWmsList);
                
                console.log(referenceWmsList.get(0));
                
                
                String localisedReferenceWmsText = XMLUtils.getLocalisedTextByLanguage(referenceWmsList.get(0), LANGUAGE);
                console.log(localisedReferenceWmsText);
                
//                String wms = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Map/ReferenceWMS");

            }

     
            
            
        }

        
        
        
//        String themeName = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Theme/Text");

    }
    
    private void renderResponse() {
        
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
        
        if (headerRow != null) {
            headerRow.remove();
        }

        resultCard.style.visibility = "hidden";

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