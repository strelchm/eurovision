package ru.strelchm.eurovision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.strelchm.eurovision.api.dto.ArtistsFileDto;
import ru.strelchm.eurovision.api.dto.VoteIntervalStatDto;
import ru.strelchm.eurovision.api.dto.VoteTotalStat;
import ru.strelchm.eurovision.api.exception.BadRequestException;
import ru.strelchm.eurovision.dao.ArtistRepository;
import ru.strelchm.eurovision.dao.VoteRepository;
import ru.strelchm.eurovision.domain.Artist;
import ru.strelchm.eurovision.domain.BaseEntity;
import ru.strelchm.eurovision.domain.Vote;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoteService implements InitializingBean {

  private final VoteRepository voteRepository;
  private final ArtistRepository artistRepository;

  @Autowired
  public VoteService(VoteRepository voteRepository, ArtistRepository artistRepository) {
    this.voteRepository = voteRepository;
    this.artistRepository = artistRepository;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    ArtistsFileDto artistNamesDto = mapper.readValue(new File("rest-votes/src/main/resources/artists.yml"), ArtistsFileDto.class);
    artistRepository.saveAll(artistNamesDto.getArtistNames().stream().map(Artist::new).collect(Collectors.toList()));
  }

  @Transactional(readOnly = true)
  public List<VoteTotalStat> getAllVotes() {
    return voteRepository.getTotalStat(null);
  }

  @Transactional(readOnly = true)
  public List<VoteIntervalStatDto> getIntervalStat(Long intervalCount, Date dateFrom, Date dateTo, String artistNames) {
    List<VoteIntervalStatDto> result = new ArrayList<>();
    List<Artist> artists = artistNames == null ? null :
        Arrays.stream(artistNames.split(",")).map(this::getArtistByName).collect(Collectors.toList());
    if (dateFrom == null) {
      dateFrom = voteRepository.findFirstByArtistIn(artists).map(BaseEntity::getCreated).orElse(null); // todo artists may be null!
    }
    if (dateTo == null) {
      dateTo = voteRepository.findFirstByArtistInOrderByCreatedDesc(artists).map(BaseEntity::getCreated).orElse(null); // todo artists may be null!
    }

    if (dateTo == null || dateFrom == null) {
      return new ArrayList<>();
    }

    long intervalPeriod = (dateTo.getTime() - dateFrom.getTime()) / intervalCount; // todo dateTo or dateFrom may be null!

    long from = dateFrom.getTime();
    long to = from + intervalPeriod;
    for (int i = 0; i < intervalCount; i++) {
      System.out.println("from = " + from + " to = " + to); // just 4 test
      System.out.println("diff = " + (to - from)); // just 4 test
      result.add(
          new VoteIntervalStatDto(
              new Date(from),
              new Date(to),
              voteRepository.count(getVoteSpecification(intervalCount, from, to, artists))
          )
      );
      from = to + 1;
      to = (i == intervalCount - 1) ? dateTo.getTime() : from + intervalPeriod;
    }
    return result;
  }

  public Specification<Vote> getVoteSpecification(Long intervalCount, Long dateFrom, Long dateTo, List<Artist> artists) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
//      if (device != null) {
//        predicates.add(cb.equal(root.get("device"), device));
//      }
      if (dateFrom != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("created"), new Date(dateFrom)));
      }
      if (dateTo != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("created"), new Date(dateTo)));
      }
      if (artists != null) {
        predicates.add(cb.and(root.get("artist").in(artists)));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  @Transactional
  public VoteTotalStat addVote(String artistName) {
    Artist artist = getArtistByName(artistName);
    Vote vote = new Vote();
    vote.setArtist(artist);
    voteRepository.save(vote);
    return new VoteTotalStat(artistName, voteRepository.getCountStat(artist.getId()));
  }

  private Artist getArtistByName(String artistName) {
    return artistRepository.findByName(artistName)
        .orElseThrow(() -> new BadRequestException(String.format("Artist %s not found", artistName)));
  }
}
