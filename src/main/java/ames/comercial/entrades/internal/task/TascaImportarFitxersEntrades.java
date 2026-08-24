package ames.comercial.entrades.internal.task;

import ames.comercial.entrades.internal.application.ImportarFitxerEntrades;
import ames.comercial.server.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
@ConditionalOnProperty(name = "tasca.entrades", havingValue = "true", matchIfMissing = false)
public class TascaImportarFitxersEntrades {

    static final Logger log = LogManager.getLogger(TascaImportarFitxersEntrades.class.getName());

    @Value("${docsapps-folder}") private String pathDocsapps;

    @Autowired ImportarFitxerEntrades importarFitxerEntrades;

    // 60.000ms (1min)
    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void executar() {
        String pathFitxersEntrades = pathDocsapps + "/comercial/entrades";
        File directoriFitxers = new File(pathFitxersEntrades);

        // Recorregut únicament dels fitxers
        File[] fitxersDirectori = directoriFitxers.listFiles(File::isFile);
        List<File> llistaFitxersEntrades = fitxersDirectori != null ? List.of(fitxersDirectori) : List.of();
        for (var fitxerEntrada : llistaFitxersEntrades) {
            try {
                importarFitxerEntrades.executar(fitxerEntrada);
            } catch (IOException e) {
                var missatge = "IMPORT_ENTRADES Error en l'importació del fitxer d'entrades de magatzem: " + fitxerEntrada.getName();
                log.error(missatge, e);
                moveToError(fitxerEntrada);
            }
        }
    }

    private void moveToError(File fitxer) {
        Path pathError = Paths.get(pathDocsapps + "/comercial/entrades/error").resolve(fitxer.getName());
        try {
            Files.move(fitxer.toPath(), pathError, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String missatge = String.format( "Error al moure el fitxer d'entrades de magatzem amb errors %s al directori %s", fitxer.getName(), pathError);
            log.error(missatge);
            throw new AppException(missatge);
        }
    }

}
