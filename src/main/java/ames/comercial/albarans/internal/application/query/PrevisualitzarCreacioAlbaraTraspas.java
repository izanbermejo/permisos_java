package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO.LiniaAlbaraTraspasDTO;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.services.CalcularAvisosStockNegatiu.AvisStockNegatiu;
import ames.comercial.albarans.internal.services.CalcularAvisosStockNegatiuTraspas;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransTraspas.CreacioNovaLiniaTraspas;
import ames.comercial.albarans.internal.services.PrepararPropostaAlbaraTraspas;
import ames.comercial.albarans.internal.services.ProviderTarifesTraspas;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Previsualitza (sense persistir res) la proposta de creació d'albarans de traspàs a partir de les peces
 * seleccionades. Retorna també la signatura de l'estat, que el frontend ha de tornar a enviar en el moment de
 * confirmar la creació ({@code POST /albara/traspas} o {@code /albara/plataforma}) per detectar canvis.
 */
@Service
public class PrevisualitzarCreacioAlbaraTraspas {

    @Autowired PrepararPropostaAlbaraTraspas prepararPropostaAlbaraTraspas;
    @Autowired CalcularAvisosStockNegatiuTraspas calcularAvisosStockNegatiuTraspas;
    @Autowired ProviderTarifesTraspas providerTarifesTraspas;

    public PreviewCrearAlbaraTraspasResponse executar(PreviewCrearAlbaraTraspasRequest request, boolean plataforma) {
        var quantitats = request.pecesServir().stream()
                .collect(Collectors.toMap(PesaServir::articleClient, PesaServir::quantitat, Long::sum));

        var preparacio = prepararPropostaAlbaraTraspas.preparar(
                request.magatzem(), request.magatzemDesti(), request.dataAlbara(), quantitats, plataforma,
                request.aprofitarAlbaransOberts(), request.albaransNoAprofitar(), request.agruparPerClient());
        var proposta = preparacio.proposta();

        // Ruta del traspàs: origen → (intermig) → destí final. El destí físic de l'albarà (magatzemReceptor)
        // és el magatzem intermig quan el destí final en té; si no, coincideix amb el destí final.
        var magatzemDestiFisic = preparacio.magatzemDesti().codi();
        var magatzemFinal = request.magatzemDesti();
        Optional<String> magatzemIntermig = magatzemDestiFisic.equals(magatzemFinal)
                ? Optional.empty()
                : Optional.of(magatzemDestiFisic);

        // Preu amb què es valorarà cada línia nova. A plataforma sempre és la tarifa de client de la peça
        // (l'albarà no creua mai empreses); al traspàs normal, la que correspon a l'albarà de destí segons
        // si es factura. Les tarifes són les mateixes que aplicarà després la creació.
        BiFunction<Boolean, KeyArticleClient, Preu> preuLinia = plataforma
                ? (isFacturable, articleClient) -> preparacio.info().get(articleClient).preu()
                : providerTarifesTraspas.provide(quantitats.keySet())::preu;

        var albaransNous = proposta.creacioAlbarans().stream()
                .map(a -> new AlbaraTraspasNouPreview(
                        a.empresaOrigen(),
                        a.empresaDesti(),
                        request.magatzem(),
                        magatzemIntermig,
                        magatzemFinal,
                        request.dataAlbara(),
                        request.tancarAlbaransNous(),
                        a.partidaArantzelaria(),
                        a.descripcioPartidaArantzelaria(),
                        // Un albarà nou es factura si creua empreses (l'abonable, que només es pot marcar
                        // després, sempre neix fals)
                        a.linies().stream()
                                .map(l -> liniaPreview(l, preuLinia.apply(!a.empresaOrigen().equals(a.empresaDesti()), l.articleClient())))
                                .toList()))
                .toList();

        var albaransExistents = construirAlbaransExistents(preparacio, preuLinia);

        // Albarans de traspàs oberts que NO es poden aprofitar perquè tenen una data diferent de la objectiu (informatiu)
        var albaransNoAprofitables = preparacio.albaransOberts().stream()
                .filter(a -> !a.data().equals(request.dataAlbara()))
                .map(a -> new AlbaraTraspasNoAprofitablePreview(a.idAlbara().codi(), a.idAlbara().empresa(), a.data()))
                .toList();

        var avisosStockNegatiu = calcularAvisosStockNegatiuTraspas.executar(
                proposta, preparacio.albaransOberts(), preparacio.info(), request.magatzem());

        return new PreviewCrearAlbaraTraspasResponse(preparacio.signatura(), albaransNous, albaransExistents, albaransNoAprofitables, avisosStockNegatiu);
    }

