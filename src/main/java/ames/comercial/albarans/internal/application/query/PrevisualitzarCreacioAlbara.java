package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.application.command.CrearAlbara.LiniaServirRequest;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.services.CalcularAutofacturableAlbara;
import ames.comercial.albarans.internal.services.CalcularAvisosStockNegatiu;
import ames.comercial.albarans.internal.services.CalcularAvisosStockNegatiu.AvisStockNegatiu;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNovaLinia;
import ames.comercial.albarans.internal.services.PrepararPropostaAlbara;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Previsualitza (sense persistir res) la proposta de creació d'albarans de sortida a partir de les línies
 * de comanda seleccionades. Retorna també la signatura de l'estat, que el frontend ha de tornar a enviar
 * en el moment de confirmar la creació ({@code POST /albara}) per detectar canvis.
 */
@Service
public class PrevisualitzarCreacioAlbara {

    @Autowired PrepararPropostaAlbara prepararPropostaAlbara;
    @Autowired CalcularAvisosStockNegatiu calcularAvisosStockNegatiu;
    @Autowired CalcularAutofacturableAlbara calcularAutofacturableAlbara;

    public PreviewCrearAlbaraResponse executar(PreviewCrearAlbaraRequest request) {
        var quantitats = request.liniesServir().stream()
                .collect(Collectors.toMap(LiniaServirRequest::id, LiniaServirRequest::quantitat));

        var preparacio = prepararPropostaAlbara.preparar(request.client(), request.magatzem(), request.dataAlbara(), quantitats,
                request.aprofitarAlbaransOberts(), request.albaransNoAprofitar());
        var proposta = preparacio.proposta();
        var clientFacturacioAutomatica = preparacio.client().isFacturacioAutomatica();

        var albaransNous = proposta.creacioAlbarans().stream()
                .map(a -> {
                    var autofacturable = calcularAutofacturableAlbara.calcular(clientFacturacioAutomatica, a.liniesAlbara());
                    return new AlbaraNouPreview(
                            a.empresa().clau(),
                            a.adresa().destinatari(),
                            poblacio(a.adresa().codiPostal(), a.adresa().poblacio()),
                            request.dataAlbara(),
                            request.tancarAlbaransNous(),
                            a.liniesAlbara().stream().map(this::liniaPreview).toList(),
                            autofacturable.autofacturable(),
                            autofacturable.motiu() == null ? null : autofacturable.motiu().name());
                })
                .toList();

        var albaransExistents = construirAlbaransExistents(proposta, quantitats, preparacio.albaransOberts());

        // Albarans oberts que NO es poden aprofitar perquè tenen una data diferent de la data objectiu (informatiu)
        var albaransNoAprofitables = preparacio.albaransOberts().stream()
                .filter(a -> !a.data().equals(request.dataAlbara()))
                .map(a -> new AlbaraNoAprofitablePreview(a.idAlbara().codi(), a.idAlbara().empresa(), a.data()))
                .toList();

        var avisosStockNegatiu = calcularAvisosStockNegatiu.executar(proposta, quantitats, request.magatzem());

        return new PreviewCrearAlbaraResponse(preparacio.signatura(), albaransNous, albaransExistents, albaransNoAprofitables, avisosStockNegatiu);
    }

    private List<AlbaraExistentPreview> construirAlbaransExistents(
            CalcularCreacioAlbaransSortidaResponse proposta,
            Map<KeyLiniaComanda, Long> quantitats,
            List<AlbaraAmbLiniesDTO> albaransOberts) {

        // Quantitat actual de cada línia d'albarà obert (per mostrar-la abans de l'increment)
        var quantitatsActuals = albaransOberts.stream()
                .flatMap(a -> a.linies().stream())
                .collect(Collectors.toMap(l -> l.idLiniaAlbara(), l -> l.quantitat(), (a, b) -> a));

        // Conjunt de tots els albarans oberts afectats (nova línia i/o increment de línia existent)
        var idsAlbara = new LinkedHashSet<KeyAlbara>();
        idsAlbara.addAll(proposta.albaransAprofitables().keySet());
        proposta.liniesAprofitables().keySet().forEach(k -> idsAlbara.add(k.idAlbara()));

        var resultat = new ArrayList<AlbaraExistentPreview>();
        for (var idAlbara : idsAlbara) {
            // Línies noves que s'afegiran a aquest albarà obert
            var liniesNoves = proposta.albaransAprofitables().getOrDefault(idAlbara, List.of()).stream()
                    .map(this::liniaPreview)
                    .toList();
            // Increments sobre línies existents d'aquest albarà obert
            var liniesIncrementades = proposta.liniesAprofitables().entrySet().stream()
                    .filter(e -> e.getKey().idAlbara().equals(idAlbara))
                    .map(e -> {
                        var lc = e.getValue().get(0);
                        long quantitatAfegida = e.getValue().stream()
                                .mapToLong(l -> quantitats.getOrDefault(l.id(), 0L))
                                .sum();
                        long quantitatActual = quantitatsActuals.getOrDefault(e.getKey(), 0L);
                        return new LiniaIncrementPreview(e.getKey().linia(), lc.projecte(), lc.referencia(), quantitatActual, quantitatAfegida);
                    })
                    .toList();
            resultat.add(new AlbaraExistentPreview(idAlbara.codi(), idAlbara.empresa(), liniesNoves, liniesIncrementades));
        }
        return resultat;
    }

