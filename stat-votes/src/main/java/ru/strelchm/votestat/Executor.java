package ru.strelchm.votestat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Component
public class Executor {
  private ExecutorService service;
  private CountDownLatch latch;

  private static final Logger LOG = Logger.getLogger(Executor.class.getName());

  private final RestTemplate restTemplate;
  private final MetricStatProvider metricStatProvider;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();

  @Autowired
  public Executor(MetricStatProvider metricStatProvider) {
    this.metricStatProvider = metricStatProvider;
    restTemplate = new CustomRestTemplate();
  }

  public void initializeExecutor(int totalNumberOfTasks) {
    this.service = Executors.newFixedThreadPool(totalNumberOfTasks);
    this.latch = new CountDownLatch(totalNumberOfTasks);
  }

  public void foo(String endpoint, int port, int requestNumber) throws InterruptedException { // todo http method param add
    String url = String.format("http://localhost:%d%s", port, endpoint);
    System.out.println("request number = " + requestNumber);
    for (int i = 0; i < requestNumber; i++) {
      service.submit(() -> {
        Map<Object, Object> data = new HashMap<>();
        data.put("phone", "9000000000");
        data.put("artist", "Eminem");
        String requestBody = null;
        try {
          requestBody = objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(data);
        } catch (JsonProcessingException e) {
          throw new UnsupportedOperationException(e.getMessage());
        }

        long startNano = System.nanoTime();
        HttpRequest request = null;
        try {
          request = HttpRequest.newBuilder()
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(requestBody))
              .uri(new URI(url))
              .setHeader("api-key", Double.toString(Math.random()))
              .build();
        } catch (URISyntaxException e) {
          throw new UnsupportedOperationException(e.getMessage());
        }

        HttpStatus status;
        try {
          HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
          status = HttpStatus.valueOf(response.statusCode());
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
          throw new UnsupportedOperationException(e.getMessage());
        }
//        HttpHeaders headers = new HttpHeaders();
//        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set();
//        HttpEntity<AddVoteDto> request = new HttpEntity<>(new AddVoteDto(), headers);
//
//        try {
//          ResponseEntity<String> response =
//              restTemplate.postForEntity(url, request, String.class);
//          status = response.getStatusCode();
//        } catch (RestClientResponseException ex) {
//          LOG.warning(ex.getMessage());
//          status = HttpStatus.valueOf(ex.getRawStatusCode());
//        }
        metricStatProvider.putStat(String.format("%s %s", endpoint, HttpMethod.POST),
            (System.nanoTime() - startNano) / 1000000, status);
        latch.countDown();
      });
    }
    latch.await();
  }

  private HttpRequest.BodyPublisher buildFormDataFromMap(Map<Object, Object> data) {
    var builder = new StringBuilder();
    for (Map.Entry<Object, Object> entry : data.entrySet()) {
      if (builder.length() > 0) {
        builder.append("&");
      }
      builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
      builder.append("=");
      builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
    }
    return HttpRequest.BodyPublishers.ofString(builder.toString());
  }
}
