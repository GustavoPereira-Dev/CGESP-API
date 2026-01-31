package com.example.cgesp.Cgesp.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.cgesp.Cgesp.dto.AlagamentoDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlagamentoScraperService {

	private static final String URL_BASE = "https://www.cgesp.org/v3/alagamentos.jsp";

    // Método Existente (Sem parâmetros, pega o dia atual/default)
    public List<AlagamentoDTO> lerDadosAgora() {
        return conectarEExtrair(null);
    }

    // NOVO MÉTODO: Recebe a data formatada (ex: "25/01/2026")
    public List<AlagamentoDTO> lerDadosPorData(String dataFormatada) {
        return conectarEExtrair(dataFormatada);
    }

    // Método privado genérico para evitar repetição de código
    private List<AlagamentoDTO> conectarEExtrair(String dataBusca) {
        List<AlagamentoDTO> lista = new ArrayList<>();

        try {
            // Configuração da conexão
            var conexao = Jsoup.connect(URL_BASE)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000);

            // Se uma data foi passada, adiciona os parâmetros na URL
            // O Jsoup vai montar: ...?dataBusca=25%2F01%2F2026&enviaBusca=Buscar
            if (dataBusca != null && !dataBusca.isEmpty()) {
                conexao.data("dataBusca", dataBusca);
                conexao.data("enviaBusca", "Buscar");
            }

            Document doc = conexao.get();
            
            // ... (O restante da lógica de extração é IDÊNTICA à anterior) ...
            Element container = doc.selectFirst("div.content");
            if (container == null) return lista;

            String zonaAtual = "Indefinida";

            for (Element el : container.children()) {
                if (el.tagName().equals("h1") && el.hasClass("tit-bairros")) {
                    zonaAtual = el.text();
                } else if (el.tagName().equals("table") && el.hasClass("tb-pontos-de-alagamentos")) {
                    processarTabela(el, zonaAtual, lista);
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao conectar no CGE: " + e.getMessage());
        }
        return lista;
    }

    private void processarTabela(Element table, String zona, List<AlagamentoDTO> lista) {
        // ... (Mesma lógica de extração do seu código anterior) ...
        // Vou resumir aqui para não ficar gigante, mas você mantém o código de parsing
        
        String bairro = table.select("td.bairro").text().replace("pts.", "").trim();
        Elements pontos = table.select("div.ponto-de-alagamento");

        for (Element ponto : pontos) {
            AlagamentoDTO dto = new AlagamentoDTO();
            dto.setZona(zona);
            dto.setBairro(bairro);
            
            // Lógica de ícones
            Element icone = ponto.selectFirst("li[title]");
            if (icone != null) dto.setStatus(icone.attr("title"));

            // Lógica de Local e Horário
            Element colLocal = ponto.selectFirst(".col-local");
            if (colLocal != null) {
                String[] partes = colLocal.html().split("<br>");
                if (partes.length > 0) dto.setHorario(Jsoup.parse(partes[0]).text());
                if (partes.length > 1) dto.setLocal(Jsoup.parse(partes[1]).text());
            }
            
            // Lógica de Sentido e Referência
            Elements descricoes = ponto.select(".arial-descr-alag");
            if (descricoes.size() > 1) {
                String[] partesRef = descricoes.get(1).html().split("<br>");
                for (String parte : partesRef) {
                    String t = Jsoup.parse(parte).text();
                    if (t.startsWith("Sentido:")) dto.setSentido(t.replace("Sentido:", "").trim());
                    if (t.startsWith("Referência:")) dto.setReferencia(t.replace("Referência:", "").trim());
                }
            }
            lista.add(dto);
        }
    }
}