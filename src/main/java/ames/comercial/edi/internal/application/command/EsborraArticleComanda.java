package ames.comercial.edi.internal.application.command;

import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EsborraArticleComanda {

    @Autowired
    ComandaEDIRepository comandaEDIRepository;
    @Autowired
    QueryRepository queryRepository;

    public EsborraArticleComanda(ComandaEDIRepository comandaEDIRepository) {
        this.comandaEDIRepository = comandaEDIRepository;
    }

    public void executar(Long codiComanda,String codiArticle) {
        if (queryRepository.listArticles(codiComanda).size()==1)
            comandaEDIRepository.updateComandaSetProcessada(codiComanda);
        comandaEDIRepository.deleteLiniesArticle(codiComanda,codiArticle);
    }

}
