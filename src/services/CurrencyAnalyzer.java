package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import records.Moeda;

public class CurrencyAnalyzer {

    public Optional<BigDecimal> calcularMediaBid(List<Moeda> historico, String codigoMoeda) {
        List<Moeda> filtradas = historico.stream()
                .filter(m -> m.code().equalsIgnoreCase(codigoMoeda))
                .toList();

        if (filtradas.isEmpty()) return Optional.empty();

        BigDecimal soma = filtradas.stream()
                .map(Moeda::bid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Optional.of(soma.divide(BigDecimal.valueOf(filtradas.size()), 4, RoundingMode.HALF_UP));
    }

    public Optional<Moeda> buscarMaiorAlta(List<Moeda> historico, String codigoMoeda) {
        return historico.stream()
                .filter(m -> m.code().equalsIgnoreCase(codigoMoeda))
                .max(Comparator.comparing(Moeda::high));
    }

    public Optional<Moeda> buscarMenorBaixa(List<Moeda> historico, String codigoMoeda) {
        return historico.stream()
                .filter(m -> m.code().equalsIgnoreCase(codigoMoeda))
                .min(Comparator.comparing(Moeda::low));
    }
}