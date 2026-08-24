package ames.comercial.costtransport.internal;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirCostTransportAds;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.costtransport.ICalcularCostTransport;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static ames.comercial.shared.Numbers.isHighEq;

@Component
public class CalcularCostTransport implements ICalcularCostTransport {

    IObtenirClientAds obtenirClientAds;
    IObtenirCostTransportAds obtenirCostTransportAds;

    public CalcularCostTransport (IObtenirClientAds obtenirClientAds,
                                  IObtenirCostTransportAds obtenirCostTransportAds) {
        this.obtenirClientAds = obtenirClientAds;
        this.obtenirCostTransportAds = obtenirCostTransportAds;
    }

    @Override
    public Optional<BigDecimal> calcula(String client, BigDecimal importTotal, BigDecimal pes, Incoterm incoterm) {
        return calcula(client, Optional.empty(), importTotal,pes,incoterm);
    }

    @Override
    public Optional<BigDecimal> calcula(String client, String pais, BigDecimal importTotal, BigDecimal pes, Incoterm incoterm) {
        return calcula(client, Optional.ofNullable(pais), importTotal,pes,incoterm);
    }

    @Override
    public Optional<BigDecimal> calcula(String codiClient, BigDecimal importTotal, BigDecimal pes) {
        var client = new ObtenirClientAds().get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
        return calcula(client.clicod(), Optional.empty(), importTotal, pes, client.incoterm());
    }

    public Optional<BigDecimal> calcula(String client, Optional<String> pais, BigDecimal importTotal, BigDecimal pes, Incoterm incoterm) {
        // En cas que l'enviament sigui EXW no aplica
        if (Incoterm.EXW.equals(incoterm))
            return Optional.empty();
        // Obtenció del client
        var cliAds = obtenirClientAds.get(client).orElseThrow(() -> new ClientNoExisteix(client));
        // Només aplica el cost de transport si el client té marcat que s'han d'aplicar els costos de transport
        if (!cliAds.isAplicaCostTransport())
            return Optional.empty();
        // Només s'aplica cost de transport als distribuidors de coixinets i als clients de filtres (branca professional 1500)
        if (!(cliAds.isDistribuidor() || cliAds.isClientFiltres()))
            return Optional.empty();
        // Si l'import és superior a 1.000 no hi ha cost
        if (Numbers.isHigh(importTotal).than(1_000))
            return Optional.empty();
        // Normalització del codi de país per obtenció del rang (en cas que no s'informi expressament s'agafa el país del client)
        var codiPais = pais.orElse(cliAds.pais());
        var codiPaisNorm = normalitzarCodiPais(codiPais);
        // Obtenció del rang de cost de transport
        var optRang = obtenirCostTransportAds.get(codiPaisNorm);
        // En cas que el pais no tingui el rang definit es torna buit
        if (optRang.isEmpty())
            return Optional.empty();
        // Obtenció del valor (si es 9999 vol dir que cal demanar preu al transportista i es torna buit)
        var rang = optRang.get();
        var cost = rang.get(pes);
        return isHighEq(cost).than(9_999) ? Optional.empty() : Optional.ofNullable(cost);
    }

    private String normalitzarCodiPais(String pais) {
        // El 0000 és Bcn, el 0001 és Tarragona, el 0002 és Lleida i el 0003 Girona
        // En tots aquests casos es deixa definit a 0000 que representa Catalunya en els rangs de transport
        if (Integer.parseInt(pais) <= 3)
            return "0000";
        // Del 0004 al 0099 son les provincies d'Espanya
        // En tots aquests casos es deixa definit a 0099 que representa Espanya en els rangs de transport
        if (Integer.parseInt(pais) <= 99)
            return "0099";
        return pais;
    }

}
