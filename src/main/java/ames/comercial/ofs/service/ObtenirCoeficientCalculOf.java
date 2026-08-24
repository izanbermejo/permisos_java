package ames.comercial.ofs.service;

import ames.comercial.shared.Numbers;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ObtenirCoeficientCalculOf {

    public BigDecimal executar() {
        return Numbers.decimal(2);
    }

}
