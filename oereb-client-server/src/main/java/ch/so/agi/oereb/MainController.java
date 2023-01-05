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

//    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
//
//    private static final HttpClient httpClient = HttpClient.newBuilder()
//            .executor(executorService)
//            .version(HttpClient.Version.HTTP_2)
//            .connectTimeout(Duration.ofSeconds(10))
//            .build();

//    private static final class GetResult {
//        String URL;
//        int STATUS_CODE;
//        Long TIMING;
//    
//        @Override public String toString(){
//            return "Result:" + STATUS_CODE + " " + TIMING + " msecs " + URL;
//          }
//    }    
//    
//    private final class Task implements Callable<GetResult> {
//        Task(String url) {
//            this.url = url;
//        }
//
//        /** Access a URL, and see if you get a healthy response. */
//        @Override
//        public GetResult call() throws Exception {
//            return pingAndReportStatus(url);
//        }
//
//        private final String url;
//    }
     
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
    public String proxy(@PathVariable String request, @RequestParam Map<String, String> queryParameters) {
        String geometryParam = queryParameters.get(PARAM_GEOMETRY);
        String withGeometry = geometryParam!=null?Boolean.toString(PARAM_CONST_TRUE.equalsIgnoreCase(geometryParam)):"false";
        String coord = queryParameters.get(PARAM_EN);
        String egrid = queryParameters.get(PARAM_EGRID);
        String canton = queryParameters.get(PARAM_CANTON);
                
        String baseUrl = settings.getOerebServiceUrls().get(canton.toUpperCase());
        
        String requestUrl = baseUrl;
        if (request.equalsIgnoreCase("getegrid")) {
             requestUrl += request + "/xml/?WITHGEOMETRY=" + withGeometry + "&EN=" + coord;  
        } else if (request.equalsIgnoreCase("extract")) {
            requestUrl += request + "/xml/?WITHGEOMETRY=" + withGeometry + "&EGRID=" + egrid;  
        }
        
        System.out.println(requestUrl);
        
        
        return requestUrl;
    }


//    private GetResult pingAndReportStatus(String URL) throws MalformedURLException {
//        GetResult result = new GetResult();
//        result.URL = URL;
//        long start = System.currentTimeMillis();
//        URL url = new URL(URL);
//        try {
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.connect();
//            result.STATUS_CODE = connection.getResponseCode();
//            long end = System.currentTimeMillis();
//            result.TIMING = end - start;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    
    @RequestMapping(value = "/getegrid", method = RequestMethod.GET, produces = { "application/json" })
    public void getEgrid(@RequestParam(value = "EN", required = true) String coord) {
        log.info("EN: <{}>", coord);

        String cantonServiceUrl = settings.getCantonServiceUrl();
        String requestUrl = cantonServiceUrl + coord;
        log.info(requestUrl);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
        log.info(response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                JsonNode root = mapper.readTree(response.getBody());
                log.info("***"+root.get("results").get(0).get("properties").get("ak").asText());
                String canton = root.get("results").get("properties").get("ak").asText();
                log.info(canton);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                // return...
            }
        }
        
        
//        ExecutorService executor = Executors.newFixedThreadPool(10);
//        CompletionService<GetResult> compService = new ExecutorCompletionService<>(executor);
//
//        for (Map.Entry<String, String> entry : settings.getOerebServiceUrls().entrySet()) {
//            String requestUrl = entry.getValue() + "getegrid/xml/?EN=" + coord;
//            log.info(requestUrl);
//            Task task = new Task(requestUrl);
//            compService.submit(task);
//        }
//
//        for (Map.Entry<String, String> entry : settings.getOerebServiceUrls().entrySet()) {
//            Future<GetResult> future = compService.take();
//            log.info(future.get().toString());
//        }
//        executor.shutdown(); // always reclaim resources
    }


        
        
//        ExecutorService pool = Executors.newFixedThreadPool(5);
//        pool.shutdownNow();
        
        //        List<URI> targets = Arrays.asList(
