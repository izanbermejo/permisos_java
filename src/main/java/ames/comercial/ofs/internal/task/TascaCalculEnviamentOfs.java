package ames.comercial.ofs.internal.task;

import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsEspecialsAction;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsMedicalAction;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsNormalitzatsAction;
import ames.comercial.ofs.internal.task.actions.TascaEnviamentOfsAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.ofs", havingValue = "true", matchIfMissing = false)
public class TascaCalculEnviamentOfs {

    private static final Logger logger = LoggerFactory.getLogger(TascaCalculEnviamentOfs.class);

    @Autowired
    TascaCalcularOfsEspecialsAction tascaCalcularOfsEspecialsAction;
    @Autowired
    TascaCalcularOfsNormalitzatsAction tascaCalcularOfsNormalitzatsAction;
    @Autowired
    TascaCalcularOfsMedicalAction tascaCalcularOfsMedicalAction;
    @Autowired
    TascaEnviamentOfsAction tascaEnviamentOfsAction;

    @Scheduled(cron = "0 0 1 ? * TUE-SAT")
    public void executar() {
        executarTasca(tascaCalcularOfsNormalitzatsAction::executar,"CÀLCUL OFs Normalitzats");
        executarTasca(tascaCalcularOfsMedicalAction::executar,"CÀLCUL OFs Medical");
        executarTasca(tascaCalcularOfsEspecialsAction::executar,"CÀLCUL OFs Especials");
        executarTasca(tascaEnviamentOfsAction::executar,"ENVIAMENT OFs");
    }

    private void executarTasca(Runnable r, String missatge) {
        logger.info("INICI {}", missatge);
        try {
            r.run();
        } catch (Exception e) {
            logger.error("ERROR " + missatge, e);
        }
        logger.info("FI {}", missatge);
    }

}
