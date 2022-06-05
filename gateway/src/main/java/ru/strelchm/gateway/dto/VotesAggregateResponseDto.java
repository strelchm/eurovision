package ru.strelchm.gateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VotesAggregateResponseDto extends DataDto<VoteAggregateIntervalStatDto> {
  public VotesAggregateResponseDto(List<VoteAggregateIntervalStatDto> data) {
    super(data);
  }
}
