package ru.strelchm.eurovision.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class IntervalVoteStatResponseDto extends DataDto<VoteIntervalStatDto> {
  public IntervalVoteStatResponseDto(List<VoteIntervalStatDto> data) {
    super(data);
  }
}
