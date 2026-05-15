package utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import records.Moeda;

public class JsonConverter {

    public List<Moeda> converterJsonParaLista(String json) {
        List<Moeda> moedas = new ArrayList<>();

        Pattern blocoPattern = Pattern.compile("\\{([^{}]+)\\}");
        Matcher blocoMatcher = blocoPattern.matcher(json);

        String lastCode = "N/A";
        String lastCodein = "N/A";
        String lastName = "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        while (blocoMatcher.find()) {
            String conteudoBloco = blocoMatcher.group(1);

            String code = extrairCampo(conteudoBloco, "code");
            if (code != null) lastCode = code;

            String codein = extrairCampo(conteudoBloco, "codein");
            if (codein != null) lastCodein = codein;

            String name = extrairCampo(conteudoBloco, "name");
            if (name != null) lastName = name;

            String bidStr = extrairCampo(conteudoBloco, "bid");
            String highStr = extrairCampo(conteudoBloco, "high");
            String lowStr = extrairCampo(conteudoBloco, "low");

            String dateStr = "Sem Data";
            String timestampStr = extrairCampo(conteudoBloco, "timestamp");

            if (timestampStr != null) {
                try {
                    long segundos = Long.parseLong(timestampStr);
                    dateStr = Instant.ofEpochSecond(segundos)
                            .atZone(ZoneId.systemDefault())
                            .format(formatter);
                } catch (Exception e) {
                    dateStr = "Data Inválida";
                }
            }

            if (bidStr != null && highStr != null && lowStr != null) {
                moedas.add(new Moeda(
                        lastCode,
                        lastCodein,
                        lastName,
                        new BigDecimal(bidStr),
                        new BigDecimal(highStr),
                        new BigDecimal(lowStr),
                        dateStr
                ));
            }
        }
        return moedas;
    }

    public Moeda converterCsvParaMoeda(String linhaCsv) {
        String[] colunas = linhaCsv.split(",");
        if (colunas.length < 6) {
            throw new IllegalArgumentException("Linha do CSV inválida ou vazia: " + linhaCsv);
        }

        String data = colunas.length >= 7 ? colunas[6].trim() : "Sem Data";

        return new Moeda(
                colunas[0].trim(),
                colunas[1].trim(),
                colunas[2].trim(),
                new BigDecimal(colunas[3].trim()),
                new BigDecimal(colunas[4].trim()),
                new BigDecimal(colunas[5].trim()),
                data
        );
    }

    private String extrairCampo(String texto, String campo) {
        Pattern p = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher m = p.matcher(texto);
        if (m.find()) {
            return m.group(1).trim();
        }
        return null;
    }
}