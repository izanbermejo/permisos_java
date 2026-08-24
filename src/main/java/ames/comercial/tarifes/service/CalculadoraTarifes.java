package ames.comercial.tarifes.service;

import ames.comercial.advantage.IObtenirTipusArticleClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.calculadorapreus.service.CalculDescompteFamilia;
import ames.comercial.comandes.service.*;
import ames.comercial.shared.*;
import ames.comercial.tarifes.TarifesException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@Component
public class CalculadoraTarifes {

    IProviderPreu providerPreus;
    IProviderTarifaActual obtenirTarifaActual;
    IProviderFamilies providerFamilies;
    IObtenirTipusArticleClientAds obtenirTipusArticleClient;

    public CalculadoraTarifes(IProviderPreu providerPreus, IProviderTarifaActual obtenirTarifaActual, IProviderFamilies providerFamilies,
                              IObtenirTipusArticleClientAds obtenirTipusArticleClient) {
        this.providerPreus = providerPreus;
        this.obtenirTarifaActual = obtenirTarifaActual;
        this.providerFamilies = providerFamilies;
        this.obtenirTipusArticleClient = obtenirTipusArticleClient;
    }

    public List<RangPreuResp> calcul (String artint, String codiClient) {
        // Obtenció del client
        var client = new ObtenirClientAds().get(codiClient).orElseThrow(() -> new SharedExceptions.ClientNoExisteix(codiClient));
        // Obtenció de les seves tarifes actuals
        var tarifesActualClient = obtenirTarifaActual.executar(codiClient);
        // Construcció del conjunt (únicament amb l'article a consultar)
        var articleClient = KeyArticleClient.ofNormalitzat(artint);

        var reqPreus = ProviderPreuRequestImpl.builder()
            .articles(Set.of(articleClient))
            .tarifaCoixinets(tarifesActualClient.coixinets())
            .tarifaBarres(tarifesActualClient.barres())
            .tarifaIbinsa(tarifesActualClient.ibinsa())
            .tarifaMedical(tarifesActualClient.medical())
            .tarifaFiltresBxx(tarifesActualClient.filtresBxx())
            .tarifaFiltresSsu(tarifesActualClient.filtresSsu())
            .tarifaFiltresSxx(tarifesActualClient.filtresSxx())
            .tarifaFiltresSsuPlaques(tarifesActualClient.filtresSsuPlaques())
            .factorAplicar(codiClient.equals("151301") ? new BigDecimal("0.6") : BigDecimal.ONE)  // Monterrey s'aplica un descompte de la tarifa d'aquí
            .build();

        var preus = providerPreus.provide(reqPreus);
        var rang = preus.rang(articleClient).orElseThrow(TarifesException.NoExisteixTarifa::new);
        var families = providerFamilies.provide(Set.of(articleClient));
        var mapTipusPesa = obtenirTipusArticleClient.get(Set.of(articleClient));

//        Calcul del descompte que te la peça segons el tipus y el client
        var calcDte = new CalculDescompteFamilia(families, mapTipusPesa, client.dtoCoixBronze(), client.dtoCoixFerro(), client.dtoFiltres());

        List<RangPreuResp> llistaRangs = new ArrayList<>();

        for (var entry : rang.asMapOfRanges().entrySet()) {
            var tram = entry.getKey().toString();
            var valor = entry.getValue();
            var descompte = calcDte.descompte(articleClient);

            var rangPreus = RangPreuRespImpl.builder()
                    .tram(tram)
                    .preuBrut(valor)
                    .descompte(descompte)
                    .build();

            llistaRangs.add(rangPreus);
        }

        return llistaRangs;
    }

//  immutable per crear un objecte que contingui el tram y el preu en brut i en net de les peçes
    @JsonDeserialize(builder = RangPreuRespImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RangPreuResp {

        String tram();
        Preu preuBrut();
        BigDecimal descompte();

//      Propietat derivada que calcula els preus en net utilitzant el preu brut i el descompte de la peça
        @Derived
        default Preu preuNet() {
            return Preu.of(preuBrut().valor()
                            .multiply(descompteAplicar(descompte()))
                            .divide(decimal(100), 3, RoundingMode.HALF_UP),
                    preuBrut().divisa());
        }

    }

}
