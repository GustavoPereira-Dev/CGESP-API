package com.example.cgesp.Cgesp.dto;

import lombok.Data; // Se não usar Lombok, gere os Getters/Setters e toString()

@Data
public class AlagamentoDTO {
    private String zona;
    private String bairro;
    private String local;
    private String horario;
    private String sentido;
    private String referencia;
    private String status; // "Ativo", "Inativo", "Transitável", etc.
}