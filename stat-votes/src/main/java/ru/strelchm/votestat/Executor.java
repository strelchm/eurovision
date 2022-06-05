package ru.strelchm.votestat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Component
public class Executor {
  private ExecutorService service;
  private CountDownLatch latch;

  private static final Logger LOG = Logger.getLogger(Executor.class.getName());

  private static final String[] ALLOWED_ARTIST_NAMES = {"mc Poopkin", "Dr Dre", "Rihanna", "Eminem"};

  private final MetricStatProvider metricStatProvider;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .build();

  @Autowired
  public Executor(MetricStatProvider metricStatProvider) {
    this.metricStatProvider = metricStatProvider;
  }

  public void initializeExecutor(int totalNumberOfTasks) {
    this.service = Executors.newFixedThreadPool(totalNumberOfTasks);
    this.latch = new CountDownLatch(totalNumberOfTasks);
  }

  public void executeRequest(String endpoint, int port, int requestNumber) throws InterruptedException { // todo {strelchm} http method param add
    String url = String.format("http://localhost:%d%s", port, endpoint);
    for (int i = 0; i < requestNumber; i++) {
      service.submit(() -> {
        Map<Object, Object> data = new HashMap<>();
        data.put("phone", getRandomTelephone());
        data.put("artist", getRandomArtistName());
        String requestBody = null;
        try {
          requestBody = objectMapper
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(data);
        } catch (JsonProcessingException e) {
          throw new UnsupportedOperationException(e.getMessage());
        }

        long startNano = System.nanoTime();
        HttpRequest request;
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
        metricStatProvider.putStat(String.format("%s %s", endpoint, HttpMethod.POST),
            (System.nanoTime() - startNano) / 1000000, status);
        latch.countDown();
      });
    }
    latch.await();
  }

  private String getRandomTelephone() {
    StringBuilder sb = new StringBuilder("9");
    for (int i = 0; i < 9; i++) {
      sb.append((int) (Math.random() * 9));
    }
    return sb.toString();
  }

  private String getRandomArtistName() {
    return ALLOWED_ARTIST_NAMES[(int) (Math.random() * ALLOWED_ARTIST_NAMES.length)];
  }
}
