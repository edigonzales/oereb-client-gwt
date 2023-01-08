package ch.so.agi.oereb;

import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.fetch;
import static org.jboss.elemento.Elements.*;
import static org.jboss.elemento.EventType.*;
import static org.dominokit.domino.ui.style.Unit.px;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.dominokit.domino.ui.button.Button;
import org.dominokit.domino.ui.button.ButtonSize;
import org.dominokit.domino.ui.chips.Chip;
import org.dominokit.domino.ui.collapsible.Accordion;
import org.dominokit.domino.ui.collapsible.AccordionPanel;
import org.dominokit.domino.ui.collapsible.Collapsible.HideCompletedHandler;
import org.dominokit.domino.ui.collapsible.Collapsible.ShowCompletedHandler;
import org.dominokit.domino.ui.dialogs.MessageDialog;
import org.dominokit.domino.ui.grid.Column;
import org.dominokit.domino.ui.grid.Row;
import org.dominokit.domino.ui.grid.flex.FlexItem;
import org.dominokit.domino.ui.grid.flex.FlexLayout;
import org.dominokit.domino.ui.icons.Icons;
import org.dominokit.domino.ui.lists.ListGroup;
import org.dominokit.domino.ui.sliders.Slider;
import org.dominokit.domino.ui.style.Color;
import org.dominokit.domino.ui.style.ColorScheme;
import org.dominokit.domino.ui.style.StyleType;
import org.dominokit.domino.ui.style.Styles;
import org.dominokit.domino.ui.themes.Theme;
import org.dominokit.domino.ui.utils.DominoElement;
import org.dominokit.domino.ui.utils.TextNode;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.gwtproject.xml.client.XMLParser;
import org.jboss.elemento.EventType;
import org.jboss.elemento.HtmlContentBuilder;
import org.gwtproject.xml.client.Element;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;

import ch.so.agi.oereb.model.ConcernedTheme;
import ch.so.agi.oereb.model.Document;
import ch.so.agi.oereb.model.Office;
import ch.so.agi.oereb.model.ReferenceWMS;
import ch.so.agi.oereb.model.Restriction;
import ch.so.agi.oereb.model.TypeTriple;

