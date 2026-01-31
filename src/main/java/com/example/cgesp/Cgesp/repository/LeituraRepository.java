package com.example.cgesp.Cgesp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cgesp.Cgesp.model.Alagamento;
import com.example.cgesp.Cgesp.model.LeituraMeteorologica;

public interface LeituraRepository extends JpaRepository<Alagamento, Long>{

	void save(LeituraMeteorologica leituraAtual);

}
