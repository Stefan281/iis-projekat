package com.iis.backend.repository;

import com.iis.backend.model.IgracPutovanje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IgracPutovanjeRepository extends JpaRepository<IgracPutovanje, Long> {
    List<IgracPutovanje> findByPutovanjeId(Long putovanjeId);
    List<IgracPutovanje> findByIgracId(Long igracId);
}
