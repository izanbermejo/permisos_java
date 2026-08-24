package ames.comercial.albarans.internal.infraestructure.adjunt;

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
import java.util.stream.Stream;

@Component
public class AdjuntAlbaraRepositoryFile implements AdjuntAlbaraRepository {

    @Value("${docsapps-folder}") private String ftpPath;

    @Override
    public void add(long numero, InputStream stream, String nomFitxer) {
        try {
            var comandesFolder = comandesFolder(numero);
            if (!comandesFolder.exists())
                comandesFolder.mkdir();
            var targetFile = new File(comandesFolder, nomFitxer);
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            stream.close();
        } catch (IOException error) {
            throw new IOExceptionWrapper(error, nomFitxer);
        }
    }

    private File comandesFolder (long numero) {
        return new File(baseFolder(), String.format("%d", numero));
    }

    /** Retorna el directori de les comandes. Si no existeix es llença un error. */
    private File baseFolder () {
        var comandesFolder = new File(ftpPath, "comercial/comandes");
        if (!comandesFolder.exists())
            throw new PathNotFound(comandesFolder);
        return comandesFolder;
    }

    @Override
    public void removeAll(long numero) {
        var folder = comandesFolder(numero);
        if (folder.exists()) {
            Stream.of(folder.listFiles()).forEach(File::delete);
            folder.delete();
        }
    }

    @Override
    public void remove(long numero, String nomFitxer) {
        var file = new File(comandesFolder(numero), nomFitxer);
        if (file.exists() && file.isFile())
            file.delete();
    }

    @Override
    public Optional<File> find(long numero, String nomFitxer) {
        var file = new File(comandesFolder(numero), nomFitxer);
        if (file.exists() && file.isFile())
            return Optional.of(file);
        return Optional.empty();
    }

}
