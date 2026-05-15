package services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import records.Moeda;
import utils.JsonConverter;

public class CsvNioManager {
    private static final Path ARQUIVO = Path.of("dados", "cotacoes.csv");
    private final JsonConverter conversor = new JsonConverter();

    public void salvarMoedas(List<Moeda> moedas) throws IOException {
        // Garante a existência dos diretórios para a criação do arquivo
        Files.createDirectories(ARQUIVO.getParent());

        boolean arquivoExiste = Files.exists(ARQUIVO);
        List<String> linhas = new ArrayList<>();

        // Se o arquivo é novo, cria o cabeçalho
        if (!arquivoExiste) {
            linhas.add("Codigo,Alvo,Nome,Compra (Bid),Maximo (High),Minimo (Low)");
        }

        // Transforma o Record em linha CSV
        moedas.stream()
                .map(m -> String.format("%s,%s,%s,%s,%s,%s",
                        m.code(), m.codein(), m.name(), m.bid(), m.high(), m.low()))
                .forEach(linhas::add);

        //Cria de fato a planilha
        Files.write(ARQUIVO, linhas, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    //Lê o arquivo já existente no diretório para retornar as informações
    public List<Moeda> lerHistorico() throws IOException {
        if (!Files.exists(ARQUIVO)) {
            return List.of();
        }

        try (Stream<String> linhas = Files.lines(ARQUIVO)) {
            return linhas.skip(1) // Ignora o cabeçalho
                    .map(conversor::converterCsvParaMoeda)
                    .toList();
        }
    }
}