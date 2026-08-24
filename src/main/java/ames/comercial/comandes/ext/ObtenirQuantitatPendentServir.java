package ames.comercial.comandes.ext;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirQuantitatPendentServir implements IObtenirQuantitatPendentServir {

    @Autowired IObtenirLiniesComandaPendents obtenirLiniesComandaPendents;

    @Override
    public long executar(KeyArticleClient articleClient, Empresa empresa) {
        return obtenirLiniesComandaPendents.executar(articleClient)
                .stream().filter(l -> l.empresa().equals(empresa))
                .mapToLong(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                .sum();
    }

}
