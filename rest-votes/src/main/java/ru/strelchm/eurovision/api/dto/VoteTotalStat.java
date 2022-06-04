package ru.strelchm.eurovision.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteTotalStat {
  private String artistName;
  private Long votes;
}
