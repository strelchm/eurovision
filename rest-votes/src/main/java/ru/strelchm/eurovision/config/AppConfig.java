package ru.strelchm.eurovision.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.strelchm.eurovision.api.RateLimitInterceptor;

@Configuration
public class AppConfig implements WebMvcConfigurer {
  private final RateLimitInterceptor rateLimitInterceptor;

  @Autowired
  public AppConfig(RateLimitInterceptor rateLimitInterceptor) {
    this.rateLimitInterceptor = rateLimitInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns("/swagger-ui/**", "/v3/**",
        "configuration/ui", "swagger-resources/**", "configuration/security", "swagger-ui.html", "webjars/**");
  }
}
