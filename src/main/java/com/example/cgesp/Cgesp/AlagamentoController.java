package com.example.cgesp.Cgesp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alagamentos")
public class AlagamentoController {

    @Autowired
    private CgeScraperService scraperService;

    @Autowired
    private AlagamentoRepository repository; // Interface JpaRepository padrão

    @PostMapping("/sincronizar")
    public ResponseEntity<List<Alagamento>> atualizarBaseDeDados() {
        try {
            // 1. Coleta dados novos
            List<Alagamento> novosDados = scraperService.coletarDados();
            
            // 2. (Opcional) Limpar dados antigos ou verificar duplicatas
            // repository.deleteAll(); 
            
            // 3. Salvar no SQL Server
            List<Alagamento> salvos = repository.saveAll(novosDados);
            
            return ResponseEntity.ok(salvos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<Alagamento> listarTodos() {
        return repository.findAll();
    }
}