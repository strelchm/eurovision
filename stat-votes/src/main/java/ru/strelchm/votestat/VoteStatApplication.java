package ru.strelchm.votestat;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.HttpMethod;

public class VoteStatApplication {
  private static final String ADD_VOTE_ENDPOINT = "/votes";
  private static final HttpMethod ADD_VOTE_ENDPOINT_METHOD_TYPE = HttpMethod.POST;
  public static final String CONCURRENCY_ARG_NAME = "-c";
  public static final String REQUEST_NUMBER_ARG_NAME = "-n";
  public static final String PORT_ARG_NAME = "vote-svc:";

  public static void main(String[] args) throws InterruptedException {
    int requestNumber = -1;
    int concurrency = -1;
    int port = -1;
    for (int i = 0; i < args.length; i++) {
      String currentArg = args[i];
      if (currentArg.equals(REQUEST_NUMBER_ARG_NAME)) {
        requestNumber = Integer.parseInt(args[i + 1]);
        ++i;
      } else if (currentArg.equals(CONCURRENCY_ARG_NAME)) {
        concurrency = Integer.parseInt(args[i + 1]);
      } else if (currentArg.contains(PORT_ARG_NAME)) {
        port = Integer.parseInt(currentArg.substring(PORT_ARG_NAME.length()));
      }
    }
    if (requestNumber < 0) {
      throw new UnsupportedOperationException(String.format("%s is not set", REQUEST_NUMBER_ARG_NAME));
    }
    if (concurrency < 0) {
      throw new UnsupportedOperationException(String.format("%s is not set", CONCURRENCY_ARG_NAME));
    }
    if (port < 0) {
      throw new UnsupportedOperationException(String.format("%s is not set", PORT_ARG_NAME));
    }

    ApplicationContext context = new AnnotationConfigApplicationContext("ru.strelchm.votestat");
    context.getBean(Executor.class).initializeExecutor(requestNumber);
    context.getBean(Executor.class).executeRequest(ADD_VOTE_ENDPOINT, port, requestNumber);
    System.out.printf("Runs %d vote requests with %d concurrent requests at the same time%n", requestNumber, concurrency);
    context.getBean(MetricStatProvider.class).printStatistics(String.format("%s %s", ADD_VOTE_ENDPOINT, ADD_VOTE_ENDPOINT_METHOD_TYPE));
    System.exit(0);
  }
}
