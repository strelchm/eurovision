package ru.strelchm.eurovision.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
  public static final String API_KEY_PARAMETER = "api-key";
  public static final String X_RATE_LIMIT_REMAINING_PARAMETER = "X-Rate-Limit-Remaining";
  public static final String RATE_LIMIT_RETRY_AFTER_SECONDS_PARAMETER = "Rate-Limit-Retry-After-Seconds";

  private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

  @Value("${rate-limits.max-request-per-period}")
  private Long maxRequestPerPeriod;
  @Value("${rate-limits.period-ms}")
  private Long periodMs;

  public Bucket resolveBucket(String apiKey) {
    return bucketCache.computeIfAbsent(apiKey, this::newBucket);
  }

  private Bucket newBucket(String s) {
    return Bucket4j.builder().addLimit(Bandwidth.classic(maxRequestPerPeriod, Refill.intervally(maxRequestPerPeriod, Duration.ofMillis(periodMs)))).build();
  }
}