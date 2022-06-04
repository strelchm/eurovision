package ru.strelchm.gateway.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.strelchm.gateway.dto.AddVoteDto;
import ru.strelchm.gateway.dto.IntervalVoteStatResponseDto;
import ru.strelchm.gateway.dto.VoteResponseDto;
import ru.strelchm.gateway.dto.VotesResponseDto;
import ru.strelchm.gateway.service.VoteService;

@RestController
@Validated
@Tag(name = "/api/v1/info/", description = "Gateway info operations")
public class GatewayInfoController extends AbstractController {
    public static final String SUCCESS_MESSAGE_FIELD = "Success";
    public static final String CREATED_MESSAGE_FIELD = "Created";
    private final Log LOG  = LogFactory.getLog(GatewayInfoController.class.getName());


    private final VoteService voteService;

    @Autowired
    public GatewayInfoController(VoteService voteService) {
        this.voteService = voteService;
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
    public Mono<VoteResponseDto> addVote(@RequestBody AddVoteDto dto
    ) {
        return voteService.addVote(dto);
    }

    /**
     * Получить общую статистику
     */
    @GetMapping
    @Operation(summary = "Get all votes", responses = @ApiResponse(
        responseCode = "200", description = SUCCESS_MESSAGE_FIELD,
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = VotesResponseDto.class))
    ))
    public Mono<VotesResponseDto> getAllVotes() {
        return voteService.getAllVotes();
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
        @RequestParam(value = "artists", required = false) String artists
    ) {
        return voteService.getIntervalStatistics(intervalCount, dateFrom, dateTo, artists);
    }
}
