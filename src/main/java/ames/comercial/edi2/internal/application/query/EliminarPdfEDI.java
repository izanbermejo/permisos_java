package ames.comercial.edi2.internal.application.query;

import ames.comercial.edi2.internal.infraestructure.inbox.InboxEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class EliminarPdfEDI {

    @Autowired InboxEdiRepository inboxEdiRepository;

    public void eliminar(long idMissatge) {
        var inbox = inboxEdiRepository.get(idMissatge).orElseThrow();

        // Si no té PDF ja no cal fer res
        var optPath = inbox.pathPDF();
        if (optPath.isEmpty() || optPath.get().isBlank()) {
            return;
        }

        String path = optPath.get();
        try {
            Files.deleteIfExists(Path.of(path));
        } catch (Exception e) {
            throw new RuntimeException("Error eliminando archivo PDF: " + path, e);
        }

        var nouInbox = inbox.eliminarPath();
        inboxEdiRepository.save(nouInbox);

    }

}
