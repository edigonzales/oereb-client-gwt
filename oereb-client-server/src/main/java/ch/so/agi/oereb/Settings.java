package ch.so.agi.oereb;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class Settings {
    private String myVar;
    
    private String searchServiceUrl;
    
    private HashMap<String,String> oerebServiceUrls;

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

    public HashMap<String, String> getOerebServiceUrls() {
        return oerebServiceUrls;
    }

    public void setOerebServiceUrls(HashMap<String, String> oerebServiceUrls) {
        this.oerebServiceUrls = oerebServiceUrls;
    }
}
