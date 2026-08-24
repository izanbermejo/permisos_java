package ames.comercial.magatzem.reports;

import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.ObtenirTrasabilitat;
import ames.comercial.advantage.internal.ObtenirTrasabilitat.TrasabilitatPesa;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class Trasabilitat {

    public ByteArrayOutputStream generateReport(KeyArticleClient keyArticleClient) throws Exception {
        InputStream input = getClass().getResourceAsStream("/jasper/Trasabilitat.jasper");
        final JasperReport report = (JasperReport) JRLoader.loadObject(input);
        final JasperPrint jasperPrint = JasperFillManager.fillReport(report, getParameters(keyArticleClient), new JREmptyDataSource());
        // Exportació a PDF i bolcat en ByteArrayOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JRXlsExporter xlsExporter = new JRXlsExporter();
        xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));
        // Configuració Excel
        SimpleXlsReportConfiguration xlsReportConfiguration = new SimpleXlsReportConfiguration();
        xlsReportConfiguration.setDetectCellType(true);
        xlsReportConfiguration.setWhitePageBackground(false);
        xlsReportConfiguration.setIgnorePageMargins(true);
        xlsReportConfiguration.setPrintPageLeftMargin(25);
        xlsReportConfiguration.setPrintPageTopMargin(25);
        xlsReportConfiguration.setPrintPageBottomMargin(25);
        xlsReportConfiguration.setPrintPageRightMargin(25);
        xlsReportConfiguration.setFreezeRow(4);
        xlsExporter.setConfiguration(xlsReportConfiguration);
        xlsExporter.exportReport();
        return baos;
	}

    private Map<String,Object> getParameters(KeyArticleClient keyArticleClient) throws Exception {
		ObtenirTrasabilitat obtenirTrasabilitat = new ObtenirTrasabilitat();

        final Map<String,Object> parameters = new HashMap<String,Object>();

        List<Map<String, Object>> resultat = new ArrayList<>();

        for (Map<String, TrasabilitatPesa> entrada : obtenirTrasabilitat.get(keyArticleClient)){
            Map<String, Object> fila = new HashMap<>();
            for (Map.Entry<String, TrasabilitatPesa> e : entrada.entrySet()){
                TrasabilitatPesa t = e.getValue();
                fila.put("ART", t.article());
                fila.put("CLICOD", t.clicod());
                fila.put("TM", t.tipusMoviment());
                fila.put("ALBARA", t.albara());
                fila.put("LOT", t.lot());
                fila.put("QUANT", t.quantitat());
                fila.put("EMPRESA", t.empresa());
                fila.put("CLIMOV", t.clientMoviment());
                fila.put("ALBESPE", t.albaraEspecial());
                fila.put("MAGCOD", t.codiMagatzem());
                fila.put("DATA", t.data());
                fila.put("ALBDAT", t.dataAlbara().orElse(null));
            }
            resultat.add(fila);
        }

        parameters.put("res", resultat);
        var art = new ObtenirArticleClientAds().query(keyArticleClient).orElseThrow(() -> new SharedExceptions.ArticleClientNotFound(keyArticleClient));
        parameters.put("artcli", art.article() + art.codiClient());
        parameters.put("denominacio", art.denominacio());
        parameters.put("referencia", art.referencia());
        //parameters.put("stock", adsDao.obtenirQuantitatStockMagatzem(art.getArtInt(), codClient, true));
        parameters.put(JRParameter.REPORT_RESOURCE_BUNDLE, ResourceBundle.getBundle("i18n.localitzaciotrazabilitat.localitzaciotrazabilitat", RequestThread.idioma()));
        parameters.put(JRParameter.REPORT_LOCALE, RequestThread.idioma());
		return parameters;
	}
}