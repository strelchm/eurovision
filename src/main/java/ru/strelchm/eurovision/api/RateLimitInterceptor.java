package ru.strelchm.eurovision.api;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.strelchm.eurovision.service.RateLimiterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static ru.strelchm.eurovision.service.RateLimiterService.*;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
  private final RateLimiterService rateLimiterService;

  @Autowired
  public RateLimitInterceptor(RateLimiterService rateLimiterService) {
    this.rateLimiterService = rateLimiterService;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String apiKey = request.getHeader(API_KEY_PARAMETER);
    if (apiKey == null || apiKey.isEmpty()) {
      response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Header: api-key");
      return false;
    }
    Bucket bucket = rateLimiterService.resolveBucket(apiKey);
    ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    if (probe.isConsumed()) {
      response.addHeader(X_RATE_LIMIT_REMAINING_PARAMETER, String.valueOf(probe.getRemainingTokens()));
      return true;
    } else {
      long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
      response.addHeader(RATE_LIMIT_RETRY_AFTER_SECONDS_PARAMETER, String.valueOf(waitForRefill));
      response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
          "You have exhausted your API Request Quota");
      return false;
    }
  }
}
