package com.example.cgesp.Cgesp.controller;

import com.example.cgesp.Cgesp.model.Noticia;
import com.example.cgesp.Cgesp.repository.NoticiaRepository;
import com.example.cgesp.Cgesp.service.NoticiaScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    @Autowired
    private NoticiaScraperService scraperService;

    @Autowired
    private NoticiaRepository repository;

    // Sincroniza as notícias mais recentes (página inicial)
    @PostMapping("/sincronizar")
    public ResponseEntity<List<Noticia>> sincronizarRecentes() {
        List<Noticia> novas = scraperService.coletarNoticias(null);
        return ResponseEntity.ok(repository.saveAll(novas));
    }

    // Busca notícias de uma data específica e salva no banco
    // Ex: /api/noticias/historico?data=2026-01-01
    @GetMapping("/historico")
    public ResponseEntity<List<Noticia>> buscarHistorico(@RequestParam String data) {
        List<Noticia> noticiasHistoricas = scraperService.coletarNoticias(data);
        repository.saveAll(noticiasHistoricas); // Salva para consulta futura
        return ResponseEntity.ok(noticiasHistoricas);
    }

    // Lista todas as notícias já salvas no banco
    @GetMapping
    public List<Noticia> listarTodas() {
        return repository.findAll();
    }

    // Busca uma notícia específica pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Noticia> buscarPorId(@PathVariable Long id) {
        Optional<Noticia> noticia = repository.findById(id);
        return noticia.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
}