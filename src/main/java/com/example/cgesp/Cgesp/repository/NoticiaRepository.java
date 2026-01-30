package com.example.cgesp.Cgesp.repository;


import com.example.cgesp.Cgesp.model.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Long> {
    // O JpaRepository já nos dá o findById, save, findAll, etc.
}