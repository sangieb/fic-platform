package com.fic.backend.repository;

import com.fic.backend.model.FicData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FicDataRepository extends JpaRepository<FicData, Long> {

    Page<FicData> findByNombreFondoContainingIgnoreCase(
        String nombre, Pageable pageable);

    Page<FicData> findBySociedadGestora(
        String gestora, Pageable pageable);
}