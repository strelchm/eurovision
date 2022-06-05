package ru.strelchm.gateway.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;
import ru.strelchm.gateway.dto.AddVoteDto;
import ru.strelchm.gateway.dto.IntervalVoteStatResponseDto;
import ru.strelchm.gateway.dto.VoteResponseDto;
import ru.strelchm.gateway.dto.VotesResponseDto;

@Component
public class VoteServiceFallback implements VoteService {
  private static final Log LOG = LogFactory.getLog(VoteServiceFallback.class);

  @Override
  public Mono<VoteResponseDto> addVote(AddVoteDto dto, String apiKey) {
    LOG.error(String.format("Error during addVote"));
    throw new UnsupportedOperationException("Vote service fell");
  }

  @Override
  public Mono<VotesResponseDto> getAllVotes(String apiKey) {
    LOG.error(String.format("Error during getAllVotes"));
    throw new UnsupportedOperationException("Vote service fell");
  }

  @Override
  public Mono<IntervalVoteStatResponseDto> getIntervalStatistics(Long intervalCount, Long dateFrom,
                                                                 Long dateTo, String artists, String apiKey) {
    LOG.error(String.format("Error during getIntervalStatistics"));
    throw new UnsupportedOperationException("Vote service fell");
  }
}