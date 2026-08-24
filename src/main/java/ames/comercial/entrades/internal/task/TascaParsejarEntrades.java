package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.ParsejarFitxerEntrades;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades.OrigenEntrada;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaParsejarEntrades {

    @Autowired InboxEntrades inboxEntrades;
    @Autowired ParsejarFitxerEntrades parsejarFitxerEntrades;

    // 60.000ms (1min)
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void executar() {
        // Recorregut de les entrades pendents rebudes a l'inbox per fer el parseig
        // i donar-les d'alta a les taules per a fer l'entrada de comercial i magatzem
        inboxEntrades.obtenirPendents(OrigenEntrada.JSON).forEach((idEntrada, contingut) -> {
            parsejarFitxerEntrades.executar(idEntrada, contingut);
        });
    }

}
