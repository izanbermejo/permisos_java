package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.service.IProviderDiesReserva;
import ames.comercial.server.I18N;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.SharedExceptions.PdfErrorGeneracio;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class GenerarPdfJustificantComandaNormalitzat {

    static final Logger log = LogManager.getLogger(GenerarPdfJustificantComandaNormalitzat.class.getName());

    private static final String PROPERTIES_FILE = "i18n/comandes/report_justificant_comanda";

    @Autowired ComandaRepository comandaRepo;
    @Autowired LiniaComandaRepository liniaComandaRepo;
    @Autowired IProviderDiesReserva providerDiesReserva;

    public byte[] run(long idComanda, boolean isDistribuidor, String idioma, TipusFormatDecimal tipusFormatDecimal) {
        Locale locale = new Locale(idioma);
        // Number format del locale
        var numFormat = buildNumberFormat(tipusFormatDecimal);
        var numDecimalFormat = buildDecimalNumberFormat(tipusFormatDecimal);
        // Obtenció de la comanda i les seves línies
        var comanda = comandaRepo.find(idComanda).orElseThrow(ComandaNoExisteix::new);
        var linies = liniaComandaRepo.findByComanda(idComanda)
                .stream()
                .sorted(Comparator.comparing(LiniaComanda::referencia))
                .toList();
        // Obtenció dels dies de reserva
        var diesReserva = providerDiesReserva.provide();
        // Build del bean pel jasper
        var jasperJustificant = JasperJustificantComandaNormalitzatImpl.builder()
                .data(buildDataJustificant(comanda, locale))
                .justificant(buildJustificant(comanda, locale))
                .client(buildClient(comanda))
                .listReferencies(buildReferencies(linies))
                .listQuantitats(buildQuantitats(linies, numFormat))
                .listPreusBruts(buildPreus(linies, numDecimalFormat))
                .listDatesEnviament(buildDatesEnviament(linies, diesReserva, locale))
                .titolAdresa(buildTitolAdresa(locale))
                .titolReferencia(getLiteral(locale, "referencia"))
                .titolQuantitat(getLiteral(locale, "quantitat"))
                .titolPreuBrut(getLiteral(locale, "preu_brut"))
                .titolDataEnviament(getLiteral(locale, "data_enviament_programada"))
                .titolDescompteAplicat(getLiteral(locale, "descompte_aplicat"))
                .titolQuantitatsFixades(getLiteral(locale, "quantitats_fixades"))
                .titolRecordatoriWeb(buildTitolWeb(locale))
                .titolSolicitudClaus(getLiteral(locale, "solicitud_claus"))
                .pagina(getLiteral(locale, "pagina"))
                .isDistribuidor(isDistribuidor)
                .build();
        // Generació del PDF
        try (InputStream input = getClass().getResourceAsStream("/jasper/JustificantComandaNormalitzat.jasper")) {
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
            log.error("ERROR GENERANT PDF JUSTIFICANT COMANDA NORMALITZAT: ", err);
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

    private String buildDatesEnviament(List<LiniaComanda> linies, long diesReserva, Locale locale) {
        var listResult = new ArrayList<String>();
        for (var linia : linies) {
            if (linia.servida()) {
                // Si la línia està servida cal que aparegui la data de sortida prevista
                listResult.add(buildData(linia.dataPrevistaSortida(), locale));
            } else {
                if (linia.dataPrevistaSortida().isAfter(RequestThread.dateLocal().plusDays(diesReserva))) {
                    // Si supera els dies de reserva no cal mirar si s'ha pogut reservar o no
                    listResult.add(buildData(linia.dataPrevistaSortida(), locale));
                } else {
                    // En cas que no superi els dies de reserva cal mirar l'stock que es pot servir
                    var infoReserva = linia.reserva().orElseThrow();
                    if (infoReserva.estat().equals(Reservable.RES )) {
                        var pattern = "<b><i>%s</i></b>";
                        listResult.add(String.format(pattern, getLiteral(locale, "trencament_stock")));
                    } else if (infoReserva.estat().equals(Reservable.PARCIAL)) {
                        var pattern = "<b><i>%s</i></b>";
                        listResult.add(String.format(pattern, getLiteral(locale, "stock_parcial")));
                    } else {
                        listResult.add(buildData(linia.dataPrevistaSortida(), locale));
                    }
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

    private String buildData(LocalDate data, Locale locale) {
        // Obtenim el nom del mes en l'idioma passat per paràmetres
        var monthWords = data.getMonth().getDisplayName(TextStyle.SHORT, locale).split(" ");
        var monthName = monthWords[monthWords.length - 1];
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        return String.format("%d %s %d", data.getDayOfMonth(), monthName, data.getYear());
    }

    private String buildTitolAdresa(Locale locale) {
        return String.format("<b>%s:</b>", getLiteral(locale, "adresa"));
    }

    private String buildTitolWeb(Locale locale) {
        return String.format("<b><i>%s</i></b> <a href='http://%s'><b><font color='0000EE'>%s</font></b></a>",
            getLiteral(locale, "recordatori_web"),
            "www.selfoil.com",
            "www.selfoil.com");
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperJustificantComandaNormalitzat {
        String getData();
        String getJustificant();
        String getClient();
        String getListReferencies();
        String getListQuantitats();
        String getListPreusBruts();
        String getListDatesEnviament();
        String getTitolAdresa();
        String getTitolReferencia();
        String getTitolQuantitat();
        String getTitolPreuBrut();
        String getTitolDataEnviament();
        String getTitolDescompteAplicat();
        String getTitolQuantitatsFixades();
        String getTitolRecordatoriWeb();
        String getTitolSolicitudClaus();
        String getPagina();
        boolean getIsDistribuidor();
    }

}
