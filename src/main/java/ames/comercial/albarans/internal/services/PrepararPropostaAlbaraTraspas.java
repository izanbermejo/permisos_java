package ames.comercial.albarans.internal.services;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.magatzem.internal.domain.Magatzem;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransTraspas.CalcularCreacioAlbaransTraspasResponse;
import ames.comercial.albarans.internal.services.IProviderInformacioArticleclient.IProviderInformacioArticleclientResponse;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prepara la proposta de creació d'albarans de traspàs a partir de les peces a servir.
 * <p>
 * És la lògica compartida entre la previsualització ({@code POST /albara/traspas/calcular} i
 * {@code POST /albara/plataforma/calcular}) i la creació ({@code POST /albara/traspas} i {@code /albara/plataforma}):
 * carrega l'estat (informació dels articles-client i albarans de traspàs oberts), en calcula la signatura i la
 * proposta de creació, de manera que la proposta que es mostra i la que es crea es calculen exactament igual.
 */
@Component
public class PrepararPropostaAlbaraTraspas {

    @Autowired IObtenirMagatzems obtenirMagatzemAds;
    @Autowired IProviderInformacioArticleclient providerInformacioArticleclient;
    @Autowired ObtenirAlbaransTraspasObertsAmbLinies obtenirAlbaransTraspasOberts;
    @Autowired CalcularCreacioAlbaransTraspas calcularCreacioAlbaransTraspas;
    @Autowired CalcularSignaturaPropostaTraspas calcularSignaturaProposta;

    public Resultat preparar(String magatzemOrigen, String magatzemDesti, LocalDate dataAlbara,
                             Map<KeyArticleClient, Long> quantitats, boolean plataforma,
                             boolean aprofitarAlbaransOberts, List<KeyAlbara> albaransNoAprofitar,
                             boolean agruparPerClient) {
        // Es comprova que els magatzems d'origen i destí existeixen
        var magatzemOrigenAds = obtenirMagatzemAds.get(magatzemOrigen).orElseThrow();
        var magatzemDestiAds = obtenirMagatzemAds.get(magatzemDesti).orElseThrow();

        // Si el destí té un magatzem intermig (relleu) per a l'origen donat, el traspàs físic (albarà i
        // moviments) ha d'anar cap a l'intermig en comptes del destí final. La resta del càlcul (empreses,
        // partida, quantitats) no canvia: només canvia el magatzem físic receptor. La restricció que
        // l'intermig sigui diferent d'origen/destí es controla al manteniment de la taula.
        var magatzemFisic = obtenirMagatzemAds.obtenirMagatzemIntermig(magatzemOrigen, magatzemDesti)
                .orElse(magatzemDestiAds);
        var magatzemDestiFisic = magatzemFisic.codi();

        // Informació dels articles-client (empresa origen/destí, partida, embalatge, adreça, etc.)
        var info = providerInformacioArticleclient.provide(quantitats.keySet());

        // Albarans de traspàs oberts candidats a aprofitar (només si es volen aprofitar), descartant els exclosos per l'usuari
        List<AlbaraTraspasAmbLiniesDTO> albaransOberts = aprofitarAlbaransOberts
                ? obtenirAlbaransTraspasOberts.executar(magatzemOrigen, magatzemDestiFisic)
                : List.of();
        if (!albaransNoAprofitar.isEmpty()) {
            Set<KeyAlbara> exclosos = Set.copyOf(albaransNoAprofitar);
            albaransOberts = albaransOberts.stream()
                    .filter(a -> !exclosos.contains(a.idAlbara()))
                    .toList();
        }

        // Signatura de l'estat (per detectar canvis entre previsualització i confirmació). Inclou la data objectiu:
        // canviar-la fa que la proposta sigui diferent (varien els albarans oberts aprofitables).
        String signatura = calcularSignaturaProposta.executar(dataAlbara, quantitats, albaransOberts);

        // Proposta de creació
        var proposta = calcularCreacioAlbaransTraspas.executar(
                quantitats, info, albaransOberts, plataforma, magatzemOrigenAds.isControlatInternament(), agruparPerClient, dataAlbara);

        return new Resultat(proposta, signatura, albaransOberts, info, magatzemFisic);
    }

    public record Resultat(
            CalcularCreacioAlbaransTraspasResponse proposta,
            String signatura,
            List<AlbaraTraspasAmbLiniesDTO> albaransOberts,
            IProviderInformacioArticleclientResponse info,
            Magatzem magatzemDesti
    ) {}

}
