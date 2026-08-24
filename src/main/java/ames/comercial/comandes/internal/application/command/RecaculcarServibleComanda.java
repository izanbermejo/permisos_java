package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.internal.service.ICalcularServible;
import org.springframework.stereotype.Component;

@Component
public class RecaculcarServibleComanda {

    ComandaRepository comandaRepo;
    LiniaComandaRepository liniaComandaRepository;
    ICalcularServible calcularServible;

    public RecaculcarServibleComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepository,
                                     ICalcularServible calcularServible) {
        this.comandaRepo = comandaRepo;
        this.liniaComandaRepository = liniaComandaRepository;
        this.calcularServible = calcularServible;
    }

    public void executar (long idComanda) {
        var comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
        var linies = liniaComandaRepository.findByComanda(idComanda);
        comanda.canviarServible(calcularServible.calcula(linies));
        comandaRepo.save(comanda);
    }

}
