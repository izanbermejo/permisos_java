package ames.comercial.calculadorapreus.service;

import ames.comercial.comandes.service.Familia;
import ames.comercial.comandes.service.ProviderFamilies.IProviderFamiliesResponse;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusArticleClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public class CalculDescompteFamilia {

    IProviderFamiliesResponse families;
    Map<KeyArticleClient, TipusArticleClient> mapTipusPesa;
    BigDecimal dteCoixinetBronze;
    BigDecimal dteCoixinetFerro;
    BigDecimal dteFiltres;

    public CalculDescompteFamilia(IProviderFamiliesResponse families, Map<KeyArticleClient, TipusArticleClient> mapTipusPesa, BigDecimal dteCoixinetBronze,
                                  BigDecimal dteCoixinetFerro, BigDecimal dteFiltres) {
        this.families = families;
        this.mapTipusPesa = mapTipusPesa;
        this.dteCoixinetBronze = dteCoixinetBronze;
        this.dteCoixinetFerro = dteCoixinetFerro;
        this.dteFiltres = dteFiltres;
    }

    public BigDecimal descompte(KeyArticleClient articleClient) {
        // Únicament s'aplica descompte en el cas de normalitzats
        TipusArticleClient client = mapTipusPesa.get(articleClient);
        boolean aplicaDescompte = Optional.ofNullable(client)
                .map(TipusArticleClient::isAplicaDescompte)
                .orElse(false);
        if (!aplicaDescompte)
            return BigDecimal.ZERO;

        var optFamilia = families.familia(articleClient.artint());
        if (optFamilia.isEmpty())
            return BigDecimal.ZERO;
        // En cas que no tingui familia es retorna 0 de descompte
        var familia = optFamilia.get();
        if (isFamiliaCoixinetBronze(familia))
            return dteCoixinetBronze;
        if (isFamiliaCoixinetFerro(familia))
            return dteCoixinetFerro;
        if (isFamiliaFiltres(familia))
            return dteFiltres;
        return BigDecimal.ZERO;
    }

    private boolean isFamiliaCoixinetBronze (Familia f){
        return f.equals(Familia.A)
                || f.equals(Familia.p)
                || f.equals(Familia.b)
                || f.equals(Familia.g);
    }

    private boolean isFamiliaFiltres (Familia f) {
        return f.equals(Familia.J);
    }

    private boolean isFamiliaCoixinetFerro (Familia f) {
        return f.equals(Familia.a);
    }

}
