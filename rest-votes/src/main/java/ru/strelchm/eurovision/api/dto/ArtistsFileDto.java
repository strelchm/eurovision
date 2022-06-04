package ru.strelchm.eurovision.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class ArtistsFileDto {
  private List<String> artistNames;
}
