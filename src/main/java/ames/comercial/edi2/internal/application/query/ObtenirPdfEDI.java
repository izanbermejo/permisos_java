package ames.comercial.edi2.internal.application.query;

import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class ObtenirPdfEDI {

	@Value("${docsapps-folder}") private String ftpPath;

	public File executar(String pathPDF) {
		var file = new File(pathPDF);

		if (file.exists() && file.isFile())
			return Optional.of(file).orElseThrow(AdjuntNotFound::new);
		throw new AdjuntNotFound();
	}

	public String obtenirNomPDF(String pathPDF) {
		var path = Paths.get(pathPDF);
		return path.getFileName().toString();
	}
	
}
