package services;

import java.util.List;
import java.util.Scanner;
import client.AwesomeApiClient;
import records.Moeda;
import utils.JsonConverter;

public class InteractiveMenu {
    private final AwesomeApiClient client = new AwesomeApiClient();
    private final JsonConverter conversor = new JsonConverter();
    private final CsvNioManager gerenciador = new CsvNioManager();
    private final CurrencyAnalyzer analisador = new CurrencyAnalyzer();

    public void iniciar() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== GERENCIADOR DE COTAÇÕES NATIVO ===");
                System.out.println("1. Buscar Cotações Atuais e Exportar p/ Excel (CSV)");
                System.out.println("2. Analisar Métricas do Histórico");
                System.out.println("3. Sair");
                System.out.print("Escolha uma opção: ");

                String opcao = scanner.nextLine().trim();
                switch (opcao) {
                    case "1" -> buscarESalvar(scanner);
                    case "2" -> analisarMetricas(scanner);
                    case "3" -> {
                        System.out.println("Encerrando o sistema...");
                        return;
                    }
                    default -> System.out.println("Opção inválida, tente novamente.");
                }
            }
        }
    }

    private void buscarESalvar(Scanner scanner) {
        System.out.print("Digite as moedas separadas por vírgula (Ex: USD-BRL,EUR-BRL,BTC-BRL): ");
        String moedas = scanner.nextLine().trim();

        // Pipeline Funcional encadeado com flatMap e tratamentos robustos
        client.buscarCotacoes(moedas)
                .map(conversor::converterJsonParaLista)
                .ifPresentOrElse(lista -> {
                    try {
                        if (lista.isEmpty()) {
                            System.out.println("Nenhuma cotação válida encontrada para a entrada digitada.");
                            return;
                        }
                        gerenciador.salvarMoedas(lista);
                        System.out.println("Planilha atualizada com sucesso em 'dados/cotacoes.csv'!");
                        lista.forEach(m -> System.out.printf(" -> %s (%s): Atual: %s\n", m.name(), m.code(), m.bid()));
                    } catch (Exception e) {
                        System.err.println("Erro ao persistir os dados: " + e.getMessage());
                    }
                }, () -> System.out.println("Erro na requisição. Verifique a conexão ou os parâmetros."));
    }

    private void analisarMetricas(Scanner scanner) {
        System.out.print("Informe a sigla da moeda que deseja analisar (Ex: USD, EUR): ");
        String codigo = scanner.nextLine().trim().toUpperCase();

        try {
            List<Moeda> historico = gerenciador.lerHistorico();

            analisador.calcularMediaBid(historico, codigo).ifPresentOrElse(
                    media -> System.out.printf(" Média de Fechamento (Bid): %s\n", media),
                    () -> System.out.println("Não há registros suficientes para esta moeda no arquivo.")
            );

            analisador.buscarMaiorAlta(historico, codigo).ifPresent(
                    max -> System.out.printf(" Pico Máximo Registrado (High): %s\n", max.high())
            );

            analisador.buscarMenorBaixa(historico, codigo).ifPresent(
                    min -> System.out.printf(" Pico Mínimo Registrado (Low): %s\n", min.low())
            );

        } catch (Exception e) {
            System.err.println("Falha ao abrir a planilha para análise: " + e.getMessage());
        }
    }
}