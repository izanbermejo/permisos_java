package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ext.IProcessarLiniesComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.informacioedi.InformacioEdiRepository;
import ames.comercial.comandes.internal.infraestructure.informacioedi.InformacioEdiRepository.InformacioEdi;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessarLiniesComanda implements IProcessarLiniesComanda {

    @Autowired ComandaRepository comandaRepo;
    @Autowired ProcessarLiniesComandaAltaLinia procesAltaLinia;
    @Autowired ProcessarLiniesComandaActualitzaLinia procesActualitzarLinia;
    @Autowired ProcessarLiniesComandaNovaComandaClient processarNovaComandaClient;
    @Autowired ActualitzarEstatComanda actualitzarEstatComanda;
    @Autowired InformacioEdiRepository informacioEdiRepository;

    @Override
    public void executar(KeyArticleClient articleClient, KeyComandaEdi comandaEdi, List<ProcessarLiniesComandaReq> reqLinies) {
        // Es guarda la informació de les línies processades per EDI per a poder fer consultes futures
        saveInformacioEdi(articleClient, comandaEdi, reqLinies);
        // S'afegeix el prefix EDI_ al nom d'usuari per a diferenciar les entrades que han sigut per EDI
        RequestThread.set("EDI_" + RequestThread.nomUsuari());
        for (var procLinia : reqLinies) {
            // En cas que no estigui informada la clau de la línia
            // vol dir que es una nova línia
            if (procLinia.clauLinia().isEmpty()) {
                procesAltaLinia.executar(articleClient, procLinia);
                continue;
            }
            // Obtenció de la comanda
            var clauLinia = procLinia.clauLinia().get();
            var comanda = comandaRepo.find(clauLinia.comanda()).orElseThrow(ComandaNoExisteix::new);
            // Comprovar si ha canviat la comanda del client
            if (comanda.informacioClient().identificador().equals(procLinia.comandaClient())) {
                procesActualitzarLinia.executar(procLinia);
                actualitzarEstatComanda.executar(comanda.codi());
            } else {
                processarNovaComandaClient.executar(articleClient, procLinia);
            }
        }
    }

    public void saveInformacioEdi (KeyArticleClient articleClient, KeyComandaEdi comandaEdi, List<ProcessarLiniesComandaReq> reqLinies) {
        var informacionsEdi = new ArrayList<InformacioEdi>();
        for (var procLinia : reqLinies) {
            informacionsEdi.add(new InformacioEdi(procLinia.comandaClient(),
                    procLinia.dataSolicitada(),
                    articleClient,
                    procLinia.quantitat(),
                    comandaEdi));
        }
        informacioEdiRepository.save(informacionsEdi);
    }

}
