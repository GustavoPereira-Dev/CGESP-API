package com.example.cgesp.Cgesp;

import jakarta.persistence.*;
import lombok.Data; // Usando Lombok para getters/setters
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_alagamento")
public class Alagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String zona;
    private String bairro;
    private String logradouro;
    private String referencia;
    private String sentido;

    @Column(name = "data_hora_inicio")
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_fim")
    private LocalDateTime dataHoraFim;

    private Boolean ativo;
    private Boolean transitavel;

    @Column(name = "data_coleta")
    private LocalDateTime dataColeta = LocalDateTime.now();
}