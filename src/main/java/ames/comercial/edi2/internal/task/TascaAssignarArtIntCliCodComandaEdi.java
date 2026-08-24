package ames.comercial.edi2.internal.task;

import ames.comercial.edi.internal.application.command.CheckPDF;
import ames.comercial.edi2.internal.application.AssignarArtIntClicodComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@ConditionalOnProperty(name = "tasca.edis", havingValue = "true", matchIfMissing = false)
public class TascaAssignarArtIntCliCodComandaEdi {

    @Autowired AssignarArtIntClicodComandaEdi artIntClicodComandaEdi;
    @Autowired ComandaEdiRepository comandaEdiRepository;

    static final Logger log = LogManager.getLogger(CheckPDF.class.getName());

    @Scheduled(fixedDelay = 180_000, initialDelay = 1)
    @Transactional
    public void executar() {
        List<KeyComandaEdi> pendents = comandaEdiRepository.obtenirComandesPerLligar();
        for (KeyComandaEdi comanda : pendents) {
            try {
                artIntClicodComandaEdi.processarComanda(comanda);
            } catch (Exception e) {
                log.error("Error procesant la comanda {}: {}", comanda, e.getMessage(), e);
            }
        }
    }}
