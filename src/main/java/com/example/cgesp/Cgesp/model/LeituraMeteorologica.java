package com.example.cgesp.Cgesp.model;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_leitura_meteorologica")
public class LeituraMeteorologica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_posto")
    private Estacao estacao;

    private LocalDateTime dataLeitura;

    // Métricas principais (usando Double para permitir nulos caso falhe a leitura)
    private Double tempAtual;
    private Double umidAtual;
    private Double ventoVel;
    private String ventoDir;
    private Double chuvaPeriodoAtual;
    private Double pressaoAtual;
}