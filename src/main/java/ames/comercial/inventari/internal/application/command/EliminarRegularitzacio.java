package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EliminarRegularitzacio {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    public void executar(long id) {
        var moviment = movimentRepository.findById(id)
                .orElseThrow(InventariException.MovimentNoTrobat::new);
        if (!TipusMoviment.REGULARITZACIO.equals(moviment.tipus())) {
            throw new InventariException.NoEsRegularitzacio();
        }
        movimentRepository.deleteById(id);
        actualitzarFitxa.executar(moviment, true);
    }

}
