package ru.strelchm.gateway.api;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.strelchm.gateway.service.VoteService.EUREKA_VOTE_API_ID;

@RestController
@Validated
@Tag(name = "/api/v1/info/", description = "Gateway info operations")
public class GatewayInfoController extends AbstractController {
  public static final String SUCCESS_MESSAGE_FIELD = "Success";
  public static final String CREATED_MESSAGE_FIELD = "Created";
  public static final String API_KEY_PARAMETER = "api-key";
  private final Log LOG = LogFactory.getLog(GatewayInfoController.class.getName());


  private final EurekaClient eurekaClient;
  private final Crc16Coder crc16Coder;

  @Autowired
  public GatewayInfoController(@Lazy EurekaClient eurekaClient, Crc16Coder crc16Coder) {
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
  public Mono<VoteResponseDto> addVote(
      @RequestBody AddVoteDto dto,
      @RequestHeader(API_KEY_PARAMETER) String apiKey
  ) {
    VoteService voteService = WebReactiveFeign.<VoteService>builder()
        .contract(new ReactiveContract(new SpringMvcContract()))
        .target(VoteService.class, getVoteServiceHomePageUrlByTelephoneNumber(dto.getPhone()));
    return voteService.addVote(dto, apiKey);
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
          List<VoteResponseDto> res = new ArrayList<>();
          for (Object response : responseArray) {
            res.addAll(((VotesResponseDto) response).getData());
          }
          return new VotesResponseDto(res);
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
  public Mono<IntervalVoteStatResponseDto> getIntervalStatistics(
      @RequestParam(value = "intervals", required = false, defaultValue = "10") Long intervalCount,
      @RequestParam(value = "from", required = false) Long dateFrom,
      @RequestParam(value = "to", required = false) Long dateTo,
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
          List<VoteIntervalStatDto> res = new ArrayList<>();
          for (Object response : responseArray) {
            res.addAll(((IntervalVoteStatResponseDto) response).getData());
          }
          return new IntervalVoteStatResponseDto(res);
        }
    );
  }

  private String getVoteServiceHomePageUrlByTelephoneNumber(String telephoneNumber) {
    List<InstanceInfo> voteServiceInstances = eurekaClient.getApplication(EUREKA_VOTE_API_ID).getInstances();
    InstanceInfo instanceInfo = voteServiceInstances.get(crc16Coder.crc16(telephoneNumber.getBytes()) % voteServiceInstances.size());
    return instanceInfo.getHomePageUrl();
  }
}
