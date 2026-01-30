package com.example.cgesp.Cgesp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_noticia")
public class Noticia {

    @Id
    private Long id; // ID vindo da CGE (não é auto-generated)

    private String titulo;

    @Column(columnDefinition = "TEXT") // Garante suporte a textos longos no SQL Server
    private String conteudo;

    @Column(name = "data_publicacao")
    private LocalDateTime dataPublicacao;

    @Column(name = "link_original")
    private String linkOriginal;

    @Column(name = "data_coleta")
    private LocalDateTime dataColeta = LocalDateTime.now();
}