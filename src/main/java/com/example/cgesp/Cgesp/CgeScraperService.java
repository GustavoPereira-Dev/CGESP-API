package com.example.cgesp.Cgesp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CgeScraperService {

    private static final String URL_CGE = "https://www.cgesp.org/v3/alagamentos.jsp?dataBusca=16%2F01%2F2026&enviaBusca=Buscar";

    public List<Alagamento> coletarDados() throws IOException {
        List<Alagamento> listaAlagamentos = new ArrayList<>();
        
        // 1. Conectar e baixar o HTML
        Document doc = Jsoup.connect(URL_CGE).get();

        System.out.println(doc.toString());
        System.out.println("AAAAAAAA");
        // 2. Selecionar a área de conteúdo principal
        Element contentDiv = doc.selectFirst("div.content");
        if (contentDiv == null) return listaAlagamentos;

        String zonaAtual = "Desconhecida";
        
        // Iterar sobre os elementos filhos diretos para manter a ordem (Zona -> Tabelas)
        for (Element element : contentDiv.children()) {
            
            // Captura a Zona (Ex: Zona Norte)
            if (element.tagName().equals("h1") && element.hasClass("tit-bairros")) {
                zonaAtual = element.text().trim();
            }
            
            // Captura as tabelas de alagamento
            else if (element.tagName().equals("table") && element.hasClass("tb-pontos-de-alagamentos")) {
                processarTabela(element, zonaAtual, listaAlagamentos);
            }
        }
        
        return listaAlagamentos;
    }

    private void processarTabela(Element table, String zona, List<Alagamento> lista) {
        // O bairro geralmente está na primeira linha (tr) dentro de um td com class 'bairro'
        String bairro = table.select("td.bairro").text().replace("pts.", "").trim();
        
        // Selecionar todos os pontos de alagamento dentro desta tabela
        Elements pontos = table.select("div.ponto-de-alagamento");

        for (Element pontoDiv : pontos) {
            Alagamento alagamento = new Alagamento();
            alagamento.setZona(zona);
            alagamento.setBairro(bairro);

            // 1. Identificar Status (Ícones)
            // As classes costumam ser 'ativo-transitavel', 'inativo-intransitavel', etc.
            Element icone = pontoDiv.selectFirst("li[title]");
            if (icone != null) {
                String classe = icone.className();
                alagamento.setAtivo(classe.contains("ativo") && !classe.contains("inativo"));
                alagamento.setTransitavel(classe.contains("transitavel") && !classe.contains("intransitavel"));
            }

            // 2. Local e Horário (Separados por <br>)
            // Ex: "De 17:42 a 20:28 <br> AV JULES RIMET"
            Element colLocal = pontoDiv.selectFirst(".col-local");
            if (colLocal != null) {
                String htmlLocal = colLocal.html();
                String[] partesLocal = htmlLocal.split("<br>");
                
                if (partesLocal.length >= 2) {
                    parseHorarios(partesLocal[0], alagamento);
                    alagamento.setLogradouro(Jsoup.parse(partesLocal[1]).text().trim());
                } else {
                    alagamento.setLogradouro(colLocal.text());
                }
            }

            // 3. Sentido e Referência
            // Ex: "Sentido: UNICO <br> Referência: AV PDE LEBRET"
            // Nota: O seletor .arial-descr-alag aparece duas vezes, precisamos pegar o segundo (o primeiro é o local)
            Elements descricoes = pontoDiv.select(".arial-descr-alag");
            if (descricoes.size() > 1) {
                Element infoExtra = descricoes.get(1); // O segundo elemento tem o sentido/ref
                String htmlInfo = infoExtra.html();
                String[] partesInfo = htmlInfo.split("<br>");

                for (String parte : partesInfo) {
                    String texto = Jsoup.parse(parte).text().trim();
                    if (texto.startsWith("Sentido:")) {
                        alagamento.setSentido(texto.replace("Sentido:", "").trim());
                    } else if (texto.startsWith("Referência:")) {
                        alagamento.setReferencia(texto.replace("Referência:", "").trim());
                    }
                }
            }
            
            lista.add(alagamento);
        }
    }

    private void parseHorarios(String textoHorario, Alagamento alagamento) {
        // Exemplo de texto: "De 17:42 a 20:28" ou "De 19:36 a"
        try {
            LocalDate hoje = LocalDate.now(); // Assumindo que o dado é de hoje
            
            // Regex simples ou split para pegar as horas
            String limpo = textoHorario.replace("De", "").trim();
            String[] tempos = limpo.split(" a ");
            
            if (tempos.length > 0 && !tempos[0].isEmpty()) {
                LocalTime inicio = LocalTime.parse(tempos[0].trim(), DateTimeFormatter.ofPattern("HH:mm"));
                alagamento.setDataHoraInicio(LocalDateTime.of(hoje, inicio));
            }
            
            if (tempos.length > 1 && !tempos[1].isEmpty()) {
                LocalTime fim = LocalTime.parse(tempos[1].trim(), DateTimeFormatter.ofPattern("HH:mm"));
                alagamento.setDataHoraFim(LocalDateTime.of(hoje, fim));
            }
        } catch (Exception e) {
            System.err.println("Erro ao converter horário: " + textoHorario);
        }
    }
}