    private LiniaPreview liniaPreview(CreacioNovaLinia l) {
        var lc = l.liniesComanda().get(0);
        boolean preuFixat = l.liniesComanda().stream().anyMatch(c -> c.isPreuFixat());
        boolean comandaBlanca = l.liniesComanda().stream().anyMatch(c -> c.comandaBlanca().isPresent());
        return new LiniaPreview(
                lc.projecte(),
                lc.referencia(),
                lc.denominacio(),
                l.comandaSegonsClient(),
                l.quantitatServir(),
                l.preu().valor(),
                l.preu().divisa().toString(),
                preuFixat,
                comandaBlanca,
                lc.unitatsEmbalatge(),
                lc.caixesPalet(),
                l.avisEmbalatge().map(Enum::name).orElse(null),
                l.avisEmbalatge().map(a -> a.severitat().name()).orElse(null));
    }

    private String poblacio(String codiPostal, String poblacio) {
        return codiPostal == null || codiPostal.isBlank() ? poblacio : (codiPostal + " " + poblacio).trim();
    }

    @JsonDeserialize(builder = PreviewCrearAlbaraRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PreviewCrearAlbaraRequest {
        String client();
        LocalDate dataAlbara();
        String magatzem();
        List<LiniaServirRequest> liniesServir();
        /** Si s'han d'aprofitar els albarans oberts (per defecte sí) */
        @Value.Default default boolean aprofitarAlbaransOberts() { return true; }
        /** Albarans oberts que l'usuari ha descartat aprofitar (buit = aprofitar tots els candidats) */
        List<KeyAlbara> albaransNoAprofitar();
        /** Si els albarans que es crein nous han de quedar tancats (per defecte sí) */
        @Value.Default default boolean tancarAlbaransNous() { return true; }
    }

    public record PreviewCrearAlbaraResponse(
            String signatura,
            List<AlbaraNouPreview> albaransNous,
            List<AlbaraExistentPreview> albaransExistents,
            List<AlbaraNoAprofitablePreview> albaransNoAprofitables,
            List<AvisStockNegatiu> avisosStockNegatiu
    ) {}

    public record AlbaraNouPreview(
            String empresa,
            String destinatari,
            String poblacio,
            /** Data amb què es crearà l'albarà */
            LocalDate data,
            /** Cert si l'albarà es crearà tancat */
            boolean esTancara,
            List<LiniaPreview> linies,
            boolean autofacturable,
            /** Motiu pel qual l'albarà no és autofacturable ({@link CalcularAutofacturableAlbara.Motiu}), o null si ho és */
            String motiuNoAutofacturable
    ) {}

    public record AlbaraNoAprofitablePreview(
            long codiAlbara,
            String empresa,
            /** Data de l'albarà obert (diferent de la data objectiu, per això no és aprofitable) */
            LocalDate data
    ) {}

    public record AlbaraExistentPreview(
            long codiAlbara,
            String empresa,
            List<LiniaPreview> liniesNoves,
            List<LiniaIncrementPreview> liniesIncrementades
    ) {}

    public record LiniaPreview(
            String article,
            String referencia,
            String denominacio,
            String comandaClient,
            long quantitat,
            BigDecimal preu,
            String divisa,
            boolean preuFixat,
            boolean comandaBlanca,
            /** Unitats per caixa de l'article-client (informatiu) */
            long unitatsEmbalatge,
            /** Caixes per palet de l'article-client (informatiu); 0 si no n'hi ha */
            long caixesPalet,
            /** Tipus d'avís d'embalatge ({@link ames.comercial.albarans.internal.services.AvisEmbalatge}), o null si la quantitat és múltiple correcte */
            String avisEmbalatge,
            /** Severitat de l'avís d'embalatge ({@link ames.comercial.albarans.internal.services.AvisEmbalatge.Severitat}: INFO/AVIS/ERROR), o null */
            String severitatEmbalatge
    ) {}

    public record LiniaIncrementPreview(
            long liniaAlbara,
            String article,
            String referencia,
            long quantitatActual,
            long quantitatAfegida
    ) {}

}
