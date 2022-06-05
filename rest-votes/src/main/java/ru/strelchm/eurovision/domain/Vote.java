package ru.strelchm.eurovision.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vote extends BaseEntity<UUID> {
  @ManyToOne(fetch= FetchType.LAZY)
  @JoinColumn(name = "artist_id")
  private Artist artist;
}
