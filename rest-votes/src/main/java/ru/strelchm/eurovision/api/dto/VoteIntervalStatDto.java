package ru.strelchm.eurovision.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteIntervalStatDto {
  private Date start;
  private Date end;
  private Long votes;
}
