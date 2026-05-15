package utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import records.Moeda;

public class JsonConverter {

    public List<Moeda> converterJsonParaLista(String json) {
        List<Moeda> moedas = new ArrayList<>();

        // Como os dados chegam do endpoint em formato Json, é necessário tratar/quebrar os blocos
        // Captura o bloco interno de cada moeda ex: "USDBRL": { ... }
        Pattern blocoPattern = Pattern.compile("\"\\w{6}\"\\s*:\\s*\\{([^}]+)\\}");
        Matcher blocoMatcher = blocoPattern.matcher(json);

        while (blocoMatcher.find()) {
            String conteudoBloco = blocoMatcher.group(1);

            String code = extrairCampo(conteudoBloco, "code");
            String codein = extrairCampo(conteudoBloco, "codein");
            String name = extrairCampo(conteudoBloco, "name");
            String bidStr = extrairCampo(conteudoBloco, "bid");
            String highStr = extrairCampo(conteudoBloco, "high");
            String lowStr = extrairCampo(conteudoBloco, "low");

            if (code != null && bidStr != null) {
                moedas.add(new Moeda(
                                    code,
                                    codein,
                                    name,
                                    new BigDecimal(bidStr),
                                    new BigDecimal(highStr),
                                    new BigDecimal(lowStr)
                ));
            }
        }
        return moedas;
    }

    public Moeda converterCsvParaMoeda(String linhaCsv) {
        String[] colunas = linhaCsv.split(",");
        if (colunas.length < 6) {
            throw new IllegalArgumentException("Linha do CSV inválida.");
        }
        return new Moeda(
                colunas[0].trim(),
                colunas[1].trim(),
                colunas[2].trim(),
                new BigDecimal(colunas[3].trim()),
                new BigDecimal(colunas[4].trim()),
                new BigDecimal(colunas[5].trim())
        );
    }

    private String extrairCampo(String texto, String campo) {
        Pattern p = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(texto);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}