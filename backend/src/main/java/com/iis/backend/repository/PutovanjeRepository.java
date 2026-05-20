package com.iis.backend.repository;

import com.iis.backend.model.Putovanje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PutovanjeRepository extends JpaRepository<Putovanje, Long> {
}
