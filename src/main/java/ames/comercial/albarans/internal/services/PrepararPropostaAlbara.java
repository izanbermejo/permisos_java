package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransObertsAmbLinies.AlbaraAmbLiniesDTO;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse;
import ames.comercial.albarans.internal.services.agrupacio.ModeAgrupacioMapper;
import ames.comercial.comandes.ComandesException.QuantitatServirExcessiva;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Prepara la proposta de creació d'albarans de sortida a partir de les línies de comanda a servir.
 * <p>
 * És la lògica compartida entre la previsualització ({@code POST /albara/calcular}) i la creació
 * ({@code POST /albara}): carrega l'estat (línies de comanda i albarans oberts), en calcula la
 * signatura i la proposta de creació. Garanteix que la proposta que es mostra i la que es crea
 * es calculen exactament igual.
 */
@Component
public class PrepararPropostaAlbara {

    @Autowired IObtenirClientAds obtenirClientAds;
    @Autowired IObtenirMagatzems obtenirMagatzemAds;
    @Autowired ObtenirAlbaransObertsAmbLinies obtenirAlbaransObertsAmbLinies;
    @Autowired IObtenirInformacioLiniesComanda obtenirLiniaComanda;
    @Autowired CalcularCreacioAlbaransSortida calcularCreacioAlbaransSortida;
    @Autowired CalcularSignaturaProposta calcularSignaturaProposta;

    public Resultat preparar(String codiClient, String magatzem, LocalDate dataAlbara, Map<KeyLiniaComanda, Long> quantitats,
                             boolean aprofitarAlbaransOberts, List<KeyAlbara> albaransNoAprofitar) {
        var client = obtenirClientAds.get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));

        // Tipus del magatzem d'origen: determina si els avisos d'embalatge són estrictes (magatzem controlat internament)
        boolean origenControlatInternament = obtenirMagatzemAds.get(magatzem)
                .map(m -> m.isControlatInternament())
                .orElse(false);

        // Estat actual: albarans oberts del client i data (només si es volen aprofitar) + línies de comanda a servir.
        // Es descarten els albarans oberts que l'usuari ha marcat com a no aprofitar.
        List<AlbaraAmbLiniesDTO> albaransOberts = aprofitarAlbaransOberts
                ? obtenirAlbaransObertsAmbLinies.executar(codiClient)
                : List.of();
        if (!albaransNoAprofitar.isEmpty()) {
            Set<KeyAlbara> exclosos = Set.copyOf(albaransNoAprofitar);
            albaransOberts = albaransOberts.stream()
                    .filter(a -> !exclosos.contains(a.idAlbara()))
                    .toList();
        }
        List<InformacioLiniaComandaDTO> linies = obtenirLiniaComanda.executar(new ArrayList<>(quantitats.keySet()));

        // La quantitat a servir no pot superar la pendent de cada línia. El frontend ja ho limita,
        // però es revalida aquí (proposta possiblement obsoleta) per no deixar l'stock en negatiu de forma inconsistent.
        for (var linia : linies) {
            long quantitatServir = quantitats.getOrDefault(linia.id(), 0L);
            if (quantitatServir > linia.quantitatPendent()) {
                throw new QuantitatServirExcessiva(
                        linia.id().comandaFormat() + " / " + linia.id().numeroFormat(),
                        linia.referencia(),
                        quantitatServir,
                        linia.quantitatPendent());
            }
        }

        // Signatura de l'estat (per detectar canvis entre previsualització i confirmació). Inclou la data objectiu:
        // canviar-la fa que la proposta sigui diferent (varien els albarans oberts aprofitables).
        String signatura = calcularSignaturaProposta.executar(dataAlbara, linies, albaransOberts);

        // Proposta de creació segons el mode d'agrupació del client
        var proposta = calcularCreacioAlbaransSortida.executar(
                ModeAgrupacioMapper.from(client), linies, quantitats, albaransOberts, origenControlatInternament, dataAlbara);

        return new Resultat(proposta, signatura, albaransOberts, client);
    }

    public record Resultat(
            CalcularCreacioAlbaransSortidaResponse proposta,
            String signatura,
            List<AlbaraAmbLiniesDTO> albaransOberts,
            ClientAds client
    ) {}

}
