package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.IObtenirEmpresesAds;
import ames.comercial.advantage.IObtenirEmpresesAds.EmpresaAds;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.server.I18N;
import ames.comercial.shared.SharedExceptions.PdfErrorGeneracio;
import com.google.common.collect.Maps;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GenerarPdfCertificatAlbara {

    static final Logger log = LogManager.getLogger(GenerarPdfCertificatAlbara.class.getName());

    private static final String PROPERTIES_FILE = "i18n/albarans/report_albara";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Set<String> VALID_TIPUS = Set.of("abc", "afbf", "ttf", "tn", "anbn");
    // Certificates ABC, AfBf, TTf use .JPG; TN, AnBn use .jpg
    private static final Map<String, String> CERT_EXT = Map.of(
            "abc", ".JPG",
            "afbf", ".JPG",
            "ttf", ".JPG",
            "tn", ".jpg",
            "anbn", ".jpg"
    );
    // Idioma mapping: locale language → certificate file suffix
    private static final Map<String, String> IDIOMA_MAP = Map.of(
            "ca", "ca",
            "es", "esp",
            "en", "eng",
            "de", "deu",
            "fr", "fra",
            "it", "ita"
    );
    private static final Map<String, String> LOGO_MAP = Map.ofEntries(
            Map.entry("AMES", "img/ames.bmp"),
            Map.entry("CMA", "img/cma.bmp"),
            Map.entry("HUNGA", "img/hunga.bmp"),
            Map.entry("MEDIC", "img/medic.bmp"),
            Map.entry("ALME", "img/alme.bmp"),
            Map.entry("APLI", "img/apli.bmp"),
            Map.entry("SIMET", "img/simet.bmp"),
            Map.entry("SIMO", "img/simo.bmp"),
            Map.entry("UTISA", "img/utisa.bmp"),
            Map.entry("TAMAR", "img/tamar.bmp"),
            Map.entry("REESE", "img/reese.bmp"),
            Map.entry("FUSIOMOL", "img/fusiomol.bmp"),
            Map.entry("WUHU", "img/wuhu.bmp"),
            Map.entry("MTY", "img/mty.bmp")
    );

    @Autowired AlbaraRepository albaraRepo;
    @Autowired IObtenirEmpresesAds obtenirEmpresesAds;

    public byte[] run(KeyAlbara idAlbara, String tipusCertificat, String idioma) {
        var tipusLower = tipusCertificat.toLowerCase();
        if (!VALID_TIPUS.contains(tipusLower)) {
            throw new IllegalArgumentException("Tipus de certificat no vàlid: " + tipusCertificat);
        }
        Locale locale = new Locale(idioma);
        var albara = albaraRepo.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        var empresa = obtenirEmpresesAds.get(idAlbara.empresa()).orElseThrow();

        var bean = JasperCertificatImpl.builder()
                .logoEmpresaPath(resolveLogoPath(empresa.codi()))
                .nomEmpresa(empresa.descripcio())
                .textCertificacio(buildTextCertificacio(albara, tipusLower, locale))
                .imageCertificatPath(buildImagePath(tipusLower, idioma))
                .pagina(getLiteral(locale, "pagina"))
                .peuRegistre(getLiteral(locale, "peu_registre"))
                .build();

        try (InputStream input = getClass().getResourceAsStream("/jasper/CertificatQualitat.jasper")) {
            JasperReport rep = (JasperReport) JRLoader.loadObject(input);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(JasperFillManager.fillReport(rep, Maps.newHashMap(), new JRBeanCollectionDataSource(List.of(bean)))));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return outStream.toByteArray();
        } catch (Exception err) {
            log.error("ERROR GENERANT PDF CERTIFICAT ALBARA: ", err);
            throw new PdfErrorGeneracio(err);
        }
    }

    private String getLiteral(Locale locale, String key, Object... params) {
        return I18N.getLiteral(PROPERTIES_FILE, locale, key, params);
    }

    private String resolveLogoPath(String codiEmpresa) {
        return LOGO_MAP.getOrDefault(codiEmpresa.toUpperCase(), "img/ames.bmp");
    }

    private String buildTextCertificacio(Albara albara, String tipusCertificat, Locale locale) {
        var numeroAlbara = albara.id().codiFormat();
        var dataAlbara = albara.data().format(DATE_FMT);
        return getLiteral(locale, "certtext." + tipusCertificat, numeroAlbara, dataAlbara);
    }

    private String buildImagePath(String tipusCertificat, String idioma) {
        var idiomaFitxer = IDIOMA_MAP.getOrDefault(idioma.toLowerCase(), "eng");
        var ext = CERT_EXT.getOrDefault(tipusCertificat, ".JPG");
        return "img/cert_qua_" + tipusCertificat.toLowerCase() + "_" + idiomaFitxer + ext;
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperCertificat {
        String getLogoEmpresaPath();
        String getNomEmpresa();
        String getTextCertificacio();
        String getImageCertificatPath();
        String getPagina();
        String getPeuRegistre();
    }

}
