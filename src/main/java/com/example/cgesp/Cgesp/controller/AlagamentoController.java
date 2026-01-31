package com.example.cgesp.Cgesp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cgesp.Cgesp.dto.AlagamentoDTO;
import com.example.cgesp.Cgesp.model.Alagamento;
import com.example.cgesp.Cgesp.repository.AlagamentoRepository;
import com.example.cgesp.Cgesp.service.AlagamentoScraperService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alagamentos")
public class AlagamentoController {

    @Autowired
    private AlagamentoScraperService scraperService;

    @Autowired
    private AlagamentoRepository repository;

    // --- NOVO ENDPOINT DE HISTÓRICO ---
    // Exemplo de chamada: GET /api/alagamentos/historico?data=25/01/2026
    @GetMapping("/historico")
    public ResponseEntity<List<Alagamento>> buscarHistorico(@RequestParam("data") String dataString) {
        try {
            System.out.println("----- BUSCANDO HISTÓRICO PARA: " + dataString + " -----");
            
            // 1. Coleta os DTOs usando a data específica
            List<AlagamentoDTO> dtos = scraperService.lerDadosPorData(dataString);

            // 2. Prepara a data base para a conversão (String -> LocalDate)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dataReferencia = LocalDate.parse(dataString, formatter);

            // 3. Converte usando a data correta
            List<Alagamento> entidades = dtos.stream()
                    .map(dto -> converterDtoParaEntity(dto, dataReferencia)) // Passamos a data aqui
                    .collect(Collectors.toList());

            // 4. Imprime no console (Feedback visual solicitado)
            if (entidades.isEmpty()) {
                System.out.println("Nenhum registro encontrado para esta data.");
            } else {
                entidades.forEach(a -> System.out.println("Salvo memória: " + a.getLogradouro() + " em " + a.getDataHoraInicio()));
            }

            // 5. Salva no Banco (Opcional, mas recomendado para histórico)
            // repository.saveAll(entidades); 

            return ResponseEntity.ok(entidades);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build(); // Retorna 400 se a data for inválida
        }
    }

    // --- ENDPOINTS ANTERIORES (Mantidos) ---
    
    @PostMapping("/sincronizar")
    public ResponseEntity<List<Alagamento>> atualizarBaseDeDados() {
        // Usa a data de HOJE como referência
        List<AlagamentoDTO> dtos = scraperService.lerDadosAgora();
        List<Alagamento> entidades = dtos.stream()
                .map(dto -> converterDtoParaEntity(dto, LocalDate.now())) 
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(repository.saveAll(entidades));
    }

    // --- MÉTODO AUXILIAR REFATORADO ---
    // Agora aceita 'dataReferencia' para saber se é hoje ou histórico
    private Alagamento converterDtoParaEntity(AlagamentoDTO dto, LocalDate dataReferencia) {
        Alagamento entity = new Alagamento();
        
        entity.setZona(dto.getZona());
        entity.setBairro(dto.getBairro());
        entity.setLogradouro(dto.getLocal());
        entity.setSentido(dto.getSentido());
        entity.setReferencia(dto.getReferencia());

        // Status
        if (dto.getStatus() != null) {
            String s = dto.getStatus().toLowerCase();
            entity.setAtivo(s.contains("ativo") && !s.contains("inativo"));
            entity.setTransitavel(s.contains("transitavel") && !s.contains("intransitavel"));
        }

        // Horário + Data de Referência
        try {
            if (dto.getHorario() != null && !dto.getHorario().isEmpty()) {
                String horaTexto = dto.getHorario().replaceAll("[^0-9:]", "").substring(0, 5); 
                LocalTime hora = LocalTime.parse(horaTexto, DateTimeFormatter.ofPattern("HH:mm"));
                
                // AQUI ESTÁ A MÁGICA: Junta a data passada na URL com a hora do site
                entity.setDataHoraInicio(LocalDateTime.of(dataReferencia, hora));
            }
        } catch (Exception e) {
            // Log de erro de parse
        }

        return entity;
    }

    @GetMapping("/checar-agora")
    public ResponseEntity<List<AlagamentoDTO>> checarEmTempoReal() {
        List<AlagamentoDTO> dados = scraperService.lerDadosAgora();

        System.out.println("----- DADOS CAPTURADOS EM TEMPO REAL -----");
        if (dados.isEmpty()) {
            System.out.println("Nenhum ponto de alagamento encontrado no site.");
        } else {
            dados.forEach(dado -> {
                System.out.println("Zona: " + dado.getZona() + " | Bairro: " + dado.getBairro());
                System.out.println("Local: " + dado.getLocal());
                System.out.println("Status: " + dado.getStatus());
                System.out.println("------------------------------------------------");
            });
        }
        return ResponseEntity.ok(dados);
    }
    
    @GetMapping
    public List<Alagamento> listarTodos() {
        return repository.findAll();
    }


}