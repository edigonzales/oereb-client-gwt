package ch.so.agi.oereb.webclient.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import ch.ehi.oereb.schemas.oereb._1_0.extract.GetExtractByIdResponse;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.DocumentBaseType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.DocumentType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.ExtractType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LanguageCodeType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedMTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.LocalisedUriType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualMTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualTextType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.MultilingualUriType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.RealEstateDPRType;
import ch.ehi.oereb.schemas.oereb._1_0.extractdata.RestrictionOnLandownershipType;
import ch.so.agi.oereb.webclient.shared.ExtractServiceException;
import ch.so.agi.oereb.webclient.shared.models.plr.AbstractTheme;
import ch.so.agi.oereb.webclient.shared.models.plr.ConcernedTheme;
import ch.so.agi.oereb.webclient.shared.models.plr.Document;
import ch.so.agi.oereb.webclient.shared.models.plr.Extract;
import ch.so.agi.oereb.webclient.shared.models.plr.NotConcernedTheme;
import ch.so.agi.oereb.webclient.shared.models.plr.Office;
import ch.so.agi.oereb.webclient.shared.models.plr.RealEstateDPR;
import ch.so.agi.oereb.webclient.shared.models.plr.ReferenceWMS;
import ch.so.agi.oereb.webclient.shared.models.plr.Restriction;
import ch.so.agi.oereb.webclient.shared.models.plr.ThemeWithoutData;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PlrService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${app.oerebWebServiceUrlServer}")
    private String oerebWebServiceUrlServer;

    @Value("${app.oerebWebServiceUrlClient}")
    private String oerebWebServiceUrlClient;

    @Value("#{${app.wmsHostMapping}}")
    HashMap<String, String> wmsHostMapping;

    @Autowired
    Jaxb2Marshaller marshaller;

    private static final LanguageCodeType DE = LanguageCodeType.DE;

    private List<String> themesOrderingList = Stream.of(
            // "LandUsePlans",
            "ch.SO.NutzungsplanungGrundnutzung", "ch.SO.NutzungsplanungUeberlagernd",
            "ch.SO.NutzungsplanungSondernutzungsplaene", "ch.SO.Baulinien", "MotorwaysProjectPlaningZones",
            "MotorwaysBuildingLines", "RailwaysProjectPlanningZones", "RailwaysBuildingLines",
            "AirportsProjectPlanningZones", "AirportsBuildingLines", "AirportsSecurityZonePlans", "ContaminatedSites",
            "ContaminatedMilitarySites", "ContaminatedCivilAviationSites", "ContaminatedPublicTransportSites",
            "GroundwaterProtectionZones", "GroundwaterProtectionSites", "NoiseSensitivityLevels", "ForestPerimeters",
            "ForestDistanceLines", "ch.SO.Einzelschutz")
            .collect(Collectors.toList());

    Map<String, String> realEstateTypesMap = Stream.of(new String[][] {
                { "Distinct_and_permanent_rights.BuildingRight", "Baurecht" }, 
                { "RealEstate", "Liegenschaft" }, 
            })
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

    public ch.so.agi.oereb.webclient.shared.models.plr.Extract getExtract(String egrid) throws ExtractServiceException {
        File xmlFile;
        try {
            xmlFile = Files.createTempFile("plr_extract_", ".xml").toFile();
            URL url = new URL(oerebWebServiceUrlServer + "extract/reduced/xml/geometry/" + egrid);
            logger.info(url.toString());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");

            if (connection.getResponseCode() == 500) {
                throw new ExtractServiceException("500");
            } else if (connection.getResponseCode() == 406) {
                throw new ExtractServiceException("406");
            } else if (connection.getResponseCode() == 204) {
                throw new ExtractServiceException("204");
            }
            
            InputStream initialStream = connection.getInputStream();
            java.nio.file.Files.copy(initialStream, xmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);  
            logger.info("File downloaded: " + xmlFile.getAbsolutePath());
        } catch (Exception e) {
            throw new ExtractServiceException(e.getMessage());
        }
        
        StreamSource xmlSource = new StreamSource(xmlFile);
        GetExtractByIdResponse obj = (GetExtractByIdResponse) marshaller.unmarshal(xmlSource);
        ExtractType xmlExtract = obj.getValue().getExtract().getValue();

        Extract extract = new Extract();
        extract.setExtractIdentifier(xmlExtract.getExtractIdentifier());

        ArrayList<ThemeWithoutData> themesWithoutData = xmlExtract.getThemeWithoutData()
                .stream()
                .map(theme -> {
                    ThemeWithoutData themeWithoutData = new ThemeWithoutData();
                    themeWithoutData.setCode(theme.getCode());
                    themeWithoutData.setName(theme.getText().getText());
                    return themeWithoutData;
                })
                .collect(collectingAndThen(toList(), ArrayList<ThemeWithoutData>::new));
        themesWithoutData.sort(compare);

        ArrayList<NotConcernedTheme> notConcernedThemes = xmlExtract.getNotConcernedTheme()
                .stream()
                .map(theme -> {
                    NotConcernedTheme notConcernedTheme = new NotConcernedTheme();
                    notConcernedTheme.setCode(theme.getCode());
                    notConcernedTheme.setName(theme.getText().getText());
                    return notConcernedTheme;
                })
                .collect(collectingAndThen(toList(), ArrayList<NotConcernedTheme>::new));
        notConcernedThemes.sort(compare);

        RealEstateDPRType xmlRealEstate = xmlExtract.getRealEstate();

        RealEstateDPR realEstate = new RealEstateDPR();
        realEstate.setEgrid(xmlRealEstate.getEGRID());
        realEstate.setFosnNr(xmlRealEstate.getFosNr());
        realEstate.setMunicipality(xmlRealEstate.getMunicipality());
        realEstate.setCanton(xmlRealEstate.getCanton().value());
        realEstate.setNumber(xmlRealEstate.getNumber());
        realEstate.setSubunitOfLandRegister(xmlRealEstate.getSubunitOfLandRegister());
        realEstate.setLandRegistryArea(xmlRealEstate.getLandRegistryArea());
        realEstate.setLimit(new Gml32ToJts().convertMultiSurface(xmlRealEstate.getLimit()).toText());
        realEstate.setThemesWithoutData(themesWithoutData);
        realEstate.setNotConcernedThemes(notConcernedThemes);        
        realEstate.setRealEstateType(realEstateTypesMap.get(xmlRealEstate.getType().value()));
        
        // Create a map with all restrictions grouped by theme text.
        Map<String, List<RestrictionOnLandownershipType>> groupedXmlRestrictions = xmlRealEstate.getRestrictionOnLandownership()
                .stream()
                .collect(Collectors.groupingBy(r -> r.getTheme().getText().getText()));

        logger.debug("*********************************************");
        logger.debug("*********************************************");

        // We create one ConcernedTheme object per theme with all restrictions belonging
        // to the same theme since this is the way we present the restriction in the GUI.
        ArrayList<ConcernedTheme> concernedThemesList = new ArrayList<ConcernedTheme>();
        for (Map.Entry<String, List<RestrictionOnLandownershipType>> entry : groupedXmlRestrictions.entrySet()) {
            logger.debug("*********************************************");
            logger.debug("ConcernedTheme: " + entry.getKey());
            logger.debug("---------------------------------------------");

            List<RestrictionOnLandownershipType> xmlRestrictions = entry.getValue();

            // Create a map with one (and only one) simplified restriction for each type
            // code.
            // We cannot use groupingBy because this will return a list of
            // restriction per type code.
            // Afterwards will add more information to the restriction.
            // FIXME: Auch hier besteht das Problem, dass 'nur' ??ber den
            // TypeCode gruppiert wird. Das reicht nicht immer.
            Map<String, Restriction> restrictionsMap = xmlRestrictions
                    .stream()
                    .filter(distinctByKey(RestrictionOnLandownershipType::getTypeCode))
                    .map(r -> {
                        Restriction restriction = new Restriction();
                        restriction.setInformation(getLocalisedText(r.getInformation(), DE));
                        restriction.setTypeCode(r.getTypeCode());
                        if (r.getSymbol() != null) {
                            String encodedImage = Base64.encode(r.getSymbol());
                            encodedImage = "data:image/png;base64," + encodedImage;
                            restriction.setSymbol(encodedImage);
                        } else if (r.getSymbolRef() != null) {
                            String symbolRef = r.getSymbolRef().replace(oerebWebServiceUrlServer,
                                    oerebWebServiceUrlClient);
                            restriction.setSymbolRef(symbolRef);
                        }
                        return restriction;
                    })
                    .collect(Collectors.toMap(Restriction::getTypeCode, Function.identity()));

            logger.debug(restrictionsMap.toString());

            // Calculate sum of the shares for each type code.
            Map<String, Integer> sumAreaShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getAreaShare() != null)
                    .collect(Collectors.groupingBy(r -> r.getTypeCode(), Collectors.summingInt(r -> r.getAreaShare())));

            Map<String, Integer> sumLengthShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getLengthShare() != null)
                    .collect(Collectors.groupingBy(r -> r.getTypeCode(), Collectors.summingInt(r -> r.getLengthShare())));

            Map<String, Integer> sumNrOfPoints = xmlRestrictions
                    .stream()
                    .filter(r -> r.getNrOfPoints() != null)
                    .collect( Collectors.groupingBy(r -> r.getTypeCode(), Collectors.summingInt(r -> r.getNrOfPoints())));

            Map<String, Double> sumAreaPercentShare = xmlRestrictions
                    .stream()
                    .filter(r -> r.getPartInPercent() != null)
                    .collect(Collectors.groupingBy(r -> r.getTypeCode(), Collectors.summingDouble(r -> r.getPartInPercent().doubleValue())));

            /*
             * Map<String, List<List<GeometryType>>> geometryGroupedLists =
             * xmlRestrictions.stream() .filter(r -> r.getGeometry() != null)
             * .collect(Collectors.groupingBy(r -> r.getTypeCode(),
             * Collectors.mapping(RestrictionOnLandownershipType::getGeometry,
             * Collectors.toList())));
             */

            logger.debug("sumAreaShare: " + sumAreaShare.toString());
            logger.debug("sumLengthShare: " + sumLengthShare.toString());
            logger.debug("sumNrOfPoints: " + sumNrOfPoints.toString());
            logger.debug("sumAreaPercentShare: " + sumAreaPercentShare.toString());
            // logger.info("geometryLists: " + geometryGroupedLists.toString());

            // Assign the sum to the simplified restriction.
            // And add the restriction to the final restrictons list.
            List<Restriction> restrictionsList = new ArrayList<Restriction>();
            for (Map.Entry<String, Restriction> restrictionEntry : restrictionsMap.entrySet()) {
                String typeCode = restrictionEntry.getKey();
                if (sumAreaShare.get(typeCode) != null) {
                    restrictionEntry.getValue().setAreaShare(sumAreaShare.get(typeCode));
                    logger.debug(String.valueOf(restrictionEntry.getValue().getAreaShare()));
                }
                if (sumLengthShare.get(typeCode) != null) {
                    restrictionEntry.getValue().setLengthShare(sumLengthShare.get(typeCode));
                    logger.debug(String.valueOf(restrictionEntry.getValue().getLengthShare()));
                }
                if (sumNrOfPoints.get(typeCode) != null) {
                    restrictionEntry.getValue().setNrOfPoints(sumNrOfPoints.get(typeCode));
                    logger.debug(String.valueOf(restrictionEntry.getValue().getNrOfPoints()));
                }
                if (sumAreaPercentShare.get(typeCode) != null) {
                    restrictionEntry.getValue().setPartInPercent(sumAreaPercentShare.get(typeCode));
                    logger.debug(String.valueOf(restrictionEntry.getValue().getPartInPercent()));
                }
                restrictionsList.add(restrictionEntry.getValue());
            }

            /*
             * ArrayList<Polygon> polygonList = new ArrayList<Polygon>(); for (Entry<String,
             * List<List<GeometryType>>> geometryListsEntry :
             * geometryGroupedLists.entrySet()) { String typeCode =
             * geometryListsEntry.getKey(); List<List<GeometryType>> geometryLists =
             * geometryListsEntry.getValue(); for(List<GeometryType> geometryList :
             * geometryLists) {
             * 
             * 
             * Iterator<GeometryType> it = geometryList.iterator(); while (it.hasNext()) {
             * SurfacePropertyTypeType surface = it.next().getSurface(); if (surface !=
             * null) {
             * 
             * Polygon restrictionPolygon = new Gml32ToJts().convertSurface(surface);
             * polygonList.add(restrictionPolygon); } } }
             * 
             * 
             * PrecisionModel precisionModel = new PrecisionModel(1000); GeometryFactory
             * factory = new GeometryFactory(precisionModel);
             * 
             * if (!polygonList.isEmpty()) { MultiPolygon restrictionMultiPolygon =
             * factory.createMultiPolygon(polygonList.toArray(new Polygon[0]));
             * restrictionsMap.get(typeCode).setMultiPointGeometry(restrictionMultiPolygon.
             * toText()); } }
             */

            // Collect responsible offices
            // Distinct by office url.
            ArrayList<Office> officeList = (ArrayList<Office>) xmlRestrictions.stream()
                    .filter(distinctByKey(r -> {
                        String officeName = r.getResponsibleOffice().getOfficeAtWeb().getValue();
                        return officeName;
                    }))
                    .map(r -> {
                        Office office = new Office();
                        if (r.getResponsibleOffice().getName() != null) {
                            office.setName(getLocalisedText(r.getResponsibleOffice().getName(), DE));
                        }
                        office.setOfficeAtWeb(r.getResponsibleOffice().getOfficeAtWeb().getValue());
                        return office;
                    })
                    .collect(Collectors.toList());

            logger.debug("size of office: " + officeList.size());

            // Get legal provisions and laws.
            List<Document> legalProvisionsList = new ArrayList<Document>();
            List<Document> lawsList = new ArrayList<Document>();

            for (RestrictionOnLandownershipType xmlRestriction : xmlRestrictions) {
                List<DocumentBaseType> xmlLegalProvisions = xmlRestriction.getLegalProvisions();
                for (DocumentBaseType xmlDocumentBase : xmlLegalProvisions) {
                    DocumentType xmlLegalProvision = (DocumentType) xmlDocumentBase;
                    Document legalProvision = new Document();
                    if (xmlLegalProvision.getTitle() != null) {
                        legalProvision.setTitle(getLocalisedText(xmlLegalProvision.getTitle(), DE));
                    }
                    if (xmlLegalProvision.getOfficialTitle() != null) {
                        legalProvision.setOfficialTitle(getLocalisedText(xmlLegalProvision.getOfficialTitle(), DE));
                    }
                    legalProvision.setOfficialNumber(xmlLegalProvision.getOfficialNumber());
                    if (xmlLegalProvision.getAbbreviation() != null) {
                        legalProvision.setAbbreviation(getLocalisedText(xmlLegalProvision.getAbbreviation(), DE));
                    }
                    if (xmlLegalProvision.getTextAtWeb() != null) {
                        legalProvision.setTextAtWeb(getLocalisedText(xmlLegalProvision.getTextAtWeb(), DE));
                    }
                    legalProvisionsList.add(legalProvision);

                    List<DocumentType> xmlLaws = xmlLegalProvision.getReference();
                    for (DocumentType xmlLaw : xmlLaws) {
                        Document law = new Document();
                        if (xmlLaw.getTitle() != null) {
                            law.setTitle(getLocalisedText(xmlLaw.getTitle(), DE));
                        }
                        if (xmlLaw.getOfficialTitle() != null) {
                            law.setOfficialTitle(getLocalisedText(xmlLaw.getOfficialTitle(), DE));
                        }
                        law.setOfficialNumber(xmlLaw.getOfficialNumber());
                        if (xmlLaw.getAbbreviation() != null) {
                            law.setAbbreviation(getLocalisedText(xmlLaw.getAbbreviation(), DE));
                        }
                        if (xmlLaw.getTextAtWeb() != null) {
                            law.setTextAtWeb(getLocalisedText(xmlLaw.getTextAtWeb(), DE));
                        }
                        lawsList.add(law);
                    }
                }
            }

            // Because restrictions can share the same legal provision and laws,
            // we need to distinct them.
            List<Document> distinctLegalProvisionsList = legalProvisionsList.stream()
                    .filter(distinctByKey(Document::getTextAtWeb)).collect(Collectors.toList());