    private List<AlbaraTraspasExistentPreview> construirAlbaransExistents(PrepararPropostaAlbaraTraspas.Resultat preparacio,
                                                                          BiFunction<Boolean, KeyArticleClient, Preu> preuLinia) {
        var proposta = preparacio.proposta();

        // Albarans oberts indexats per clau, per saber si el que s'hi afegeix es facturarà (creua empreses
        // i no és abonable) i, per tant, amb quina tarifa neixeran les línies noves
        Map<KeyAlbara, AlbaraTraspasAmbLiniesDTO> albaransPerClau = preparacio.albaransOberts().stream()
                .collect(Collectors.toMap(AlbaraTraspasAmbLiniesDTO::idAlbara, a -> a, (a, b) -> a));

        // Índex de les línies dels albarans oberts per clau, per obtenir la quantitat actual i l'article dels increments
        Map<KeyLiniaAlbara, LiniaAlbaraTraspasDTO> liniesObertes = preparacio.albaransOberts().stream()
                .flatMap(a -> a.linies().stream())
                .collect(Collectors.toMap(LiniaAlbaraTraspasDTO::idLiniaAlbara, l -> l, (a, b) -> a));

        // Conjunt de tots els albarans oberts afectats (nova línia i/o increment de línia existent)
        var idsAlbara = new LinkedHashSet<KeyAlbara>();
        idsAlbara.addAll(proposta.albaransAprofitables().keySet());
        proposta.liniesAprofitables().keySet().forEach(k -> idsAlbara.add(k.idAlbara()));

        var resultat = new ArrayList<AlbaraTraspasExistentPreview>();
        for (var idAlbara : idsAlbara) {
            boolean isFacturable = Optional.ofNullable(albaransPerClau.get(idAlbara))
                    .map(AlbaraTraspasAmbLiniesDTO::isTraspasFacturable)
                    .orElse(false);
            // Línies noves que s'afegiran a aquest albarà obert
            var liniesNoves = proposta.albaransAprofitables().getOrDefault(idAlbara, List.of()).stream()
                    .map(l -> liniaPreview(l, preuLinia.apply(isFacturable, l.articleClient())))
                    .toList();
            // Increments sobre línies existents d'aquest albarà obert: conserven el preu que ja té la línia
            var liniesIncrementades = proposta.liniesAprofitables().entrySet().stream()
                    .filter(e -> e.getKey().idAlbara().equals(idAlbara))
                    .map(e -> {
                        var liniaOberta = liniesObertes.get(e.getKey());
                        var infoArticle = preparacio.info().get(liniaOberta.articleClient());
                        return new LiniaTraspasIncrementPreview(
                                e.getKey().linia(),
                                infoArticle.matriu(),
                                infoArticle.referencia(),
                                liniaOberta.quantitat(),
                                e.getValue(),
                                liniaOberta.preu().valor(),
                                liniaOberta.preu().divisa().toString(),
                                importLinia(liniaOberta.preu(), e.getValue()));
                    })
                    .toList();
            resultat.add(new AlbaraTraspasExistentPreview(idAlbara.codi(), idAlbara.empresa(), liniesNoves, liniesIncrementades));
        }
        return resultat;
    }

    private LiniaTraspasPreview liniaPreview(CreacioNovaLiniaTraspas l, Preu preu) {
        return new LiniaTraspasPreview(
                l.matriu(),
                l.articleClient().clicod(),
                l.referencia(),
                l.denominacio(),
                l.nomClient(),
                l.quantitatServir(),
                l.unitatsEmbalatge(),
                l.caixesPalet(),
                l.avisEmbalatge().map(Enum::name).orElse(null),
                l.avisEmbalatge().map(a -> a.severitat().name()).orElse(null),
                preu.valor(),
                preu.divisa().toString(),
                importLinia(preu, l.quantitatServir()));
    }

