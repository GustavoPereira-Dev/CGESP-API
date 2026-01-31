package com.example.cgesp.Cgesp.service;

import com.example.cgesp.Cgesp.model.Estacao;
import com.example.cgesp.Cgesp.model.LeituraMeteorologica;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EstacaoScraperService {

    private static final String URL_LISTA = "https://www.cgesp.org/v3/estacoes-meteorologicas.jsp";
    private static final String URL_DETALHE_BASE = "https://www.cgesp.org/v3/estacao.jsp?POSTO=";

    // 1. Coleta a lista de estações disponíveis (Nome e ID)
    public List<Estacao> listarEstacoesDisponiveis() {
        List<Estacao> lista = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL_LISTA).get();
            // Seleciona os links dentro da lista de estações
            Elements links = doc.select("#lista-estacoes li a");

            for (Element link : links) {
                Estacao estacao = new Estacao();
                estacao.setNome(link.text());
                
                // Extrai o ID do href (ex: estacao.jsp?POSTO=1000635)
                String href = link.attr("href");
                if (href.contains("POSTO=")) {
                    String idStr = href.split("POSTO=")[1];
                    estacao.setIdPosto(Long.parseLong(idStr));
                    lista.add(estacao);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // 2. Coleta os dados ATUAIS de uma estação específica
    public LeituraMeteorologica coletarDadosAtuais(Long idPosto) {
        LeituraMeteorologica leitura = new LeituraMeteorologica();
        String url = URL_DETALHE_BASE + idPosto;

        try {
            Document doc = Jsoup.connect(url).get();
            
            // Configura a estação na leitura (apenas ID para referência)
            Estacao estacaoRef = new Estacao();
            estacaoRef.setIdPosto(idPosto);
            leitura.setEstacao(estacaoRef);
            leitura.setDataLeitura(LocalDateTime.now());

            // A estrutura interna do iframe é baseada em tabelas/colunas. 
            // Como não temos o HTML exato do iframe no prompt, assumiremos seletores 
            // baseados em textos comuns de tabelas meteorológicas ou posições relativas.
            // *Estratégia:* Procurar pelos rótulos e pegar o valor próximo.
            
            // Exemplo fictício de parsing baseado no texto visual da imagem:
            // "Temperatura Atual: 18.1 °C"
            
            Elements conteudos = doc.getElementsContainingOwnText("Atual:");
            
            for (Element el : conteudos) {
                String texto = el.text(); // ex: "Atual: 18.1 ºC"
                String valorStr = texto.replace("Atual:", "").replace("ºC", "").replace("mm", "").replace("%", "").replace("hPa", "").trim();
                
                // Tenta identificar qual métrica é baseada no elemento pai ou coluna anterior
                // Nota: Em produção, inspecione o HTML do iframe para usar seletores CSS precisos (ex: td.valor-temp)
                
                if (verificarContexto(el, "Temperatura")) {
                    leitura.setTempAtual(parseVariavel(valorStr));
                } else if (verificarContexto(el, "Umidade")) {
                    leitura.setUmidAtual(parseVariavel(valorStr));
                } else if (verificarContexto(el, "Pressão") || verificarContexto(el, "Pressao")) {
                    leitura.setPressaoAtual(parseVariavel(valorStr));
                } else if (verificarContexto(el, "Chuva")) {
                    leitura.setChuvaPeriodoAtual(parseVariavel(valorStr));
                }
            }
            
            // Tratamento especial para Vento (Geralmente tem Direção e Velocidade)
            // Lógica similar seria aplicada aqui

        } catch (Exception e) {
            System.err.println("Erro ao ler estação " + idPosto + ": " + e.getMessage());
        }
        return leitura;
    }
    
    // Auxiliar para converter String "18.1" em Double
    private Double parseVariavel(String valor) {
        try {
            return Double.parseDouble(valor.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // Auxiliar para checar se o elemento está perto de um cabeçalho (ex: coluna "Temperatura")
    private boolean verificarContexto(Element el, String palavraChave) {
        // Verifica nos pais ou irmãos anteriores se a palavra chave existe
        // Isso depende muito da estrutura da tabela HTML do CGE
        Element pai = el.parent();
        while (pai != null) {
            if (pai.text().contains(palavraChave)) return true;
            pai = pai.parent();
            if (pai != null && pai.tagName().equals("body")) break;
        }
        return false;
    }
}