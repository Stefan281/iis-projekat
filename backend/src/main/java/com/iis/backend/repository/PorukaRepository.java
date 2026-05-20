package com.iis.backend.repository;

import com.iis.backend.model.Poruka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PorukaRepository extends JpaRepository<Poruka, Long> {
    List<Poruka> findByPrimalacIdOrderByVremeSlanjaDesc(Long primalacId);
    List<Poruka> findByPosiljalacIdOrderByVremeSlanjaDesc(Long posiljalacId);
}
