package ru.strelchm.gateway.api;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactivefeign.ReactiveContract;
import reactivefeign.webclient.WebReactiveFeign;
import reactor.core.publisher.Mono;
import ru.strelchm.gateway.dto.*;
import ru.strelchm.gateway.service.Crc16Coder;
import ru.strelchm.gateway.service.VoteService;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.strelchm.gateway.service.VoteService.EUREKA_VOTE_API_ID;

@RestController
@Validated
@Tag(name = "/api/v1/info/", description = "Gateway info operations")
public class VoteGatewayController extends AbstractController {
  public static final String SUCCESS_MESSAGE_FIELD = "Success";
  public static final String CREATED_MESSAGE_FIELD = "Created";
  public static final String API_KEY_PARAMETER = "api-key";
  private final Log LOG = LogFactory.getLog(VoteGatewayController.class.getName());


  private final EurekaClient eurekaClient;
  private final Crc16Coder crc16Coder;

  @Autowired
  public VoteGatewayController(@Lazy EurekaClient eurekaClient, Crc16Coder crc16Coder) {
    this.eurekaClient = eurekaClient;
    this.crc16Coder = crc16Coder;
  }

  /**
   * Проголосовать за исполнителя
   */
  @PostMapping
  @ResponseStatus(value = HttpStatus.CREATED)
  @ApiResponse(description = "Created", responseCode = "201")
  @Operation(summary = "Add vote", responses = @ApiResponse(
      responseCode = "201", description = CREATED_MESSAGE_FIELD,
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = VoteResponseDto.class))
  ))
  public Mono<Void> addVote(
      @RequestBody AddVoteDto dto,
      @RequestHeader(API_KEY_PARAMETER) String apiKey
  ) {
    VoteService voteService = WebReactiveFeign.<VoteService>builder()
        .contract(new ReactiveContract(new SpringMvcContract()))
        .target(VoteService.class, getVoteServiceHomePageUrlByTelephoneNumber(dto.getPhone()));
    return voteService.addVote(dto, apiKey).then(); // todo - aggregation from all services and full dto in response?
  }

  /**
   * Получить общую статистику
   */
  @GetMapping
  @Operation(summary = "Get all votes", responses = @ApiResponse(
      responseCode = "200", description = SUCCESS_MESSAGE_FIELD,
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = VotesResponseDto.class))
  ))
  public Mono<VotesResponseDto> getAllVotes(
      @RequestHeader(API_KEY_PARAMETER) String apiKey
  ) {
    return Mono.zip(
        eurekaClient.getApplication(EUREKA_VOTE_API_ID).getInstances().stream()
            .map(v -> WebReactiveFeign.<VoteService>builder()
                .contract(new ReactiveContract(new SpringMvcContract()))
                .target(VoteService.class, v.getHomePageUrl()))
            .map(voteService -> voteService.getAllVotes(apiKey))
            .collect(Collectors.toList()),
        responseArray -> {
          Map<String, Long> map = Stream.of(responseArray)
              .map(VotesResponseDto.class::cast)
              .flatMap(voteStats -> voteStats.getData().stream())
              .collect(Collectors.groupingBy(VoteResponseDto::getName, Collectors.summingLong(VoteResponseDto::getVotes)));
          return new VotesResponseDto(
              map.entrySet().stream()
                  .map(v -> new VoteResponseDto(v.getKey(), v.getValue()))
                  .collect(Collectors.toList())
          );
        }
    );
  }

  /**
   * Получить агреггированную статистику по голосам
   */
  @GetMapping("/stats")
  @Operation(summary = "Get interval statistics", responses = @ApiResponse(
      responseCode = "200", description = SUCCESS_MESSAGE_FIELD,
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = IntervalVoteStatResponseDto.class))
  ))
  public Mono<VotesAggregateResponseDto> getIntervalStatistics(
      @RequestParam(value = "intervals", required = false, defaultValue = "10") Long intervalCount,
      @RequestParam(value = "from") @NotNull Long dateFrom, // need be required 4 interval aggregating TODO
      @RequestParam(value = "to") @NotNull Long dateTo, // need be required 4 interval aggregating TODO
      @RequestParam(value = "artists", required = false) String artists,
      @RequestHeader(API_KEY_PARAMETER) String apiKey
  ) {
    return Mono.zip(
        eurekaClient.getApplication(EUREKA_VOTE_API_ID).getInstances().stream()
            .map(v -> WebReactiveFeign.<VoteService>builder()
                .contract(new ReactiveContract(new SpringMvcContract()))
                .target(VoteService.class, v.getHomePageUrl()))
            .map(voteService -> voteService.getIntervalStatistics(intervalCount, dateFrom, dateTo, artists, apiKey))
            .collect(Collectors.toList()),
        responseArray -> {
          Map<IntervalMultiInstanceAggregator, Long> multiInstanceStatistics = Stream.of(responseArray)
              .map(IntervalVoteStatResponseDto.class::cast)
              .flatMap(voteStats -> voteStats.getData().stream())
              .collect(
                  Collectors.groupingBy(
                      voteStatDto -> new IntervalMultiInstanceAggregator(voteStatDto.getStart(), voteStatDto.getEnd()),
                      Collectors.summingLong(VoteIntervalStatDto::getVotes)
                  )
              );
          return new VotesAggregateResponseDto(
              multiInstanceStatistics.entrySet().stream()
                  .map(stat -> new VoteAggregateIntervalStatDto(stat.getKey().start.getTime(), stat.getKey().end.getTime(), stat.getValue()))
                  .sorted(Comparator.comparingLong(VoteAggregateIntervalStatDto::getStart))
                  .collect(Collectors.toList())
          );
        }
    );
  }

  private String getVoteServiceHomePageUrlByTelephoneNumber(String telephoneNumber) {
    List<InstanceInfo> voteServiceInstances = eurekaClient.getApplication(EUREKA_VOTE_API_ID).getInstances();
    InstanceInfo instanceInfo = voteServiceInstances.get(crc16Coder.crc16(telephoneNumber.getBytes()) % voteServiceInstances.size());
    return instanceInfo.getHomePageUrl();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class IntervalMultiInstanceAggregator {
    private Date start;
    private Date end;

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      IntervalMultiInstanceAggregator that = (IntervalMultiInstanceAggregator) o;
      return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
      return Objects.hash(start, end);
    }
  }
}
