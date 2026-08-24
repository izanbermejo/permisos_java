package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.PropostaModificada;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNouAlbarans;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNovaLinia;
import ames.comercial.albarans.internal.services.CalcularAutofacturableAlbara;
import ames.comercial.albarans.internal.services.CreacioLiniesAlbara;
import ames.comercial.albarans.internal.services.CrearCapsaleraAlbara;
import ames.comercial.albarans.internal.services.PrepararPropostaAlbara;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.comandes.internal.application.command.ServirLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.inventari.ext.ICrearMovimentSortida;
import ames.comercial.inventari.internal.domain.service.CrearMovimentSortidaRequestImpl;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentSortida.CrearMovimentSortidaRequest;
import ames.comercial.shared.Pair;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrearAlbara {

    @Autowired PrepararPropostaAlbara prepararPropostaAlbara;
    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired CrearCapsaleraAlbara crearCapsaleraAlbara;
    @Autowired CalcularAutofacturableAlbara calcularAutofacturableAlbara;
    @Autowired ServirLiniaComanda servirLiniaComanda;
    @Autowired ICrearMovimentSortida crearMovimentSortida;

    @Transactional
    public ResultatCreacioAlbaransResponse executar(CrearAlbaraRequest request) {
        // Quantitats confirmades a servir per cada línia de comanda
        var quantitats = request.liniesServir().stream()
                .collect(Collectors.toMap(LiniaServirRequest::id, LiniaServirRequest::quantitat));

        // Es prepara la proposta a partir de l'estat actual (valida també que el client existeix)
        var preparacio = prepararPropostaAlbara.preparar(request.client(), request.magatzem(), request.dataAlbara(), quantitats,
                request.aprofitarAlbaransOberts(), request.albaransNoAprofitar());

        // Detecció de canvis: si la signatura de l'estat actual no coincideix amb la que es va acceptar
        // a la previsualització, la proposta ha canviat i no es crea res.
        if (!preparacio.signatura().equals(request.signatura())) {
            throw new PropostaModificada();
        }

        var propostesCreacio = preparacio.proposta();

        // L'albarà és autofacturable segons el flag del client a Advantage (cli6.autofac = 'S')
        boolean clientFacturacioAutomatica = preparacio.client().isFacturacioAutomatica();

        // 1.Creació dels nous albarans (no s'aprofiten albarans oberts ni línies ja existents d'albarans)
        var albaransCreats = creacioNousAlbarans(request, propostesCreacio.creacioAlbarans(), quantitats, clientFacturacioAutomatica);
        // 2.Creació de les línies d'albarà en albarans ja existents
        creacioLiniesAlbaraExistents(propostesCreacio.albaransAprofitables(), quantitats);
        // 3.- Modificació de les línie d'albarà en albarans ja existents (quantitats a servir i moviment de sortida associat)
        modificacioLiniesAlbaraExistents(propostesCreacio.liniesAprofitables(), quantitats);

        return construirResultat(albaransCreats, propostesCreacio);
    }

    /** Construeix el resultat: albarans creats de nou + albarans oberts aprofitats (nova línia o increment) */
    private ResultatCreacioAlbaransResponse construirResultat(List<Albara> albaransCreats,
                                                              CalcularCreacioAlbaransSortidaResponse propostesCreacio) {
        var idsAprofitats = new LinkedHashSet<KeyAlbara>();
        idsAprofitats.addAll(propostesCreacio.albaransAprofitables().keySet());
        propostesCreacio.liniesAprofitables().keySet().forEach(k -> idsAprofitats.add(k.idAlbara()));

        var albaransAprofitats = idsAprofitats.stream()
                .map(id -> albaraRepository.find(id).orElseThrow())
                .toList();
        return ResultatCreacioAlbaransResponse.de(albaransCreats, albaransAprofitats);
    }

    private List<Albara> creacioNousAlbarans(CrearAlbaraRequest request, List<CreacioNouAlbarans> creacioAlbarans,
                                     Map<KeyLiniaComanda, Long> quantitats, boolean clientFacturacioAutomatica) {
        var albaransCreats = new ArrayList<Albara>();
        for (var proposta : creacioAlbarans) {
            // L'albarà és autofacturable segons el client i les seves línies (veure CalcularAutofacturableAlbara)
            boolean albaraAutofacturable = calcularAutofacturableAlbara
                    .calcular(clientFacturacioAutomatica, proposta.liniesAlbara())
                    .autofacturable();

            // Creació de la capçalera de l'albarà, s'emmagatzema a BBDD i s'afegeix a la llista de resultats
            var capsalera = crearCapsaleraAlbara.executar(
                    proposta.empresa(),
                    proposta.adresa(),
                    proposta.informacioEnviament(),
                    request.client(),
                    request.dataAlbara(),
                    request.magatzem(),
                    albaraAutofacturable,
                    request.tancarAlbaransNous()
            );
            albaraRepository.save(capsalera);
            albaransCreats.add(capsalera);
            ReplicaAdvantage.instance().addAlbaraInsert(capsalera);
            // Creació de les línies d'albarà
            var liniesAlbara = new CreacioLiniesAlbara(capsalera.id())
                    .executar(proposta.liniesAlbara());
            // Per cada linia d'albarà, es guarda a BBDD, es serveix la línia de comanda associada i es crea el moviment de sortida corresponent
            for (var pair : liniesAlbara) {
                var liniaAlbara = pair.first();
                var liniesComanda = pair.second();
                // Guardar la linia d'albarà
                liniaAlbaraRepository.save(liniaAlbara);
                for (var liniaComanda : liniesComanda) {
                    var quantitatServir = quantitatServir(quantitats, liniaComanda);
                    // Servir línia de comanda
                    servirLiniaComanda.executar(liniaComanda.id(), quantitatServir);
                    // Crear moviment de sortida per a la línia de l'albarà
                    var movimentRequest = buildRequest(capsalera, liniaAlbara, liniaComanda, quantitatServir);
                    crearMovimentSortida.executar(movimentRequest);
                }
            }

            // Rèplica Advantage de les línies d'albarà
            liniesAlbara.stream().map(Pair::first)
                    .forEach(ReplicaAdvantage.instance()::addLiniaAlbaraInsert);
        }
        return albaransCreats;
    }

    private void creacioLiniesAlbaraExistents(Map<KeyAlbara, List<CreacioNovaLinia>> keyAlbaraListMap, Map<KeyLiniaComanda, Long> quantitats) {
        for (var entry : keyAlbaraListMap.entrySet()) {
            var keyAlbara = entry.getKey();
            var liniesAlbaraCrear = entry.getValue();
            var albara = albaraRepository.find(keyAlbara).orElseThrow();
            var liniesAlbara = new CreacioLiniesAlbara(albara.id())
                    .withNumeroLiniaAlbara(liniaAlbaraRepository.nextNumero(keyAlbara))
                    .executar(liniesAlbaraCrear);
            // Per cada linia d'albarà, es guarda a BBDD, es serveix la línia de comanda associada i es crea el moviment de sortida corresponent
            for (var pair : liniesAlbara) {
                var liniaAlbara = pair.first();
                var liniesComanda = pair.second();
                // Guardar la linia d'albarà
                liniaAlbaraRepository.save(liniaAlbara);
                for (var liniaComanda : liniesComanda) {
                    var quantitatServir = quantitatServir(quantitats, liniaComanda);
                    // Servir línia de comanda
                    servirLiniaComanda.executar(liniaComanda.id(), quantitatServir);
                    // Crear moviment de sortida per a la línia de l'albarà
                    var movimentRequest = buildRequest(albara, liniaAlbara, liniaComanda, quantitatServir);
                    crearMovimentSortida.executar(movimentRequest);
                }
            }

            // Rèplica Advantage de les línies d'albarà
            liniesAlbara.stream().map(Pair::first)
                    .forEach(ReplicaAdvantage.instance()::addLiniaAlbaraInsert);
        }
    }

    private void modificacioLiniesAlbaraExistents(Map<KeyLiniaAlbara, List<InformacioLiniaComandaDTO>> keyLiniaAlbaraListMap, Map<KeyLiniaComanda, Long> quantitats) {
        for (var entry : keyLiniaAlbaraListMap.entrySet()) {
            var keyLiniaAlbara = entry.getKey();
            var liniesComanda = entry.getValue();
            var albara = albaraRepository.find(keyLiniaAlbara.idAlbara()).orElseThrow();
            var liniaAlbara = liniaAlbaraRepository.find(keyLiniaAlbara).orElseThrow();
            for (var liniaComanda : liniesComanda) {
                var quantitatServir = quantitatServir(quantitats, liniaComanda);
                // S'incrementa la quantitat de la línia de l'albarà
                liniaAlbara = liniaAlbara.incrementarQuantitat(quantitatServir);
                // Servir línia de comanda
                servirLiniaComanda.executar(liniaComanda.id(), quantitatServir);
                // Crear moviment de sortida per a la línia de l'albarà
                var movimentRequest = buildRequest(albara, liniaAlbara, liniaComanda, quantitatServir);
                crearMovimentSortida.executar(movimentRequest);
            }
            liniaAlbaraRepository.save(liniaAlbara);
        }
    }

    private long quantitatServir(Map<KeyLiniaComanda, Long> quantitats, InformacioLiniaComandaDTO liniaComanda) {
        return quantitats.getOrDefault(liniaComanda.id(), 0L);
    }

    private CrearMovimentSortidaRequest buildRequest(Albara albara, LiniaAlbara liniaAlbara, InformacioLiniaComandaDTO liniaComanda, long quantitat) {
        return CrearMovimentSortidaRequestImpl.builder()
                .articleClient(liniaAlbara.articleClient())
                .empresa(albara.id().empresa())
                .magatzem(albara.magatzem())
                .data(albara.data())
                .quantitat(quantitat)
                .client(albara.client().orElseThrow())
                .liniaComanda(liniaComanda.id())
                .liniaAlbara(liniaAlbara.id())
                .build();
    }

    @JsonDeserialize(builder = CrearAlbaraRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CrearAlbaraRequest {
        String client();
        LocalDate dataAlbara();
        String magatzem();
        List<LiniaServirRequest> liniesServir();
        /** Signatura de la proposta acceptada (retornada per {@code POST /albara/calcular}) */
        String signatura();
        /** Si s'han d'aprofitar els albarans oberts (per defecte sí) */
        @Value.Default default boolean aprofitarAlbaransOberts() { return true; }
        /** Albarans oberts que l'usuari ha descartat aprofitar */
        List<KeyAlbara> albaransNoAprofitar();
        /** Si els albarans que es crein nous han de quedar tancats (per defecte sí) */
        @Value.Default default boolean tancarAlbaransNous() { return true; }
    }

    @JsonDeserialize(builder = LiniaServirRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface LiniaServirRequest {
        KeyLiniaComanda id();
        long quantitat();
    }

}
