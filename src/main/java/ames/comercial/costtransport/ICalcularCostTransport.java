package ames.comercial.costtransport;

import ames.comercial.shared.Incoterm;

import java.math.BigDecimal;
import java.util.Optional;

public interface ICalcularCostTransport {

    Optional<BigDecimal> calcula(String client, BigDecimal importTotal, BigDecimal pes, Incoterm incoterm);
    Optional<BigDecimal> calcula(String client, String pais, BigDecimal importTotal, BigDecimal pes, Incoterm incoterm);
    Optional<BigDecimal> calcula(String codiClient, BigDecimal importTotal, BigDecimal pes);

}
