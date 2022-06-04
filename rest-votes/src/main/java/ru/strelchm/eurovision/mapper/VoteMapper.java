package ru.strelchm.eurovision.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.strelchm.eurovision.api.dto.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class VoteMapper {
  public VotesResponseDto toVotesResponseDto(List<VoteTotalStat> allVotes) {
    return new VotesResponseDto(allVotes.stream().map(this::toVoteResponseDto).collect(Collectors.toList()));
  }

  @Mapping(target = "name", expression = "java(vote.getArtistName())")
  public abstract VoteResponseDto toVoteResponseDto(VoteTotalStat vote);

  public IntervalVoteStatResponseDto toIntervalResponseDto(List<VoteIntervalStatDto> allVotes) {
    return new IntervalVoteStatResponseDto(allVotes);
  }
}
