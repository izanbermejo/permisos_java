package ames.comercial.comandes.service;

import ames.comercial.comandes.service.response.LiniaNormalitzatResp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculadoraUtils {

    public static BigDecimal pesKg (List<LiniaNormalitzatResp> linies) {
        return linies.stream()
                .map(LiniaNormalitzatResp::pes)
                .reduce(BigDecimal::add).orElseGet(() -> BigDecimal.ZERO)
                .divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP);
    }

}
