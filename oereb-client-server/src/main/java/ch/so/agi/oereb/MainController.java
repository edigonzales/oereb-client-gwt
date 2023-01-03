package ch.so.agi.oereb;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${lucene.queryDefaultRecords}")
    private Integer QUERY_DEFAULT_RECORDS;

    @Value("${lucene.queryMaxRecords}")
    private Integer QUERY_MAX_RECORDS;   

    @Autowired
    Settings settings;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .executor(executorService)
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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

    @RequestMapping(value = "/getegrid", method = RequestMethod.GET, produces = { "application/json" })
    public void getEgrid(@RequestParam(value="EN", required=true) String coord) throws URISyntaxException, InterruptedException, ExecutionException {
        log.info("EN: <{}>", coord);

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
//                            
//                            
//                            try {
//                                Thread.sleep(10000);
//                            } catch (InterruptedException e) {
//                                // TODO Auto-generated catch block
//                                e.printStackTrace();
//                            }
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

    
    
    
}
