package ch.so.agi.oereb.webclient.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.jsonp.client.JsonpRequestBuilder;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

import ch.so.agi.oereb.webclient.shared.ExtractResponse;
import ch.so.agi.oereb.webclient.shared.ExtractService;
import ch.so.agi.oereb.webclient.shared.ExtractServiceAsync;
import ch.so.agi.oereb.webclient.shared.SettingsResponse;
import ch.so.agi.oereb.webclient.shared.SettingsService;
import ch.so.agi.oereb.webclient.shared.SettingsServiceAsync;
import ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.Address;
import ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.Building;
import ch.so.agi.oereb.webclient.shared.models.plr.ConcernedTheme;
import ch.so.agi.oereb.webclient.shared.models.plr.Extract;
import ch.so.agi.oereb.webclient.shared.models.plr.NotConcernedTheme;
import ch.so.agi.oereb.webclient.shared.models.plr.Office;
import ch.so.agi.oereb.webclient.shared.models.plr.RealEstateDPR;
import ch.so.agi.oereb.webclient.shared.models.plr.ReferenceWMS;
import ch.so.agi.oereb.webclient.shared.models.plr.Restriction;
import ch.so.agi.oereb.webclient.shared.models.plr.ThemeWithoutData;

import com.google.gwt.user.client.ui.Widget;

import gwt.material.design.addins.client.autocomplete.MaterialAutoComplete;
import gwt.material.design.addins.client.window.MaterialWindow;
import gwt.material.design.client.constants.ButtonSize;
import gwt.material.design.client.constants.ButtonType;
import gwt.material.design.client.constants.Color;
import gwt.material.design.client.constants.Display;
import gwt.material.design.client.constants.IconPosition;
import gwt.material.design.client.constants.IconType;
import gwt.material.design.client.constants.Position;
import gwt.material.design.client.constants.TextAlign;
import gwt.material.design.client.constants.WavesType;
import gwt.material.design.client.ui.MaterialButton;
import gwt.material.design.client.ui.MaterialCard;
import gwt.material.design.client.ui.MaterialCardContent;
import gwt.material.design.client.ui.MaterialChip;
import gwt.material.design.client.ui.MaterialCollapsible;
import gwt.material.design.client.ui.MaterialCollapsibleBody;
import gwt.material.design.client.ui.MaterialCollapsibleHeader;
import gwt.material.design.client.ui.MaterialCollapsibleItem;
import gwt.material.design.client.ui.MaterialCollection;
import gwt.material.design.client.ui.MaterialCollectionItem;
import gwt.material.design.client.ui.MaterialColumn;
import gwt.material.design.client.ui.MaterialIcon;
import gwt.material.design.client.ui.MaterialLabel;
import gwt.material.design.client.ui.MaterialLink;
import gwt.material.design.client.ui.MaterialLoader;
import gwt.material.design.client.ui.MaterialPanel;
import gwt.material.design.client.ui.MaterialRange;
import gwt.material.design.client.ui.MaterialRow;
import gwt.material.design.client.ui.MaterialTab;
import gwt.material.design.client.ui.MaterialTabItem;
import gwt.material.design.client.ui.MaterialToast;
import gwt.material.design.client.ui.html.Div;
import gwt.material.design.client.ui.html.Span;
import ol.Collection;
import ol.Coordinate;
import ol.Extent;
import ol.Feature;
import ol.FeatureOptions;
import ol.Map;
import ol.MapBrowserEvent;
import ol.MapOptions;
import ol.OLFactory;
import ol.View;
import ol.ViewOptions;
import ol.control.Control;
import ol.event.EventListener;
import ol.format.GeoJson;
import ol.format.Wkt;
import ol.geom.Geometry;
import ol.interaction.DefaultInteractionsOptions;
import ol.interaction.Interaction;
import ol.layer.Base;
import ol.layer.Image;
import ol.layer.LayerOptions;
import ol.layer.Tile;
import ol.layer.VectorLayerOptions;
import ol.proj.Projection;
import ol.proj.ProjectionOptions;
import ol.source.ImageWms;
import ol.source.ImageWmsOptions;
import ol.source.ImageWmsParams;
import ol.source.Vector;
import ol.source.VectorOptions;
import ol.source.Wmts;
import ol.source.WmtsOptions;
import ol.style.Stroke;
import ol.style.Style;
import ol.tilegrid.TileGrid;
import ol.tilegrid.WmtsTileGrid;
import ol.tilegrid.WmtsTileGridOptions;
import proj4.Proj4;

public class AppEntryPoint implements EntryPoint {
    private PLRMessages messages = GWT.create(PLRMessages.class);
    private final ExtractServiceAsync extractService = GWT.create(ExtractService.class);
    private final SettingsServiceAsync settingsService = GWT.create(SettingsService.class);

    private String SUB_HEADER_FONT_SIZE = "16px";
    private String BODY_FONT_SIZE = "14px";
    private String SMALL_FONT_SIZE = "12px";

    private String RESULT_CARD_HEIGHT = "calc(100% - 215px)";

    private String ID_ATTR_NAME = "id";
    private String BACKGROUND_LAYER_ID = "ch.so.agi.hintergrundkarte_sw";
    private String RESTRICTION_VECTOR_LAYER_ID = "restriction_vector_layer";
    private String RESTRICTION_VECTOR_FEATURE_ID = "restriction_fid";
    private String REAL_ESTATE_VECTOR_LAYER_ID = "real_estate_vector_layer";
    private String REAL_ESTATE_VECTOR_FEATURE_ID = "real_estate_fid";

    private String OEREB_SERVICE_URL;
    private String CADASTRAL_SURVEYING_PARCEL_URL;
    private String SEARCH_SERVICE_PATH;
    private String REAL_ESTATE_DATAPRODUCT_ID;
    private String ADDRESS_DATAPRODUCT_ID;
    private String DATA_SERVICE_URL;
    private String BACKGROUND_WMTS_URL;
    private String BACKGROUND_WMTS_LAYER;

    private HashMap<String, String> WMS_HOST_MAPPING;

    private NumberFormat fmtDefault = NumberFormat.getDecimalFormat();
    private NumberFormat fmtPercent = NumberFormat.getFormat("#0.0");

    private MaterialAutoComplete autocomplete;
    private Map map;
    private MaterialCard searchCard;
    private MaterialCard resultCard;
    private MaterialCardContent searchCardContent;
    private MaterialCardContent resultCardContent;
    private MaterialRow resultHeaderRow;
    private MaterialTab resultTab;
    private Div resultDiv;
    private MaterialColumn cadastralSurveyingResultColumn;
    private MaterialColumn plrResultColumn;

    private MaterialWindow realEstateWindow;
    private MaterialCollapsible plrCollapsibleConcernedTheme;
    private MaterialCollapsible plrInnerCollapsibleConcernedTheme;
    private String plrExpandedLayerId;
    private MaterialCollapsible plrCollapsibleNotConcernedTheme;
    private MaterialCollapsible plrCollapsibleThemesWithoutData;
    private MaterialCollapsible plrCollapsibleGeneralInformation;

    private ArrayList<String> concernedWmsLayers = new ArrayList<String>();

