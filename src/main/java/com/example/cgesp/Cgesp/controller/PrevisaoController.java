package com.example.cgesp.Cgesp.controller;

import com.example.cgesp.Cgesp.model.PrevisaoDia;
import com.example.cgesp.Cgesp.repository.PrevisaoDiaRepository;
import com.example.cgesp.Cgesp.service.PrevisaoScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; // Importante!
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId; // Importante para o fuso horário
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/previsao")
public class PrevisaoController {

    @Autowired
    private PrevisaoScraperService scraperService;

    @Autowired
    private PrevisaoDiaRepository repository;

    // Sincroniza os dados do site para o banco
    @PostMapping("/sincronizar")
    @Transactional // Garante que o delete e o save ocorram na mesma transação
    public ResponseEntity<List<PrevisaoDia>> sincronizarPrevisao() {
        try {
            List<PrevisaoDia> novasPrevisoes = scraperService.coletarPrevisao();

            for (PrevisaoDia nova : novasPrevisoes) {
                // Verifica se já existe previsão para aquela data
                Optional<PrevisaoDia> existente = repository.findByDataPrevisao(nova.getDataPrevisao());
                
                // Se existir, deletamos a antiga para inserir a nova (refresh completo)
                // O CascadeType.ALL na entidade PrevisaoDia garantirá que os períodos filhos também sejam apagados
                existente.ifPresent(repository::delete);
            }
            
            // repository.flush(); // Opcional: força a deleção ocorrer antes da inserção
            List<PrevisaoDia> salvos = repository.saveAll(novasPrevisoes);
            
            return ResponseEntity.ok(salvos);
            
        } catch (Exception e) {
            e.printStackTrace(); // Ajuda a ver o erro no console
            return ResponseEntity.internalServerError().build();
        }
    }

    // Retorna a previsão completa salva no banco
    @GetMapping
    public List<PrevisaoDia> listarPrevisoes() {
        return repository.findAll();
    }
    
    // Retorna a previsão de hoje
    @GetMapping("/hoje")
    public ResponseEntity<PrevisaoDia> previsaoHoje() {
        // Garante que pegamos a data de São Paulo, não a do servidor (que pode ser UTC)
        LocalDate hojeSP = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        return repository.findByDataPrevisao(hojeSP)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}