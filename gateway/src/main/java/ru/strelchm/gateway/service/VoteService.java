package ru.strelchm.gateway.service;

import org.springframework.web.bind.annotation.*;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import ru.strelchm.gateway.dto.AddVoteDto;
import ru.strelchm.gateway.dto.IntervalVoteStatResponseDto;
import ru.strelchm.gateway.dto.VoteResponseDto;
import ru.strelchm.gateway.dto.VotesResponseDto;

//@ReactiveFeignClient(name = VoteService.EUREKA_VOTE_API_ID, fallback = VoteServiceFallback.class)
public interface VoteService {
    String EUREKA_VOTE_API_ID = "voteapi";

    @PostMapping("/votes")
    Mono<VoteResponseDto> addVote(@RequestBody AddVoteDto dto, @RequestHeader("api-key") String apiKey);

    @GetMapping("/votes")
    Mono<VotesResponseDto> getAllVotes(@RequestHeader("api-key") String apiKey);

    @GetMapping("/votes/stats")
    Mono<IntervalVoteStatResponseDto> getIntervalStatistics(
        @RequestParam(value = "intervals", required = false, defaultValue = "10") Long intervalCount,
        @RequestParam(value = "from", required = false) Long dateFrom,
        @RequestParam(value = "to", required = false) Long dateTo,
        @RequestParam(value = "artists", required = false) String artists,
        @RequestHeader("api-key") String apiKey
    );
}