    public void onModuleLoad() {
        settingsService.settingsServer(new AsyncCallback<SettingsResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log("error: " + caught.getMessage());
                MaterialToast.fireToast(caught.getMessage());
            }

            @Override
            public void onSuccess(SettingsResponse result) {
                OEREB_SERVICE_URL = (String) result.getSettings().get("OEREB_SERVICE_URL");
                CADASTRAL_SURVEYING_PARCEL_URL = (String) result.getSettings().get("CADASTRAL_SURVEYING_PARCEL_URL");
                SEARCH_SERVICE_PATH = (String) result.getSettings().get("SEARCH_SERVICE_PATH");
                REAL_ESTATE_DATAPRODUCT_ID = (String) result.getSettings().get("REAL_ESTATE_DATAPRODUCT_ID");
                ADDRESS_DATAPRODUCT_ID = (String) result.getSettings().get("ADDRESS_DATAPRODUCT_ID");
                DATA_SERVICE_URL = (String) result.getSettings().get("DATA_SERVICE_URL");
                BACKGROUND_WMTS_URL = (String) result.getSettings().get("BACKGROUND_WMTS_URL");
                BACKGROUND_WMTS_LAYER = (String) result.getSettings().get("BACKGROUND_WMTS_LAYER");
                WMS_HOST_MAPPING = (HashMap<String, String>) result.getSettings().get("WMS_HOST_MAPPING");
                init();
            }
        });
    }

    private void init() {
        // div for ol3 map
        Div mapDiv = new Div();
        mapDiv.setId("map");

        // Dummy button for testing with a hardcode egrid.
        MaterialButton dummyButton = new MaterialButton();
        dummyButton.setId("dummyButton");
        dummyButton.setType(ButtonType.FLOATING);
        dummyButton.setSize(ButtonSize.LARGE);
        dummyButton.setIconType(IconType.HELP_OUTLINE);
        dummyButton.addClickHandler(new DummyButtonClickHandler());

        // A material card for the search.
        searchCard = new MaterialCard();
        searchCard.setId("searchCard");

        searchCardContent = new MaterialCardContent();
        searchCardContent.setId("searchCardContent");

        MaterialRow logoRow = new MaterialRow();

        com.google.gwt.user.client.ui.Image plrImage = new com.google.gwt.user.client.ui.Image();
        plrImage.setUrl(GWT.getHostPageBaseURL() + "grundstuecksinfo_logo.png");
        plrImage.setWidth("60%");

        MaterialColumn plrLogoColumn = new MaterialColumn();
        plrLogoColumn.setId("plrLogoColumn");
        plrLogoColumn.setGrid("s6");
        plrLogoColumn.add(plrImage);

        com.google.gwt.user.client.ui.Image cantonImage = new com.google.gwt.user.client.ui.Image();
        cantonImage.setUrl(GWT.getHostPageBaseURL() + "Logo.png");
        cantonImage.setWidth("80%");

        MaterialColumn cantonLogoColumn = new MaterialColumn();
        cantonLogoColumn.setId("cantonLogoColumn");
        cantonLogoColumn.setGrid("s6");
        cantonLogoColumn.add(cantonImage);

        logoRow.add(plrLogoColumn);
        logoRow.add(cantonLogoColumn);
        searchCardContent.add(logoRow);

        MaterialRow searchRow = new MaterialRow();
        searchRow.setId("searchRow");

        SearchOracle searchOracle = new SearchOracle(SEARCH_SERVICE_PATH);
        autocomplete = new MaterialAutoComplete(searchOracle);
        autocomplete.setId("autocomplete");
        // It's not possible to get the object with AutocompleteType.TEXT
        // you only the the text then. But we definitely
        // need the object.
        // The chip can be made invisible with CSS. But the size
        // must be also set to zero.
        autocomplete.setPlaceholder(messages.searchPlaceholder());
        autocomplete.setAutoSuggestLimit(5);
        autocomplete.setLimit(1);
        autocomplete.addValueChangeHandler(new SearchValueChangeHandler());

        searchRow.add(autocomplete);
        searchCardContent.add(searchRow);
        searchCard.add(searchCardContent);

        // A material card for the result.
        resultCard = new MaterialCard();
        resultCard.setId("resultCard");

        resultCardContent = new MaterialCardContent();
        resultCardContent.setId("resultCardContent");
        resultCard.add(resultCardContent);

        Div fadeoutBottomDiv = new Div();
        fadeoutBottomDiv.setId("fadeoutBottomDiv");
        resultCard.add(fadeoutBottomDiv);

        // RootPanel.get().add(dummyButton);
        RootPanel.get().add(mapDiv);
        RootPanel.get().add(searchCard);
        RootPanel.get().add(resultCard);

        initMap(mapDiv.getId());

        // If there is an egrid query parameter in the url,
        // we request the extract without further interaction.
        if (Window.Location.getParameter("egrid") != null) {
            String egrid = Window.Location.getParameter("egrid").toString();
            MaterialLoader.loading(true);
            resetGui();
            sendEgridToServer(egrid);
        }
    }

    private void sendEgridToServer(String egrid) {
        extractService.extractServer(egrid, new AsyncCallback<ExtractResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                MaterialLoader.loading(false);

                if (caught.getMessage().equalsIgnoreCase("204")) {
                    MaterialToast.fireToast(messages.responseError204(egrid));
                } else if (caught.getMessage().equalsIgnoreCase("500")) {
                    MaterialToast.fireToast(messages.responseError500());
                    MaterialToast.fireToast(caught.getMessage());
                } else {
                    MaterialToast.fireToast(messages.responseError500());
                    MaterialToast.fireToast(caught.getMessage());
                }
                GWT.log("error: " + caught.getMessage());
            }

            @Override
            public void onSuccess(ExtractResponse result) {
                MaterialLoader.loading(false);

                String newUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost()
                        + Window.Location.getPath() + "?egrid=" + egrid;
                updateURLWithoutReloading(newUrl);

                Extract extract = result.getPlrExtract();
                RealEstateDPR realEstate = extract.getRealEstate();
                String number = realEstate.getNumber();
                String municipality = realEstate.getMunicipality();
                String subunitOfLandRegister = realEstate.getSubunitOfLandRegister();
                String canton = realEstate.getCanton();
                String egrid = realEstate.getEgrid();
                int area = realEstate.getLandRegistryArea();
                String realEstateType = realEstate.getRealEstateType();

                removePlrLayers();

                // create the vector layer for highlighting the real estate
                ol.layer.Vector vlayer = createRealEstateVectorLayer(result.getPlrExtract().getRealEstate().getLimit());

                // set new extent and center according the real estate
                Geometry geometry = new Wkt().readGeometry(result.getPlrExtract().getRealEstate().getLimit());
                Extent extent = geometry.getExtent();

                View view = map.getView();
                double resolution = view.getResolutionForExtent(extent);
                view.setZoom(Math.floor(view.getZoomForResolution(resolution)) - 1);

                double x = extent.getLowerLeftX() + extent.getWidth() / 2;
                double y = extent.getLowerLeftY() + extent.getHeight() / 2;

                // Das ist jetzt ziemlich heuristisch...
                // 500 = Breite des Suchresultates
                view.setCenter(new Coordinate(x - (resultCard.getWidth() * view.getResolution()) / 2, y));

                vlayer.setZIndex(1001);
                map.addLayer(vlayer);

                resultDiv = new Div();
                resultDiv.setId("resultDiv");
//                resultDiv.setBackgroundColor(Color.WHITE);
                resultDiv.setBackgroundColor(Color.GREY_LIGHTEN_5);

                resultHeaderRow = new MaterialRow();
                resultHeaderRow.setId("resultHeaderRow");

                MaterialColumn resultParcelColumn = new MaterialColumn();
                resultParcelColumn.setId("resultParcelColumn");
                resultParcelColumn.setGrid("s8");

                String lblString = messages.resultHeader(number);
                Label lbl = new Label(lblString);
                resultParcelColumn.add(lbl);
                resultHeaderRow.add(resultParcelColumn);

                MaterialColumn resultButtonColumn = new MaterialColumn();
                resultButtonColumn.setId("resultButtonColumn");
                resultButtonColumn.setGrid("s4");

                MaterialButton deleteExtractButton = new MaterialButton();
                deleteExtractButton.setId("deleteExtractButton");
                deleteExtractButton.setIconType(IconType.CLOSE);
                deleteExtractButton.setType(ButtonType.FLOATING);
                deleteExtractButton.setTooltip(messages.resultCloseTooltip());
                deleteExtractButton.setTooltipPosition(Position.TOP);
                deleteExtractButton.addClickHandler(event -> {
                    resetGui();
                });
                resultButtonColumn.add(deleteExtractButton);

                MaterialButton minmaxExtractButton = new MaterialButton();
                minmaxExtractButton.setId("minmaxExtractButton");
                minmaxExtractButton.setMarginLeft(10);
                minmaxExtractButton.setIconType(IconType.REMOVE);
                minmaxExtractButton.setType(ButtonType.FLOATING);
                minmaxExtractButton.setTooltip(messages.resultMinimizeTooltip());
                minmaxExtractButton.setTooltipPosition(Position.TOP);

                minmaxExtractButton.addClickHandler(event -> {
                    if (resultCard.getOffsetHeight() > resultHeaderRow.getOffsetHeight()) {
                        minmaxExtractButton.setIconType(IconType.ADD);
                        minmaxExtractButton.setTooltip(messages.resultMaximizeTooltip());

                        resultCard.getElement().getStyle().setProperty("overflowY", "hidden");
                        resultCard.setHeight(String.valueOf(resultHeaderRow.getOffsetHeight()) + "px");
                        resultDiv.setVisibility(com.google.gwt.dom.client.Style.Visibility.HIDDEN);
                    } else {
                        minmaxExtractButton.setIconType(IconType.REMOVE);
                        minmaxExtractButton.setTooltip(messages.resultMinimizeTooltip());

                        resultDiv.setVisibility(com.google.gwt.dom.client.Style.Visibility.VISIBLE);
                        resultCard.getElement().getStyle().setProperty("overflowY", "auto");
                        resultCard.getElement().getStyle().setProperty("height", RESULT_CARD_HEIGHT);
                    }
                });
                resultButtonColumn.add(minmaxExtractButton);

                resultHeaderRow.add(resultButtonColumn);
                resultCardContent.add(resultHeaderRow);

                MaterialRow tabRow = new MaterialRow();
                tabRow.setId("tabRow");

                MaterialColumn tabHeaderColumn = new MaterialColumn();
                tabHeaderColumn.setId("tabHeaderColumn");
                tabHeaderColumn.setGrid("s12");

                resultTab = new MaterialTab();
                resultTab.setId("resultTab");
                resultTab.setShadow(1);
                // #e8c432
                // #aed634
                // #52b1a7
                // #75a5d4
                resultTab.setBackgroundColor(Color.RED_LIGHTEN_1);
                resultTab.setIndicatorColor(Color.WHITE);

                MaterialTabItem cadastralSurveyingHeaderTabItem = new MaterialTabItem();
                cadastralSurveyingHeaderTabItem.setWaves(WavesType.LIGHT);
                cadastralSurveyingHeaderTabItem.setGrid("s4");

                MaterialLink cadastralSurveyingHeaderTabLink = new MaterialLink();
                cadastralSurveyingHeaderTabLink.setText(messages.tabTitleCadastralSurveying());
                cadastralSurveyingHeaderTabLink.setHref("#cadastralSurveyingResultColumn");
                cadastralSurveyingHeaderTabLink.setTextColor(Color.WHITE);
                cadastralSurveyingHeaderTabItem.add(cadastralSurveyingHeaderTabLink);
                resultTab.add(cadastralSurveyingHeaderTabItem);

                MaterialTabItem grundbuchHeaderTabItem = new MaterialTabItem();
                grundbuchHeaderTabItem.setWaves(WavesType.LIGHT);
                grundbuchHeaderTabItem.setGrid("s4");
                grundbuchHeaderTabItem.setEnabled(false);

                MaterialLink grundbuchHeaderTabLink = new MaterialLink();
                grundbuchHeaderTabLink.setText(messages.tabTitleLandRegister());
                grundbuchHeaderTabLink.setHref("#tab2");
                grundbuchHeaderTabLink.setTextColor(Color.WHITE);
                grundbuchHeaderTabItem.add(grundbuchHeaderTabLink);
                resultTab.add(grundbuchHeaderTabItem);

                MaterialTabItem oerebHeaderTabItem = new MaterialTabItem();
                oerebHeaderTabItem.setWaves(WavesType.LIGHT);
                oerebHeaderTabItem.setGrid("s4");

                MaterialLink oerebHeaderTabLink = new MaterialLink();
                oerebHeaderTabLink.setText(messages.tabTitlePlr());
                oerebHeaderTabLink.setHref("#plrResultColumn");
                oerebHeaderTabLink.setTextColor(Color.WHITE);
                oerebHeaderTabItem.add(oerebHeaderTabLink);
                resultTab.add(oerebHeaderTabItem);

                tabHeaderColumn.add(resultTab);
                tabRow.add(tabHeaderColumn);

                cadastralSurveyingResultColumn = new MaterialColumn();
                cadastralSurveyingResultColumn.setId("cadastralSurveyingResultColumn");
                cadastralSurveyingResultColumn.addStyleName("resultColumn");
                cadastralSurveyingResultColumn.setGrid("s12");
                addCadastralSurveyingContent(result.getCadastralSurveyingExtract());
                tabRow.add(cadastralSurveyingResultColumn);

                plrResultColumn = new MaterialColumn();
                plrResultColumn.setId("plrResultColumn");
                plrResultColumn.addStyleName("resultColumn");
                plrResultColumn.setGrid("s12");
                addPlrContent(result.getPlrExtract());
                tabRow.add(plrResultColumn);

                resultTab.addSelectionHandler(event -> {
                    if (event.getSelectedItem() == 2) {
                        for (String layerId : concernedWmsLayers) {
                            Image wmsLayer = (Image) getLayerById(layerId);
                            if (layerId.equalsIgnoreCase(plrExpandedLayerId)) {
                                wmsLayer.setVisible(true);
                            } else {
                                wmsLayer.setVisible(false);
                            }
                        }
                    } else {
                        for (String layerId : concernedWmsLayers) {
                            Image wmsLayer = (Image) getLayerById(layerId);
                            wmsLayer.setVisible(false);
                        }
                    }
                });

                resultDiv.add(tabRow);

                resultCardContent.add(resultDiv);
                resultCard.getElement().getStyle().setProperty("height", RESULT_CARD_HEIGHT);
                resultCard.getElement().getStyle().setProperty("overflowY", "auto");
                resultCard.getElement().getStyle().setProperty("visibility", "visible");
            }
        });
    }

    private void addPlrContent(ch.so.agi.oereb.webclient.shared.models.plr.Extract extract) {
        {
            MaterialRow pdfRow = new MaterialRow();
            pdfRow.setId("plrPdfRow");

            MaterialColumn pdfButtonColumn = new MaterialColumn();
            pdfButtonColumn.setId("plrPdfButtonColumn");
            pdfButtonColumn.setGrid("s4");
            MaterialButton pdfButton = new MaterialButton();
            pdfButton.setType(ButtonType.OUTLINED);
            pdfButton.setBackgroundColor(Color.WHITE);
            pdfButton.setTextColor(Color.RED_LIGHTEN_1);
            pdfButton.setText("PDF");
            // pdfButton.setIconType(IconType.INSERT_DRIVE_FILE);
            pdfButton.setWidth("100%");
            pdfButton.setTooltip(messages.resultPDFTooltip());
            pdfButtonColumn.add(pdfButton);

            pdfRow.add(pdfButtonColumn);
            plrResultColumn.add(pdfRow);

            pdfButton.addClickHandler(event -> {
                Window.open(OEREB_SERVICE_URL + "extract/reduced/pdf/geometry/" + extract.getRealEstate().getEgrid(),
                        "_blank", null);
            });
        }

        Div plrCollapsibleDiv = new Div();

        {
            plrCollapsibleConcernedTheme = new MaterialCollapsible();
            plrCollapsibleConcernedTheme.addStyleName("plrTopLevelCollapsible");
            plrCollapsibleConcernedTheme.setShadow(0);

            plrCollapsibleConcernedTheme.addExpandHandler(event -> {
                plrCollapsibleNotConcernedTheme.closeAll();
                plrCollapsibleThemesWithoutData.closeAll();
                plrCollapsibleGeneralInformation.closeAll();
            });

            MaterialCollapsibleItem collapsibleConcernedThemeItem = new MaterialCollapsibleItem();

            MaterialCollapsibleHeader collapsibleConcernedThemeHeader = new MaterialCollapsibleHeader();
            collapsibleConcernedThemeHeader.addStyleName("plrCollapsibleThemeHeader");

            MaterialRow collapsibleConcernedThemeHeaderRow = new MaterialRow();
            collapsibleConcernedThemeHeaderRow.addStyleName("collapsibleThemeHeaderRow");

            MaterialColumn collapsibleConcernedThemeColumnLeft = new MaterialColumn();
            collapsibleConcernedThemeColumnLeft.addStyleName("collapsibleThemeColumnLeft");
            collapsibleConcernedThemeColumnLeft.setGrid("s10");
            MaterialColumn collapsibleConcernedThemeColumnRight = new MaterialColumn();
            collapsibleConcernedThemeColumnRight.addStyleName("collapsibleThemeColumnRight");
            collapsibleConcernedThemeColumnRight.setGrid("s2");

            MaterialLink collapsibleThemesHeaderLink = new MaterialLink();
            collapsibleThemesHeaderLink.addStyleName("collapsibleThemesHeaderLink");
            collapsibleThemesHeaderLink.setText(messages.concernedThemes());
            collapsibleConcernedThemeColumnLeft.add(collapsibleThemesHeaderLink);

            MaterialChip collapsibleThemesHeaderChip = new MaterialChip();
            collapsibleThemesHeaderChip.addStyleName("collapsibleThemesHeaderChip");
            collapsibleThemesHeaderChip.setText(String.valueOf(extract.getRealEstate().getConcernedThemes().size()));
            collapsibleConcernedThemeColumnRight.add(collapsibleThemesHeaderChip);

            collapsibleConcernedThemeHeaderRow.add(collapsibleConcernedThemeColumnLeft);
            collapsibleConcernedThemeHeaderRow.add(collapsibleConcernedThemeColumnRight);

            collapsibleConcernedThemeHeader.add(collapsibleConcernedThemeHeaderRow);

            MaterialCollapsibleBody collapsibleConcernedThemeBody = new MaterialCollapsibleBody();
            if (extract.getRealEstate().getConcernedThemes().size() > 0) {
                collapsibleConcernedThemeBody.setPadding(0);

                plrInnerCollapsibleConcernedTheme = new MaterialCollapsible();
                plrInnerCollapsibleConcernedTheme.addStyleName("concernedThemeCollapsible");
                plrInnerCollapsibleConcernedTheme.setAccordion(true);
                int i = 0;

                for (ConcernedTheme theme : extract.getRealEstate().getConcernedThemes()) {
                    i++;

                    plrInnerCollapsibleConcernedTheme.setShadow(0);

                    Image wmsLayer = createPlrWmsLayer(theme.getReferenceWMS());
                    map.addLayer(wmsLayer);

                    MaterialCollapsibleItem item = new MaterialCollapsibleItem();

                    // Cannot use the code since all subthemes share
                    // the same code.
                    String layerId = theme.getReferenceWMS().getLayers();
                    item.setId(layerId);
                    concernedWmsLayers.add(layerId);

                    MaterialCollapsibleHeader header = new MaterialCollapsibleHeader();
                    header.addStyleName("collapsibleThemeLayerHeader");
                    if (i < extract.getRealEstate().getConcernedThemes().size()) {
                        header.setBorderBottom("1px solid #dddddd");
                    } else {
                        header.setBorderBottom("0px solid #dddddd");
                    }

                    Div aParent = new Div();
                    aParent.addStyleName("helperParent");

                    MaterialLink link = new MaterialLink();
                    link.addStyleName("collapsibleThemeLayerLink");
                    link.setText(theme.getName());

                    aParent.add(link);
                    header.add(aParent);
                    item.add(header);

                    MaterialCollapsibleBody body = new MaterialCollapsibleBody();
                    body.addStyleName("collapsibleThemeLayerBody");
                    body.addMouseOverHandler(event -> {
                        body.getElement().getStyle().setCursor(Cursor.DEFAULT);
                    });
                    if (i < extract.getRealEstate().getConcernedThemes().size()) {
                        body.setBorderBottom("1px solid #dddddd");
                    } else {
                        body.setBorderBottom("0px solid #dddddd");
                        body.setBorderTop("1px solid #dddddd");
                    }

                    MaterialRow sliderRow = new MaterialRow();
                    sliderRow.addStyleName("opacitySliderRow");

                    MaterialColumn sliderRowLeft = new MaterialColumn();
                    sliderRowLeft.setGrid("s3");
                    MaterialColumn sliderRowRight = new MaterialColumn();
                    sliderRowRight.setGrid("s9");

                    MaterialRange slider = new MaterialRange();
                    slider.addStyleName("opacitySlider");
                    slider.setMin(0);
                    slider.setMax(100);
                    slider.setValue(Double.valueOf((theme.getReferenceWMS().getLayerOpacity() * 100)).intValue());
                    slider.addValueChangeHandler(event -> {
                        double opacity = slider.getValue() / 100.0;
                        wmsLayer.setOpacity(opacity);
                    });
                    sliderRowLeft.add(new Label(messages.resultOpacity() + ":"));
                    sliderRowLeft.addStyleName("opacitySliderRowLeft");

                    sliderRowRight.add(slider);
                    sliderRow.add(sliderRowLeft);
                    sliderRow.add(sliderRowRight);
                    body.add(sliderRow);

                    {
                        MaterialRow informationHeaderRow = new MaterialRow();
                        informationHeaderRow.addStyleName("layerInfoHeaderRow");

                        MaterialColumn typeColumn = new MaterialColumn();
                        typeColumn.addStyleName("layerTypeColumn");
                        typeColumn.setGrid("s6");
                        typeColumn.add(new Label(messages.resultType()));

                        MaterialColumn symbolColumn = new MaterialColumn();
                        symbolColumn.addStyleName("layerSymbolColumn");
                        symbolColumn.setGrid("s1");
                        symbolColumn.add(new HTML("&nbsp;"));

                        MaterialColumn shareColumn = new MaterialColumn();
                        shareColumn.addStyleName("layerShareColumn");
                        shareColumn.setGrid("s3");
                        shareColumn.add(new Label(messages.resultShare()));

                        MaterialColumn sharePercentColumn = new MaterialColumn();
                        sharePercentColumn.addStyleName("layerPercentColumn");
                        sharePercentColumn.setGrid("s2");
                        sharePercentColumn.add(new Label(messages.resultShareInPercent()));

                        informationHeaderRow.add(typeColumn);
                        informationHeaderRow.add(symbolColumn);
                        informationHeaderRow.add(shareColumn);
                        informationHeaderRow.add(sharePercentColumn);
                        body.add(informationHeaderRow);
                    }

                    {
                        for (Restriction restriction : theme.getRestrictions()) {
                            if (restriction.getAreaShare() != null) {
                                MaterialRow informationRow = processRestrictionRow(restriction, GeometryType.POLYGON);
                                body.add(informationRow);
                            }

                            if (restriction.getLengthShare() != null) {
                                MaterialRow informationRow = processRestrictionRow(restriction, GeometryType.LINE);
                                body.add(informationRow);
                            }

                            if (restriction.getNrOfPoints() != null) {
                                MaterialRow informationRow = processRestrictionRow(restriction, GeometryType.POINT);
                                body.add(informationRow);
                            }
                        }
                        MaterialRow fakeRow = new MaterialRow();
                        fakeRow.setBorderBottom("1px #bdbdbd solid");
                        body.add(fakeRow);
                    }

                    if (theme.getLegendAtWeb() != null) {
                        MaterialRow legendRow = new MaterialRow();
                        legendRow.addStyleName("layerLegendRow");

                        MaterialColumn legendColumn = new MaterialColumn();
                        legendColumn.addStyleName("layerLegendColumn");
                        legendColumn.setGrid("s12");

                        MaterialLink legendLink = new MaterialLink();
                        legendLink.addStyleName("resultLink");
                        legendLink.setText(messages.resultShowLegend());
                        legendColumn.add(legendLink);

                        legendRow.add(legendColumn);
                        body.add(legendRow);

                        com.google.gwt.user.client.ui.Image legendImage = new com.google.gwt.user.client.ui.Image();
                        legendImage.setUrl(theme.getLegendAtWeb());
                        legendImage.setVisible(false);
                        body.add(legendImage);

                        MaterialRow fakeRow = new MaterialRow();
                        fakeRow.setBorderBottom("1px #bdbdbd solid");
                        body.add(fakeRow);

                        legendLink.addClickHandler(event -> {
                            if (legendImage.isVisible()) {
                                legendImage.setVisible(false);
                                legendLink.setText(messages.resultShowLegend());
                            } else {
                                legendImage.setVisible(true);
                                legendLink.setText(messages.resultHideLegend());
                            }
                        });
                    }

                    {
                        MaterialRow legalProvisionsHeaderRow = new MaterialRow();
                        legalProvisionsHeaderRow.addStyleName("documentsHeaderRow");
                        legalProvisionsHeaderRow.add(new Label(messages.legalProvisions()));
                        body.add(legalProvisionsHeaderRow);

                        for (ch.so.agi.oereb.webclient.shared.models.plr.Document legalProvision : theme
                                .getLegalProvisions()) {
                            MaterialRow row = new MaterialRow();
                            row.addStyleName("documentRow");

                            MaterialLink legalProvisionLink = new MaterialLink();

                            if (legalProvision.getOfficialTitle() != null) {
                                legalProvisionLink.setText(legalProvision.getOfficialTitle());
                            } else {
                                legalProvisionLink.setText(legalProvision.getTitle());
                            }
                            legalProvisionLink.setHref(legalProvision.getTextAtWeb());
                            legalProvisionLink.setTarget("_blank");
                            legalProvisionLink.addStyleName("resultLink");
                            row.add(legalProvisionLink);
                            body.add(row);

                            MaterialRow additionalInfoRow = new MaterialRow();
                            additionalInfoRow.addStyleName("documentAdditionalInfoRow");

                            String labelText = legalProvision.getTitle();
                            if (legalProvision.getOfficialNumber() != null) {
                                labelText += " Nr. " + legalProvision.getOfficialNumber();
                            }
                            Label label = new Label(labelText);
                            additionalInfoRow.add(label);
                            body.add(additionalInfoRow);
                        }

                        MaterialRow lawsHeaderRow = new MaterialRow();
                        lawsHeaderRow.addStyleName("documentsHeaderRow");
                        lawsHeaderRow.add(new Label(messages.laws()));
                        body.add(lawsHeaderRow);

                        for (ch.so.agi.oereb.webclient.shared.models.plr.Document law : theme.getLaws()) {
                            MaterialRow row = new MaterialRow();
                            row.addStyleName("lawRow");

                            MaterialLink lawLink = new MaterialLink();

                            String linkText = "";
                            if (law.getOfficialTitle() != null) {
                                linkText = law.getOfficialTitle();
                            } else {
                                linkText = law.getTitle();
                            }
                            if (law.getAbbreviation() != null) {
                                linkText += " (" + law.getAbbreviation() + ")";
                            }
                            if (law.getOfficialNumber() != null) {
                                linkText += ", " + law.getOfficialNumber();
                            }
                            lawLink.setText(linkText);
                            lawLink.setHref(law.getTextAtWeb());
                            lawLink.setTarget("_blank");
                            lawLink.addStyleName("resultLink");
                            row.add(lawLink);
                            body.add(row);
                        }
                        MaterialRow fakeRow = new MaterialRow();
                        fakeRow.setBorderBottom("1px #bdbdbd solid");
                        fakeRow.setPaddingTop(5);
                        body.add(fakeRow);
                    }
                    {
                        MaterialRow responsibleOfficeHeaderRow = new MaterialRow();
                        responsibleOfficeHeaderRow.addStyleName("documentsHeaderRow");
                        responsibleOfficeHeaderRow.add(new Label(messages.responsibleOffice()));
                        body.add(responsibleOfficeHeaderRow);

                        for (Office office : theme.getResponsibleOffice()) {
                            MaterialRow row = new MaterialRow();
                            row.addStyleName("documentRow");

                            MaterialLink officeLink = new MaterialLink();
                            officeLink.setText(office.getName());
                            officeLink.setHref(office.getOfficeAtWeb());
                            officeLink.setTarget("_blank");
                            officeLink.addStyleName("resultLink");
                            row.add(officeLink);
                            body.add(row);
                        }
                    }
                    item.add(body);
                    plrInnerCollapsibleConcernedTheme.add(item);
                }

                // Handle visibility of plr wms layers.
                // Show them only if plr tab is selected.
                plrInnerCollapsibleConcernedTheme.addExpandHandler(event -> {
                    plrExpandedLayerId = event.getTarget().getId();
                    if (resultTab.getTabIndex() == 2) {
                        for (String layerId : concernedWmsLayers) {
                            Image wmsLayer = (Image) getLayerById(layerId);
                            if (layerId.equalsIgnoreCase(plrExpandedLayerId)) {
                                wmsLayer.setVisible(true);
                            } else {
                                wmsLayer.setVisible(false);
                            }
                        }
//                      MaterialCollapsibleItem item = event.getTarget();
//                      MaterialCollapsibleHeader header = item.getHeader();
//                      List<Widget> children = header.getChildrenList();
//                      for (Widget child : children) {
//                          if (child instanceof gwt.material.design.client.ui.MaterialLink) {
//                              MaterialLink link = (MaterialLink) child;
//                              link.setIconType(IconType.EXPAND_LESS);
//                          }
//                      }
                    }
                });

                plrInnerCollapsibleConcernedTheme.addCollapseHandler(event -> {
                    plrExpandedLayerId = null;
                    Image wmsLayer = (Image) getLayerById(event.getTarget().getId());
                    wmsLayer.setVisible(false);
//                  MaterialCollapsibleItem item = event.getTarget();
//                  MaterialCollapsibleHeader header = item.getHeader();
//                  List<Widget> children = header.getChildrenList();
//                  for (Widget child : children) {
//                      if (child instanceof gwt.material.design.client.ui.MaterialLink) {
//                          MaterialLink link = (MaterialLink) child;
//                          link.setIconType(IconType.EXPAND_MORE);
//                      }
//                  }
                });

                plrInnerCollapsibleConcernedTheme.open(1);
                collapsibleConcernedThemeBody.add(plrInnerCollapsibleConcernedTheme);
            }

            collapsibleConcernedThemeItem.add(collapsibleConcernedThemeHeader);
            if (extract.getRealEstate().getConcernedThemes().size() > 0) {
                collapsibleConcernedThemeItem.add(collapsibleConcernedThemeBody);
            }

            plrCollapsibleConcernedTheme.add(collapsibleConcernedThemeItem);

            if (extract.getRealEstate().getConcernedThemes().size() > 0) {
                plrCollapsibleConcernedTheme.open(1);
            }

            plrCollapsibleDiv.add(plrCollapsibleConcernedTheme);
        }

        {
            plrCollapsibleNotConcernedTheme = new MaterialCollapsible();
            plrCollapsibleNotConcernedTheme.addStyleName("plrTopLevelCollapsible");
            plrCollapsibleNotConcernedTheme.setShadow(0);

            plrCollapsibleNotConcernedTheme.addExpandHandler(event -> {
                plrCollapsibleConcernedTheme.close(1);
                plrCollapsibleThemesWithoutData.closeAll();
                plrCollapsibleGeneralInformation.closeAll();
            });

            MaterialCollapsibleItem collapsibleNotConcernedThemeItem = new MaterialCollapsibleItem();

            MaterialCollapsibleHeader collapsibleNotConcernedThemeHeader = new MaterialCollapsibleHeader();
            collapsibleNotConcernedThemeHeader.addStyleName("plrCollapsibleThemeHeader");

            MaterialRow collapsibleNotConcernedThemeHeaderRow = new MaterialRow();
            collapsibleNotConcernedThemeHeaderRow.addStyleName("collapsibleThemeHeaderRow");

            MaterialColumn collapsibleNotConcernedThemeColumnLeft = new MaterialColumn();
            collapsibleNotConcernedThemeColumnLeft.addStyleName("collapsibleThemeColumnLeft");
            collapsibleNotConcernedThemeColumnLeft.setGrid("s10");
            MaterialColumn collapsibleNotConcernedThemeColumnRight = new MaterialColumn();
            collapsibleNotConcernedThemeColumnRight.addStyleName("collapsibleThemeColumnRight");
            collapsibleNotConcernedThemeColumnRight.setGrid("s2");

            MaterialLink collapsibleNotConcernedHeaderLink = new MaterialLink();
            collapsibleNotConcernedHeaderLink.addStyleName("collapsibleThemesHeaderLink");
            collapsibleNotConcernedHeaderLink.setText(messages.notConcernedThemes());
            collapsibleNotConcernedThemeColumnLeft.add(collapsibleNotConcernedHeaderLink);

            MaterialChip collapsibleNotConcernedHeaderChip = new MaterialChip();
            collapsibleNotConcernedHeaderChip.addStyleName("collapsibleThemesHeaderChip");
            collapsibleNotConcernedHeaderChip
                    .setText(String.valueOf(extract.getRealEstate().getNotConcernedThemes().size()));
            collapsibleNotConcernedThemeColumnRight.add(collapsibleNotConcernedHeaderChip);

            collapsibleNotConcernedThemeHeaderRow.add(collapsibleNotConcernedThemeColumnLeft);
            collapsibleNotConcernedThemeHeaderRow.add(collapsibleNotConcernedThemeColumnRight);
            collapsibleNotConcernedThemeHeader.add(collapsibleNotConcernedThemeHeaderRow);

            MaterialCollapsibleBody collapsibleBody = new MaterialCollapsibleBody();
            collapsibleBody.addMouseOverHandler(event -> {
                collapsibleBody.getElement().getStyle().setCursor(Cursor.DEFAULT);
            });
            collapsibleBody.setPadding(0);
            MaterialCollection collection = new MaterialCollection();

            for (NotConcernedTheme theme : extract.getRealEstate().getNotConcernedThemes()) {
                MaterialCollectionItem item = new MaterialCollectionItem();
                MaterialLabel label = new MaterialLabel(theme.getName());
                label.addStyleName("notConcernedThemesLabel");
                item.add(label);
                collection.add(item);
            }
            collapsibleBody.add(collection);

            collapsibleNotConcernedThemeItem.add(collapsibleNotConcernedThemeHeader);
            collapsibleNotConcernedThemeItem.add(collapsibleBody);
            plrCollapsibleNotConcernedTheme.add(collapsibleNotConcernedThemeItem);

            plrCollapsibleDiv.add(plrCollapsibleNotConcernedTheme);
        }
        {
            plrCollapsibleThemesWithoutData = new MaterialCollapsible();
            plrCollapsibleThemesWithoutData.addStyleName("plrTopLevelCollapsible");
            plrCollapsibleThemesWithoutData.setShadow(0);

            plrCollapsibleThemesWithoutData.addExpandHandler(event -> {
                plrCollapsibleConcernedTheme.close(1);
                plrCollapsibleNotConcernedTheme.closeAll();
                plrCollapsibleGeneralInformation.closeAll();
            });

            MaterialCollapsibleItem collapsibleThemesWithoutDataItem = new MaterialCollapsibleItem();

            MaterialCollapsibleHeader collapsibleThemesWithoutDataHeader = new MaterialCollapsibleHeader();
            collapsibleThemesWithoutDataHeader.addStyleName("plrCollapsibleThemeHeader");
            collapsibleThemesWithoutDataHeader.setBackgroundColor(Color.GREY_LIGHTEN_3);

            MaterialRow collapsibleThemesWithoutDataHeaderRow = new MaterialRow();
            collapsibleThemesWithoutDataHeaderRow.addStyleName("collapsibleThemeHeaderRow");

            MaterialColumn collapsibleThemesWithoutDataColumnLeft = new MaterialColumn();
            collapsibleThemesWithoutDataColumnLeft.addStyleName("collapsibleThemeColumnLeft");
            collapsibleThemesWithoutDataColumnLeft.setGrid("s10");
            MaterialColumn collapsibleThemesWithoutDataColumnRight = new MaterialColumn();
            collapsibleThemesWithoutDataColumnRight.addStyleName("collapsibleThemeColumnRight");
            collapsibleThemesWithoutDataColumnRight.setGrid("s2");

            MaterialLink collapsibleThemesWithoutHeaderLink = new MaterialLink();
            collapsibleThemesWithoutHeaderLink.addStyleName("collapsibleThemesHeaderLink");
            collapsibleThemesWithoutHeaderLink.setText(messages.themesWithoutData());
            collapsibleThemesWithoutDataColumnLeft.add(collapsibleThemesWithoutHeaderLink);

            MaterialChip collapsibleThemesWithoutHeaderChip = new MaterialChip();
            collapsibleThemesWithoutHeaderChip.addStyleName("collapsibleThemesHeaderChip");
            collapsibleThemesWithoutHeaderChip
                    .setText(String.valueOf(extract.getRealEstate().getThemesWithoutData().size()));
            collapsibleThemesWithoutDataColumnRight.add(collapsibleThemesWithoutHeaderChip);

            collapsibleThemesWithoutDataHeaderRow.add(collapsibleThemesWithoutDataColumnLeft);
            collapsibleThemesWithoutDataHeaderRow.add(collapsibleThemesWithoutDataColumnRight);
            collapsibleThemesWithoutDataHeader.add(collapsibleThemesWithoutDataHeaderRow);

            MaterialCollapsibleBody collapsibleBody = new MaterialCollapsibleBody();
            collapsibleBody.addMouseOverHandler(event -> {
                collapsibleBody.getElement().getStyle().setCursor(Cursor.DEFAULT);
            });
            collapsibleBody.setPadding(0);
            MaterialCollection collection = new MaterialCollection();

            for (ThemeWithoutData theme : extract.getRealEstate().getThemesWithoutData()) {
                MaterialCollectionItem item = new MaterialCollectionItem();
                MaterialLabel label = new MaterialLabel(theme.getName());
                label.addStyleName("withoutDataThemesLabel");
                item.add(label);
                collection.add(item);
            }
            collapsibleBody.add(collection);

            collapsibleThemesWithoutDataItem.add(collapsibleThemesWithoutDataHeader);
            collapsibleThemesWithoutDataItem.add(collapsibleBody);
            plrCollapsibleThemesWithoutData.add(collapsibleThemesWithoutDataItem);

            plrCollapsibleDiv.add(plrCollapsibleThemesWithoutData);
        }
        {
            plrCollapsibleGeneralInformation = new MaterialCollapsible();
            plrCollapsibleGeneralInformation.addStyleName("plrTopLevelCollapsible");
            plrCollapsibleGeneralInformation.setShadow(0);

            plrCollapsibleGeneralInformation.addExpandHandler(event -> {
                plrCollapsibleConcernedTheme.close(1);
                plrCollapsibleNotConcernedTheme.closeAll();
                plrCollapsibleThemesWithoutData.closeAll();
            });

            MaterialCollapsibleItem collapsibleGeneralInformationItem = new MaterialCollapsibleItem();

            MaterialCollapsibleHeader collapsibleGeneralInformationHeader = new MaterialCollapsibleHeader();
            collapsibleGeneralInformationHeader.addStyleName("plrCollapsibleThemeHeader");

            MaterialCollapsibleBody body = new MaterialCollapsibleBody();
            body.addStyleName("collapsibleGeneralInformationBody");
            body.addMouseOverHandler(event -> {
                body.getElement().getStyle().setCursor(Cursor.DEFAULT);
            });

            HTML infoHtml = new HTML();

            StringBuilder html = new StringBuilder();
            html.append("<b>Katasterverantwortliche Stelle</b>");
            html.append("<br>");
            html.append(extract.getPlrCadastreAuthority().getName());

            infoHtml.setHTML(html.toString());
            body.add(infoHtml);

            MaterialRow collapsibleGeneralInformationHeaderRow = new MaterialRow();
            collapsibleGeneralInformationHeaderRow.addStyleName("collapsibleThemeHeaderRow");

            MaterialColumn collapsibleGeneralInformationColumnLeft = new MaterialColumn();
            collapsibleGeneralInformationColumnLeft.addStyleName("collapsibleThemeColumnLeft");
            collapsibleGeneralInformationColumnLeft.setGrid("s10");

            MaterialLink collapsibleThemesWithoutHeaderLink = new MaterialLink();
            collapsibleThemesWithoutHeaderLink.addStyleName("collapsibleThemesHeaderLink");
            collapsibleThemesWithoutHeaderLink.setText(messages.generalInformation());
            collapsibleGeneralInformationColumnLeft.add(collapsibleThemesWithoutHeaderLink);

            collapsibleGeneralInformationHeaderRow.add(collapsibleGeneralInformationColumnLeft);
            collapsibleGeneralInformationHeader.add(collapsibleGeneralInformationHeaderRow);

            collapsibleGeneralInformationItem.add(collapsibleGeneralInformationHeader);
            collapsibleGeneralInformationItem.add(body);
            plrCollapsibleGeneralInformation.add(collapsibleGeneralInformationItem);

            plrCollapsibleDiv.add(plrCollapsibleGeneralInformation);
        }

        plrResultColumn.add(plrCollapsibleDiv);
    }

    private void addCadastralSurveyingContent(
            ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.Extract extract) {
        ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.RealEstateDPR realEstate = extract.getRealEstate();
        String number = realEstate.getNumber();
        String identnd = realEstate.getIdentND();
        String egrid = realEstate.getEgrid();
        int area = realEstate.getLandRegistryArea();
        String type = realEstate.getRealEstateType();
        String municipality = realEstate.getMunicipality();
        String subunitOfLandRegister = realEstate.getSubunitOfLandRegister();
        List<String> localNames = realEstate.getLocalNames().stream().sorted().collect(Collectors.toList());

        addCadastralSurveyingContentKeyValue(new Label("E-GRID:"), new Label(egrid));
        addCadastralSurveyingContentKeyValue(new Label("NBIdent:"), new Label(identnd));
        addCadastralSurveyingContentKeyValue(new HTML("&nbsp;"), new HTML("&nbsp;"));

        addCadastralSurveyingContentKeyValue(new Label("Grundst??cksart:"), new Label(type));
        addCadastralSurveyingContentKeyValue(new Label("Grundst??cksfl??che:"),
                new HTML(fmtDefault.format(area) + " m<sup>2</sup>"));
        addCadastralSurveyingContentKeyValue(new HTML("&nbsp;"), new HTML("&nbsp;"));

        addCadastralSurveyingContentKeyValue(new Label("Gemeinde:"), new Label(municipality));
        addCadastralSurveyingContentKeyValue(new Label("Grundbuch:"), new Label(subunitOfLandRegister));
        addCadastralSurveyingContentKeyValue(new HTML("&nbsp;"), new HTML("&nbsp;"));

        addCadastralSurveyingContentKeyValue(new Label("Flurnamen:"), new Label(String.join(", ", localNames)));

        {
            MaterialColumn fakeColumn = new MaterialColumn();
            fakeColumn.addStyleName("fakeColumn mt15");
            fakeColumn.setGrid("s12");
            cadastralSurveyingResultColumn.add(fakeColumn);
        }

        {
            Div header = new Div();
            header.addStyleName("cadastralSurveyingInfoHeader");
            header.setGrid("s12");
            header.add(new Label("Bodenbedeckung"));
            cadastralSurveyingResultColumn.add(header);

            MaterialColumn typeColumn = new MaterialColumn();
            typeColumn.addStyleName("cadastralSurveyingInfoHeaderSmall");
            typeColumn.setGrid("s6");
            typeColumn.add(new Label(messages.resultType()));

            MaterialColumn shareColumn = new MaterialColumn();
            shareColumn.addStyleName("cadastralSurveyingInfoHeaderSmall textRight");
            shareColumn.setGrid("s4");
            shareColumn.add(new Label(messages.resultShare()));

            MaterialColumn sharePercentColumn = new MaterialColumn();
            sharePercentColumn.addStyleName("cadastralSurveyingInfoHeaderSmall textRight");
            sharePercentColumn.setGrid("s2");
            sharePercentColumn.add(new Label(messages.resultShareInPercent()));

            cadastralSurveyingResultColumn.add(typeColumn);
            cadastralSurveyingResultColumn.add(shareColumn);
            cadastralSurveyingResultColumn.add(sharePercentColumn);

            java.util.Map<String, Integer> landCoverSharesUnsorted = realEstate.getLandCoverShares();
            java.util.Map<String, Integer> landCoverShares = landCoverSharesUnsorted.entrySet().stream()
                    .sorted(java.util.Map.Entry.comparingByKey()).collect(Collectors.toMap(java.util.Map.Entry::getKey,
                            java.util.Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));

            landCoverShares.forEach((k, v) -> {
                MaterialColumn landCoverShareKeyColumn = new MaterialColumn();
                landCoverShareKeyColumn.addStyleName("landCoverShareKeyColumn");
                landCoverShareKeyColumn.setGrid("s6");
                landCoverShareKeyColumn.add(new Label(k));
                cadastralSurveyingResultColumn.add(landCoverShareKeyColumn);

                MaterialColumn landCoverShareValueColumn = new MaterialColumn();
                landCoverShareValueColumn.addStyleName("landCoverShareValueColumn");
                landCoverShareValueColumn.setGrid("s4");
                landCoverShareValueColumn.add(new HTML(fmtDefault.format(v) + " m<sup>2</sup>"));
                cadastralSurveyingResultColumn.add(landCoverShareValueColumn);

                MaterialColumn landCoverPercentValueColumn = new MaterialColumn();
                landCoverPercentValueColumn.addStyleName("landCoverShareValueColumn");
                landCoverPercentValueColumn.setGrid("s2");
                double percent = (new Double(v) / realEstate.getLandRegistryArea()) * 100.0;
                landCoverPercentValueColumn.add(new HTML(fmtPercent.format(percent)));
                cadastralSurveyingResultColumn.add(landCoverPercentValueColumn);
            });
        }

        {
            MaterialColumn fakeColumn = new MaterialColumn();
            fakeColumn.addStyleName("fakeColumn mt15");
            fakeColumn.setGrid("s12");
            cadastralSurveyingResultColumn.add(fakeColumn);
        }

        {
            Div header = new Div();
            header.setGrid("s12");
            header.addStyleName("cadastralSurveyingInfoHeader");
            header.add(new Label("Geb??ude"));
            cadastralSurveyingResultColumn.add(header);

            MaterialColumn addressColumn = new MaterialColumn();
            addressColumn.addStyleName("cadastralSurveyingInfoHeaderSmall");
            addressColumn.setGrid("s7");
            addressColumn.add(new Label("Adresse"));

            MaterialColumn egidColumn = new MaterialColumn();
            egidColumn.addStyleName("cadastralSurveyingInfoHeaderSmall");
            egidColumn.setTextAlign(TextAlign.LEFT);
            egidColumn.setGrid("s4");
            egidColumn.add(new Label("EGID"));

            MaterialColumn edidColumn = new MaterialColumn();
            edidColumn.addStyleName("cadastralSurveyingInfoHeaderSmall");
            edidColumn.setTextAlign(TextAlign.LEFT);
            edidColumn.setGrid("s1");
            edidColumn.add(new Label("EDID"));

            cadastralSurveyingResultColumn.add(addressColumn);
            cadastralSurveyingResultColumn.add(egidColumn);
            cadastralSurveyingResultColumn.add(edidColumn);

            java.util.List<Building> buildings = realEstate.getBuildings();

            buildings.forEach(b -> {
                Address address = b.getPostalAddress();
                String addressHtmlString = address.getStreet() + " " + address.getNumber() + "<br>"
                        + address.getPostalCode() + " " + address.getCity();

                MaterialColumn addressCol = new MaterialColumn();
                addressCol.addStyleName("buildingColumn");
                addressCol.setGrid("s7");
                addressCol.add(new HTML(addressHtmlString));
                cadastralSurveyingResultColumn.add(addressCol);

                MaterialColumn egidCol = new MaterialColumn();
                egidCol.addStyleName("buildingColumn");
                egidCol.setGrid("s4");
                egidCol.add(new Label(String.valueOf(b.getEgid())));
                cadastralSurveyingResultColumn.add(egidCol);

                MaterialColumn edidCol = new MaterialColumn();
                edidCol.addStyleName("buildingColumn");
                edidCol.setGrid("s1");
                edidCol.add(new Label(String.valueOf(b.getEdid())));
                cadastralSurveyingResultColumn.add(edidCol);
            });
        }

        {
            MaterialColumn fakeColumn = new MaterialColumn();
            fakeColumn.addStyleName("fakeColumn");
            fakeColumn.setGrid("s12");
            fakeColumn.setMarginTop(5);
            fakeColumn.setMarginBottom(0);
            cadastralSurveyingResultColumn.add(fakeColumn);
        }

        {
            Div header = new Div();
            header.addStyleName("cadastralSurveyingInfoHeader");
            header.setGrid("s12");
            header.add(new Label("Nachf??hrungsgeometer"));
            cadastralSurveyingResultColumn.add(header);

            ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.Office surveyorOffice = realEstate
                    .getSurveyorOffice();
            Address surveyorAddress = surveyorOffice.getPostalAddress();

            StringBuilder htmlString = new StringBuilder();
            htmlString.append(surveyorOffice.getName() + "<br>");
            htmlString.append(surveyorAddress.getLine1() + "<br>");
            if (surveyorAddress.getLine2() != null) {
                htmlString.append(surveyorAddress.getLine2() + "<br>");
            }
            htmlString.append(surveyorAddress.getStreet() + " " + surveyorAddress.getNumber() + "<br>");
            htmlString.append(surveyorAddress.getPostalCode() + " " + surveyorAddress.getCity() + "<br>");
            htmlString.append(surveyorAddress.getPhone() + "<br>");
            htmlString.append(makeEmailLink(surveyorAddress.getEmail()) + "<br>");
            htmlString.append(makeHtmlLink(surveyorAddress.getWeb()));

            Div value = new Div();
            value.addStyleName("cadastralSurveyingInfoValueColumn");
            value.setGrid("s12");
            value.add(new HTML(htmlString.toString()));
            cadastralSurveyingResultColumn.add(value);
        }

        {
            Div header = new Div();
            header.addStyleName("cadastralSurveyingInfoHeader");
            header.setGrid("s12");
            header.add(new Label("Vermessungsaufsicht"));
            cadastralSurveyingResultColumn.add(header);

            ch.so.agi.oereb.webclient.shared.models.cadastralsurveying.Office authorityOffice = extract
                    .getCadastralSurveyingAuthority();
            Address authorityAddress = authorityOffice.getPostalAddress();

            String htmlString = authorityOffice.getName() + "<br>" + authorityAddress.getStreet() + " "
                    + authorityAddress.getNumber() + "<br>" + authorityAddress.getPostalCode() + " "
                    + authorityAddress.getCity() + "<br>" + authorityAddress.getPhone() + "<br>"
                    + makeEmailLink(authorityAddress.getEmail()) + "<br>" + makeHtmlLink(authorityAddress.getWeb());

            Div value = new Div();
            value.addStyleName("cadastralSurveyingInfoValueColumn");
            value.setGrid("s12");
            value.add(new HTML(htmlString));
            cadastralSurveyingResultColumn.add(value);
        }

    }

    private String makeHtmlLink(String text) {
        String html = "<a class='resultLink' href='" + text + "' target='_blank'>" + text + "</a>";
        return html;
    }

    private String makeEmailLink(String text) {
        String html = "<a class='resultLink' href='mailto:" + text + "'>" + text + "</a>";
        return html;
    }

    private void addCadastralSurveyingContentKeyValue(Label key, Label value) {
        Div row = new Div();
        row.addStyleName("cadastralSurveyingInfoRow");

        Div keyColumn = new Div();
        keyColumn.addStyleName("cadastralSurveyingInfoKeyColumn");
        keyColumn.setGrid("s5");
        keyColumn.add(key);
        cadastralSurveyingResultColumn.add(keyColumn);

        Div valueColumn = new Div();
        valueColumn.addStyleName("cadastralSurveyingInfoValueColumn");
        valueColumn.setGrid("s7");
        valueColumn.add(value);
        cadastralSurveyingResultColumn.add(valueColumn);
    }

    private void removePlrLayers() {
        // I cannot iterate over map.getLayers() and
        // use map.removeLayers(). Seems to get some
        // confusion with the indices or whatever...
        for (String layerId : concernedWmsLayers) {
            Image rlayer = (Image) getLayerById(layerId);
            map.removeLayer(rlayer);
        }

        // Remove vector layer
        Base vlayer = getLayerById(REAL_ESTATE_VECTOR_LAYER_ID);
        map.removeLayer(vlayer);

        // Empty concernedWmsLayers list.
        concernedWmsLayers.clear();
    }

    private ol.layer.Vector createRealEstateVectorLayer(String geometry) {
        Geometry realEstateGeometry = new Wkt().readGeometry(geometry);
        return createRealEstateVectorLayer(realEstateGeometry);
    }
    
    private ol.layer.Vector createRealEstateVectorLayer(Geometry geometry) {
        FeatureOptions featureOptions = OLFactory.createOptions();
        featureOptions.setGeometry(geometry);

        Feature feature = new Feature(featureOptions);
        feature.setId(REAL_ESTATE_VECTOR_FEATURE_ID);

        Style style = new Style();
        Stroke stroke = new Stroke();
        stroke.setWidth(8);
        stroke.setColor(new ol.color.Color(230, 0, 0, 0.6));
        style.setStroke(stroke);
        feature.setStyle(style);

        Collection<Feature> lstFeatures = new Collection<Feature>();
        lstFeatures.push(feature);

        VectorOptions vectorSourceOptions = OLFactory.createOptions();
        vectorSourceOptions.setFeatures(lstFeatures);
        Vector vectorSource = new Vector(vectorSourceOptions);

        VectorLayerOptions vectorLayerOptions = OLFactory.createOptions();
        vectorLayerOptions.setSource(vectorSource);
        ol.layer.Vector vectorLayer = new ol.layer.Vector(vectorLayerOptions);
        vectorLayer.set(ID_ATTR_NAME, REAL_ESTATE_VECTOR_LAYER_ID);

        return vectorLayer;
    }
    
    private void removeRealEstateVectorLayer() {
        Base vlayer = getLayerById(REAL_ESTATE_VECTOR_LAYER_ID);
        map.removeLayer(vlayer);
    }

    private void initMap(String id) {
        Proj4.defs("EPSG:2056",
                "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs");

        ProjectionOptions projectionOptions = OLFactory.createOptions();
        projectionOptions.setCode("EPSG:2056");
        projectionOptions.setUnits("m");
        projectionOptions.setExtent(new Extent(2420000, 1030000, 2900000, 1350000));

        Projection projection = new Projection(projectionOptions);

        WmtsOptions wmtsOptions = OLFactory.createOptions();
        wmtsOptions.setUrl(BACKGROUND_WMTS_URL);
        wmtsOptions.setLayer(BACKGROUND_WMTS_LAYER);
        wmtsOptions.setRequestEncoding("REST");
        wmtsOptions.setFormat("image/png");
        wmtsOptions.setMatrixSet("EPSG:2056");
        wmtsOptions.setStyle("default");
        wmtsOptions.setProjection(projection);
        wmtsOptions.setWrapX(true);
        wmtsOptions.setTileGrid(this.createWmtsTileGrid(projection));

        Wmts wmtsSource = new Wmts(wmtsOptions);

        LayerOptions wmtsLayerOptions = OLFactory.createOptions();
        wmtsLayerOptions.setSource(wmtsSource);

        Tile wmtsLayer = new Tile(wmtsLayerOptions);
        wmtsLayer.setOpacity(1.0);
        wmtsLayer.set(ID_ATTR_NAME, BACKGROUND_LAYER_ID);

        ViewOptions viewOptions = OLFactory.createOptions();
        viewOptions.setProjection(projection);
        viewOptions.setResolutions(new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0,
                2.5, 1.0, 0.5, 0.25, 0.1 });
        View view = new View(viewOptions);
        Coordinate centerCoordinate = new Coordinate(2616491, 1240287);

        view.setCenter(centerCoordinate);
        view.setZoom(6);

        MapOptions mapOptions = OLFactory.createOptions();
        mapOptions.setTarget(id);
        mapOptions.setView(view);
        mapOptions.setControls(new Collection<Control>());

        DefaultInteractionsOptions interactionOptions = new ol.interaction.DefaultInteractionsOptions();
        interactionOptions.setPinchRotate(false);
        mapOptions.setInteractions(Interaction.defaults(interactionOptions));

        map = new Map(mapOptions);

        map.addLayer(wmtsLayer);

        // FIXME
        // Either make a proper overview layer (db and wms) or delete code.
        // map.addLayer(wmsLayer);

        map.addSingleClickListener(new MapSingleClickListener());
    }

    private TileGrid createWmtsTileGrid(Projection projection) {
        WmtsTileGridOptions wmtsTileGridOptions = OLFactory.createOptions();

        double resolutions[] = new double[] { 4000.0, 2000.0, 1000.0, 500.0, 250.0, 100.0, 50.0, 20.0, 10.0, 5.0, 2.5,
                1.0, 0.5, 0.25, 0.1 };
        String[] matrixIds = new String[resolutions.length];

        for (int z = 0; z < resolutions.length; ++z) {
            matrixIds[z] = String.valueOf(z);
        }

        Coordinate tileGridOrigin = projection.getExtent().getTopLeft();
        wmtsTileGridOptions.setOrigin(tileGridOrigin);
        wmtsTileGridOptions.setResolutions(resolutions);
        wmtsTileGridOptions.setMatrixIds(matrixIds);

        return new WmtsTileGrid(wmtsTileGridOptions);
    }

    private Base getLayerById(String id) {
        Collection<Base> layers = map.getLayers();
        for (int i = 0; i < layers.getLength(); i++) {
            Base item = layers.item(i);

            try {
                String layerId = item.get(ID_ATTR_NAME);
                if (item.get(ID_ATTR_NAME).toString().equalsIgnoreCase(id)) {
                    return item;
                }
            } catch (Exception e) {
            }

        }
        return null;
    }

    private void resetGui() {
        removePlrLayers();

        if (resultDiv != null) {
            resultCardContent.remove(resultDiv);
        }

        if (realEstateWindow != null) {
            realEstateWindow.removeFromParent();
        }

        if (resultHeaderRow != null) {
            resultHeaderRow.removeFromParent();
        }

        resultCard.getElement().getStyle().setProperty("visibility", "hidden");
    }

    public class DummyButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            MaterialLoader.loading(true);
            resetGui();
            // CH870679603216 (KbS ??V)
            // CH857632820629 (Kappel)
            // CH807306583219 (Messen)
            sendEgridToServer("CH807306583219");
        }
    }

    public class SearchValueChangeHandler implements ValueChangeHandler {
        @Override
        public void onValueChange(ValueChangeEvent event) {
            MaterialLoader.loading(true);
            resetGui();

            // We only allow one result in the autocomplete widget.
            List<? extends SuggestOracle.Suggestion> values = (List<? extends Suggestion>) event.getValue();
            SearchSuggestion searchSuggestion = (SearchSuggestion) values.get(0);
            SearchResult searchResult = searchSuggestion.getSearchResult();

            String dataproductId = searchResult.getDataproductId();
            String idFieldName = searchResult.getIdFieldName();
            String featureId = searchResult.getFeatureId();

            // Remove the chip from the text field. Even if it is not visible.
            autocomplete.reset();

            String searchServiceUrl = GWT.getHostPageBaseURL() + SEARCH_SERVICE_PATH;
            String requestUrl = searchServiceUrl + dataproductId + "/" + idFieldName + "/" + featureId;
            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, requestUrl);
            try {
                builder.sendRequest("", new RequestCallback() {
                    @Override
                    public void onResponseReceived(com.google.gwt.http.client.Request request,
                            com.google.gwt.http.client.Response response) {
                        int statusCode = response.getStatusCode();
                        if (statusCode == com.google.gwt.http.client.Response.SC_OK) {
                            String responseBody = response.getText();
                            JSONArray responseArray = new JSONArray(JsonUtils.safeEval(responseBody));

                            if (responseArray.size() == 0) {
                                MaterialLoader.loading(false);
                                return;
                            }

                            String egrid = null;
                            for (int i = 0; i < responseArray.size(); i++) {
                                JSONObject obj = responseArray.get(i).isObject();
                                egrid = obj.get("egrid").isString().stringValue();

                                // If there are multiple hits, we prefer the Liegenschaft.
                                String type = obj.get("art").isString().stringValue();
                                if (type.equalsIgnoreCase("Liegenschaft")) {
                                    break;
                                }
                            }
                            sendEgridToServer(egrid);
                            return;
                        } else {
                            MaterialLoader.loading(false);
                            GWT.log("error from request");
                            GWT.log(String.valueOf(statusCode));
                            GWT.log(response.getStatusText());
                        }
                    }

                    @Override
                    public void onError(com.google.gwt.http.client.Request request, Throwable exception) {
                        MaterialLoader.loading(false);
                        GWT.log("error actually sending the request, never got sent");
                    }
                });
            } catch (Exception e) {
                MaterialLoader.loading(true);
                e.printStackTrace();
            }
        }
    }

    public final class MapSingleClickListener implements EventListener<MapBrowserEvent> {
        @Override
        public void onEvent(MapBrowserEvent event) {
            resetGui();

            Coordinate coordinate = event.getCoordinate();
//            String bbox = coordinate.getX() + "," + coordinate.getY() + "," + coordinate.getX() + ","
//                    + coordinate.getY();

//            String searchServiceUrl = GWT.getHostPageBaseURL() + SEARCH_SERVICE_PATH;
//            String requestUrl = searchServiceUrl + REAL_ESTATE_DATAPRODUCT_ID + "/bbox/" + bbox;

            String requestUrl = CADASTRAL_SURVEYING_PARCEL_URL + "?XY=" + coordinate.getX() + "," + coordinate.getY();

            RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, requestUrl);
            try {
                builder.sendRequest("", new RequestCallback() {
                    @Override
                    public void onResponseReceived(com.google.gwt.http.client.Request request,
                            com.google.gwt.http.client.Response response) {
                        int statusCode = response.getStatusCode();
                        if (statusCode == com.google.gwt.http.client.Response.SC_OK) {
                            String responseBody = response.getText();
                            JSONObject responseObject = new JSONObject(JsonUtils.safeEval(responseBody));
                            Feature[] unfilteredFeatures = (new GeoJson()).readFeatures(responseObject.toString());

                            List<Feature> features = Arrays.stream(unfilteredFeatures)
                                .filter(f -> f.getProperties().get("validityType").toString().equalsIgnoreCase("gueltig"))
                                .collect(Collectors.toList());
                            
                            String egrid;
                            if (features.size() > 1) {
                                realEstateWindow = new MaterialWindow();
                                realEstateWindow.setTitle("Grundst??cke");
                                realEstateWindow.setFontSize("16px");
                                realEstateWindow.setMarginLeft(0);
                                realEstateWindow.setMarginRight(0);
                                realEstateWindow.setWidth("300px");
                                realEstateWindow.setToolbarColor(Color.RED_LIGHTEN_1);

                                MaterialIcon maximizeIcon = realEstateWindow.getIconMaximize();
                                maximizeIcon.getElement().getStyle().setProperty("visibility", "hidden");

                                realEstateWindow.setMaximize(false);
                                realEstateWindow.setTop(event.getPixel().getY());
                                realEstateWindow.setLeft(event.getPixel().getX());

                                MaterialPanel realEstatePanel = new MaterialPanel();

                                for (Feature feature : features) {
                                    String number = (String) feature.getProperties().get("number");
                                    egrid = (String) feature.getProperties().get("egrid");
                                    String type = (String) feature.getProperties().get("type");        
                                    
                                    MaterialRow realEstateRow = new MaterialRow();
                                    realEstateRow.setId(egrid);
                                    realEstateRow.setMarginBottom(0);
                                    realEstateRow.setPadding(5);
                                    realEstateRow.add(new Label("GB-Nr.: " + number + " ("+ type.substring(type.lastIndexOf(".") + 1) + ")"));

                                    realEstateRow.addClickHandler(event -> {
                                        realEstateWindow.removeFromParent();
                                        GWT.log("get extract from click for (multiple result): " + realEstateRow.getId());

                                        MaterialLoader.loading(true);
                                        sendEgridToServer(realEstateRow.getId());
                                    });

                                    realEstateRow.addMouseOverHandler(event -> {
                                        realEstateRow.setBackgroundColor(Color.GREY_LIGHTEN_3);
                                        realEstateRow.getElement().getStyle().setCursor(Cursor.POINTER);
                                        ol.layer.Vector vlayer = createRealEstateVectorLayer(feature.getGeometry());
                                        map.addLayer(vlayer);
                                    });

                                    realEstateRow.addMouseOutHandler(event -> {
                                        realEstateRow.setBackgroundColor(Color.WHITE);
                                        realEstateRow.getElement().getStyle().setCursor(Cursor.DEFAULT);
                                        removeRealEstateVectorLayer();
                                    });

                                    realEstatePanel.add(realEstateRow);
                                }
                                realEstateWindow.add(realEstatePanel);
                                realEstateWindow.open();
                            }  else {
                                egrid = (String) features.get(0).getProperties().get("egrid");
                                GWT.log("get extract from click for (single result): " + egrid);

                                MaterialLoader.loading(true);
                                sendEgridToServer(egrid);
                            }
                            return;
                        } else {
                            MaterialLoader.loading(false);
                            GWT.log("error from request");
                            GWT.log(String.valueOf(statusCode));
                            GWT.log(response.getStatusText());
                        }
                    }

                    @Override
                    public void onError(com.google.gwt.http.client.Request request, Throwable exception) {
                        MaterialLoader.loading(false);
                        GWT.log("error actually sending the request, never got sent");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Image createPlrWmsLayer(ReferenceWMS referenceWms) {
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
        wmsLayer.setZIndex(referenceWms.getLayerIndex());

        return wmsLayer;
    }

    private MaterialRow processRestrictionRow(Restriction restriction, GeometryType type) {
        MaterialRow informationRow = new MaterialRow();
        informationRow.setMarginBottom(10);

        MaterialColumn typeColumn = new MaterialColumn();
        typeColumn.setGrid("s6");
        typeColumn.setPadding(0);
        typeColumn.setMarginRight(0);
        typeColumn.setFontSize(BODY_FONT_SIZE);
        Label lbl = new Label(restriction.getInformation());
        typeColumn.add(lbl);

        MaterialColumn symbolColumn = new MaterialColumn();
        symbolColumn.setPadding(0);
        symbolColumn.setMarginRight(0);
        symbolColumn.setGrid("s1");
        symbolColumn.setTextAlign(TextAlign.CENTER);
        symbolColumn.setVerticalAlign(VerticalAlign.MIDDLE);

        Span helper = new Span();
        helper.setDisplay(Display.INLINE_BLOCK);
        helper.setVerticalAlign(VerticalAlign.MIDDLE);
        symbolColumn.add(helper);

        com.google.gwt.user.client.ui.Image symbolImage;
        if (restriction.getSymbol() != null) {
            symbolImage = new com.google.gwt.user.client.ui.Image(restriction.getSymbol());
        } else {
            symbolImage = new com.google.gwt.user.client.ui.Image(
                    UriUtils.fromSafeConstant(restriction.getSymbolRef()));
        }
        symbolImage.setWidth("30px");
        symbolImage.getElement().getStyle().setProperty("border", "1px solid black");
        symbolImage.getElement().getStyle().setProperty("verticalAlign", "middle");
        symbolColumn.add(symbolImage);

        /*
         * symbolColumn.addMouseOverHandler(event -> { GWT.log("mouse over symbol"); });
         * 
         * symbolColumn.addMouseOutHandler(event -> { GWT.log("mouse out symbol"); });
         */

        MaterialColumn shareColumn = new MaterialColumn();
        shareColumn.setTextAlign(TextAlign.RIGHT);
        shareColumn.setPadding(0);
        shareColumn.setGrid("s3");
        shareColumn.setFontSize(BODY_FONT_SIZE);

        if (type == GeometryType.POLYGON) {
            HTML htmlArea;
            if (restriction.getAreaShare() < 0.1) {
                htmlArea = new HTML("< 0.1 m<sup>2</sup>");
            } else {
                htmlArea = new HTML(fmtDefault.format(restriction.getAreaShare()) + " m<sup>2</sup>");
            }
            shareColumn.add(htmlArea);
        }
        if (type == GeometryType.LINE) {
            HTML htmlLength;
            if (restriction.getLengthShare() < 0.1) {
                htmlLength = new HTML("< 0.1 m");
            } else {
                htmlLength = new HTML(fmtDefault.format(restriction.getLengthShare()) + " m");
            }
            shareColumn.add(htmlLength);
        }
        if (type == GeometryType.POINT) {
            HTML htmlPoints = new HTML(fmtDefault.format(restriction.getNrOfPoints()));
            shareColumn.add(htmlPoints);
        }

        MaterialColumn sharePercentColumn = new MaterialColumn();
        sharePercentColumn.setTextAlign(TextAlign.RIGHT);
        sharePercentColumn.setPadding(0);
        sharePercentColumn.setGrid("s2");
        sharePercentColumn.setFontSize(BODY_FONT_SIZE);

        if (type == GeometryType.POLYGON && restriction.getPartInPercent() != null) {
            HTML htmlArea;
            if (restriction.getPartInPercent() < 0.1) {
                htmlArea = new HTML("< 0.1");
            } else {
                htmlArea = new HTML(fmtPercent.format(restriction.getPartInPercent()));
            }
            sharePercentColumn.add(htmlArea);
        } else {
            sharePercentColumn.add(new HTML("&nbsp;"));
        }

        informationRow.add(typeColumn);
        informationRow.add(symbolColumn);
        informationRow.add(shareColumn);
        informationRow.add(sharePercentColumn);

        return informationRow;
    }

    private static native void updateURLWithoutReloading(String newUrl) /*-{
                                                                        $wnd.history.pushState(newUrl, "", newUrl);
                                                                        }-*/;
}