import elemental2.core.Global;
import elemental2.core.JsArray;
import elemental2.core.JsIIterableResult;
import elemental2.core.JsIteratorIterable;
import elemental2.core.JsString;
import elemental2.dom.CSSProperties;
import elemental2.dom.CustomEvent;
import elemental2.dom.DomGlobal;
import elemental2.dom.Event;
import elemental2.dom.EventListener;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.Location;
import elemental2.dom.URL;
import elemental2.dom.URLSearchParams;
import elemental2.dom.XMLHttpRequest;
import jsinterop.base.Any;
import jsinterop.base.Js;
import jsinterop.base.JsPropertyMap;
import ol.Coordinate;
import ol.Extent;
import ol.Map;
import ol.MapBrowserEvent;
import ol.OLFactory;
import ol.Overlay;
import ol.OverlayOptions;
import ol.View;
import ol.Feature;
import ol.layer.Base;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.layer.VectorLayerOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.style.Stroke;
import ol.style.Style;

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

    // Format settings
    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");
    private static final String SUB_HEADER_FONT_SIZE = "16px";
    private static final String BODY_FONT_SIZE = "14px";
    private static final String SMALL_FONT_SIZE = "12px";

    
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
    
    private LinkedList<ConcernedTheme> concernedThemes;
    private LinkedList<String> notConcernedThemes;
    private LinkedList<String> themesWithoutData;
    private ArrayList<String> oerebWmsLayers = new ArrayList<String>();

    private Accordion innerAccordion;
    private boolean oerebAccordionPanelConcernedThemeState = false;
    private boolean oerebAccordionPanelNotConcernedThemeState = false;
    private boolean oerebAccordionPanelThemesWithoutDataState = false;
    private boolean oerebAccordionPanelGeneralInformationState = false;

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
                    for (int i = 0; i < notSupportedCantonsArray.length; i++) {
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
                    .createMessage(messages.error_message_not_supported_canton_title(),
                            messages.error_message_not_supported_canton_detail(canton))
                    .error();
            errorMessage.open();
            return;
        }

        String coord = searchResult.getCoordinate().toStringXY(3).replace(" ", "");

        DomGlobal.fetch("proxy/getegrid/xml/?CANTON=" + canton + "&GEOMETRY=true&EN=" + coord).then(response -> {
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
                Feature[] fs = new Feature[] { f };
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
        updateUrlLocation(grundstueck.getEgrid());
      
        Extent extent = grundstueck.getGeometrie().getExtent();
        View view = map.getView();
        double resolution = view.getResolutionForExtent(extent);
        view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 1);
        double x = extent.getLowerLeftX() + extent.getWidth() / 2;
        double y = extent.getLowerLeftY() + extent.getHeight() / 2;
        view.setCenter(new Coordinate(x, y));

        resultDiv = div().id("result-div").element();

        Button closeBtn = Button.createPrimary(Icons.ALL.clear()).circle().setSize(ButtonSize.SMALL)
                .setButtonType(StyleType.PRIMARY).setTooltip(messages.result_header_button_tooltip_remove())
                .style().setMargin(px.of(3)).setBackgroundColor("#2196F3").get();

        closeBtn.addClickListener(event -> {
            reset();
        });

        Button expandBtn = Button.createPrimary(Icons.ALL.remove()).circle().setSize(ButtonSize.SMALL)
                .setButtonType(StyleType.PRIMARY)
                .setTooltip(messages.result_header_button_tooltip_minimize()).style().setMargin(px.of(3))
                .setBackgroundColor("#2196F3").get();

        expandBtn.addClickListener(event -> {
            if (resultCard.offsetHeight > headerRow.offsetHeight) {
                expandBtn.setIcon(Icons.ALL.add());
                expandBtn.setTooltip(messages.result_header_button_tooltip_maximize());

                resultCard.style.overflow = "hidden";
                resultCard.style.height = CSSProperties.HeightUnionType
                        .of(String.valueOf(headerRow.offsetHeight) + "px");
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
        HTMLElement resultParcelSpan = span().id("result-parcel-span")
                .textContent(messages.result_header_real_estate(grundstueck.getNummer() + " (" + grundstueck.getEgrid()) + ")").element();

        HTMLElement resultButtonSpan = span().id("result-button-span").element();
        resultButtonSpan.appendChild(expandBtn.element());
        resultButtonSpan.appendChild(closeBtn.element());

        headerRow.appendChild(resultParcelSpan);
        headerRow.appendChild(resultButtonSpan);

        resultCardContent.appendChild(headerRow);
        
        DomGlobal.fetch("proxy/extract/xml/?CANTON=" + grundstueck.getCanton() + "&GEOMETRY=false&EGRID="
                + grundstueck.getEgrid()).then(response -> {
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
                    parseResponse(xml, grundstueck);
                    renderResponse(grundstueck);
                    return null;
                }).catch_(error -> {
                    console.log(error);
                    return null;
                });

    }

    private void parseResponse(String xml, Grundstueck grundstueck) {
        org.gwtproject.xml.client.Document doc = XMLParser.parse(xml);
        
        // Real estate information
        grundstueck.setMunicipalityName(XMLUtils.getElementValueByPath(doc.getDocumentElement(), "Extract/RealEstate/MunicipalityName"));
        grundstueck.setMunicipalityNumber(XMLUtils.getElementValueByPath(doc.getDocumentElement(), "Extract/RealEstate/MunicipalityCode"));
        grundstueck.setSubunitOfLandRegister(XMLUtils.getElementValueByPath(doc.getDocumentElement(), "Extract/RealEstate/SubunitOfLandRegister"));
        grundstueck.setSubunitOfLandRegisterDesignation(XMLUtils.getElementValueByPath(doc.getDocumentElement(), "Extract/RealEstate/SubunitOfLandRegisterDesignation"));
        grundstueck.setLandRegistryArea(Integer.valueOf(XMLUtils.getElementValueByPath(doc.getDocumentElement(), "Extract/RealEstate/LandRegistryArea")));
        
        // Concerned themes
        // ----------------
        // Theme/Text/Text und Rechtskraft entspricht im GUI einem Element zum Aufklappen (z.B. Nutzungsplanung überlagernd).
        // Ein Aufklapp-Element entspricht einem ConcernedTheme-Objekt. Ein solches Objekt kann
        // beliebig viele Restrictions haben.
        // Eine Restriction wird über typeCode und typeCodelist gruppiert.
        // Annahme: Restrictions mit gleichem TypeTuple haben die gleichen Dokumente (so falsch
        // ist das nicht, oder sogar definitiv logisch/richtig?)
        LinkedHashMap<String,ConcernedTheme> concernedThemesMap = new LinkedHashMap<>();
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
            
            String lawStatus = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Lawstatus/Code");
            
            List<Element> lawStatusTextElementList = new ArrayList<Element>();
            XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "Lawstatus/Text", lawStatusTextElementList);
            String lawStatusText = XMLUtils.getLocalisedTextByLanguage(lawStatusTextElementList.get(0), LANGUAGE);

            localisedThemeName +=  " (" + lawStatusText + ")";
                    
            //console.log(localisedThemeName);
            
            if (!concernedThemesMap.containsKey(localisedThemeName)) {
                ConcernedTheme theme = new ConcernedTheme();
                theme.setCode(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Theme/Code"));
                theme.setName(localisedThemeName);
                theme.setSubtheme(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "SubTheme"));  
                
                // WMS
                String layerOpacity = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Map/layerOpacity");
                String layerIndex = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "Map/layerIndex");

                // TODO: 
                // - port 
                // - styles
                List<Element> referenceWmsList = new ArrayList<Element>();
                XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "Map/ReferenceWMS", referenceWmsList);
                String localisedReferenceWmsText = XMLUtils.getLocalisedTextByLanguage(referenceWmsList.get(0), LANGUAGE);

                URL wmsUrl = new URL(fixUrl(localisedReferenceWmsText));                
                String host = wmsUrl.host;
                String protocol = wmsUrl.protocol;
                String pathname = wmsUrl.pathname;
                
                String layers = null;
                String imageFormat = null;
                String transparent = null;
                String searchParamsString = "";
                URLSearchParams params = wmsUrl.searchParams;
                JsIteratorIterable<JsArray<String>> paramEntries = params.entries();
                JsIIterableResult<JsArray<String>> paramEntryArray = paramEntries.next();
                while(!paramEntryArray.isDone()) {
                    List<String> paramEntryList = paramEntryArray.getValue().asList();
                    String key = paramEntryList.get(0);
                    String value = paramEntryList.get(1);
                    
                    if (key.equalsIgnoreCase("LAYERS")) {
                        layers = value;
                    } else if (key.equalsIgnoreCase("FORMAT")) {
                        imageFormat = value;
                    } else if (key.equalsIgnoreCase("TRANSPARENT")) {
                        transparent = value;
                    } else if (key.equalsIgnoreCase("STYLES")) {
                        
                        // in createOerebWmsLayer set(..,..) Styles setzen.
                        // styles property in pojo
                        
                        // TODO
                    } else if (key.equalsIgnoreCase("SERVICE") || key.equalsIgnoreCase("REQUEST")
                            || key.equalsIgnoreCase("VERSION") || key.equalsIgnoreCase("BBOX")
                            || key.equalsIgnoreCase("WIDTH") || key.equalsIgnoreCase("HEIGHT")
                            || key.equalsIgnoreCase("SRS") || key.equalsIgnoreCase("CRS")) {
                    } else {
                        String separator = "&";
                        if (searchParamsString.length() == 0) {
                            separator = "?";
                        }
                        searchParamsString += separator+key+"="+value;
                    }
                    paramEntryArray = paramEntries.next();
                }
                
                if (layers == null || imageFormat == null) {
                    Window.alert("could not parse reference wms: " + localisedReferenceWmsText);
                    return;
                }
                                
                String baseUrl = protocol + "//" + host + pathname + searchParamsString;
                               
                ReferenceWMS referenceWMS = new ReferenceWMS();
                referenceWMS.setBaseUrl(baseUrl);
                referenceWMS.setImageFormat(imageFormat);
                referenceWMS.setLayers(layers);
                referenceWMS.setLayerOpacity(Double.valueOf(layerOpacity));
                referenceWMS.setLayerIndex(Integer.valueOf(layerIndex));
                theme.setReferenceWMS(referenceWMS);

                // ResponsibleOffice
                List<Element> officeList = new ArrayList<Element>();
                XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "ResponsibleOffice", officeList);                
                for (Element officeElement : officeList) {
                    Office office = new Office();

                    List<Element> officeNameList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(officeElement, "Name", officeNameList);
                    String localisedOfficeNameText = XMLUtils.getLocalisedTextByLanguage(officeNameList.get(0), LANGUAGE);
                    office.setName(localisedOfficeNameText);

                    List<Element> officeAtWebElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(officeElement, "OfficeAtWeb", officeAtWebElementList);
                    if (officeAtWebElementList.size() > 0) {
                        String localisedOfficeAtWebText = XMLUtils.getLocalisedTextByLanguage(officeAtWebElementList.get(0), LANGUAGE);
                        office.setOfficeAtWeb(localisedOfficeAtWebText);
                    }

                    theme.getResponsibleOffice().add(office);
                }
                concernedThemesMap.put(localisedThemeName, theme);
            }
            
            String typeCode = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "TypeCode");
            String typeCodelist = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "TypeCodelist");
            TypeTriple typeTriple = new TypeTriple(typeCode, typeCodelist, lawStatus);
            
            String areaShare = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "AreaShare");
            String partInPercent = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "PartInPercent");
            String lengthShare = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "LengthShare");
            String nrOfPoints = XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "NrOfPoints");

            ConcernedTheme theme = concernedThemesMap.get(localisedThemeName);
            if(theme.getRestrictions().containsKey(typeTriple)) {
                console.log(typeTriple + " bereits vorhanden");

                Restriction restriction = theme.getRestrictions().get(typeTriple);
                if (areaShare != null) {
                    restriction.updateAreaShare(Integer.valueOf(areaShare));
                }
                if (partInPercent != null) {
                    restriction.updatePartInPercent(Double.valueOf(partInPercent));
                }
                if (lengthShare != null) {
                    restriction.updateLengthShare(Integer.valueOf(lengthShare));
                }
                if (nrOfPoints != null) {
                    restriction.updateNrOfPoints(Integer.valueOf(nrOfPoints));
                }                
            } else {
                Restriction restriction = new Restriction();  
                restriction.setLawStatus(lawStatus);
                restriction.setLawStatusText(lawStatusText);
                
                List<Element> legendTextList = new ArrayList<Element>();
                XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "LegendText", legendTextList);
                String localisedLegendText = XMLUtils.getLocalisedTextByLanguage(legendTextList.get(0), LANGUAGE);
                restriction.setLegendText(localisedLegendText);
                
                restriction.setTypeCode(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "typeCode"));
                restriction.setTypeCodelist(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "typeCodelist"));
                restriction.setSymbolRef(XMLUtils.getElementValueByPath(restrictionOnLandownershipElement, "SymbolRef"));

                if (areaShare != null) {
                    restriction.setAreaShare(Integer.valueOf(areaShare));
                }
                if (partInPercent != null) {
                    restriction.setPartInPercent(Double.valueOf(partInPercent));
                }
                if (lengthShare != null) {
                    restriction.setLengthShare(Integer.valueOf(lengthShare));
                }
                if (nrOfPoints != null) {
                    restriction.setNrOfPoints(Integer.valueOf(nrOfPoints));
                }
                theme.getRestrictions().put(typeTriple, restriction);

                List<Element> legalProvisionsList = new ArrayList<Element>();
                XMLUtils.getElementsByPath(restrictionOnLandownershipElement, "LegalProvisions", legalProvisionsList);
                for (Element legalProvisionsElement : legalProvisionsList) {                    
                    
                    Document document = new Document();
                    document.setType(XMLUtils.getElementValueByPath(legalProvisionsElement, "Type/Code"));

                    List<Element> typeElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "Type/Text", typeElementList);
                    String localisedTypeText = XMLUtils.getLocalisedTextByLanguage(typeElementList.get(0), LANGUAGE);
                    document.setTypeText(localisedTypeText);
                    
                    List<Element> titleElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "Title", titleElementList);
                    String localisedTitleText = XMLUtils.getLocalisedTextByLanguage(titleElementList.get(0), LANGUAGE);
                    document.setTitle(localisedTitleText);

                    List<Element> officialNumberElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "OfficialNumber", officialNumberElementList);
                    if (officialNumberElementList.size() > 0) {
                        String localisedOfficialNumberText = XMLUtils.getLocalisedTextByLanguage(officialNumberElementList.get(0), LANGUAGE);
                        document.setOfficialNumber(localisedOfficialNumberText);
                    }
                    
                    List<Element> abbreviationElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "Abbreviation", abbreviationElementList);
                    if (abbreviationElementList.size() > 0) {
                        String localisedAbbreviationText = XMLUtils.getLocalisedTextByLanguage(abbreviationElementList.get(0), LANGUAGE);
                        document.setAbbreviation(localisedAbbreviationText);
                    }

                    List<Element> textAtWebElementList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "TextAtWeb", textAtWebElementList);
                    if (textAtWebElementList.size() > 0) {
                        String localisedTextAtWebText = XMLUtils.getLocalisedTextByLanguage(textAtWebElementList.get(0), LANGUAGE);
                        document.setTextAtWeb(localisedTextAtWebText);
                    }

                    document.setLawStatus(XMLUtils.getElementValueByPath(legalProvisionsElement, "Lawstatus/Code"));

                    List<Element> lawStatusTextElementDocumentList = new ArrayList<Element>();
                    XMLUtils.getElementsByPath(legalProvisionsElement, "Lawstatus/Text", lawStatusTextElementDocumentList);
                    String lawStatusDocumentText = XMLUtils.getLocalisedTextByLanguage(lawStatusTextElementDocumentList.get(0), LANGUAGE);
                    document.setLawStatusText(lawStatusDocumentText);

                    // TEMPORARY
                    // Kanton AG hat None.
                    try {
                        document.setIdx(Integer.valueOf(XMLUtils.getElementValueByPath(legalProvisionsElement, "Index")));
                    } catch (NumberFormatException e) {
                        document.setIdx(1);
                    }
                                        
                    theme.getDocuments().add(document);
                }
            }
        }

        // TODO: Test: Stimmt die Sortierung mit .values()?
        concernedThemes = new LinkedList<ConcernedTheme>(concernedThemesMap.values());

        
        // Not concerned themes
        List<Element> notConcernedThemesList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(doc.getDocumentElement(), "Extract/NotConcernedTheme", notConcernedThemesList);
        notConcernedThemes = new LinkedList<String>();
        for (Element element : notConcernedThemesList) {
            List<Element> notConcernedThemeTextList = new ArrayList<Element>();
            XMLUtils.getElementsByPath(element, "Text", notConcernedThemeTextList);
            String localisedThemeText = XMLUtils.getLocalisedTextByLanguage(notConcernedThemeTextList.get(0), LANGUAGE);
            notConcernedThemes.add(localisedThemeText);
        }

        // Themes without data
        List<Element> themeWithoutDataList = new ArrayList<Element>();
        XMLUtils.getElementsByPath(doc.getDocumentElement(), "Extract/ThemeWithoutData", themeWithoutDataList);
        themesWithoutData = new LinkedList<String>();
        for (Element element : themeWithoutDataList) {
            List<Element> themeWithoutDataTextList = new ArrayList<Element>();
            XMLUtils.getElementsByPath(element, "Text", themeWithoutDataTextList);
            String localisedThemeText = XMLUtils.getLocalisedTextByLanguage(themeWithoutDataTextList.get(0), LANGUAGE);
            themesWithoutData.add(localisedThemeText);
        }
    }

    private void renderResponse(Grundstueck grundstueck) {
        HTMLDivElement div = div().element();

        {
            Button pdfBtn = Button.create(Icons.ALL.file_pdf_box_outline_mdi())
                .setContent("PDF")
                .setBackground(Color.WHITE)
                .elevate(0)
                .style()
                .setColor("#2196F3")
                .setBorder("1px #2196F3 solid")
                .setPadding("5px 5px 5px 0px;")
                .setMinWidth(px.of(120)).get();
            
            pdfBtn.setTooltip(messages.result_button_request_pdf());
                    
            pdfBtn.addClickListener(event -> {
                String canton = grundstueck.getCanton();
                String serviceUrl = ((JsString) OEREB_SERVICE_URLS.get(canton)).normalize();
                Window.open(serviceUrl + "/extract/pdf/?EGRID="+grundstueck.getEgrid(), "_blank", null);
            });
                       
            div.appendChild(pdfBtn.element());
        }

        {
            Row row = Row.create();
            row.style().cssText("padding-top:15px;");
            row.appendChild(Column.span6().css("result-real-estate-info-title").setTextContent(messages.result_municipality()+":"));
            row.appendChild(Column.span6().css("result-real-estate-info-text").setTextContent(grundstueck.getMunicipalityName() + " ("+ grundstueck.getMunicipalityNumber() + ")"));
            div.appendChild(row.element());
        }
        {
            if (grundstueck.getSubunitOfLandRegister() != null) {
                Row row = Row.create();
                row.style().cssText("padding-top:5px;");
                row.appendChild(Column.span6().css("result-real-estate-info-title").setTextContent(grundstueck.getSubunitOfLandRegisterDesignation()+":"));
                row.appendChild(Column.span6().css("result-real-estate-info-text").setTextContent(grundstueck.getSubunitOfLandRegister()));
                div.appendChild(row.element());
            }
        }        
        {
            Row row = Row.create();
            row.style().cssText("padding-top:5px;");
            row.appendChild(Column.span6().css("result-real-estate-info-title").setTextContent(messages.result_land_register_area()+":"));
            String area = fmtDefault.format(grundstueck.getLandRegistryArea()) + " m<span class=\"sup\">2</span>";
            row.appendChild(Column.span6().css("result-real-estate-info-text").appendChild(span().innerHtml(SafeHtmlUtils.fromTrustedString(area))));
            div.appendChild(row.element());
        }        
        {
            Row row = Row.create();
            row.style().cssText("padding-top:5px;");
            row.appendChild(Column.span6().css("result-real-estate-info-title").setTextContent(messages.result_land_register_type()+":"));
            row.appendChild(Column.span6().css("result-real-estate-info-text").setTextContent(grundstueck.getArt()));
            div.appendChild(row.element());
        }
        
        Accordion accordion = Accordion.create()
                .setHeaderBackground(Color.GREY_LIGHTEN_3)
                .style()
                .setMarginTop("20px")
                .get();
        
        div.appendChild(accordion.element());

        // Concerned themes
        {
            AccordionPanel accordionPanel = AccordionPanel.create(messages.result_theme_concerned_themes());
            accordionPanel.elevate(0);
            accordionPanel.css("accordion-panel-theme");
            DominoElement<HTMLDivElement> accordionPanelConcernedThemeHeaderElement = accordionPanel.getHeaderElement();
            accordionPanelConcernedThemeHeaderElement.addCss("accordion-panel-header-element");
            
            Chip chip = Chip.create().setValue(String.valueOf(concernedThemes.size()))
                    .setColor(Color.GREY_LIGHTEN_1)
                    .style()
                    .setPadding("0px")
                    .setMargin("4px")
                    .setTextAlign("center").get();
            accordionPanelConcernedThemeHeaderElement.appendChild(span().css("accordion-panel-header-chip").add(chip));
            
            accordion.appendChild(accordionPanel);

            // TODO / FIXME
            // Event listener nur auf dem Header Element. Ansonsten schliesst es sich 
            // auch wenn ich auf einen Sub-Panel klicke.
//            oerebAccordionPanelConcernedTheme.getHeaderElement().addEventListener(EventType.click, new EventListener() {
//                @Override
//                public void handleEvent(Event evt) {                    
//                    if (!oerebAccordionPanelConcernedThemeState) {
//                        oerebAccordionPanelConcernedTheme.show();
//                        oerebAccordionPanelConcernedThemeState = true;
//                        oerebAccordionPanelNotConcernedThemeState = false;
//                        oerebAccordionPanelThemesWithoutDataState = false;
//                        oerebAccordionPanelGeneralInformationState = false;
//                        List<AccordionPanel> panels = oerebAccordion.getPanels();
//                        for (AccordionPanel panel : panels) {
//                            if(!panel.equals(oerebAccordionPanelConcernedTheme)) {
//                                panel.hide();
//                            }
//                        }                          
//                    } else {
//                        oerebAccordionPanelConcernedTheme.hide();
//                        oerebAccordionPanelConcernedThemeState = false;
//                    }            
//                }
//            });  
            
            innerAccordion = Accordion.create()
                    .setId("accordion-concerned-theme")
                    .setHeaderBackground(Color.GREY_LIGHTEN_4);

            for (ConcernedTheme theme : concernedThemes) {
                Image wmsLayer = createOerebWmsLayer(theme.getReferenceWMS());
                map.addLayer(wmsLayer);

                String layerId = theme.getReferenceWMS().getLayers();
                oerebWmsLayers.add(layerId);                    

                // TODO brauche ich das?
//                innerOerebPanelStateMap.put(layerId, false);
                
                AccordionPanel themeAccordionPanel = AccordionPanel.create(theme.getName()).css("accordion-panel-concerned-theme");
                themeAccordionPanel.elevate(0);
                themeAccordionPanel.setId(layerId);
                
                themeAccordionPanel.addShowListener(new ShowCompletedHandler() {
                    @Override
                    public void onShown() {
                        Image wmsLayer = (Image) getMapLayerById(layerId);
                        wmsLayer.setVisible(true); 
                    } 
                });
                
                themeAccordionPanel.addHideListener(new HideCompletedHandler() {
                    @Override
                    public void onHidden() {
                        Image wmsLayer = (Image) getMapLayerById(layerId);
                        wmsLayer.setVisible(false);
                        // TODO
                        //innerOerebPanelStateMap.put(accordionPanel.getId(), false);
                    } 
                });
                
                // ?? Text? Code ist sinnvoll, Begründung mir jetzt noch unklar.
                // Damit wird der Click Event nicht in das Tab Panel weitergereicht.
                // Und somit wird nicht unnötiger Code ausgeführt.
//                accordionPanel.getHeaderElement().addEventListener(EventType.click, new EventListener() {
//                    @Override
//                    public void handleEvent(Event evt) {
//                        //console.log("vorher: " + accordionPanel.getId() + " " + innerOerebPanelStateMap.get(accordionPanel.getId()));
//                        if (innerOerebPanelStateMap.get(accordionPanel.getId())) {
//                            innerOerebPanelStateMap.put(accordionPanel.getId(), false);
//                            accordionPanel.hide();
//                        } else {
//                            innerOerebPanelStateMap.put(accordionPanel.getId(), true);
//                            accordionPanel.show();
//                        }
//                        //console.log("nachher: " + accordionPanel.getId() + " " + innerOerebPanelStateMap.get(accordionPanel.getId()));
//
//                        evt.stopPropagation();
//                    }
//                });

                // Create a div element for the content.
                HTMLDivElement contentDiv = div().css("theme-content").element();

                // Slider
                int opacity = Double.valueOf((theme.getReferenceWMS().getLayerOpacity() * 100)).intValue();;
                Slider slider = Slider.create(100).setMinValue(0).setValue(opacity).withoutThumb();
                slider.addChangeHandler(handler -> {
                    wmsLayer.setOpacity(handler.intValue() / 100.0);
                });
                                        
                FlexLayout sliderRow = FlexLayout.create();
                sliderRow.appendChild(FlexItem.create().setFlexGrow(0).style().setPaddingRight("20px").get().appendChild(span().textContent(messages.result_wms_opacity()).element()));
                sliderRow.appendChild(FlexItem.create().setFlexGrow(1).appendChild(slider.element()));
                contentDiv.appendChild(sliderRow.element());
                
                contentDiv.appendChild(div().css("fake-column").element());
                contentDiv.appendChild(div().css(Styles.padding_10).element());

                // Eigentumsbeschränkungen
                Row restrictionHeaderRow = Row.create();
                restrictionHeaderRow.appendChild(Column.span6().style().setFontSize(SMALL_FONT_SIZE).get().setTextContent(messages.result_type_name()));
                restrictionHeaderRow.appendChild(Column.span1().style().setFontSize(SMALL_FONT_SIZE).get().setTextContent(""));
                restrictionHeaderRow.appendChild(Column.span3().style().setFontSize(SMALL_FONT_SIZE).setTextAlign("right").get().setTextContent(messages.result_share_name()));
                restrictionHeaderRow.appendChild(Column.span2().style().setFontSize(SMALL_FONT_SIZE).setTextAlign("right").get().setTextContent(messages.result_share_in_percent_name()));
                contentDiv.appendChild(restrictionHeaderRow.element());

                for (java.util.Map.Entry<TypeTriple, Restriction> entry : theme.getRestrictions().entrySet()) {
                    Restriction restriction = entry.getValue();
                    
                    if (restriction.getAreaShare() != null) {
                        contentDiv.appendChild(processRestrictionRow(restriction, GeometryType.POLYGON));
                    }

                    if (restriction.getLengthShare() != null) {
                        contentDiv.appendChild(processRestrictionRow(restriction, GeometryType.LINE));
                    }

                    if (restriction.getNrOfPoints() != null) {
                        contentDiv.appendChild(processRestrictionRow(restriction, GeometryType.POINT));
                    }
                }

                contentDiv.appendChild(div().css("fake-column").element());
                contentDiv.appendChild(div().css(Styles.padding_10).element());
                
                // Rechtsvorschriften
                {
                    contentDiv.appendChild(div().css("font-semi-bold").textContent(messages.result_documents_legal_provisions()).element());
                    
                    LinkedList<Document> documents = theme.getDocuments().stream()
                        .filter(d -> d.getType().equalsIgnoreCase("LegalProvision"))
                        .sorted(Comparator.comparingInt(Document::getIdx))
                        .collect(Collectors.toCollection(LinkedList::new));
                    
                    for (Document document : documents) {
                        String title = document.getTitle();
                        String number = document.getOfficialNumber();
                        String abbrevation = document.getAbbreviation();
                        String textAtWeb = document.getTextAtWeb();
                        
                        String linkName = title;
                        if (number != null) {
                            linkName += ", " + number;
                        }
                        
                        if (textAtWeb != null) {
                            HTMLElement link = a().css("result-link")
                                    .attr("href", textAtWeb)
                                    .attr("target", "_blank")
                                    .add(TextNode.of(linkName)).element();
                            contentDiv.appendChild(div().add(link).element());
                        } else  {
                            contentDiv.appendChild(div().add(linkName).element());
                        }     
                    }
                }
                
                // Gesetzliche Grundlagen
                {
                    contentDiv.appendChild(div().css(Styles.padding_5).element());                    
                    contentDiv.appendChild(div().css("font-semi-bold").textContent(messages.result_documents_laws()).element());
                    
                    LinkedList<Document> documents = theme.getDocuments().stream()
                            .filter(d -> d.getType().equalsIgnoreCase("Law"))
                            .sorted(Comparator.comparingInt(Document::getIdx))
                            .collect(Collectors.toCollection(LinkedList::new));
                        
                        for (Document document : documents) {
                            String title = document.getTitle();
                            String number = document.getOfficialNumber();
                            String abbreviation = document.getAbbreviation();
                            String textAtWeb = document.getTextAtWeb();
                            
                            String linkName = title;
                            if (abbreviation != null) {
                                linkName += " (" + abbreviation + ")";
                            }
                            if (number != null) {
                                linkName += ", " + number;
                            }
                            
                            if (textAtWeb != null) {
                                HTMLElement link = a().css("result-link")
                                        .attr("href", textAtWeb)
                                        .attr("target", "_blank")
                                        .add(TextNode.of(linkName)).element();
                                contentDiv.appendChild(div().add(link).element());
                            } else  {
                                contentDiv.appendChild(div().add(linkName).element());
                            }     
                        }
                }
                
                // Hinweise
                {
                    LinkedList<Document> documents = theme.getDocuments().stream()
                            .filter(d -> d.getType().equalsIgnoreCase("Hint"))
                            .sorted(Comparator.comparingInt(Document::getIdx))
                            .collect(Collectors.toCollection(LinkedList::new));
                    
                    if (documents.size() > 0) {
                        contentDiv.appendChild(div().css(Styles.padding_5).element());                    
                        contentDiv.appendChild(div().css("font-semi-bold").textContent(messages.result_documents_laws()).element());
                        
                        for (Document document : documents) {
                            String title = document.getTitle();
                            String number = document.getOfficialNumber();
                            String abbreviation = document.getAbbreviation();
                            String textAtWeb = document.getTextAtWeb();
                            
                            String linkName = title;
                            if (abbreviation != null) {
                                linkName += " (" + abbreviation + ")";
                            }
                            if (number != null) {
                                linkName += ", " + number;
                            }
                            
                            if (textAtWeb != null) {
                                HTMLElement link = a().css("result-link")
                                        .attr("href", textAtWeb)
                                        .attr("target", "_blank")
                                        .add(TextNode.of(linkName)).element();
                                contentDiv.appendChild(div().add(link).element());
                            } else  {
                                contentDiv.appendChild(div().add(linkName).element());
                            }     
                        }
                    }
                }

                contentDiv.appendChild(div().css("fake-column").element());
                contentDiv.appendChild(div().css(Styles.padding_10).element());

                // Zuständige Stelle(n)
                contentDiv.appendChild(div().css("font-semi-bold").textContent(messages.result_responsible_offices()).element());

                for (Office office : theme.getResponsibleOffice()) {
                    String textAtWeb = office.getOfficeAtWeb();
                    
                    if (textAtWeb != null) {
                        HTMLElement link = a().css("result-link")
                                .attr("href", textAtWeb)
                                .attr("target", "_blank")
                                .add(TextNode.of(office.getName())).element();
                        contentDiv.appendChild(div().add(link).element());
                    } else  {
                        contentDiv.appendChild(div().add(office.getName()).element());
                    }     
                    contentDiv.appendChild(div().css(Styles.padding_5).element());
                }

                themeAccordionPanel.setContent(contentDiv);
                innerAccordion.appendChild(themeAccordionPanel);
            }
            accordionPanel.appendChild(innerAccordion);
        }        
        
        {
            AccordionPanel accordionPanel = AccordionPanel.create(messages.result_theme_not_concerned_themes());
            accordionPanel.css("accordion-panel-theme");
            accordionPanel.elevate(0);            
            DominoElement<HTMLDivElement> accordionPanelNotConcernedThemeHeaderElement = accordionPanel.getHeaderElement();
            accordionPanelNotConcernedThemeHeaderElement.addCss("accordion-panel-header-element");
            
            Chip chip = Chip.create().setValue(String.valueOf(notConcernedThemes.size()))
                    .setColor(Color.GREY_LIGHTEN_1)
                    .style()
                    .setPadding("0px")
                    .setMargin("4px")
                    .setTextAlign("center").get();
            accordionPanelNotConcernedThemeHeaderElement.appendChild(span().css("accordion-panel-header-chip").add(chip));
            
            ListGroup<String> listGroup = ListGroup.<String>create()
                    .setBordered(false)
                    .setItemRenderer((listGroup1, listItem) -> {
                        listItem.appendChild(div()
                                .css(Styles.padding_10)
                                .css("theme-list")
                                .add(span().textContent(listItem.getValue())));                        
                    })
                    .setItems(notConcernedThemes);
            accordionPanel.setContent(listGroup);
            accordion.appendChild(accordionPanel);

            accordionPanel.getHeaderElement().addEventListener(EventType.click, new EventListener() {
                @Override
                public void handleEvent(Event evt) {
//                    if (!oerebAccordionPanelNotConcernedThemeState) {
//                        accordionPanel.show();
//                        oerebAccordionPanelConcernedThemeState = false;
//                        oerebAccordionPanelNotConcernedThemeState = true;
//                        oerebAccordionPanelThemesWithoutDataState = false;
//                        oerebAccordionPanelGeneralInformationState = false;
//                        List<AccordionPanel> panels = oerebAccordion.getPanels();
//                        for (AccordionPanel panel : panels) {
//                            if(!panel.equals(accordionPanel)) {
//                                panel.hide();
//                            }
//                        }                        
//                    } else {
//                        accordionPanel.hide();
//                        oerebAccordionPanelNotConcernedThemeState = false;
//                    }            
                }
            });            
        }

        {
            AccordionPanel accordionPanelThemesWithoutData = AccordionPanel.create(messages.result_theme_theme_without_data());
            accordionPanelThemesWithoutData.css("accordion-panel-theme");  
            accordionPanelThemesWithoutData.elevate(0);       
            DominoElement<HTMLDivElement> accordionPanelThemesWithoutDataElement = accordionPanelThemesWithoutData.getHeaderElement();
            accordionPanelThemesWithoutDataElement.addCss("accordion-panel-header-element");
            
            Chip chip = Chip.create().setValue(String.valueOf(themesWithoutData.size()))
                    .setColor(Color.GREY_LIGHTEN_1)
                    .style()
                    .setPadding("0px")
                    .setMargin("4px")
                    .setTextAlign("center").get();
            accordionPanelThemesWithoutDataElement.appendChild(span().css("accordion-panel-header-chip").add(chip));
            
            ListGroup<String> listGroup = ListGroup.<String>create()
                    .setBordered(false)
                    .setItemRenderer((listGroup1, listItem) -> {
                        listItem.appendChild(div()
                                .css(Styles.padding_10)
                                .css("theme-list")
                                .add(span().textContent(listItem.getValue())));                        
                    })
                    .setItems(themesWithoutData);
            accordionPanelThemesWithoutData.setContent(listGroup);
            accordion.appendChild(accordionPanelThemesWithoutData);

            accordionPanelThemesWithoutData.getHeaderElement().addEventListener(EventType.click, new EventListener() {
                @Override
                public void handleEvent(Event evt) {
//                    if (!oerebAccordionPanelThemesWithoutDataState) {
//                        accordionPanelThemesWithoutData.show();
//                        oerebAccordionPanelConcernedThemeState = false;
//                        oerebAccordionPanelNotConcernedThemeState = false;
//                        oerebAccordionPanelThemesWithoutDataState = true;
//                        oerebAccordionPanelGeneralInformationState = false;
//                        List<AccordionPanel> panels = oerebAccordion.getPanels();
//                        for (AccordionPanel panel : panels) {
//                            if(!panel.equals(accordionPanelThemesWithoutData)) {
//                                panel.hide();
//                            }
//                        }
//                    } else {
//                        accordionPanelThemesWithoutData.hide();
//                        oerebAccordionPanelThemesWithoutDataState = false;
//                    }            
                }
            });            
        }

        
        
        resultDiv.appendChild(div);
        resultCardContent.appendChild(resultDiv);
        
        // Show result
        resultCard.style.height = CSSProperties.HeightUnionType.of(RESULT_CARD_HEIGHT);
        resultCard.style.overflow = "auto";
        resultCard.style.visibility = "visible";
    }
    
    private HTMLElement processRestrictionRow(Restriction restriction, GeometryType type) {
        Row row = Row.create().css("restriction-row");
        
        row.appendChild(Column.span6().setTooltip(restriction.getLegendText()).style().cssText("overflow:hidden; white-space:nowrap; text-overflow: ellipsis;").get().setTextContent(restriction.getLegendText()));
        
        String srcAttr = fixUrl(restriction.getSymbolRef());        
        HTMLElement symbol = img().attr("src", srcAttr)
                .attr("alt", "Symbol " + restriction.getLegendText())
                .attr("width", "30px")
                .style("border: 0px solid black").element();

        row.appendChild(Column.span1().appendChild(symbol));

        if (type == GeometryType.POLYGON) {
            Column col = Column.span3().style().setTextAlign("right").get();
            if (restriction.getAreaShare() < 0.1) {
                col.appendChild(span().innerHtml(SafeHtmlUtils.fromTrustedString("< 0.1 m<span class=\"sup\">2</span>")));
            } else {
                col.appendChild(span().innerHtml(SafeHtmlUtils.fromTrustedString(fmtDefault.format(restriction.getAreaShare()) + " m<span class=\"sup\">2</span>")));
            }
            row.appendChild(col);
        }

        if (type == GeometryType.POLYGON && restriction.getPartInPercent() != null) {
            Column col = Column.span2().style().setTextAlign("right").get();
            if (restriction.getPartInPercent() < 0.1) {
                col.appendChild(span().textContent("< 0.1"));
            } else {
                col.appendChild(span().textContent(fmtPercent.format(restriction.getPartInPercent())));
            }
            row.appendChild(col);
        }

        if (type == GeometryType.LINE) {
            Column col = Column.span3().style().setTextAlign("right").get();            
            if (restriction.getLengthShare() < 0.1) {
                col.appendChild(span().textContent("< 0.1 m"));
            } else {
                col.appendChild(span().textContent(fmtDefault.format(restriction.getLengthShare()) + " m"));

            }
            row.appendChild(col);            
        }

        if (type == GeometryType.POINT) {
            Column col = Column.span3().style().setTextAlign("right").get();
            String str = fmtDefault.format(restriction.getNrOfPoints()) + " " + messages.result_nr_of_points();
            col.appendChild(span().textContent(str));
            row.appendChild(col);                        
        }
        return row.element();        
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
        popupBuilder.add(div().id("realestate-popup-header").add(span().textContent(messages.map_popup_title()))
                .add(span().id("realestate-popup-close").add(closeButton)));

        HashMap<String, Grundstueck> grundstueckeMap = new HashMap<String, Grundstueck>();
        for (Grundstueck grundstueck : grundstueckeList) {
            String egrid = grundstueck.getEgrid();
            String number = grundstueck.getNummer();
            String type = grundstueck.getArt();

            String label = new String("GB-Nr.: " + number + " (" + type + ")");
            HTMLDivElement row = div().id(egrid).css("realestate-popup-row").add(span().textContent(label)).element();

            grundstueckeMap.put(egrid, grundstueck);

            bind(row, mouseover, evt -> {
                row.style.backgroundColor = "#efefef";
                row.style.cursor = "pointer";
                Feature feature = OLFactory.createFeature();
                feature.setGeometry(grundstueck.getGeometrie());
                Feature[] fs = new Feature[] { feature };
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
                Feature[] fs = new Feature[] { f };
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
        
        removeOerebWmsLayers();

        if (headerRow != null) {
            headerRow.remove();
        }
        
        if (resultDiv != null) {
            resultDiv.remove();
        }
        
        updateUrlLocation(null);
        
        resultCard.style.visibility = "hidden";
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
        // stroke.setColor(new ol.color.Color(249, 128, 0, 1.0));
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
    
    private Image createOerebWmsLayer(ReferenceWMS referenceWms) {
        ImageWmsParams imageWMSParams = OLFactory.createOptions();
        imageWMSParams.setLayers(referenceWms.getLayers());

        ImageWmsOptions imageWMSOptions = OLFactory.createOptions();

        String baseUrl = referenceWms.getBaseUrl();

        imageWMSOptions.setUrl(baseUrl);
        imageWMSOptions.setParams(imageWMSParams);
        imageWMSOptions.setRatio(1.5f);

        ImageWms imageWMSSource = new ImageWms(imageWMSOptions);

        LayerOptions layerOptions = OLFactory.createOptions();
        layerOptions.setSource(imageWMSSource);

        Image wmsLayer = new Image(layerOptions);
        wmsLayer.set(ID_ATTR_NAME, referenceWms.getLayers());
        wmsLayer.setVisible(false);
        wmsLayer.setOpacity(referenceWms.getLayerOpacity());
 
        return wmsLayer;
    }
    
    private void removeOerebWmsLayers() {
        for (String layerId : oerebWmsLayers) {
            Image rlayer = (Image) getMapLayerById(layerId);
            map.removeLayer(rlayer);
        }
        oerebWmsLayers.clear();
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

    private String fixUrl(String url) {
        return url
                .replace("%3A%2F%2F", "://")
                .replace("%2C", ",")
                .replace("%2F", "/")
                .replace("%3A", ":")
                .replace("%3F", "?")
                .replace("%3D", "=");
    }
    
    private void updateUrlLocation(String egrid) {
        URL url = new URL(DomGlobal.location.href);
        String host = url.host;
        String protocol = url.protocol;
        String pathname = url.pathname;
        
        String newUrl = protocol + "//" + host + pathname;
        if (egrid != null) {
            URLSearchParams params = url.searchParams;
            params.set("egrid", egrid);
            newUrl += "?" + params.toString(); 
        } 
        updateUrlWithoutReloading(newUrl);
    }

    // Update the URL in the browser without reloading the page.
    private static native void updateUrlWithoutReloading(String newUrl) /*-{
        $wnd.history.pushState(newUrl, "", newUrl);
    }-*/;
}