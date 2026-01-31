package com.example.cgesp.Cgesp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_previsao_periodo")
public class PrevisaoPeriodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_previsao_dia")
    @JsonIgnore // Evita loop infinito no JSON
    private PrevisaoDia previsaoDia;

    @Column(name = "nome_periodo")
    private String nomePeriodo; // Madrugada, Manhã...

    private String condicao; // Nublado, Sol...
    
    @Column(name = "potencial_tempestade")
    private String potencialTempestade; // Baixo, Moderado...
    
    private String iconeUrl;
}