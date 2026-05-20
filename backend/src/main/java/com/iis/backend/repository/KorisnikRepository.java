package com.iis.backend.repository;

import com.iis.backend.enums.UlogaKorisnika;
import com.iis.backend.model.Korisnik;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KorisnikRepository extends JpaRepository<Korisnik, Long> {
    Optional<Korisnik> findByKorisnickoIme(String korisnickoIme);
    Optional<Korisnik> findByEmail(String email);
    List<Korisnik> findByUloga(UlogaKorisnika uloga);
}
