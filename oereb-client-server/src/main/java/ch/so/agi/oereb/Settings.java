package ch.so.agi.oereb;

import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class Settings {
    private String myVar;
    
    private String searchServiceUrl;
    
    private String cantonServiceUrl;

    private HashMap<String,String> oerebServiceUrls;
    
    private List<String> notSupportedCantons;

    public String getMyVar() {
        return myVar;
    }

    public void setMyVar(String myVar) {
        this.myVar = myVar;
    }

    public String getSearchServiceUrl() {
        return searchServiceUrl;
    }

    public void setSearchServiceUrl(String searchServiceUrl) {
        this.searchServiceUrl = searchServiceUrl;
    }

    public String getCantonServiceUrl() {
        return cantonServiceUrl;
    }

    public void setCantonServiceUrl(String cantonServiceUrl) {
        this.cantonServiceUrl = cantonServiceUrl;
    }

    public HashMap<String, String> getOerebServiceUrls() {
        return oerebServiceUrls;
    }

    public void setOerebServiceUrls(HashMap<String, String> oerebServiceUrls) {
        this.oerebServiceUrls = oerebServiceUrls;
    }

    public List<String> getNotSupportedCantons() {
        return notSupportedCantons;
    }

    public void setNotSupportedCantons(List<String> notSupportedCantons) {
        this.notSupportedCantons = notSupportedCantons;
    }
}
