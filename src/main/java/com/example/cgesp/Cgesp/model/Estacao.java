package com.example.cgesp.Cgesp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "tb_estacao")
public class Estacao {
    @Id
    @Column(name = "id_posto")
    private Long idPosto; // Ex: 1000635

    private String nome;

    // Relacionamento Opcional: Lista de leituras dessa estação
    @OneToMany(mappedBy = "estacao", cascade = CascadeType.ALL)
    private List<LeituraMeteorologica> leituras;
}