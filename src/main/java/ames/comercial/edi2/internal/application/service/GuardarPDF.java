package ames.comercial.edi2.internal.application.service;

import ames.comercial.edi2.internal.application.query.EliminarPdfEDI;
import ames.comercial.edi2.internal.infraestructure.inbox.InboxEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GuardarPDF {

    @Value("${docsapps-folder}") private String ftpPath;

    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private EliminarPdfEDI eliminarPdfEDI;
    @Autowired
    private InboxEdiRepository inboxEdiRepository;


    public void guardar(long idMissatge, String nomFitxer, InputStream inputStream) throws IOException {
        var inbox = inboxEdiRepository.get(idMissatge).orElseThrow();

        //crea la ruta al directori del pdf
        String directoriPDF = crearRuta();
        //crea la ruta del pdf

        Path rutaPDF = Paths.get(directoriPDF, nomFitxer);
        //crea la carpeta i afegeix el pdf
        Files.createDirectories(rutaPDF.getParent());
        Files.copy(inputStream, rutaPDF, StandardCopyOption.REPLACE_EXISTING);

        //si el missatge ja te pdf l'elimina
        eliminarPdfEDI.eliminar(idMissatge);

        //normalitza el format de la ruta per insertar en bbdd correctament
        String rutaNormalitzada = rutaPDF.toAbsolutePath().toString()
                .replace("\\", "/")   // separadores Windows → Linux
                .replaceFirst("^[A-Za-z]:", "");

        var nouInbox = inbox.canviPdf(rutaNormalitzada, nomFitxer);
        inboxEdiRepository.save(nouInbox);

    }

    public String crearRuta() {
        String any = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String mes = LocalDate.now().format(DateTimeFormatter.ofPattern("MM"));
        return ftpPath + "/comercial/edis_indra/pdf/" + any + "/" + mes + "/";
    };
}
