package ru.strelchm.eurovision.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.strelchm.eurovision.domain.Artist;
import ru.strelchm.eurovision.domain.Vote;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
  Optional<Artist> findByName(String artistName);
}
