package ames.comercial.inventari.internal.application.command;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.ext.IDesferMoviments;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import ames.comercial.inventari.internal.services.desfermoviment.EstrategiesDesferMoviment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DesferMoviments implements IDesferMoviments {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;
    @Autowired EstrategiesDesferMoviment estrategiesDesferMoviment;

    @Override
    public void executar(List<KeyLiniaAlbara> idLiniesAlbara) {
        // Obtenció dels moviments associats a les línies de l'albarà
        var moviments = movimentRepository.findByLiniaAlbara(idLiniesAlbara);
        // Per cada moviment: es reverteix la fitxa d'estoc (pas comú) i s'aplica el pas específic del tipus
        for (var moviment : moviments) {
            actualitzarFitxa.executar(moviment, true);
            estrategiesDesferMoviment.get(moviment.tipus()).desfer(moviment);
        }
        // S'eliminen tots els moviments associats a les línies de l'albarà
        movimentRepository.delete(idLiniesAlbara);
    }

}
