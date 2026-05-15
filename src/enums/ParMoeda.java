package enums;

public enum ParMoeda {
    USD_BRL("Dólar Americano", "USD-BRL"),
    EUR_BRL("Euro", "EUR-BRL"),
    BTC_BRL("Bitcoin", "BTC-BRL"),
    GBP_BRL("Libra Esterlina", "GBP-BRL");

    private final String nome;
    private final String codigoApi;

    ParMoeda(String nome, String codigoApi) {
        this.nome = nome;
        this.codigoApi = codigoApi;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigoApi() {
        return codigoApi;
    }
}