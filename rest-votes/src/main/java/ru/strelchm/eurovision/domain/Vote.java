package ru.strelchm.eurovision.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Vote extends BaseEntity<UUID> {
  @ManyToOne
  @JoinColumn(name = "artist_id")
  private Artist artist;
}
