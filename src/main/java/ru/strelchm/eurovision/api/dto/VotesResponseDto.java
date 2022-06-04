package ru.strelchm.eurovision.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class VotesResponseDto extends DataDto<VoteResponseDto> {
  public VotesResponseDto(List<VoteResponseDto> data) {
    super(data);
  }
}
