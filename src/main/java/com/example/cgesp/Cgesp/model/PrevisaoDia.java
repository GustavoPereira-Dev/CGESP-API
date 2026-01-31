package com.example.cgesp.Cgesp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "tb_previsao_dia")
public class PrevisaoDia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_previsao")
    private LocalDate dataPrevisao;

    @Column(name = "dia_semana")
    private String diaSemana;

    private Integer tempMin;
    private Integer tempMax;
    private Integer umidMin;
    private Integer umidMax;

    @Column(name = "data_coleta")
    private LocalDateTime dataColeta = LocalDateTime.now();

    // Relacionamento: Um dia tem vários períodos
    @OneToMany(mappedBy = "previsaoDia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrevisaoPeriodo> periodos = new ArrayList<>();

    public void adicionarPeriodo(PrevisaoPeriodo periodo) {
        periodos.add(periodo);
        periodo.setPrevisaoDia(this);
    }
}