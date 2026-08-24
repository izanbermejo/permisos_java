package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.request.LiniaComandaRequest;
import ames.comercial.comandes.request.LiniaComandaRequestImpl;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProcessarLiniesComandaAltaLinia {

    @Autowired ComandaRepository comandaRepo;
    @Autowired CrearLiniaComanda crearLiniaComanda;
    @Autowired CrearComandaEntradaEDI crearComandaEntradaEDI;

    @Transactional
    public void executar(KeyArticleClient articleClient, ProcessarLiniesComandaReq procLinia) {
        // Obtenció de l'articleclient
        var artcli = new ObtenirArticleClientAds().query(articleClient).orElseThrow(() -> new ArticleClientNotFound(articleClient));
        // Obtenció de la comanda
        var optComandaExistent = comandaRepo.findByComandaClient(articleClient.clicod(), procLinia.comandaClient(), artcli.empresa());
        if (optComandaExistent.isPresent()) {
            var comanda = optComandaExistent.get();
            crearLiniaComanda.executar(comanda.codi(), buildRequest(articleClient, procLinia));
        } else {
            // Si no existeix la comanda es dona d'alta la comanda amb la nova línia
            crearComandaEntradaEDI.executar(articleClient, procLinia);
        }
    }

    private LiniaComandaRequest buildRequest(KeyArticleClient articleClient, ProcessarLiniesComandaReq procLinia) {
        var artCli = new ObtenirArticleClientAds().query(articleClient).orElseThrow();
        return LiniaComandaRequestImpl.builder()
                .quantitat(procLinia.quantitat())
                .articleClient(articleClient)
                .dataSolicitada(procLinia.dataSolicitada())
                .dataPrevistaSortida(procLinia.dataPrevistaSortida())
                .dataPrevistaSortidaInterna(procLinia.dataPrevistaSortidaInterna())
                .tipus(procLinia.tipus())
                .preu(artCli.preu())
                .divisa(artCli.divisa())
                .isPreuFixat(false)
                .comentarisClient(procLinia.comentarisClient())
                .comentarisInterns(procLinia.comentarisInterns())
                .build();
    }

}
