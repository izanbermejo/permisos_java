package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.AlbaransException.PendentServirInsuficient;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.comandes.internal.application.command.ServirLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Serveix (FIFO) les línies de comanda pendents d'un article-client amb la quantitat consumida i
 * retorna els segments servits. Cada segment correspon a una línia de comanda servida i porta la seva
 * clau i la quantitat, per poder vincular el moviment de sortida a la línia de comanda i, en desfer el
 * consum, restaurar-ne el pendent.
 * <p>
 * El criteri FIFO és la <b>data sol·licitada</b> ascendent (desempat per comanda i número de línia).
 * Només es consideren les línies pendents de l'empresa del consum; les línies de stock de seguretat
 * queden excloses (comportament per defecte de {@link IObtenirLiniesComandaPendents}).
 * <p>
 * Si el pendent de servir de les comandes no arriba a la quantitat consumida, es llença
 * {@link PendentServirInsuficient} i s'avorta la transacció (bloqueig).
 */
@Service
public class ServirComandaFIFO {

    @Autowired IObtenirLiniesComandaPendents obtenirLiniesComandaPendents;
    @Autowired ServirLiniaComanda servirLiniaComanda;

    public List<SegmentServir> executar(Empresa empresa, KeyArticleClient articleClient, long quantitat) {
        var linies = obtenirLiniesComandaPendents.executar(articleClient).stream()
                .filter(l -> l.empresa().equals(empresa))
                // FIFO: primer la comanda demanada abans (data sol·licitada), desempat per comanda i número
                .sorted(Comparator
                        .comparing(ObtenirLiniesComandaPendentsResponse::dataSolicitada)
                        .thenComparingLong(l -> l.clauLinia().comanda())
                        .thenComparingLong(l -> l.clauLinia().numero()))
                .toList();

        long restant = quantitat;
        List<SegmentServir> segments = new ArrayList<>();
        for (var linia : linies) {
            if (restant <= 0) {
                break;
            }
            long aServir = Math.min(restant, linia.quantitatPendent());
            if (aServir <= 0) {
                continue;
            }
            servirLiniaComanda.executar(linia.clauLinia(), aServir);
            segments.add(new SegmentServir(linia.clauLinia(), aServir));
            restant -= aServir;
        }

        if (restant > 0) {
            throw new PendentServirInsuficient(articleClient.artint() + articleClient.clicod(), quantitat, quantitat - restant);
        }

        return segments;
    }

    /** Segment de servit: quantitat servida d'una línia de comanda concreta. */
    public record SegmentServir(KeyLiniaComanda clauLinia, long quantitat) {}

}
