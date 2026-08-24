package ames.comercial.clients.internal.infraestructure.adjunt;

import ames.comercial.shared.SharedExceptions.IOExceptionWrapper;
import ames.comercial.shared.SharedExceptions.PathNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Component
public class AdjuntClientRepositoryFile implements AdjuntClientRepository {

    @Value("${docsapps-folder}") private String ftpPath;

    @Override
    public void add(String client, InputStream stream, String nomFitxer) {
        try {
            var clientFolder = clientFolder(client);
            if (!clientFolder.exists())
                clientFolder.mkdir();
            var targetFile = new File(clientFolder, nomFitxer);
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            stream.close();
        } catch (IOException error) {
            throw new IOExceptionWrapper(error, nomFitxer);
        }
    }

    private File clientFolder (String client) {
        return new File(baseFolder(), client);
    }

    /** Retorna el directori de les comandes. Si no existeix es llença un error. */
    private File baseFolder () {
        var comandesFolder = new File(ftpPath, "comercial/adjunts_clients");
        if (!comandesFolder.exists())
            throw new PathNotFound(comandesFolder);
        return comandesFolder;
    }

    @Override
    public void remove(String codiClient, String nomFitxer) {
        var file = new File(clientFolder(codiClient), nomFitxer);
        if (file.exists() && file.isFile())
            file.delete();
    }

    @Override
    public Optional<File> find(String codiClient, String nomFitxer) {
        var file = new File(clientFolder(codiClient), nomFitxer);
        if (file.exists() && file.isFile())
            return Optional.of(file);
        return Optional.empty();
    }

}
