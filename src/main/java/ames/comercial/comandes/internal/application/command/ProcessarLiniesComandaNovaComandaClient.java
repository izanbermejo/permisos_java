package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessarLiniesComandaNovaComandaClient {

    @Autowired CrearComandaEntradaEDI crearComandaEntradaEDI;
    @Autowired LiniaComandaRepository liniaComandaRepo;

    public void executar(KeyArticleClient articleClient, ProcessarLiniesComandaReq procLinia) {
        // 1.- Obtenció de la línia actual
        var liniaActual = liniaComandaRepo.find(procLinia.clauLinia().orElseThrow()).orElseThrow(LiniaComandaNoExisteix::new);
        // 2.- Es posa la seva quantitat a 0
        var liniaActualCancelada = liniaActual.cancelar();
        // 3.- Es guarda la linia actual cancel·lada
        liniaComandaRepo.save(liniaActualCancelada);
        // 4.- Es dona d'alta una nova comanda amb la línia a afegir
        crearComandaEntradaEDI.executar(articleClient, procLinia);
    }

}