    /** Import d'una línia: preu × quantitat, igual que {@code LiniaAlbara.importBrut()} */
    private BigDecimal importLinia(Preu preu, long quantitat) {
        return preu.impBrut(quantitat);
    }

    @JsonDeserialize(builder = PreviewCrearAlbaraTraspasRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PreviewCrearAlbaraTraspasRequest {
        LocalDate dataAlbara();
        String magatzem();
        String magatzemDesti();
        List<PesaServir> pecesServir();
        /** Si s'han d'aprofitar els albarans de traspàs oberts (per defecte sí) */
        @Value.Default default boolean aprofitarAlbaransOberts() { return true; }
        /** Albarans oberts que l'usuari ha descartat aprofitar */
        List<KeyAlbara> albaransNoAprofitar();
        /** Si es vol crear un albarà independent per cada client (per defecte no) */
        @Value.Default default boolean agruparPerClient() { return false; }
        /** Si els albarans que es crein nous han de quedar tancats (per defecte sí) */
        @Value.Default default boolean tancarAlbaransNous() { return true; }
    }

    @JsonDeserialize(builder = PesaServirImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PesaServir {
        KeyArticleClient articleClient();
        long quantitat();
    }

    public record PreviewCrearAlbaraTraspasResponse(
            String signatura,
            List<AlbaraTraspasNouPreview> albaransNous,
            List<AlbaraTraspasExistentPreview> albaransExistents,
            List<AlbaraTraspasNoAprofitablePreview> albaransNoAprofitables,
            List<AvisStockNegatiu> avisosStockNegatiu
    ) {}

    public record AlbaraTraspasNouPreview(
            String empresaOrigen,
            String empresaDesti,
            String magatzemOrigen,
            /** Magatzem intermig (relleu) pel qual passa físicament el traspàs; buit si no n'hi ha */
            Optional<String> magatzemIntermig,
            /** Magatzem de destí final */
            String magatzemDesti,
            /** Data amb què es crearà l'albarà */
            LocalDate data,
            /** Cert si l'albarà es crearà tancat */
            boolean esTancara,
            String partidaArantzelaria,
            String descripcioPartidaArantzelaria,
            List<LiniaTraspasPreview> linies
    ) {}

    public record AlbaraTraspasNoAprofitablePreview(
            long codiAlbara,
            String empresa,
            /** Data de l'albarà obert (diferent de la data objectiu, per això no és aprofitable) */
            LocalDate data
    ) {}

    public record AlbaraTraspasExistentPreview(
            long codiAlbara,
            String empresa,
            List<LiniaTraspasPreview> liniesNoves,
            List<LiniaTraspasIncrementPreview> liniesIncrementades
    ) {}

    public record LiniaTraspasPreview(
            String article,
            String clicod,
            String referencia,
            String denominacio,
            String nomClient,
            long quantitat,
            long unitatsEmbalatge,
            long caixesPalet,
            /** Tipus d'avís d'embalatge ({@link ames.comercial.albarans.internal.services.AvisEmbalatge}), o null */
            String avisEmbalatge,
            /** Severitat de l'avís d'embalatge (INFO/AVIS/ERROR), o null */
            String severitatEmbalatge,
            /** Preu amb què naixerà la línia: tarifa AMES si el traspàs es factura i de client si no */
            BigDecimal preu,
            String divisa,
            /** Import de la línia: preu × quantitat */
            BigDecimal importLinia
    ) {}

    public record LiniaTraspasIncrementPreview(
            long liniaAlbara,
            String article,
            String referencia,
            long quantitatActual,
            long quantitatAfegida,
            /** Preu de la línia existent, que l'increment no canvia */
            BigDecimal preu,
            String divisa,
            /** Import de la quantitat que s'afegeix: preu × quantitat afegida */
            BigDecimal importAfegit
    ) {}

}
