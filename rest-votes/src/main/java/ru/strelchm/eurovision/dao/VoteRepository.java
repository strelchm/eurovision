package ru.strelchm.eurovision.dao;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.strelchm.eurovision.api.dto.VoteIntervalStatDto;
import ru.strelchm.eurovision.domain.Artist;
import ru.strelchm.eurovision.domain.Vote;
import ru.strelchm.eurovision.api.dto.VoteTotalStat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID>, JpaSpecificationExecutor<Vote> {
  @Query("SELECT new ru.strelchm.eurovision.api.dto.VoteTotalStat(v.artist.name, COUNT(v)) FROM Vote v GROUP BY v.artist.name")
  List<VoteTotalStat> getTotalStat(Specification<Vote> specification);

//  @Query("SELECT new ru.strelchm.eurovision.api.dto.VoteIntervalStatDto(v.created, v.created, COUNT(v)) FROM Vote v GROUP BY v.artist.name, v.created") // todo delete v.created
//  List<VoteIntervalStatDto> getIntervalStat(Specification<Vote> specification);

  long count(Specification<Vote> spec);

  Optional<Vote> findFirstByArtistIn(Collection<Artist> artists);

  Optional<Vote> findFirstBy();

  Optional<Vote> findFirstByArtistInOrderByCreatedDesc(Collection<Artist> artists);

  Optional<Vote> findFirstByOrderByCreatedDesc();

  @Query(value = "SELECT COUNT(*) FROM vote v WHERE v.artist_id = ?1", nativeQuery = true)
  long getCountStat(UUID artistId);

  Optional<Vote> findByArtist_Name(String artistName);
}
