package ames.comercial.edi2.internal.task;

import ames.comercial.edi2.ComandaEDI2Config;
import ames.comercial.edi2.internal.application.ImportarFitxersEdi;
import ames.comercial.server.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@Component
@ConditionalOnProperty(name = "tasca.edis", havingValue = "true", matchIfMissing = false)
public class TascaImportarFitxersEdi {

    static final Logger log = LogManager.getLogger(TascaImportarFitxersEdi.class.getName());

    @Autowired ImportarFitxersEdi importarFitxersEdi;
    @Autowired ComandaEDI2Config ediConfig;

    // 60.000ms (1min)
    @Scheduled(fixedDelay = 60_000, initialDelay = 1)
    public void executar() {
        String pathFitxersEdi = ediConfig.getDirIN();
        File directoriFitxers = new File(pathFitxersEdi);

        // Recorregut únicament dels fitxers
        File[] fitxersDirectori = directoriFitxers.listFiles(File::isFile);
        List<File> llistaFitxersEntrades = fitxersDirectori != null ? Arrays.asList(fitxersDirectori) : List.of();
        for (var fitxerEntrada : llistaFitxersEntrades) {
            try {
                importarFitxersEdi.executar(fitxerEntrada);
            } catch (IOException e) {
                var missatge = "IMPORT_EDI Error en l'importació del fitxer d'edi: " + fitxerEntrada.getName();
                log.error(missatge, e);
                moveToError(fitxerEntrada);
            }
        }
    }

    private void moveToError(File fitxer) {
        Path pathError = Paths.get(ediConfig.getDirERROR()).resolve(fitxer.getName());
        try {
            Files.move(fitxer.toPath(), pathError, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String missatge = String.format( "Error al moure el fitxer d'edi amb errors %s al directori %s", fitxer.getName(), pathError);
            log.error(missatge);
            throw new AppException(missatge);
        }
    }

}
