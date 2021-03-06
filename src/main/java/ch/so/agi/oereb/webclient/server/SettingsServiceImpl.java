package ch.so.agi.oereb.webclient.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ch.so.agi.oereb.webclient.shared.SettingsResponse;
import ch.so.agi.oereb.webclient.shared.SettingsService;

public class SettingsServiceImpl extends RemoteServiceServlet implements SettingsService {

    @Value("${app.oerebWebServiceUrlClient}")
    private String oerebWebServiceUrl;

    @Value("${cadastral.parcelServiceUrl}")
    private String parcelServiceUrl;

    @Value("${app.searchServicePath}")
    private String searchServicePath;
    
    @Value("${app.realEstateDataproductId}")
    private String realEstateDataproductId;

    @Value("${app.addressDataproductId}")
    private String addressDataproductId;

    @Value("${app.dataServiceUrl}")
    private String dataServiceUrl;

    @Value("${app.backgroundWmtsUrl}")
    private String backgroundWmtsUrl;

    @Value("${app.backgroundWmtsLayer}")
    private String backgroundWmtsLayer;

    @Value("#{${app.wmsHostMapping}}")
    private HashMap<String, String> wmsHostMapping;

    @Override
    public void init() throws ServletException {
         super.init();
         SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }
    
    @Override
    public SettingsResponse settingsServer() throws IllegalArgumentException, IOException {
        HashMap<String,Object> settings = new HashMap<String,Object>();
        
        settings.put("OEREB_SERVICE_URL", oerebWebServiceUrl);
        settings.put("CADASTRAL_SURVEYING_PARCEL_URL", parcelServiceUrl);
        settings.put("SEARCH_SERVICE_PATH", searchServicePath);
        settings.put("REAL_ESTATE_DATAPRODUCT_ID", realEstateDataproductId);
        settings.put("ADDRESS_DATAPRODUCT_ID", addressDataproductId);
        settings.put("DATA_SERVICE_URL", dataServiceUrl);
        settings.put("WMS_HOST_MAPPING", wmsHostMapping);
        settings.put("BACKGROUND_WMTS_URL", backgroundWmtsUrl);
        settings.put("BACKGROUND_WMTS_LAYER", backgroundWmtsLayer);

        SettingsResponse response = new SettingsResponse();
        response.setSettings(settings);
        
        return response;
    }
}
