package com.example.cgesp.Cgesp.repository;

import com.example.cgesp.Cgesp.model.PrevisaoDia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PrevisaoDiaRepository extends JpaRepository<PrevisaoDia, Long> {

    Optional<PrevisaoDia> findByDataPrevisao(LocalDate dataPrevisao);

}