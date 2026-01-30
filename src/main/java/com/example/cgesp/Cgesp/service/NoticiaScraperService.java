package com.example.cgesp.Cgesp.service;

import com.example.cgesp.Cgesp.model.Noticia;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class NoticiaScraperService {

    private static final String URL_BASE = "https://www.cgesp.org/v3/noticias.jsp";

    public List<Noticia> coletarNoticias(String dataEspecifica) {
        List<Noticia> noticias = new ArrayList<>();
        String url = URL_BASE;

        // Se passar data (formato yyyy-MM-dd), adiciona o parâmetro
        if (dataEspecifica != null && !dataEspecifica.isEmpty()) {
            url += "?data=" + dataEspecifica;
        }

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            // Seleciona todas as divs de notícia
            Elements divsNoticia = doc.select("div.noticia");

            for (Element div : divsNoticia) {
                Noticia noticia = new Noticia();

                // 1. Extração do Link e ID
                Element linkElement = div.selectFirst("a[href]");
                if (linkElement != null) {
                    String href = linkElement.attr("href"); // ex: noticias.jsp?id=53716
                    noticia.setLinkOriginal("https://www.cgesp.org/v3/" + href);
                    
                    // Extrai apenas o número do ID
                    if (href.contains("id=")) {
                        String idStr = href.split("id=")[1];
                        noticia.setId(Long.parseLong(idStr));
                    }
                    
                    // 2. Título
                    Element h1 = linkElement.selectFirst("h1");
                    if (h1 != null) noticia.setTitulo(h1.text());

                    // 3. Data
                    // Formato original: "01/01/2026 20:46 - Quinta-feira"
                    Element h2 = linkElement.selectFirst("h2");
                    if (h2 != null) {
                        String textoData = h2.text().split(" - ")[0].trim(); // Pega só a data/hora
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        noticia.setDataPublicacao(LocalDateTime.parse(textoData, formatter));
                    }
                }

                // 4. Conteúdo (Agrupar todos os <p>)
                Elements paragrafos = div.select("p");
                StringBuilder conteudoCompleto = new StringBuilder();
                for (Element p : paragrafos) {
                    conteudoCompleto.append(p.text()).append("\n");
                }
                noticia.setConteudo(conteudoCompleto.toString().trim());

                if (noticia.getId() != null) {
                    noticias.add(noticia);
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao coletar notícias: " + e.getMessage());
        }

        return noticias;
    }
}