package ru.strelchm.votestat;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MetricStatProvider {
  private final Map<String, List<StatNode>> methodStatMap = new ConcurrentHashMap<>();

  /**
   * endpointId = endpointMethodType + " " + endpointName
   */
  public synchronized void putStat(String endpointId, long duration, HttpStatus responseStatus) {
    List<StatNode> statNodes = methodStatMap.computeIfAbsent(endpointId, k -> new ArrayList<>());
    statNodes.add(new StatNode(duration, responseStatus));
  }

  public void printStatistics(String addVoteEndpointStatId) {
    List<StatNode> statNodes = methodStatMap.get(addVoteEndpointStatId);
    int totalCount = statNodes.size();
    long totalDuration = statNodes.stream().mapToLong(v -> v.duration).sum();
    System.out.println("Summary:");
    System.out.printf("Total:%s secs%n", totalDuration / 1000);
    System.out.printf("Slowest:%s secs%n", statNodes.stream().mapToLong(v -> v.duration).max().getAsLong() / 1000);
    System.out.printf("Fastest:%s secs%n", statNodes.stream().mapToLong(v -> v.duration).min().getAsLong() / 1000);
    System.out.printf("Average:%s secs%n", statNodes.stream().mapToLong(v -> v.duration).average().getAsDouble() / 1000);
    System.out.printf("Requests/sec:%s%n", totalCount * 1000 / totalDuration);

    System.out.println("Latency distribution:");

    List<Long> sortedDurations = statNodes.stream().map(v -> v.duration).sorted().collect(Collectors.toList());

    System.out.printf("10%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(10, totalCount)));
    System.out.printf("25%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(25, totalCount)));
    System.out.printf("50%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(50, totalCount)));
    System.out.printf("75%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(75, totalCount)));
    System.out.printf("90%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(90, totalCount)));
    System.out.printf("95%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(95, totalCount)));
    System.out.printf("99%% in %d  secs%n", sortedDurations.get(getIndexFromPercentile(99, totalCount)));

    System.out.println("Status code distribution:");
    statNodes.stream()
        .collect(Collectors.groupingBy(v -> v.responseStatus.value(), Collectors.counting()))
        .forEach((key, value) -> System.out.printf("[%s] %d responses%n", key, value));
  }

  private int getIndexFromPercentile(int percentile, int arrSize) {
    return Math.round(percentile / 100.0f * arrSize);
  }

  @Data
  @AllArgsConstructor
  static class StatNode {
    private long duration;
    private HttpStatus responseStatus;
  }
}
