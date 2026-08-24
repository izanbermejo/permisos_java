package ames.comercial.edi2.internal.application;

import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.internal.application.command.ProcessarLiniesComanda;
import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessarComandaEdi {

    @Autowired ComandaEdiRepository comandaEdiRepository;
    @Autowired ProcessarLiniesComanda processarLiniesComanda;

    public void executar(KeyComandaEdi keyComandaEdi, KeyArticleClient keyArticleClient, List<ProcessarLiniesComandaReq> req){
        var comanda = comandaEdiRepository.obtenirComanda(keyComandaEdi).orElseThrow(EDIException.ComandaNoTrobada::new);

        if (comanda.potProcessarComanda()){
            processarLiniesComanda.executar(keyArticleClient, keyComandaEdi, req);
            var comandaProcessada = comanda.marcaComandaProcessada();
            comandaEdiRepository.save(comandaProcessada);
        } else {
            throw new EDIException.ComandaNoEsPotProcessar();
        }
    }
}