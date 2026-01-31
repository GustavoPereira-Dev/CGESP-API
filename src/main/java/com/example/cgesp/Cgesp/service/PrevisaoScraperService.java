package com.example.cgesp.Cgesp.service;

import com.example.cgesp.Cgesp.model.PrevisaoDia;
import com.example.cgesp.Cgesp.model.PrevisaoPeriodo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrevisaoScraperService {

    private static final String URL_PREVISAO = "https://www.cgesp.org/v3/previsao_estendida.jsp";

    public List<PrevisaoDia> coletarPrevisao() {
        List<PrevisaoDia> previsoes = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(URL_PREVISAO).get();
            // Cada dia está dentro de uma div com classe 'col-previsao'
            // Nota: O HTML usa 'col-previsao-simples' como wrapper e 'col-previsao' dentro.
            Elements colunas = doc.select("div.col-previsao");

            for (Element col : colunas) {
                PrevisaoDia dia = new PrevisaoDia();

                // 1. Data e Dia da Semana
                Element divData = col.selectFirst(".data-prev");
                Element divDiaSemana = col.selectFirst(".dia-semana");
                
                if (divData != null && divDiaSemana != null) {
                    dia.setDiaSemana(divDiaSemana.text().replace("Qui", "Quinta")
                            .replace("Sex", "Sexta").replace("Sáb", "Sábado").replace("Dom", "Domingo")
                            .replace("Seg", "Segunda").replace("Ter", "Terça").replace("Qua", "Quarta"));

                    // O texto vem algo como "29/01 2026". Juntamos para parsear.
                    String dataTexto = divData.text().trim(); // "29/01 2026"
                    // Remove quebras de linha extras se houver e formata
                    String[] partesData = dataTexto.split(" ");
                    if (partesData.length >= 2) {
                        String dataCompleta = partesData[0] + "/" + partesData[1]; // 29/01/2026
                        dia.setDataPrevisao(LocalDate.parse(dataCompleta, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                }

                // 2. Temperaturas (Removemos o símbolo °)
                String minTemp = col.select(".col-temp-min .temp-min").text().replaceAll("[^0-9]", "");
                String maxTemp = col.select(".col-temp-max .temp-max").text().replaceAll("[^0-9]", "");
                if (!minTemp.isEmpty()) dia.setTempMin(Integer.parseInt(minTemp));
                if (!maxTemp.isEmpty()) dia.setTempMax(Integer.parseInt(maxTemp));

                // 3. Umidade (Removemos o símbolo %)
                String minUmid = col.select(".col-umid-min .umid-min").text().replaceAll("[^0-9]", "");
                String maxUmid = col.select(".col-umid-max .umid-max").text().replaceAll("[^0-9]", "");
                if (!minUmid.isEmpty()) dia.setUmidMin(Integer.parseInt(minUmid));
                if (!maxUmid.isEmpty()) dia.setUmidMax(Integer.parseInt(maxUmid));

                // 4. Períodos (Madrugada, Manhã, Tarde, Noite)
                processarPeriodo(col, ".prev-madrug", "Madrugada", dia);
                processarPeriodo(col, ".prev-manha", "Manhã", dia);
                processarPeriodo(col, ".prev-tarde", "Tarde", dia);
                processarPeriodo(col, ".prev-noite", "Noite", dia);

                previsoes.add(dia);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return previsoes;
    }

    private void processarPeriodo(Element containerPai, String classeCss, String nomePeriodo, PrevisaoDia dia) {
        Element divPeriodo = containerPai.selectFirst(classeCss);
        if (divPeriodo != null) {
            PrevisaoPeriodo periodo = new PrevisaoPeriodo();
            periodo.setNomePeriodo(nomePeriodo);

            // Ícone
            Element img = divPeriodo.selectFirst("img");
            if (img != null) periodo.setIconeUrl(img.attr("src"));

            // Condição e Potencial (Dentro de .cond-tempo h2)
            Elements h2s = divPeriodo.select(".cond-tempo h2");
            if (h2s.size() >= 1) {
                periodo.setCondicao(h2s.get(0).text());
            }
            if (h2s.size() >= 2) {
                // Texto vem como "PT: Baixo" -> Queremos só "Baixo"
                String ptTexto = h2s.get(1).text();
                periodo.setPotencialTempestade(ptTexto.replace("PT:", "").trim());
            }

            dia.adicionarPeriodo(periodo);
        }
    }
}