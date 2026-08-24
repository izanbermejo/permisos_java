package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.albarans.internal.services.canviardataalbara.EstrategiesCanviarDataAlbara;
import ames.comercial.inventari.ext.IActualitzarDataMoviments;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Rectifica la data d'un albarà de qualsevol tipus. Només es permet mentre l'albarà està obert (validat
 * al domini a {@link ames.comercial.albarans.internal.domain.albara.Albara#canviarData}), i s'admeten
 * tant dates passades com futures.
 * <p>
 * La data de la capçalera és la que es va copiar en crear cada línia als <b>moviments d'inventari</b>
 * ({@code inventari.moviment.data}) i, en els albarans que intervenen en un consum de plataforma, a la
 * <b>traçabilitat</b> {@code albarans.sortides_plataforma} —com a {@code albconsum_data} si l'albarà és
 * el consum, o com a {@code albtraspas_data} si és el traspàs consumit. El canvi s'hi ha de propagar
 * perquè no quedin amb la data antiga. Tot es fa dins la mateixa transacció, i la capçalera es
 * reinsereix a Advantage per actualitzar-hi {@code albcap.albdat}.
 */
@Service
public class CanviarDataAlbara {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired IActualitzarDataMoviments actualitzarDataMoviments;
    @Autowired EstrategiesCanviarDataAlbara estrategiesCanviarDataAlbara;

    @Transactional
    public void executar(KeyAlbara idAlbara, CanviarDataAlbaraRequest request) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        // Si la data no canvia no es fa res: s'evita replicar la capçalera i reescriure moviments
        if (request.data().equals(albara.data())) {
            return;
        }

        var nouAlbara = albara.canviarData(request.data());
        albaraRepository.save(nouAlbara);
        // A Advantage la data de la capçalera (albdat) s'actualitza reinserint l'albcap
        ReplicaAdvantage.instance().addAlbaraDeleteInsert(nouAlbara);

        // Propagació comuna: els moviments d'inventari de les línies van heretar la data de la capçalera
        var linies = liniaAlbaraRepository.findByAlbara(idAlbara);
        var idLinies = linies.stream()
                .map(LiniaAlbara::id)
                .toList();
        actualitzarDataMoviments.executar(idLinies, nouAlbara.data());
        // Propagació específica del tipus d'albarà (traçabilitat de consums de plataforma, pendents
        // d'abonar dels traspassos abonables)
        estrategiesCanviarDataAlbara.get(albara.tipus()).propagar(albara, nouAlbara, linies);
    }

    @JsonDeserialize(builder = CanviarDataAlbaraRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CanviarDataAlbaraRequest {
        /** Nova data de l'albarà; s'admeten dates passades i futures */
        LocalDate data();
    }

}
