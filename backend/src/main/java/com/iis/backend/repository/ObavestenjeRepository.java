package com.iis.backend.repository;

import com.iis.backend.model.Obavestenje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObavestenjeRepository extends JpaRepository<Obavestenje, Long> {
    List<Obavestenje> findAllByOrderByDatumKreiranjaDesc();
}
