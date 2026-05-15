package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import records.Moeda;
import utils.JsonConverter;

public class CsvNioManager {
    private static final Path ARQUIVO = Path.of("dados", "cotacoes.csv");
    private final JsonConverter conversor = new JsonConverter();

    public void salvarMoedas(List<Moeda> moedas) throws IOException {
        Files.createDirectories(ARQUIVO.getParent());

        boolean arquivoExiste = Files.exists(ARQUIVO);
        List<String> linhas = new ArrayList<>();

        if (!arquivoExiste) {
            linhas.add("Codigo,Alvo,Nome,Compra (Bid),Maximo (High),Minimo (Low),Data");
        }

        moedas.stream()
                .map(m -> String.format("%s,%s,%s,%s,%s,%s,%s",
                        m.code(), m.codein(), m.name(), m.bid(), m.high(), m.low(), m.data()))
                .forEach(linhas::add);

        Files.write(ARQUIVO, linhas, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public List<Moeda> lerHistorico() throws IOException {
        if (!Files.exists(ARQUIVO)) {
            return List.of();
        }

        try (Stream<String> linhas = Files.lines(ARQUIVO)) {
            return linhas.skip(1)
                    .map(conversor::converterCsvParaMoeda)
                    .toList();
        }
    }

    public boolean apagarArquivo() throws IOException {
        return Files.deleteIfExists(ARQUIVO);
    }

    public List<String> obterMoedasDisponiveis() throws IOException {
        if (!Files.exists(ARQUIVO)) return List.of();

        try (Stream<String> linhas = Files.lines(ARQUIVO)) {
            return linhas.skip(1)
                    .map(linha -> linha.split(",")[0])
                    .distinct()
                    .collect(Collectors.toList());
        }
    }
}