//                new URI("https://httpbin.org/get?name=mkyong1"),
//                new URI("https://httpbin.org/get?name=mkyong2"),
//                new URI("https://httpbin.org/get?name=mkyong3"));
//
//        List<CompletableFuture<String>> result = targets.stream()
//                .map(url -> httpClient.sendAsync(
//                        HttpRequest.newBuilder(url)
//                                .GET()
//                                .setHeader("User-Agent", "Java 11 HttpClient Bot")
//                                .build(),
//                        HttpResponse.BodyHandlers.ofString())
//                        .thenApply(response -> response.body()))
//                .collect(Collectors.toList());
//
//        for (CompletableFuture<String> future : result) {
//            System.out.println(future.get());
//        }


//        ConcurrentHashMap<String, String> tempResults = new ConcurrentHashMap();
//
//        HttpRequest request;
//        
//        ExecutorService executor = Executors.newFixedThreadPool(1);
//
//        HttpClient client = HttpClient.newBuilder()
//                .executor(executor)
//                .version(Version.HTTP_1_1)
//                .followRedirects(Redirect.NEVER)
//                .connectTimeout(Duration.ofSeconds(2))
//                .build();
//
//        Set<CompletableFuture> futures = new HashSet();
//
//        try {
//            for (Map.Entry<String, String> entry : settings.getOerebServiceUrls().entrySet()) {
//                String requestUrl = entry.getValue() + "getegrid/xml/?EN=" + coord;
//                log.info(requestUrl);
//                
//                URI uri = new URI(requestUrl);
//
//                request = HttpRequest.newBuilder()
//                        .uri(uri)
//                        .build();
//
//                CompletableFuture<Void> future = client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                        .thenAccept(resp -> {  
//                            String body = resp.body();
//                            log.info("requestUrl: " + requestUrl + "\n" + body);
//                            
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e1) {
//                                // TODO Auto-generated catch block
//                                e1.printStackTrace();
//                            }
//                            
//                            
//                            if (body.contains("egrid")) {System.out.println("shutdown");
//                              try {
//                                  executor.shutdownNow();
//
//                                  executor.awaitTermination(1, TimeUnit.MILLISECONDS);
//                            } catch (InterruptedException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }}
//                            
//                            
//                            
//                            
////                            try {
////                                Thread.sleep(10000);
////                            } catch (InterruptedException e) {
////                                // TODO Auto-generated catch block
////                                e.printStackTrace();
////                            }
//
//                            // the task returns a JSON Object, for convenience of handling
//                            
//                            // as you see below, it is very easy and convenient to define operations
//                            // on the body (here, a String) returned by each concurrent task 
//                            
////                            JsonReader jsonReader = Json.createReader(new StringReader(body));
////                            JsonObject jsonObject = jsonReader.readObject();
////                            Document docReturn = new Document();
////                            if (jsonObject != null && !jsonObject.isEmpty()) {
////
////                                String key = jsonObject.keySet().iterator().next();
////                                docReturn.setId(Integer.valueOf(key));
////                                docReturn.setText(mapOfLines.get(Integer.valueOf(key)));
////
////                                // Category._11 is the label for "positive sentiment" 
////                                if (jsonObject.getString(key).equals(Category._11.toString())) {
////                                    docReturn.setSentiment(Categories.Category._11);
////                                }
////
////                                // Category._12 is the label for "negative sentiment" 
////                                if (jsonObject.getString(key).equals(Category._12.toString())) {
////                                    docReturn.setSentiment(Categories.Category._12);
////                                }
////                                
////                                tempResults.put(Integer.valueOf(key), docReturn);
////                            }
//                        });
//                futures.add(future);
//            }
//            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray((new CompletableFuture[0])));            
//            combinedFuture.join();
//            log.info("******finito");
//
//        
//        } catch (URISyntaxException exception) {
//            System.out.println("URI syntax exception: " + exception);
//        } 
    
}
