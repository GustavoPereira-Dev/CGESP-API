package com.example.cgesp.Cgesp.controller;

import com.example.cgesp.Cgesp.model.Estacao;
import com.example.cgesp.Cgesp.model.LeituraMeteorologica;
import com.example.cgesp.Cgesp.repository.EstacaoRepository;
import com.example.cgesp.Cgesp.repository.LeituraRepository;
import com.example.cgesp.Cgesp.service.EstacaoScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estacoes")
public class EstacaoController {

    @Autowired
    private EstacaoScraperService scraperService;

    @Autowired
    private EstacaoRepository estacaoRepository;
    
    @Autowired
    private LeituraRepository leituraRepository;

    // 1. Sincroniza/Lista as estações (apenas nomes e IDs)
    @GetMapping("/sincronizar-lista")
    public ResponseEntity<List<Estacao>> sincronizarLista() {
        List<Estacao> estacoes = scraperService.listarEstacoesDisponiveis();
        estacaoRepository.saveAll(estacoes);
        return ResponseEntity.ok(estacoes);
    }
    
    // 2. Endpoint solicitado: Ver dado atual de uma estação específica
    // Exemplo de uso: /api/estacoes/1000635/atual
    @GetMapping("/{idPosto}")
    public ResponseEntity<LeituraMeteorologica> getDadosAtuais(@PathVariable Long idPosto) {
        // Busca dados em tempo real no site
        LeituraMeteorologica leituraAtual = scraperService.coletarDadosAtuais(idPosto);
        
        // Verifica se conseguiu coletar algo válido (ex: temperatura não nula)
        if (leituraAtual.getTempAtual() == null && leituraAtual.getChuvaPeriodoAtual() == null) {
            return ResponseEntity.noContent().build();
        }
        
        // Salva no histórico do banco
        // Primeiro garantimos que a estação existe no banco para não dar erro de FK
        if (!estacaoRepository.existsById(idPosto)) {
            Estacao nova = new Estacao();
            nova.setIdPosto(idPosto);
            nova.setNome("Estação " + idPosto); // Nome provisório se não tiver sincronizado a lista antes
            estacaoRepository.save(nova);
        }
        
        leituraRepository.save(leituraAtual);
        
        return ResponseEntity.ok(leituraAtual);
    }
}