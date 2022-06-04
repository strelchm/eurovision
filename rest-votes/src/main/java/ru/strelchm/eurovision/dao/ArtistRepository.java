package ru.strelchm.eurovision.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.strelchm.eurovision.domain.Artist;
import ru.strelchm.eurovision.domain.Vote;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {
//    @Query("SELECT a FROM Address a, Region r WHERE " +
//            "a.region = r AND a.city = ?1 AND a.street = ?2 AND a.number = ?3 AND a.region.id = ?4")
//    Optional<Address> findByCityAndStreetAndNumberAndRegionId(String city, String street, String number, Long regionId);

  Optional<Artist> findByName(String artistName);
}
