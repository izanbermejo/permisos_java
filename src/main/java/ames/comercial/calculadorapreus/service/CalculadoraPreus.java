package ames.comercial.calculadorapreus.service;

import ames.comercial.calculadorapreus.request.CalculadoraPreusReq;
import ames.comercial.calculadorapreus.request.CalculadoraPreusReq.LiniaCalculPreuReq;
import ames.comercial.calculadorapreus.response.CalculadoraPreusResp;
import ames.comercial.calculadorapreus.response.CalculadoraPreusResp.LiniaCalculPreuResp;
import ames.comercial.calculadorapreus.response.CalculadoraPreusRespImpl;
import ames.comercial.calculadorapreus.response.LiniaCalculPreuRespImpl;
import ames.comercial.comandes.service.IProviderFamilies;
import ames.comercial.comandes.service.IProviderPreu;
import ames.comercial.comandes.service.ProviderPreuRequestImpl;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculadoraPreus {

    IProviderPreu providerPreus;
    IProviderFamilies providerFamilies;
    Map<KeyArticleClient, TipusArticleClient> mapTipusPesa;
    BigDecimal dteCoixinetBronze;
    BigDecimal dteCoixinetFerro;
    BigDecimal dteFiltres;

    public CalculadoraPreus(IProviderPreu providerPreus, IProviderFamilies providerFamilies, Map<KeyArticleClient, TipusArticleClient> mapTipusPesa, BigDecimal dteCoixinetBronze,
                            BigDecimal dteCoixinetFerro, BigDecimal dteFiltres) {
        this.providerPreus = providerPreus;
        this.providerFamilies = providerFamilies;
        this.mapTipusPesa = mapTipusPesa;
        this.dteCoixinetBronze = dteCoixinetBronze;
        this.dteCoixinetFerro = dteCoixinetFerro;
        this.dteFiltres = dteFiltres;
    }

    public CalculadoraPreusResp calcul (CalculadoraPreusReq req) {
        // Agrupació de les peces per peça i quantitat
        Map<KeyArticleClient, Long> pesesAgrupades = req.linies().stream()
                .collect(Collectors.groupingBy(LiniaCalculPreuReq::articleClient,
                        Collectors.summingLong(LiniaCalculPreuReq::quantitat)));
        // Obtenció dels preus segons la tarifa
        var reqPreus = ProviderPreuRequestImpl.builder()
                .articles(pesesAgrupades.keySet())
                .tarifaCoixinets(req.tarifaCoixinets())
                .tarifaBarres(req.tarifaBarres())
                .tarifaIbinsa(req.tarifaIbinsa())
                .tarifaMedical(req.tarifaMedical())
                .tarifaFiltresBxx(req.tarifaFiltresBxx())
                .tarifaFiltresSsu(req.tarifaFiltresSsu())
                .tarifaFiltresSxx(req.tarifaFiltresSxx())
                .tarifaFiltresSsuPlaques(req.tarifaFiltresSsuPlaques())
                .factorAplicar(req.client().equals("151301") ? new BigDecimal("0.6") : BigDecimal.ONE)  // Monterrey s'aplica un descompte de la tarifa d'aquí
                .build();
        var preus = providerPreus.provide(reqPreus);
        // Obtenció de la divisa base
        var divisaBase = preus.divisa().orElse(req.divisa());
        // Obtenció de les families per per poder calcular el descompte segons la familia
        var families = providerFamilies.provide(pesesAgrupades.keySet());
        // Només s'apliquen els descomptes als articles normalitzats
        var calcDte = new CalculDescompteFamilia(families, mapTipusPesa, dteCoixinetBronze, dteCoixinetFerro, dteFiltres);

        // Per cada línia es calcula el preu
        List<LiniaCalculPreuResp> linies = new ArrayList<>();
        for (var linia : req.linies()) {
            var articleClient = linia.articleClient();
            var pesesCalcul = pesesAgrupades.get(linia.articleClient());
            var optPreu = preus.preu(articleClient, pesesCalcul);
            // S'afegeix la línia de resposta
            linies.add(LiniaCalculPreuRespImpl.builder()
                            .linia(linia.linia())
                            .articleClient(linia.articleClient())
                            .quantitat(linia.quantitat())
                            .preu(Preu.of(optPreu.map(Preu::valor).orElse(BigDecimal.ZERO), optPreu.map(Preu::divisa).orElse(divisaBase)))
                            .descompte(calcDte.descompte(linia.articleClient()))
                            .quantitatCalcul(pesesCalcul)
                            .build());
        }

        // Retorn del resultat
        return CalculadoraPreusRespImpl.builder()
                .divisa(divisaBase)
                .linies(linies)
                .build();
    }

}
