package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransTraspasObertsAmbLinies.AlbaraTraspasAmbLiniesDTO;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.services.IProviderInformacioArticleclient.IProviderInformacioArticleclientResponse;
import ames.comercial.albarans.internal.services.ProviderInformacioArticleclient.InformacioArticleclient;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Calcula la proposta de creació d'albarans de traspàs a partir de les peces a servir.
 * <p>
 * Les peces s'agrupen en albarans per <b>(empresa d'origen, empresa de destí, partida arantzelària)</b>
 * — el magatzem d'origen i el de destí són comuns a tota la proposta. Es reaprofiten els albarans de traspàs
 * oberts que coincideixen amb la clau (incrementant una línia existent de la mateixa peça o afegint-n'hi de noves),
 * i es creen albarans nous per a la resta.
 * <p>
 * Per a cada línia es calcula l'avís d'embalatge amb el magatzem d'origen (veure {@link CalcularAvisosEmbalatge}).
 * En la variant plataforma l'empresa de destí és la mateixa que la d'origen.
 */
@Service
public class CalcularCreacioAlbaransTraspas {

    @Autowired CalcularAvisosEmbalatge calcularAvisosEmbalatge;

    /**
     * @param quantitats                 quantitat a servir per article-client
     * @param info                       informació dels articles-client (empresa origen/destí, partida, embalatge)
     * @param albaransOberts             albarans de traspàs oberts candidats a aprofitar
     * @param plataforma                 cert si és un traspàs a plataforma (l'empresa de destí és la d'origen)
     * @param origenControlatInternament cert si el magatzem d'origen és controlat internament (per als avisos d'embalatge)
     * @param agruparPerClient           cert si es vol crear un albarà independent per cada client (s'afegeix el client a la clau d'agrupació)
     * @param dataObjectiu               data amb què es crearan els albarans nous i única data per la qual es poden aprofitar albarans oberts
     */
    public CalcularCreacioAlbaransTraspasResponse executar(Map<KeyArticleClient, Long> quantitats,
                                                           IProviderInformacioArticleclientResponse info,
                                                           List<AlbaraTraspasAmbLiniesDTO> albaransOberts,
                                                           boolean plataforma,
                                                           boolean origenControlatInternament,
                                                           boolean agruparPerClient,
                                                           LocalDate dataObjectiu) {
        // Peces a servir resoltes (es descarten les de quantitat <= 0), agrupades per clau
        Map<ClauTraspas, List<PesaResolta>> grups = new HashMap<>();
        for (var entry : quantitats.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) continue;
            var pesa = resoldre(entry.getKey(), entry.getValue(), info.get(entry.getKey()), plataforma);
            grups.computeIfAbsent(pesa.clau(agruparPerClient), k -> new ArrayList<>()).add(pesa);
        }

        List<CreacioNouAlbaraTraspas> creacioAlbarans = new ArrayList<>();
        Map<KeyLiniaAlbara, Long> liniesAprofitables = new HashMap<>();
        Map<KeyAlbara, List<CreacioNovaLiniaTraspas>> albaransAprofitables = new HashMap<>();

        for (var entry : grups.entrySet()) {
            var clau = entry.getKey();
            List<PesaResolta> perCrear = new ArrayList<>();

            for (var pesa : entry.getValue()) {
                // Albarans oberts que coincideixen amb la clau (empresa origen, empresa destí i partida arantzelària)
                // i amb la data objectiu (només s'aprofiten els albarans oberts de la mateixa data)
                var candidats = albaransOberts.stream()
                        .filter(a -> a.data().equals(dataObjectiu))
                        .filter(a -> coincideix(a, clau))
                        .toList();

                // 1. Intentar incrementar una línia existent de la mateixa peça
                var liniaExistent = candidats.stream()
                        .flatMap(a -> a.linies().stream())
                        .filter(l -> l.articleClient().equals(pesa.articleClient()))
                        .findFirst();
                if (liniaExistent.isPresent()) {
                    liniesAprofitables.merge(liniaExistent.get().idLiniaAlbara(), pesa.quantitat(), Long::sum);
                    continue;
                }

                // 2. Afegir una línia nova al primer albarà obert candidat
                if (!candidats.isEmpty()) {
                    albaransAprofitables
                            .computeIfAbsent(candidats.get(0).idAlbara(), k -> new ArrayList<>())
                            .add(novaLinia(pesa, origenControlatInternament));
                    continue;
                }

                // 3. Si no s'ha pogut aprofitar cap albarà obert, es marca per crear un albarà nou
                perCrear.add(pesa);
            }

            if (!perCrear.isEmpty()) {
                var linies = perCrear.stream().map(p -> novaLinia(p, origenControlatInternament)).toList();
                creacioAlbarans.add(CreacioNouAlbaraTraspasImpl.builder()
                        .empresaOrigen(clau.empresaOrigen())
                        .empresaDesti(clau.empresaDesti())
                        .partidaArantzelaria(clau.partidaArantzelaria())
                        .descripcioPartidaArantzelaria(perCrear.get(0).descripcioPartida())
                        .linies(linies)
                        .build());
            }
        }

        return CalcularCreacioAlbaransTraspasResponseImpl.builder()
                .creacioAlbarans(creacioAlbarans)
                .albaransAprofitables(albaransAprofitables)
                .liniesAprofitables(liniesAprofitables)
                .build();
    }

    private PesaResolta resoldre(KeyArticleClient articleClient, long quantitat, InformacioArticleclient info, boolean plataforma) {
        // En la variant plataforma l'empresa de destí és la mateixa d'origen; en el traspàs normal és l'empresa d'entrega
        String empresaDesti = plataforma ? info.codiEmpresa() : info.codiEmpresaEntrega();
        return new PesaResolta(articleClient, quantitat, info.codiEmpresa(), empresaDesti,
                info.codiPartidaArantzelaria(), info.descripcioPartidaArantzelaria(), info.unitatsEmbalatge(), info.caixesPalet(),
                info.matriu(), info.referencia(), info.denominacio(), info.nomClient());
    }

    private boolean coincideix(AlbaraTraspasAmbLiniesDTO albara, ClauTraspas clau) {
        return albara.idAlbara().empresa().equals(clau.empresaOrigen())
                && albara.empresaReceptora().equals(clau.empresaDesti())
                && partidaAlbara(albara).map(p -> p.equals(clau.partidaArantzelaria())).orElse(false)
                && coincideixClient(albara, clau);
    }

    /**
     * Quan s'agrupa per client (la clau porta un {@code clicod}), un albarà obert només és aprofitable si totes
     * les seves línies són del mateix client; així no es barregen clients dins d'un mateix albarà. Si no s'agrupa
     * per client ({@code clicod} nul), no imposa cap restricció addicional.
     */
    private boolean coincideixClient(AlbaraTraspasAmbLiniesDTO albara, ClauTraspas clau) {
        if (clau.clicod() == null) return true;
        return albara.linies().stream().allMatch(l -> clau.clicod().equals(l.articleClient().clicod()));
    }

    /** Partida arantzelària de l'albarà obert (la de les seves línies, que comparteixen partida); buit si no té línies */
    private Optional<String> partidaAlbara(AlbaraTraspasAmbLiniesDTO albara) {
        return albara.linies().stream().findFirst().map(l -> l.codiPartidaArantzelaria());
    }

    private CreacioNovaLiniaTraspas novaLinia(PesaResolta pesa, boolean origenControlatInternament) {
        var avis = calcularAvisosEmbalatge.calcular(pesa.quantitat(), pesa.unitatsEmbalatge(), pesa.caixesPalet(), origenControlatInternament);
        return CreacioNovaLiniaTraspasImpl.builder()
                .articleClient(pesa.articleClient())
                .quantitatServir(pesa.quantitat())
                .matriu(pesa.matriu())
                .referencia(pesa.referencia())
                .denominacio(pesa.denominacio())
                .nomClient(pesa.nomClient())
                .partidaArantzelaria(pesa.partidaArantzelaria())
                .unitatsEmbalatge(pesa.unitatsEmbalatge())
                .caixesPalet(pesa.caixesPalet())
                .avisEmbalatge(avis)
                .build();
    }

    /** Clau d'agrupació d'albarans de traspàs. {@code clicod} és nul si no s'agrupa per client. */
    private record ClauTraspas(String empresaOrigen, String empresaDesti, String partidaArantzelaria, String clicod) {}

    private record PesaResolta(KeyArticleClient articleClient, long quantitat, String empresaOrigen, String empresaDesti,
                               String partidaArantzelaria, String descripcioPartida, long unitatsEmbalatge, long caixesPalet,
                               String matriu, String referencia, String denominacio, String nomClient) {
        ClauTraspas clau(boolean agruparPerClient) {
            return new ClauTraspas(empresaOrigen, empresaDesti, partidaArantzelaria,
                    agruparPerClient ? articleClient.clicod() : null);
        }
    }

    @JsonDeserialize(builder = CalcularCreacioAlbaransTraspasResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CalcularCreacioAlbaransTraspasResponse {
        /** Albarans nous a crear, agrupats per (empresa origen, empresa destí, partida arantzelària) */
        List<CreacioNouAlbaraTraspas> creacioAlbarans();
        /** Línies noves a afegir a albarans de traspàs oberts existents */
        Map<KeyAlbara, List<CreacioNovaLiniaTraspas>> albaransAprofitables();
        /** Quantitat a incrementar sobre línies d'albarà de traspàs ja existents */
        Map<KeyLiniaAlbara, Long> liniesAprofitables();
    }

    @JsonDeserialize(builder = CreacioNouAlbaraTraspasImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CreacioNouAlbaraTraspas {
        String empresaOrigen();
        String empresaDesti();
        String partidaArantzelaria();
        String descripcioPartidaArantzelaria();
        List<CreacioNovaLiniaTraspas> linies();
    }

    @JsonDeserialize(builder = CreacioNovaLiniaTraspasImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CreacioNovaLiniaTraspas {
        KeyArticleClient articleClient();
        long quantitatServir();
        String matriu();
        String referencia();
        String denominacio();
        String nomClient();
        String partidaArantzelaria();
        long unitatsEmbalatge();
        long caixesPalet();
        /** Avís d'embalatge si la quantitat no és múltiple de l'embalatge; buit si és correcta */
        Optional<AvisEmbalatge> avisEmbalatge();
    }

}
