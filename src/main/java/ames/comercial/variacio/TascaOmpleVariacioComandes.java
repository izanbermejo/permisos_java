package ames.comercial.variacio;

import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.internal.infraestructure.variacio.VariacioComandaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "tasca.variacio", havingValue = "true", matchIfMissing = false)
public class TascaOmpleVariacioComandes {

    private static final Logger logger = LoggerFactory.getLogger(TascaOmpleVariacioComandes.class);

    @Autowired VariacioComandaRepository variacioRepo;
    @Autowired LiniaComandaRepository liniaRepo;
    @Autowired AfegirVariacio afegirVariacio;

    // Cada hora
    @Scheduled(fixedDelay = 3_600_000, initialDelay = 600_000)
    public void executar() {
        // Obtenció de les línies que passen del moment
        var liniesProcessar = liniaRepo.findByMomentPosterior(variacioRepo.maxDatareg());
        // Registre log
        logger.info(" OMPLIR VARIACIÓ AMB REGISTRES: {}", liniesProcessar.size());
        // Per cada linia a processar s'ha de mirar si té versió anterior
        for (var parella : liniesProcessar) {
            var datareg = parella.first();
            var linia = parella.second();
            var optRegistreAnterior = liniaRepo.findVersioAnterior(linia.id(), datareg);
            // En cas d'existir es fa un INSERT en negatiu a la variació amb la data de variació de la línia que ha canviat
            if (optRegistreAnterior.isPresent()) {
                var registreAnterior = optRegistreAnterior.get();
                afegirVariacio.registraAmbAnterior(linia, datareg, registreAnterior.second(), registreAnterior.first());
            } else {
                afegirVariacio.registraSenseAnterior(linia, datareg);
            }
        }
    }

}
