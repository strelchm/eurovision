package ru.strelchm.eurovision.api.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class AddVoteDto {
  @NotEmpty
  @Pattern(regexp = "^9\\d{9}$")
  private String phone;
  @NotEmpty
  private String artist;
}
