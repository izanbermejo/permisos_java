package ames.comercial.entrades.internal.application;

import ames.comercial.entrades.internal.infraestructure.InboxEntrades;
import ames.comercial.entrades.internal.infraestructure.InboxEntrades.OrigenEntrada;
import ames.comercial.server.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImportarFitxerEntrades {

    @Value("${docsapps-folder}") private String pathDocsapps;

    static final Logger log = LogManager.getLogger(ImportarFitxerEntrades.class.getName());

    @Autowired InboxEntrades inboxEntrades;

    @Transactional
    public void executar(File fitxer) throws IOException {
        var nomFitxer = fitxer.getName();
        if (inboxEntrades.exists(nomFitxer)) {
            log.error("IMPORT_ENTRADES Fitxer d'entrada repetit: {}. Es mou al directori de repetits.", nomFitxer);
            moveToRepetit(fitxer);
            return;
        }
        var charsetWindows1252 = Charset.forName("windows-1252");
        String contingut = Files.readString(Path.of(fitxer.getAbsolutePath()), charsetWindows1252);
        // S'afegeix a l'inbox d'entrades i s'elimina el fitxer
        inboxEntrades.save(nomFitxer, contingut, OrigenEntrada.TXT);
        fitxer.delete();
        log.error("Fitxer d'entrada rebut a l'inbox: {}", nomFitxer);
    }

    private void moveToRepetit(File fitxer) {
        Path pathRepe = Paths.get(pathDocsapps + "/comercial/entrades/repe").resolve(fitxer.getName());
        try {
            Files.move(fitxer.toPath(), pathRepe, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException(String.format( "Error al moure el fitxer d'entrades de magatzem repetit %s al directori %s", fitxer.getName(), pathRepe));
        }
    }

}
