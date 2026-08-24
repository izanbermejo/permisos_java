package ames.comercial.edi.internal.application.command;

import ames.comercial.edi.ComandaEDIConfig;
import ames.comercial.edi.ComandesEDIException;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.shared.Dates;
import ames.comercial.shared.KeyArticleAmes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "comandesedi.pdfcheck.schedule.enabled", havingValue = "true")
@Component
@Service
public class CheckPDF {

	ApplicationEventPublisher applicationEventPublisher;
	private final ComandaEDIConfig config;
	ComandaEDIRepository comandaEDIRepo;
	@Autowired
	QueryRepository queryRepository;
	static final Logger log = LogManager.getLogger(CheckPDF.class.getName());

	public CheckPDF(ComandaEDIConfig config,ComandaEDIRepository comandaEDIRepo,
					ApplicationEventPublisher applicationEventPublisher) {
		this.config=config;
		this.comandaEDIRepo=comandaEDIRepo;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Scheduled(fixedDelayString = "${comercial.comandesedi.pdfcheckscheduled}", initialDelay = 1000 )
	public void check() {

		log.trace("Proces de check i enregistrament de path del EDI en format PDF Iniciat");
		List<String> totesLesComandesNoProcessades = listOrdersPDFPaths();
		log.info("Llegeixo el directori: " + config.getDirPDF() + " per localitzar els PDFs associats a les comandes EDI");

		Path pathPDFs = Paths.get(config.getDirPDF()+ "/" + Dates.getCurrentDateYear());// + "/" + Dates.getCurrentDateMonth());

		if (!Files.exists(pathPDFs)) {
			log.error("Directori " + pathPDFs + " no trobat.");
			return;
		}

		for (String pathEDI : totesLesComandesNoProcessades) {
			String nomFitxer = Paths.get(pathEDI).getFileName().toString();
			String regex = "^\\d{9}_\\d{8}_\\d{9}\\.TXT$";
			Pattern pattern = Pattern.compile(regex);

			if (pattern.matcher(nomFitxer).matches()) {


				Optional<Path> pathPDF = locateFileInPathWithNamePattern(pathPDFs, cropFileExtension(nomFitxer.split("_")[0]) + ".pdf");
				if (!pathPDF.isEmpty()) {
					log.info("L'he trobat actualitzo el path a la BBDD");
					comandaEDIRepo.setPathPDFofPathEDIFiles(pathPDF.get().toString().replaceAll("\\\\","/"), pathEDI);
				} else {
					//addCommentsToCommand(comanda.codi().get(),"No localitzo el PDF, demà ho provem de nou");
					log.warn("[alert][logis]No localitzo el PDF del fitxer edi-> " + pathEDI + ", demà ho provem de nou");
				}
			} else {
				//addCommentsToCommand(comanda.codi().get(),"Fitxer EDI en format antic. No podem deduir ruta al PDF");
				log.warn("[alert][logis]Fitxer EDI "+nomFitxer+" en format antic. No podem deduir ruta al PDF");
			}
//            Path pdfPath = Paths.get(pdfPathString);
//            if (!Files.exists(pdfPath)) {
//                log.info("El PDF no esta a la ruta especificada: " + pdfPath);
//                Path alternativePathPDF=null;
//                if (Dates.getCurrentDateMonth().equals("12") && Dates.getCurrentDateDay().equals("31"))
//                    alternativePathPDF=Paths.get(config.getDirpdfprefix() + "//"+Dates.getCurrentDateYear(1)+"//01//" + pdfPath.getFileName());
//                else
//                    alternativePathPDF=Paths.get(config.getDirpdfprefix() + "//"+Dates.getCurrentDateYear()+"//"+Dates.getCurrentDateMonth(1) + "//" + pdfPath.getFileName());
//                if (!Files.exists(alternativePathPDF))
//                        log.warn("[alert][logis]El PDF no esta a la ruta especificada: " + pdfPath + " i tampoc a alternativa: " + alternativePathPDF);
//                else {
//                    log.info("PDF trobat a: " + alternativePathPDF +" actualitzo la BBDD amb aquesta dada");
//                    changePathPDF(pdfPath.toString(),alternativePathPDF.toString());
//                }
//            }
		}
//        for (DadesComandaEDINoJSON comanda : totesLesComandesNoProcessades) {
//            if (!Files.exists(Paths.get(comanda.pathPDF())))
//                log.warn("[alert][logis]El PDF no esta a la ruta especificada: " + comanda.pathPDF());
//        }
		log.trace("Proces de check i enregistrament de path del EDI en format PDF finalitzat");
	}

	private List<String> listOrdersPDFPaths() {
		return queryRepository.listComandesSensePathPDF();
	}

//	private List<DadesLiniaEDI> listLinesEDI(Long codi_comanda) {
//		List<DadesLiniaEDI> list = comandaEDIRepo.listLinies(codi_comanda);
//		return list;
//	}

//	private List<DadesLiniaEDI> listLinesEDI(Long codi_comanda, String codi_article) {
//		List<DadesLiniaEDI> list = comandaEDIRepo.listLinies(codi_comanda, codi_article);
//		return list;
//	}
//
//	private List<KeyArticleAmes> listProducts(Long codi_comanda) {
//		List<KeyArticleAmes> list = comandaEDIRepo.listArticles(codi_comanda);
//		return list;
//	}

	private String cropFileExtension(String nombreArchivo) {
		return nombreArchivo.replaceFirst("[.][^.]+$", "");
	}

	private Optional<Path> locateFileInPathWithNamePattern(Path path, String namePattern) {
		try (Stream<Path> archivos = Files.walk(path)) {
			Optional<Path> archivoEncontrado = archivos
					.filter(Files::isRegularFile) // Solo archivos regulares
					.filter(f -> f.getFileName().toString().endsWith(namePattern)) // Que terminen en xxxx.TXT
					.findFirst(); // Obtener el primer archivo encontrado

			// Comprobar si se encontró algún archivo
			if (archivoEncontrado.isPresent()) {
				return archivoEncontrado;
			} else {
				return Optional.empty();
			}
		} catch (IOException e) {
			return Optional.empty();
		}
	}
	
}
