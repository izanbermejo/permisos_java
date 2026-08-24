package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException.StockReservableInsuficientException;
import ames.comercial.inventari.ext.IIncrementarReservaArticleclient;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.inventari.internal.infraestructure.fitxa.FitxaRepository;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IncrementarReservaArticleclient implements IIncrementarReservaArticleclient {

    @Autowired FitxaRepository fitxaRepository;

    @Override
    public void executar(KeyArticleClient articleClient, Empresa empresa, long quantitat) {
        var clauFitxa = KeyFitxa.of(articleClient, empresa.clau(), empresa.magatzem());
        // Comprovació que la fitxa existeix
        fitxaRepository.find(clauFitxa).orElseThrow();
        // Actualització de la reserva
        var updateFet = fitxaRepository.incrementaStockReservat(clauFitxa, quantitat);
        // En cas que no s'hagi pogut incrementar la reserva (perquè no hi ha prou stock disponible), es llença una excepció
        if (updateFet == 0) {
            throw new StockReservableInsuficientException();
        }
    }

}
