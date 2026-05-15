package services;

import java.util.List;
import java.util.Scanner;
import clients.AwesomeApiClient;
import enums.ParMoeda;
import records.Moeda;
import utils.JsonConverter;

public class InteractiveMenu {

    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";

    private final AwesomeApiClient client = new AwesomeApiClient();
    private final JsonConverter conversor = new JsonConverter();
    private final CsvNioManager gerenciador = new CsvNioManager();
    private final CurrencyAnalyzer analisador = new CurrencyAnalyzer();

    public void iniciar() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n======================================");
                System.out.println("  MONITOR DE MOEDAS - AWESOME API");
                System.out.println("======================================");
                System.out.println("[1] Baixar Histórico de Moeda (Exportar CSV)");
                System.out.println("[2] Analisar Métricas do Arquivo Salvo");
                System.out.println(ANSI_RED + "[9] APAGAR TODOS OS DADOS (RESET)" + ANSI_RESET);
                System.out.println("[0] Sair do Sistema");
                System.out.print("Escolha uma opção: ");

                String opcao = scanner.nextLine().trim();
                switch (opcao) {
                    case "1" -> fluxoBaixarDados(scanner);
                    case "2" -> analisarMetricasDinamico(scanner);
                    case "9" -> confirmarExclusao(scanner);
                    case "0" -> {
                        System.out.println("Encerrando o sistema...");
                        return;
                    }
                    default -> System.out.println("Opção inválida, tente novamente.");
                }
            }
        }
    }

    private void fluxoBaixarDados(Scanner scanner) {
        ParMoeda moedaEscolhida = escolherMoeda(scanner);
        if (moedaEscolhida == null) return;

        System.out.print("Quantos dias de histórico deseja baixar? ");
        int dias;
        try {
            dias = Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Valor inválido.");
            return;
        }

        client.buscarHistorico(moedaEscolhida.getCodigoApi(), dias)
                .map(conversor::converterJsonParaLista)
                .ifPresentOrElse(lista -> {
                    try {
                        gerenciador.salvarMoedas(lista);
                        System.out.println("Sucesso! Planilha atualizada.");
                    } catch (Exception e) {
                        System.err.println("Erro ao salvar: " + e.getMessage());
                    }
                }, () -> System.out.println("Falha na conexão."));
    }

    private void analisarMetricasDinamico(Scanner scanner) {
        try {
            List<String> disponiveis = gerenciador.obterMoedasDisponiveis();

            if (disponiveis.isEmpty()) {
                System.out.println("\n[!] O arquivo está vazio ou não existe. Baixe dados primeiro.");
                return;
            }

            System.out.println("\nMoedas encontradas no seu histórico: " + disponiveis);
            System.out.print("Digite o código da moeda para analisar (Ex: USD): ");
            String codigo = scanner.nextLine().trim().toUpperCase();

            if (!disponiveis.contains(codigo)) {
                System.out.println("[!] Esta moeda não consta nos registros salvos.");
                return;
            }

            List<Moeda> historico = gerenciador.lerHistorico();

            analisador.calcularMediaBid(historico, codigo).ifPresent(
                    media -> System.out.printf("\n -> Média de Compra (Bid): R$ %s\n", media));

            analisador.buscarMaiorAlta(historico, codigo).ifPresent(
                    max -> System.out.printf(" -> Maior Alta (High): R$ %s em %s\n", max.high(), max.data()));

            analisador.buscarMenorBaixa(historico, codigo).ifPresent(
                    min -> System.out.printf(" -> Menor Baixa (Low): R$ %s em %s\n", min.low(), min.data()));

        } catch (Exception e) {
            System.err.println("Falha ao abrir arquivo: " + e.getMessage());
        }
    }

    private void confirmarExclusao(Scanner scanner) {
        System.out.print(ANSI_RED + "\n[CUIDADO] Deseja realmente APAGAR o arquivo CSV? (S/N): " + ANSI_RESET);
        String confirma = scanner.nextLine().trim();

        if (confirma.equalsIgnoreCase("S")) {
            try {
                if (gerenciador.apagarArquivo()) {
                    System.out.println("Arquivo de cotações excluído com sucesso.");
                } else {
                    System.out.println("Não havia arquivo para apagar.");
                }
            } catch (Exception e) {
                System.err.println("Erro na exclusão: " + e.getMessage());
            }
        }
    }

    private ParMoeda escolherMoeda(Scanner scanner) {
        System.out.println("\nSelecione a moeda:");
        ParMoeda[] opcoes = ParMoeda.values();
        for (int i = 0; i < opcoes.length; i++) {
            System.out.printf("[%d] %s\n", i + 1, opcoes[i].getNome());
        }
        System.out.print("Escolha: ");
        try {
            int escolha = Integer.parseInt(scanner.nextLine().trim());
            return opcoes[escolha - 1];
        } catch (Exception e) {
            return null;
        }
    }
}