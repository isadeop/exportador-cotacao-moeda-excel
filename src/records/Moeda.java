package records;

import java.math.BigDecimal;

public record Moeda(
        String code,
        String codein,
        String name,
        BigDecimal bid,
        BigDecimal high,
        BigDecimal low,
        String data
) {}