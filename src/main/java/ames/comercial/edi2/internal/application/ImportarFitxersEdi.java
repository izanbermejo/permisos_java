package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.ComandaEDI2Config;
import ames.comercial.edi2.internal.domain.InboxImpl;
import ames.comercial.edi2.internal.infraestructure.capsalera.CapsaleraEdiRepository;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
import ames.comercial.edi2.internal.infraestructure.inbox.InboxEdiRepository;
import ames.comercial.server.exception.AppException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ImportarFitxersEdi {

    static final Logger log = LogManager.getLogger(ImportarFitxersEdi.class.getName());

    @Autowired InboxEdiRepository inboxEdiRepository;
    @Autowired ComandaEDI2Config ediConfig;
    @Autowired ParsejarFitxersInbox parsejarFitxersInbox;
    @Autowired CapsaleraEdiRepository capsaleraEdiRepository;
    @Autowired ComandaEdiRepository comandaEdiRepository;

    //@Transactional
    public void executar(File fitxer) throws IOException {
        var nomFitxer = fitxer.getName();
        var id = inboxEdiRepository.nextId();
        if (inboxEdiRepository.exists(nomFitxer)) {
            log.error("IMPORT_ENTRADES Fitxer d'entrada repetit: {}. Es mou al directori de repetits.", nomFitxer);
            moveToError(fitxer, id);
            return;
        }

        var charset = Charset.forName("windows-1252");
        String contingut = Files.readString(fitxer.toPath(), charset);
        var directoriDesti = path(LocalDateTime.now());

        // S'afegeix a l'inbox d'edi
        var inbox = InboxImpl.builder()
                .id(id)
                .missatge(nomFitxer)
                .path(directoriDesti.getPath())
                .dataReg(LocalDateTime.now())
                .contingut(contingut)
                .build();

        inboxEdiRepository.save(inbox);

        try {
            parsejaFitxer(id, contingut);
            moveToBkp(fitxer);
            inboxEdiRepository.marcaProcessat(id);
        } catch (Exception e) {
            inboxEdiRepository.marcaError(id, e.getMessage());
            moveToError(fitxer, id);
            throw e;
        }
    }

    private void moveToError(File fitxer, long id) {
        Path pathRepe = Paths.get(ediConfig.getDirERROR()).resolve(fitxer.getName());
        try {
            Files.move(fitxer.toPath(), pathRepe, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new AppException(String.format( "Error al moure el fitxer d'entrades de magatzem repetit %s al directori %s", fitxer.getName(), pathRepe));
        }
        inboxEdiRepository.marcaError(id, "ERROR");
    }

    private String moveToBkp(File fitxer) throws IOException {
        var directoriDesti = path(LocalDateTime.now());

        // Crear carpetes si no existeixen
        if (!directoriDesti.exists()) {
            boolean created = directoriDesti.mkdirs();
            if (!created) {
                throw new IOException("No s'han pogut crear els directoris de backup: " + directoriDesti.getAbsolutePath());
            }
        }

        File fitxerDesti = new File(directoriDesti, fitxer.getName());

        // Mou el fitxer
        boolean moved = fitxer.renameTo(fitxerDesti);

        if (!moved) {
            throw new IOException("No s'ha pogut moure el fitxer a backup: " + fitxer.getName());
        }

        return directoriDesti.getAbsolutePath();
    }

    private File path(LocalDateTime dataReg){
        LocalDate data = dataReg.toLocalDate();
        String any = String.valueOf(data.getYear());
        String mes = String.format("%02d", data.getMonthValue());

        String pathBkp = ediConfig.getDirBKP();

        File directoriDesti = new File(pathBkp + "/" + any + "/" + mes);

        return directoriDesti;
    }

    private void parsejaFitxer(long id, String contingut){
        var resultats = parsejarFitxersInbox.executar(id, contingut);
        for (ParsejarFitxersInbox.ResultatParsejat bloc : resultats) {
            capsaleraEdiRepository.save(bloc.capsaleres());
            comandaEdiRepository.save(bloc.comandes());
        }
    }
}