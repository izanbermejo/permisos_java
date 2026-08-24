package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO.LiniaAlbaraDTO;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNouAlbarans;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNovaLinia;
import ames.comercial.albarans.internal.services.agrupacio.AgrupadorFactory;
import ames.comercial.albarans.internal.services.agrupacio.ClauAgrupacio;
import ames.comercial.albarans.internal.services.agrupacio.ModeAgrupacio;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CalcularCreacioAlbaransSortida {

    @Autowired AgrupadorFactory agrupadorAlbaraFactory;
    @Autowired CalcularAvisosEmbalatge calcularAvisosEmbalatge;

    /**
     * Calcula la proposta de creació d'albarans de sortida.
     *
     * @param modeAgrupacio               mode d'agrupació segons el client
     * @param linies                      línies de comanda candidates (ja carregades)
     * @param quantitats                  quantitat confirmada a servir per cada línia de comanda
     * @param albaransOberts              albarans oberts del client i data (per aprofitar-los)
     * @param origenControlatInternament  cert si el magatzem d'origen és controlat internament ({@code mag.tipus='N'}), per calcular els avisos d'embalatge
     * @param dataObjectiu                data amb què es crearan els albarans nous i única data per la qual es poden aprofitar albarans oberts
     */
    public CalcularCreacioAlbaransSortidaResponse executar(ModeAgrupacio modeAgrupacio,
                                                           List<InformacioLiniaComandaDTO> linies,
                                                           Map<KeyLiniaComanda, Long> quantitats,
                                                           List<AlbaraAmbLiniesDTO> albaransOberts,
                                                           boolean origenControlatInternament,
                                                           LocalDate dataObjectiu) {
        // Es descarten les línies sense quantitat confirmada a servir
        var liniesComanda = linies.stream()
                .filter(linia -> quantitats.getOrDefault(linia.id(), 0L) > 0)
                .toList();
        // Es calcula l'agrupació d'albarans (segons el mode d'agrupació del client, empresa, adreça i informació d'enviament)
        // per a les línies de comanda a servir
        var agrupacioAlbarans = agrupadorAlbaraFactory.get(modeAgrupacio).agrupar(liniesComanda);

        // Per cada agrupació es calcula la proposta de creació i s'afegeix al resultat final
        List<CreacioNouAlbarans> creacioAlbarans = new ArrayList<>();
        Map<KeyLiniaAlbara, List<InformacioLiniaComandaDTO>> liniesAprofitables = new HashMap<>();
        Map<KeyAlbara, List<CreacioNovaLinia>> albaransAprofitables = new HashMap<>();
        for (var entry : agrupacioAlbarans.entrySet()) {
            var clauAgrupacio = entry.getKey();
            var liniesAgrupades = entry.getValue();
            var proposta = calcularPropostaCreacio(clauAgrupacio, liniesAgrupades, albaransOberts, quantitats, origenControlatInternament, dataObjectiu);
            creacioAlbarans.addAll(proposta.creacioAlbarans());
            liniesAprofitables.putAll(proposta.liniesAprofitables());
            albaransAprofitables.putAll(proposta.albaransAprofitables());
        }

        // Retorn del resultat
        return CalcularCreacioAlbaransSortidaResponseImpl.builder()
                .creacioAlbarans(creacioAlbarans)
                .liniesAprofitables(liniesAprofitables)
                .albaransAprofitables(albaransAprofitables)
                .build();
    }

    private CalcularCreacioAlbaransSortidaResponse calcularPropostaCreacio (ClauAgrupacio clauAgrupacio,
                                                                List<InformacioLiniaComandaDTO> linies,
                                                                List<AlbaraAmbLiniesDTO> albaransOberts,
                                                                Map<KeyLiniaComanda, Long> quantitats,
                                                                boolean origenControlatInternament,
                                                                LocalDate dataObjectiu) {

        // Per a cada agrupació, intentar aprofitar al màxim les línies d'albarans oberts (primer a nivell de línia i després a nivell d'albarà)
        // i marcar les línies de comanda que no es poden aprofitar per crear un nou albarà
        List<InformacioLiniaComandaDTO> liniesPerCrearAlbara = new ArrayList<>();
        Map<KeyLiniaAlbara, List<InformacioLiniaComandaDTO>> liniesAprofitables = new HashMap<>();
        Map<KeyAlbara, List<InformacioLiniaComandaDTO>> albaransAprofitables = new HashMap<>();

        // Creació del servei per calcular els albarans aprofitables segons el mode d'agrupació del client, l'adreça i informació
        // d'enviament de les línies de comanda agrupades
        var filtradorAlbaransAprofitables = new FiltrarAlbaransAprofitables(albaransOberts, clauAgrupacio, dataObjectiu);

        // Recorregut de cada línia agrupada
        for (InformacioLiniaComandaDTO liniaComanda : linies) {
            boolean aprofitada = false;

            // Obtenció dels albarans aprofitables per a la línia de comanda segons el mode d'agrupació del client,
            // l'adreça i informació d'enviament
            List<AlbaraAmbLiniesDTO> albaransCandidats = filtradorAlbaransAprofitables.executar(liniaComanda);

            // 1. Intentar aprofitar la línia de comanda en les línies dels albarans oberts candidats
            // La línia d'albarà ha de ser de la mateixa peça, tenir el mateix preu i correspondre a la mateixa
            // comanda segons client (incrementar una línia existent hi acumula quantitat, així que ha de mantenir
            // la mateixa comanda; el filtre d'albarans candidats només garanteix la comanda en els modes per comanda)
            for (AlbaraAmbLiniesDTO albara : albaransCandidats) {
                for (var liniaAlbara : albara.linies()) {
                    if (potAprofitarLinia(liniaComanda, liniaAlbara)) {
                        liniesAprofitables
                                .computeIfAbsent(liniaAlbara.idLiniaAlbara(), k -> new ArrayList<>())
                                .add(liniaComanda);
                        aprofitada = true;
                        break;
                    }
                }
                if (aprofitada) break;
            }

            // 2. Si no s'ha aprofitat cap línia d'albarà, s'intenta aprofitar algun albarà obert candidat
            if (!aprofitada && !albaransCandidats.isEmpty()) {
                var albara = albaransCandidats.get(0); // S'agafa el primer albarà candidat
                albaransAprofitables
                        .computeIfAbsent(albara.idAlbara(), k -> new ArrayList<>())
                        .add(liniaComanda);
                aprofitada = true;
            }

            // 3. Si no s'ha pogut aprofitar cap línia ni albarà obert, marcar per crear nou albarà
            if (!aprofitada) {
                liniesPerCrearAlbara.add(liniaComanda);
            }
        }

        // Crear proposta de nou albarà si hi ha línies no aprofitables
        List<CreacioNouAlbarans> creacioAlbarans = new ArrayList<>();
        if (!liniesPerCrearAlbara.isEmpty()) {
            var novesLinies = agrupaLiniesAlbara(liniesPerCrearAlbara, quantitats, origenControlatInternament);
            creacioAlbarans.add(CreacioNouAlbaransImpl.builder()
                    .liniesAlbara(novesLinies)
                    .empresa(Empresa.getByClau(clauAgrupacio.empresa()))
                    .adresa(clauAgrupacio.adresa())
                    .informacioEnviament(clauAgrupacio.informacioEnviament())
                    .build());
        }

        return CalcularCreacioAlbaransSortidaResponseImpl.builder()
                .creacioAlbarans(creacioAlbarans)
                .liniesAprofitables(liniesAprofitables)
                .albaransAprofitables(agrupaLiniesAlbara(albaransAprofitables, quantitats, origenControlatInternament))
                .build();
    }

    private List<CreacioNovaLinia> agrupaLiniesAlbara (List<InformacioLiniaComandaDTO> liniesComanda, Map<KeyLiniaComanda, Long> quantitats, boolean origenControlatInternament) {
        // Agrupar les línies de comanda per peça i preu (es poden agrupar a la mateixa línia d'albarà)
        Map<String, List<InformacioLiniaComandaDTO>> agrupacio = new HashMap<>();
        for (InformacioLiniaComandaDTO linia : liniesComanda) {
            String clauAgrupacio = linia.comandaSegonsClient() +  linia.articleClient() + "_" + linia.preu();
            agrupacio.computeIfAbsent(clauAgrupacio, k -> new ArrayList<>()).add(linia);
        }
        // Crear les línies d'albarà agrupant les línies de comanda
        List<CreacioNovaLinia> liniesAlbara = new ArrayList<>();
        for (var entry : agrupacio.entrySet()) {
            var liniesGrup = entry.getValue();
            long quantitatServir = sumaQuantitats(liniesGrup, quantitats);
            // Avís d'embalatge segons les unitats de la peça i el tipus de magatzem d'origen (veure CalcularAvisosEmbalatge)
            var primera = liniesGrup.get(0);
            var avisEmbalatge = calcularAvisosEmbalatge.calcular(
                    quantitatServir, primera.unitatsEmbalatge(), primera.caixesPalet(), origenControlatInternament);
            liniesAlbara.add(CreacioNovaLiniaImpl.builder()
                    .liniesComanda(liniesGrup)
                    .quantitatServir(quantitatServir)
                    .avisEmbalatge(avisEmbalatge)
                    .build());
        }
        return liniesAlbara;
    }

    private Map<KeyAlbara, List<CreacioNovaLinia>> agrupaLiniesAlbara (Map<KeyAlbara, List<InformacioLiniaComandaDTO>> liniesComanda, Map<KeyLiniaComanda, Long> quantitats, boolean origenControlatInternament) {
        Map<KeyAlbara, List<CreacioNovaLinia>> agrupacio = new HashMap<>();
        for (var entry : liniesComanda.entrySet()) {
            var keyAlbara = entry.getKey();
            var linies = entry.getValue();
            List<CreacioNovaLinia> liniesAlbara = agrupaLiniesAlbara(linies, quantitats, origenControlatInternament);
            agrupacio.put(keyAlbara, liniesAlbara);
        }
        return agrupacio;
    }

    private long sumaQuantitats(List<InformacioLiniaComandaDTO> linies, Map<KeyLiniaComanda, Long> quantitats) {
        return linies.stream().mapToLong(l -> quantitats.getOrDefault(l.id(), 0L)).sum();
    }

    private boolean potAprofitarLinia(InformacioLiniaComandaDTO liniaComanda, LiniaAlbaraDTO liniaAlbara) {
        return liniaAlbara.articleClient().equals(liniaComanda.articleClient())
                && Preu.mateix(liniaAlbara.preu(), liniaComanda.preu())
                && coincideixComandaClient(liniaComanda, liniaAlbara);
    }

    private boolean coincideixComandaClient(InformacioLiniaComandaDTO liniaComanda, LiniaAlbaraDTO liniaAlbara) {
        var comandaClient = liniaComanda.comandaSegonsClient();
        return comandaClient != null && liniaAlbara.infoComanda()
                .map(infoComanda -> comandaClient.equals(infoComanda.comandaClient()))
                .orElse(false);
    }

    @JsonDeserialize(builder = CalcularCreacioAlbaransSortidaResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CalcularCreacioAlbaransSortidaResponse {
        List<CreacioNouAlbarans> creacioAlbarans();
        Map<KeyLiniaAlbara, List<InformacioLiniaComandaDTO>> liniesAprofitables();
        Map<KeyAlbara, List<CreacioNovaLinia>> albaransAprofitables();

        @JsonDeserialize(builder = CalcularCreacioAlbaransSortidaResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface CreacioNouAlbarans {
            List<CreacioNovaLinia> liniesAlbara();
            Empresa empresa();
            Adresa adresa();
            InformacioEnviament informacioEnviament();
        }

        @JsonDeserialize(builder = CalcularCreacioAlbaransSortidaResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface CreacioNovaLinia {
            List<InformacioLiniaComandaDTO> liniesComanda();

            /** Quantitat total a servir per aquesta línia d'albarà (suma de les quantitats confirmades de les línies de comanda agrupades) */
            long quantitatServir();

            /** Avís d'embalatge si la quantitat a servir no és múltiple de les unitats d'embalatge (veure {@link CalcularAvisosEmbalatge}); buit si és correcta */
            Optional<AvisEmbalatge> avisEmbalatge();

            @Derived
            default KeyArticleClient articleClient() {
                return liniesComanda().get(0).articleClient();
            }

            @Derived
            default Long comanda() {
                return liniesComanda().get(0).id().comanda();
            }

            @Derived
            default Preu preu() {
                return liniesComanda().get(0).preu();
            }

            @Derived
            default boolean isPreuFixat() {
                return liniesComanda().stream().findAny().map(InformacioLiniaComandaDTO::isPreuFixat).orElse(false);
            }

            /**
             * Descompte en % de la línia d'albarà. S'agafa de la primera línia de comanda agrupada, igual
             * que el preu: el descompte només depèn de l'article i del client (veure {@code CalculDescompteFamilia})
             * i l'agrupació ja és per article, així que totes les línies del grup el comparteixen.
             */
            @Derived
            default BigDecimal descompte() {
                return liniesComanda().get(0).descompte();
            }

            @Derived
            default String comandaSegonsClient() {
                return liniesComanda().get(0).comandaSegonsClient();
            }

            @Derived
            default String programa() {
                return liniesComanda().get(0).programa();
            }

        }

    }


}
