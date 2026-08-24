package ames.comercial.edi2.internal.application;

import ames.comercial.edi2.ComandaEDI2Config;
import ames.comercial.edi2.internal.infraestructure.inbox.InboxEdiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@Service
public class LligarEdiPdf {

    @Autowired InboxEdiRepository inboxEdiRepository;
    @Autowired ComandaEDI2Config ediConfig;

    @Transactional
    public void executar(String txtMissatge, long idMissatge) throws IOException {
        // Procés de tots el registres que no tenen el PDF lligat
        String idFile = getIdFromTxt(txtMissatge);
        String pathPdf = buscarPdf(idFile).orElseThrow(() -> new IllegalStateException("No s'ha trobat el PDF"));
        inboxEdiRepository.marcaPdfLligat(idMissatge, pathPdf);

    }

    private Optional<String> buscarPdf(String txtMissatge) {
        LocalDate avui = LocalDate.now();
        YearMonth mesActual = YearMonth.from(avui);
        YearMonth mesAnterior = mesActual.minusMonths(1);

        // Busco el PDF en la carpeta del mes actual i del mes anterior
        // i d'aquest manera s'evita perdre PDF's que puguin entrar el dia 1 de mes
        // quan el TXT va entrar a finals del mes anterior
        return buscarPdfCarpeta(txtMissatge, mesActual)                 // Primer busquem en el mes actual
                .or(() -> buscarPdfCarpeta(txtMissatge, mesAnterior));  // Si no el troba busquem en el mes anterior
    }

    private Optional<String> buscarPdfCarpeta(String txtMissatge, YearMonth anyMes) {
        // Buscar segons les normes si el txt està dins la carpeta any/mes
        File carpeta = getCarpetaPdf(anyMes);
        if (!carpeta.exists() || !carpeta.isDirectory()) return Optional.empty();

        File[] fitxersPdf = getFitxersPdf(carpeta);
        if (fitxersPdf == null) return Optional.empty();

        for (File pdf : fitxersPdf) {
            String idPdf = getIdFromPdf(pdf.getName());
            if (idPdf.equals(txtMissatge)) {
                return Optional.of(pdf.getAbsolutePath());
            }
        }
        return Optional.empty();
    }

    private File getCarpetaPdf(YearMonth ym) {
        String any = String.valueOf(ym.getYear());
        String mes = String.format("%02d", ym.getMonthValue());
        return new File(ediConfig.getDirPDF() + any + "/" + mes);
    }

    private File[] getFitxersPdf(File carpeta) {
        return carpeta.listFiles(f -> f.isFile() && f.getName().toLowerCase().endsWith(".pdf"));
    }

    private String getIdFromTxt(String nomFitxer) {
        //S'agafa nomes l'id del txt (primera part del nom, fins la primera _) per identificar el pdf.
        return nomFitxer.split("_")[0];
    }

    private String getIdFromPdf(String nomFitxer) {
        String senseExtensio = nomFitxer.replace(".pdf", "");
        String[] parts = senseExtensio.split("_");
        return parts[parts.length - 1];
    }
}
