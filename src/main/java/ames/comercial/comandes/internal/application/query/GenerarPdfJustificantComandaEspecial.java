package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.server.I18N;
import ames.comercial.shared.Dates;
import ames.comercial.shared.SharedExceptions.PdfErrorGeneracio;
import ames.comercial.shared.TipusArticleClient;
import ames.comercial.shared.TipusFormatDecimal;
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GenerarPdfJustificantComandaEspecial {

    static final Logger log = LogManager.getLogger(GenerarPdfJustificantComandaEspecial.class.getName());

    private static final String PROPERTIES_FILE = "i18n/comandes/report_justificant_comanda";

    @Autowired ComandaRepository comandaRepo;
    @Autowired LiniaComandaRepository liniaComandaRepo;

    public byte[] run(long idComanda, String idioma, TipusFormatDecimal tipusFormatDecimal, Optional<TipusLiniaComanda> tipusComanda) {
        Locale locale = new Locale(idioma);
        // Number format del locale
        var numFormat = buildNumberFormat(tipusFormatDecimal);
        var numDecimalFormat = buildDecimalNumberFormat(tipusFormatDecimal);
        // Obtenció de la comanda i les seves línies
        var comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
        var linies = liniaComandaRepo.findByComanda(idComanda)
                .stream()
                .filter(liniaComanda -> !liniaComanda.servida())
                .filter(linia -> {
                    if (tipusComanda.isPresent()) {
                        return linia.tipus() == tipusComanda.get();
                    } else {
                        return linia.tipus() == TipusLiniaComanda.FERM
                                || linia.tipus() == TipusLiniaComanda.ORIENTATIU;
                    }
                })
                .sorted(Comparator.comparing(LiniaComanda::referencia))
                .toList();
        // Build del bean pel jasper
        var jasperJustificant = JasperJustificantComandaEspecialImpl.builder()
                .data(buildDataJustificant(comanda, locale))
                .justificant(buildJustificant(comanda, locale))
                .client(buildClient(comanda))
                .listReferencies(buildReferencies(linies))
                .listQuantitats(buildQuantitats(linies, numFormat))
                .listPreusBruts(buildPreus(linies, numDecimalFormat))
                .listDatesSolicitades(buildDatesSolicitades(linies, locale))
                .listDisponibilitatsPrevistes(buildDisponibiliatPrevista(linies,locale))
                .titolAdresa(buildTitolAdresa(locale))
                .titolReferencia(getLiteral(locale, "referencia"))
                .titolQuantitat(getLiteral(locale, "quantitat"))
                .titolPreuBrut(getLiteral(locale, "preu_brut"))
                .titolDataSolicitada(getLiteral(locale, "data_solicitada"))
                .titolDisponibilitatPrevista(getLiteral(locale,"disponibilitat_prevista"))
                .infoQuantitats(getLiteral(locale, "info_quantitats"))
                .pagina(getLiteral(locale, "pagina"))
                .build();
        // Generació del PDF
        try (InputStream input = getClass().getResourceAsStream("/jasper/JustificantComandaEspecial.jasper")) {
            JasperReport rep = (JasperReport) JRLoader.loadObject(input);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of(jasperJustificant));
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(JasperFillManager.fillReport(rep, Maps.newHashMap(), dataSource)));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return outStream.toByteArray();
        } catch (Exception err) {
            log.error("ERROR GENERANT PDF JUSTIFICANT COMANDA ESPECIAL: ", err);
            throw new PdfErrorGeneracio(err);
        }
    }

    private String getLiteral(Locale locale, String key, Object... params) {
        return I18N.getLiteral(PROPERTIES_FILE, locale, key, params);
    }

    private NumberFormat buildNumberFormat (TipusFormatDecimal tipusFormatDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(true);
        format.setDecimalFormatSymbols(tipusFormatDecimal.decimalFormatSymbols());
        return format;
    }

    private NumberFormat buildDecimalNumberFormat (TipusFormatDecimal tipusFormatDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(true);
        format.setDecimalFormatSymbols(tipusFormatDecimal.decimalFormatSymbols());
        format.applyPattern("0.000");
        return format;
    }

    private String buildReferencies(List<LiniaComanda> linies) {
        return linies.stream()
                .map(LiniaComanda::referencia)
                .collect(Collectors.joining("<br>"));
    }

    private String buildQuantitats(List<LiniaComanda> linies, NumberFormat numFormat) {
        return linies.stream()
                .map(LiniaComanda::quantitat)
                .map(numFormat::format)
                .collect(Collectors.joining("<br>"));
    }

    private String buildPreus(List<LiniaComanda> linies, NumberFormat numFormat) {
        return linies.stream()
                .map(l -> numFormat.format(l.preu().valor()) + " " + l.preu().divisa())
                .collect(Collectors.joining("<br>"));
    }

    private String buildDatesSolicitades(List<LiniaComanda> linies, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", locale);

        return linies.stream()
                .map(l -> l.dataSolicitada().format(formatter))
                .collect(Collectors.joining("<br>"));
    }

    private String buildDisponibiliatPrevista(List<LiniaComanda> linies, Locale locale) {
        var listResult = new ArrayList<String>();
        for (var linia : linies) {
            if (linia.tipusArticleClient().equals(TipusArticleClient.ESPECIAL)) {
                if (!linia.dataConfirmadaFabrica().isEmpty()) {
                    // Si supera els dies de reserva no cal mirar si s'ha pogut reservar o no
                    listResult.add(getLiteral(locale, "setmana") + " " +Dates.setmana(linia.dataConfirmadaFabrica().get()));
                } else {
                    listResult.add("");
                }
            } else {
                if (linia.reserva().map(reserva -> reserva.estat().equals(Reservable.RES)).orElse(false)) {
                    listResult.add(getLiteral(locale, "trencament_stock"));
                } else if (linia.reserva().map(reserva -> reserva.estat().equals(Reservable.PARCIAL)).orElse(false)) {
                    listResult.add(getLiteral(locale, "stock_parcial"));
                } else {
                    listResult.add("");
                }
            }
        }

        return String.join("<br>", listResult);
    }

    private String buildDataJustificant(Comanda comanda, Locale locale) {
        var pattern = "<b>%s:</b> %s";
        return String.format(pattern, getLiteral(locale, "data"),
                buildData(locale));
    }

    private String buildJustificant(Comanda comanda, Locale locale){
        var pattern = "%s: %s";
        var comandaClient = comanda.informacioClient().identificador();
        return String.format(pattern,
                getLiteral(locale, "justificant_rebut").toUpperCase(),
                comandaClient);
    }

    private String buildClient(Comanda comanda) {
        var pattern = "%s<br>%s<br>%s";
        return String.format(pattern, comanda.adresa().destinatari(), comanda.adresa().adresa(), comanda.adresa().poblacio());
    }

    private String buildData(Locale locale) {
        var data = LocalDateTime.now();
        // Obtenim el nom del mes en l'idioma passat per paràmetres
        var monthWords = data.getMonth().getDisplayName(TextStyle.FULL, locale).split(" ");
        var monthName = monthWords[monthWords.length - 1];
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        return String.format("%d %s %d", data.getDayOfMonth(), monthName, data.getYear());
    }

    private String buildTitolAdresa(Locale locale) {
        return String.format("<b>%s:</b>", getLiteral(locale, "adresa"));
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperJustificantComandaEspecial {
        String getData();
        String getJustificant();
        String getClient();
        String getListReferencies();
        String getListQuantitats();
        String getListPreusBruts();
        String getListDatesSolicitades();
        String getListDisponibilitatsPrevistes();
        String getTitolAdresa();
        String getTitolReferencia();
        String getTitolQuantitat();
        String getTitolPreuBrut();
        String getTitolDataSolicitada();
        String getTitolDisponibilitatPrevista();
        String getInfoQuantitats();
        String getPagina();
    }

}
