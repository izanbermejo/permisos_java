package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.infraestructure.fitxa.FitxaRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActualitzarFitxa {

    @Autowired FitxaRepository fitxaRepository;

    public void executar(Moviment m) {
        executar(m, false);
    }

    public void executar(Moviment m, boolean desfer) {
        var keyFitxa = Moviment.fromMoviment(m);
        // Càlcul de la quantitat a actualitzar segons el tipus de moviment
        var quantitatCalculada = calcularQuantitat(m.tipus(), m.quantitat());

        // En cas que sigui desfer, es nega la quantitat calculada
        if (desfer) {
            quantitatCalculada = -quantitatCalculada;
        }

        // Comprovació si existeix la fitxa
        if (fitxaRepository.find(keyFitxa).isEmpty()) {
            // Si no existeix, es crea una nova fitxa amb la quantitat calculada i la reservada a 0
            fitxaRepository.save(Fitxa.create(keyFitxa, quantitatCalculada));
        } else {
            // Càlcul de la quantitat reservada a actualitzar segons el tipus d'articleclient
            var quantitatReservadaCalculada = calcularQuantitatReservada(m.tipus(), m.articleClient(), quantitatCalculada);
            fitxaRepository.updateStock(Moviment.fromMoviment(m), quantitatCalculada, quantitatReservadaCalculada);
        }
    }

    // En el cas de les sortides l'stock a actualitzar és nega ja que les sortides resten stock
    // i una -sortida (devolució) suma stock
    private long calcularQuantitat(TipusMoviment tipus, long quantitat) {
        if (TipusMoviment.SORTIDA.equals(tipus))
            return -quantitat;
        return quantitat;
    }

    private long calcularQuantitatReservada(TipusMoviment tipus, KeyArticleClient articleClient, long quantitat) {
        // Només afecta la quantitat reservada en el cas que l'articleclient sigui normalitzat, ja que només aquests es poden reservar
        if (!articleClient.isNormalitzat())
             return 0L;
        if (TipusMoviment.SORTIDA.equals(tipus))
            return quantitat;
        return 0L;
    }

}
