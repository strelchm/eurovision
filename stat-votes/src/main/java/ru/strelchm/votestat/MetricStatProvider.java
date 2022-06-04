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
    long totalCount = methodStatMap.get(addVoteEndpointStatId).size();
    long totalDuration = methodStatMap.get(addVoteEndpointStatId).stream().mapToLong(v -> v.duration).sum();
    System.out.println("Summary:");
    System.out.printf("Total:%s secs%n", totalDuration / 1000);
    System.out.printf("Slowest:%s secs%n", methodStatMap.get(addVoteEndpointStatId).stream().mapToLong(v -> v.duration).max().getAsLong() / 1000);
    System.out.printf("Fastest:%s secs%n", methodStatMap.get(addVoteEndpointStatId).stream().mapToLong(v -> v.duration).min().getAsLong() / 1000);
    System.out.printf("Average:%s secs%n", methodStatMap.get(addVoteEndpointStatId).stream().mapToLong(v -> v.duration).average().getAsDouble() / 1000);
    System.out.printf("Requests/sec:%s%n", totalCount * 1000 / totalDuration);

    System.out.println("Latency distribution:");

//    10% in 1.0042 secs
//
//    25% in 1.0045 secs
//
//    50% in 1.0052 secs
//
//    75% in 1.0065 secs
//
//    90% in 1.0122 secs
//
//    95% in 1.0138 secs
//
//    99% in 1.0146 secs

    System.out.println("Status code distribution:");
    methodStatMap.get(addVoteEndpointStatId).stream()
        .collect(Collectors.groupingBy(v -> v.responseStatus.value(), Collectors.counting()))
        .entrySet().stream()
        .forEach(stat -> System.out.printf("[%s] %d responses%n", stat.getKey(), stat.getValue()));

//    насчет задания за 40 баллов по бэкэнду, я правильно понимаю, что нужно использовать nearest-rank, который n = ceiling((P / 100) x N) (где n это индекс, P - процентиль и N - размер)?
  }

  @Data
  @AllArgsConstructor
  static class StatNode {
    private long duration;
    private HttpStatus responseStatus;
  }
}
