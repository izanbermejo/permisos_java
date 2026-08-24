package ames.comercial.edi2.internal.application;

import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssignarArtIntComandaEdi {

    @Autowired ComandaEdiRepository comandaEdiRepository;
    @Autowired ObtenirArticleClientAds articleClientAds;

    public Optional<String> executar(KeyArticleClient keyArtCli, KeyComandaEdi keyComEdi){
        var comanda = comandaEdiRepository.obtenirComanda(keyComEdi).orElseThrow(EDIException.ComandaNoTrobada::new);
        articleClientAds.query(keyArtCli).orElseThrow(() -> new ArticleClientNotFound(keyArtCli));
        comanda = comanda.lligar(keyArtCli);
        comandaEdiRepository.save(comanda);
        return comanda.error();
    }
}