package com.iis.backend.repository;

import com.iis.backend.model.Smestaj;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SmestajRepository extends JpaRepository<Smestaj, Long> {
    List<Smestaj> findByPutovanjeId(Long putovanjeId);
    Optional<Smestaj> findByPutovanjeIdAndIzabranTrue(Long putovanjeId);
}
