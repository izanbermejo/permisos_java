package ames.comercial.ofs.internal.application.command;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.service.CompararTerminis;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsEspecialsAction.Artcli;
import ames.comercial.ofs.service.CrearNovaOf;
import ames.comercial.ofs.service.RecalculTerminisOrdreFabricacioEspecials;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class CalcularOfEspecial {

    @Autowired IObtenirLiniesComandaPendents obtenirLiniesComandaPendents;
    @Autowired CrearNovaOf crearNovaOf;
    @Autowired OrdreFabricacioRepository ofRepo;
    @Autowired IObtenirStocks obtenirStocks;

    @Transactional
    public void executar(Artcli article) {
        // Obtenció de les línies pendents amb les línies d'stock incloses
        var liniesPendents = obtenirLiniesComandaPendents.executar(article.clau(), true);
        // Obtenció de l'OF actual (pot ser que no tingui cap)
        var optUltimaOf = ofRepo.get(article.clau());
        // Obtenció de l'stock actual de l'article
        var stock = obtenirStocks.stockTotal(article.clau());
        // Càlcul dels terminis tenint en compte l'stock i les línies pendents de servir
        var terminis = new RecalculTerminisOrdreFabricacioEspecials(stock, liniesPendents).executar();

        if (optUltimaOf.isPresent()) {
            // En cas que existeixi una OF cal comprovar si els terminis són els mateixos
            actualitzarOfExistent(article.clau(), article.codiFabrica(), optUltimaOf.get(), terminis);
        } else {
            // En cas que no existeixi una OF caldrà crear una nova si hi han terminis a fabricar
            if (!terminis.isEmpty())
                crearNovaOf.executar(article.clau(), article.codiFabrica(), terminis);
        }
    }

    private void actualitzarOfExistent(KeyArticleClient articleClient, String codiFabrica, OrdreFabricacio ultimaOf, List<Termini> terminis) {
        // En cas que no hagin terminis (vol dir que no hi ha res pendent de fabricar)
        // i que l'última OF estigui tancada (vol dir que no queda res pendent de rebre)
        // no cal fer res ja que per exemple si l'última OF no està acabada i no hi han terminis
        // s'hauria de crear una nova OF per anul·lar els terminis
        if (terminis.isEmpty() && ultimaOf.isFinalitzada()) {
            return;
        }
        // Si els terminis calculats no són iguals que els de la OF actual cal crear una nova OF
        if (!new CompararTerminis(ultimaOf.terminis(), terminis).executar()) {
            crearNovaOf.executar(articleClient, codiFabrica, terminis);
        }
    }

}
