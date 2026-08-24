package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Component
public class ActualitzacioPreuArticle {

    @Autowired LiniaComandaRepository liniaComandaRepo;

    @Transactional
    public void executar (KeyArticleClient articleClient, BigDecimal preu, Divisa divisa) {
        // Obtenció de les línies pendents de l'article i filtrar les que ja s'han
        // servit parcialment o be tenen el preu fixat o no son d'una CB
        var liniesCanvi = liniaComandaRepo.findByArticlePendent(articleClient).stream()
                .filter(l -> !l.servida())
                .filter(l-> !l.isPreuFixat())
                .filter(l-> l.comandaBlanca().isEmpty())
                .toList();
        var liniesGuardar = new ArrayList<LiniaComanda>();
        for (var l : liniesCanvi) {
            liniesGuardar.add(l.actualitzacioPreu(preu, divisa));
        }
        // Canvi del nom d'usuari al RequestThread
        RequestThread.set("[ACTPREU]");
        // Es guarden els canvis a les línies de comanda
        liniaComandaRepo.save(liniesGuardar);
        // Es marca com a processada el canvi de preu
        updateAdvantage(articleClient);
    }

    private void updateAdvantage (KeyArticleClient articleClient) {
        PreparedStatementProvider p = connection -> {
            var query = "UPDATE actpre SET actual = 'X' WHERE artint = ? and clicod = ?";
            var prep = connection.prepareStatement(query);
            prep.setString(1, articleClient.artint());
            prep.setString(2,articleClient.clicod());
            return prep;
        };
        new AdvantageDao().executeUpdate(p);
    }

}
