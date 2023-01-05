package ch.so.agi.oereb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String PARAM_CONST_TRUE = "TRUE";
    private static final String PARAM_EN = "EN";
    private static final String PARAM_EGRID = "EGRID";
    private static final String PARAM_GEOMETRY = "GEOMETRY";
    private static final String PARAM_CANTON = "CANTON";

    @Autowired
    ObjectMapper mapper;
   
    @Autowired
    Settings settings;
    
    @Autowired
    HttpClient httpClient;

    @PostConstruct
    public void init() throws Exception {
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return new ResponseEntity<String>("oereb-client", HttpStatus.OK);
    }
    
    @RequestMapping(value = "/settings", method = RequestMethod.GET, produces = { "application/json" })
    public Settings settings() {
        return settings;
    }
    
    @RequestMapping(value = "/proxy/{request}/xml/", method = RequestMethod.GET, produces = { "application/xml" })
    public ResponseEntity<String> proxy(@PathVariable String request, @RequestParam Map<String, String> queryParameters)  {
        String geometryParam = queryParameters.get(PARAM_GEOMETRY);
        String withGeometry = geometryParam!=null?Boolean.toString(PARAM_CONST_TRUE.equalsIgnoreCase(geometryParam)):"false";
        String coord = queryParameters.get(PARAM_EN);
        String egrid = queryParameters.get(PARAM_EGRID);
        String canton = queryParameters.get(PARAM_CANTON);
                
        String baseUrl = settings.getOerebServiceUrls().get(canton.toUpperCase());
        
        String requestUrl = baseUrl;
        if (request.equalsIgnoreCase("getegrid")) {
             requestUrl += request + "/xml/?GEOMETRY=" + withGeometry + "&EN=" + coord;  
        } else if (request.equalsIgnoreCase("extract")) {
            requestUrl += request + "/xml/?GEOMETRY=" + withGeometry + "&EGRID=" + egrid;  
        }
                
        HttpResponse<String> response = null;
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder().GET().uri(new URI(requestUrl))
                    .timeout(Duration.ofSeconds(120L)).build();
            
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            e.printStackTrace();
            return new ResponseEntity<String>(new String(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        int statusCode = response.statusCode();
        if (statusCode == 200) {
            return new ResponseEntity<String>(response.body(), HttpStatus.OK);            
        } else if (statusCode == 204) {
            return new ResponseEntity<String>(response.body(), HttpStatus.NO_CONTENT);            
        }
        
        return new ResponseEntity<String>(new String(), HttpStatus.BAD_REQUEST);
    }
    
//    @RequestMapping(value = "/getegrid", method = RequestMethod.GET, produces = { "application/json" })
//    public void getEgrid(@RequestParam(value = "EN", required = true) String coord) {
//        log.info("EN: <{}>", coord);
//
//        String cantonServiceUrl = settings.getCantonServiceUrl();
//        String requestUrl = cantonServiceUrl + coord;
//        log.info(requestUrl);
//        
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
//        log.info(response.getBody());
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            try {
//                JsonNode root = mapper.readTree(response.getBody());
//                log.info("***"+root.get("results").get(0).get("properties").get("ak").asText());
//                String canton = root.get("results").get("properties").get("ak").asText();
//                log.info(canton);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//                // return...
//            }
//        }
    
}
