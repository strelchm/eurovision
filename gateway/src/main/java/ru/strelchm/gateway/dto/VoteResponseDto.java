package ru.strelchm.gateway.dto;

import lombok.Data;

@Data
public class VoteResponseDto {
  private String name;
  private Long votes;
}
