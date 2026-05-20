package com.iis.backend.repository;

import com.iis.backend.model.Soba;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SobaRepository extends JpaRepository<Soba, Long> {
    List<Soba> findBySmestajId(Long smestajId);
}
