package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.PendentConsumirInsuficient;
import ames.comercial.albarans.internal.application.query.ObtenirTraspassosPlataformaPendentConsumir;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Descompta (FIFO) el pendent de consumir de les línies dels albarans de traspàs a plataforma d'una
 * peça i retorna els segments consumits. Cada segment correspon a una línia de traspàs consumida i
 * porta el seu preu, la informació de peça i les dades de l'albarà de traspàs d'origen (per registrar
 * la traçabilitat i poder desfer el consum).
 * <p>
 * Si el pendent de consumir disponible no arriba a la quantitat sol·licitada, es llença
 * {@link PendentConsumirInsuficient} i s'avorta la transacció (bloqueig de sobre-consum).
 */
@Service
public class ConsumirPendentTraspasFIFO {

    @Autowired ObtenirTraspassosPlataformaPendentConsumir obtenirTraspassosPendent;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    public List<SegmentConsum> executar(String magatzemPlataforma, String empresa, KeyArticleClient articleClient, long quantitat) {
        var linies = obtenirTraspassosPendent.executar(magatzemPlataforma, empresa, articleClient);

        long restant = quantitat;
        List<SegmentConsum> segments = new ArrayList<>();
        for (var ltp : linies) {
            if (restant <= 0) {
                break;
            }
            var linia = ltp.linia();
            long aConsumir = Math.min(restant, linia.quantitatPendentConsumir());
            // Es descompta el pendent de consumir de la línia de traspàs i es replica a Advantage (qtypendent)
            var liniaConsumida = linia.consumir(aConsumir);
            liniaAlbaraRepository.save(liniaConsumida);
            ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(liniaConsumida);

            segments.add(new SegmentConsum(aConsumir, linia.articleClient(),
                    linia.id(), ltp.magatzemTraspas(), ltp.dataTraspas(), linia.quantitat()));
            restant -= aConsumir;
        }

        if (restant > 0) {
            throw new PendentConsumirInsuficient(articleClient.artint() + articleClient.clicod(), quantitat, quantitat - restant);
        }

        return segments;
    }

    /**
     * Segment de consum: quantitat consumida d'una línia de traspàs concreta i les dades de la línia
     * de traspàs d'origen (clau, magatzem, data i quantitat total) per a la traçabilitat.
     */
    public record SegmentConsum(
            long quantitat,
            KeyArticleClient articleClient,
            KeyLiniaAlbara origen,
            String magatzemTraspas,
            LocalDate dataTraspas,
            long albtraspasQuantitat
    ) {}

}
