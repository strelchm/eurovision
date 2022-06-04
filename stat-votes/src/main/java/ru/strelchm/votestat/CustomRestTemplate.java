package ru.strelchm.votestat;


import org.apache.commons.logging.Log;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class CustomRestTemplate extends RestTemplate {
  private static final int DEFAULT_CONNECT_TIMEOUT = 10;
  private static final int DEFAULT_READ_TIMEOUT = 10;

  public CustomRestTemplate() {
    if (getRequestFactory() instanceof SimpleClientHttpRequestFactory) {
      System.out.println("1");
      ((SimpleClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
      ((SimpleClientHttpRequestFactory) getRequestFactory()).setReadTimeout(DEFAULT_READ_TIMEOUT);
    } else if (getRequestFactory() instanceof HttpComponentsClientHttpRequestFactory) {
      System.out.println("2");
      ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setReadTimeout(DEFAULT_READ_TIMEOUT);
      ((HttpComponentsClientHttpRequestFactory) getRequestFactory()).setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
    }
  }
}