package ru.strelchm.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteAggregateIntervalStatDto {
  private Long start;
  private Long end;
  private Long votes;
}
