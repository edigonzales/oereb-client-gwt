package ch.so.agi.oereb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app")
public class Settings {
    private String myVar;
    
    private String searchServiceUrl;

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
}