//            distinctLegalProvisionsList.stream().forEach(d -> {
//                logger.info(d.getAbbreviation());
//            }); 
//            List<Document> sortedLegalProvisionsList = new ArrayList<Document>();
//            if (distinctLegalProvisionsList.size() > 1) {
//                sortedLegalProvisionsList = distinctLegalProvisionsList.stream().sorted((d1, d2) -> d1.getAbbreviation().compareTo(d2.getAbbreviation())).collect(Collectors.toList());  
//            }

            List<Document> distinctLawsList = lawsList.stream().filter(distinctByKey(Document::getTextAtWeb))
                    .collect(Collectors.toList());

            // WMS
            double layerOpacity = xmlRestrictions.get(0).getMap().getLayerOpacity();
            int layerIndex = xmlRestrictions.get(0).getMap().getLayerIndex();
            String wmsUrl = xmlRestrictions.get(0).getMap().getReferenceWMS();

            // Replace wms host (or other parts of the url).
            for (Entry<String, String> hostEntry : wmsHostMapping.entrySet()) {
                if (wmsUrl.contains(hostEntry.getKey())) {
                    wmsUrl = wmsUrl.replace(hostEntry.getKey(), hostEntry.getValue());
                }
            }

            UriComponents uriComponents = UriComponentsBuilder.fromUriString(wmsUrl).build();
            String schema = uriComponents.getScheme();
            String host = uriComponents.getHost();
            String path = uriComponents.getPath();
            String layers = uriComponents.getQueryParams().getFirst("LAYERS"); // FIXME case insensitivity
            String imageFormat = uriComponents.getQueryParams().getFirst("FORMAT"); // FIXME case insensitivity

            StringBuilder baseUrlBuilder = new StringBuilder();
            baseUrlBuilder.append(schema).append("://").append(host);
            if (uriComponents.getPort() != -1) {
                baseUrlBuilder.append(":" + String.valueOf(uriComponents.getPort()));
            }
            baseUrlBuilder.append(path);
            String baseUrl = baseUrlBuilder.toString();

            ReferenceWMS referenceWMS = new ReferenceWMS();
            referenceWMS.setBaseUrl(baseUrl);
            referenceWMS.setLayers(layers);
            referenceWMS.setImageFormat(imageFormat);
            referenceWMS.setLayerOpacity(layerOpacity);
            referenceWMS.setLayerIndex(layerIndex);

            // Bundesthemen haben Stand heute keine LegendeImWeb
            String legendAtWeb = null;
            if (xmlRestrictions.get(0).getMap().getLegendAtWeb() != null) {
                legendAtWeb = xmlRestrictions.get(0).getMap().getLegendAtWeb().getValue();

                for (Entry<String, String> hostEntry : wmsHostMapping.entrySet()) {
                    if (legendAtWeb.contains(hostEntry.getKey())) {
                        legendAtWeb = legendAtWeb.replace(hostEntry.getKey(), hostEntry.getValue());
                    }
                }
            }

            // Finally we create the concerned theme with all information.
            ConcernedTheme concernedTheme = new ConcernedTheme();
            concernedTheme.setRestrictions(restrictionsList);
            concernedTheme.setLegalProvisions(distinctLegalProvisionsList);
            concernedTheme.setLaws(distinctLawsList);
            concernedTheme.setReferenceWMS(referenceWMS);
            concernedTheme.setLegendAtWeb(legendAtWeb);
            concernedTheme.setCode(xmlRestrictions.get(0).getTheme().getCode());
            concernedTheme.setName(xmlRestrictions.get(0).getTheme().getText().getText());
            concernedTheme.setSubtheme(xmlRestrictions.get(0).getSubTheme());
            concernedTheme.setResponsibleOffice(officeList);

            concernedThemesList.add(concernedTheme);
        }
        concernedThemesList.sort(compare);

        realEstate.setConcernedThemes(concernedThemesList);
        extract.setRealEstate(realEstate);

        // TODO: not used?! does it make any sense?
        extract.setPdfLink(oerebWebServiceUrlClient + "extract/reduced/pdf/geometry/" + egrid);

        Office plrCadastreAuthority = new Office();
        plrCadastreAuthority.setName(getLocalisedText(xmlExtract.getPLRCadastreAuthority().getName(), DE));
        plrCadastreAuthority.setOfficeAtWeb(xmlExtract.getPLRCadastreAuthority().getOfficeAtWeb().getValue());
        plrCadastreAuthority.setStreet(xmlExtract.getPLRCadastreAuthority().getStreet());
        plrCadastreAuthority.setNumber(xmlExtract.getPLRCadastreAuthority().getNumber());
        plrCadastreAuthority.setPostalCode(xmlExtract.getPLRCadastreAuthority().getPostalCode());
        plrCadastreAuthority.setCity(xmlExtract.getPLRCadastreAuthority().getCity());
        extract.setPlrCadastreAuthority(plrCadastreAuthority);
               
        return extract;    
    }

    Comparator<AbstractTheme> compare = new Comparator<AbstractTheme>() {
        public int compare(AbstractTheme t1, AbstractTheme t2) {
            if (t1.getSubtheme() != null && t2.getSubtheme() == null) {
                return themesOrderingList.indexOf(t1.getSubtheme()) - themesOrderingList.indexOf(t2.getCode());
            }

            if (t2.getSubtheme() != null && t1.getSubtheme() == null) {
                return themesOrderingList.indexOf(t1.getCode()) - themesOrderingList.indexOf(t2.getSubtheme());
            }

            if (t1.getSubtheme() != null && t2.getSubtheme() != null) {
                return themesOrderingList.indexOf(t1.getSubtheme()) - themesOrderingList.indexOf(t2.getSubtheme());
            }
            return themesOrderingList.indexOf(t1.getCode()) - themesOrderingList.indexOf(t2.getCode());
        }
    };

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
    
    private String getLocalisedText(MultilingualTextType multilingualTextType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedTextType> it = multilingualTextType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedTextType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }
        return null;
    }
    
    private String getLocalisedText(MultilingualMTextType multilingualMTextType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedMTextType> it = multilingualMTextType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedMTextType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }
        return null;
    }    
    
    private String getLocalisedText(MultilingualUriType multilingualUriType, LanguageCodeType languageCodeType) {
        Iterator<LocalisedUriType> it = multilingualUriType.getLocalisedText().iterator();
        while(it.hasNext()) {
            LocalisedUriType textType = it.next();
            if (textType.getLanguage().compareTo(languageCodeType) == 0) {
                return textType.getText();
            }
        }
        return null;
    }
}
