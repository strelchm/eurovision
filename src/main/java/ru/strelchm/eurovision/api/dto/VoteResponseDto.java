package ru.strelchm.eurovision.api.dto;

import lombok.Data;

@Data
public class VoteResponseDto {
  private String name;
  private Long votes;
}
