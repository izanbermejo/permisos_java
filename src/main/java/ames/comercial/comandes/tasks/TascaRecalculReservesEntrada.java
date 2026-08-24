package ames.comercial.comandes.tasks;

import ames.comercial.comandes.internal.infraestructure.entradanormalitzat.EntradaNormalitzatRepository;
import ames.comercial.comandes.tasks.action.TascaRecalculReservesEntradaAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TascaRecalculReservesEntrada {

    private static final Logger logger = LoggerFactory.getLogger(TascaRecalculReservesEntrada.class);

    @Autowired EntradaNormalitzatRepository entradaNormalitzatRepo;
    @Autowired TascaRecalculReservesEntradaAction action;

    // 90_000ms (1,5min) per anar actualitzant la tasca
    @Scheduled(fixedDelay = 90_000, initialDelay = 90_000)
    public void executar() {
        entradaNormalitzatRepo.obtenirPendents().forEach(entradaNorm -> {
            var articleClient = entradaNorm.first();
            var empresa = entradaNorm.second();
            try {
                action.run(articleClient, empresa);
            } catch (Exception ex) {
                var msg = String.format("Error TASCA RESERVES NORMALITZATS per artint: %s, clicod: %s, empresa: %s",
                        articleClient.artint(), articleClient.clicod(), empresa.clau());
                logger.error(msg, ex);
            }
        });
    }

}
