package ru.strelchm.eurovision.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.strelchm.eurovision.api.dto.AddVoteDto;
import ru.strelchm.eurovision.api.dto.IntervalVoteStatResponseDto;
import ru.strelchm.eurovision.api.dto.VoteResponseDto;
import ru.strelchm.eurovision.api.dto.VotesResponseDto;
import ru.strelchm.eurovision.api.exception.BadRequestException;
import ru.strelchm.eurovision.mapper.VoteMapper;
import ru.strelchm.eurovision.service.VoteService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;

@RestController("/votes")
@RequestMapping("/votes")
@Validated
@Tag(name = "/votes", description = "Votes operations")
public class EurovisionController {

  public static final String NULL_CREATE_OBJECT_REQUEST_EXCEPTION = "Instance that must be created not found in request body";
  public static final String SUCCESS_MESSAGE_FIELD = "Success";
  public static final String CREATED_MESSAGE_FIELD = "Created";

  private final VoteService voteService;
  private final VoteMapper voteMapper;

  @Autowired
  public EurovisionController(VoteService voteService, VoteMapper voteMapper) {
    this.voteService = voteService;
    this.voteMapper = voteMapper;
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
  public VoteResponseDto addVote(
      @NotNull(message = NULL_CREATE_OBJECT_REQUEST_EXCEPTION) @Valid @RequestBody AddVoteDto dto
  ) {
    return voteMapper.toVoteResponseDto(voteService.addVote(dto.getArtist()));
  }

  /**
   * Получить общую статистику
   */
  @GetMapping
  @Operation(summary = "Get all votes", responses = @ApiResponse(
      responseCode = "200", description = SUCCESS_MESSAGE_FIELD,
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = VotesResponseDto.class))
  ))
  public VotesResponseDto getAllVotes() {
    return voteMapper.toVotesResponseDto(voteService.getAllVotes());
  }

  /**
   * Получить агреггированную статистику по голосам
   */
  @GetMapping("/stats")
  @Operation(summary = "Get interval statistics", responses = @ApiResponse(
      responseCode = "200", description = SUCCESS_MESSAGE_FIELD,
      content = @Content(mediaType = "application/json", schema = @Schema(implementation = IntervalVoteStatResponseDto.class))
  ))
  public IntervalVoteStatResponseDto getIntervalStatistics(
      @RequestParam(value = "intervals", required = false, defaultValue = "10") Long intervalCount,
      @RequestParam(value = "from", required = false) Long dateFrom,
      @RequestParam(value = "to", required = false) Long dateTo,
      @RequestParam(value = "artists", required = false) String artists
  ) {
    if (dateFrom != null && dateTo != null && dateFrom > dateTo) {
      throw new BadRequestException("dateFrom is greater than dateTo");
    }
    return new IntervalVoteStatResponseDto(
        voteService.getIntervalStat(
            intervalCount,
            dateFrom == null ? null : new Date(dateFrom),
            dateTo == null ? null : new Date(dateTo),
            artists
    ));
  }
}
