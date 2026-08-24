package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirEmpresesAds;
import ames.comercial.advantage.IObtenirEmpresesAds.EmpresaAds;
import ames.comercial.advantage.IObtenirEtiquetesAlbara;
import ames.comercial.advantage.IObtenirEtiquetesAlbara.EtiquetaTransportAlbara;
import ames.comercial.advantage.IObtenirDestinsTransport;
import ames.comercial.advantage.IObtenirPaisosAds;
import ames.comercial.advantage.IObtenirTransportistesAds;
import ames.comercial.advantage.internal.ObtenirDestinsAds.DestiAds;
import ames.comercial.advantage.internal.ObtenirPaisosAds.PaisAds;
import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.server.I18N;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.Preu;
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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class GenerarPdfAlbara {

    static final Logger log = LogManager.getLogger(GenerarPdfAlbara.class.getName());

    private static final String PROPERTIES_FILE = "i18n/albarans/report_albara";

    @Autowired AlbaraRepository albaraRepo;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepo;
    @Autowired IObtenirEmpresesAds obtenirEmpresesAds;
    @Autowired IObtenirEtiquetesAlbara obtenirEtiquetes;
    @Autowired IObtenirPaisosAds obtenirPaisosAds;
    @Autowired IObtenirTransportistesAds obtenirTransportistesAds;
    @Autowired IObtenirDestinsTransport obtenirDestins;
    @Autowired IObtenirClientAds obtenirClientAds;

    public byte[] run(KeyAlbara idAlbara, String idioma, TipusFormatDecimal formatDecimal, boolean incloureEtiquetes, boolean isCopia) {
        Locale locale = new Locale(idioma);
        var numDecimalFormat = buildDecimalNumberFormat(formatDecimal);
        var numImportFormat = buildImportNumberFormat(formatDecimal);
        var albara = albaraRepo.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        var linies = liniaAlbaraRepo.findByAlbara(idAlbara).stream()
                .sorted(Comparator.comparingLong(l -> l.id().linia()))
                .toList();
        var empresa = obtenirEmpresesAds.get(idAlbara.empresa()).orElseThrow();
        List<EtiquetaTransportAlbara> etiquetes = incloureEtiquetes ? obtenirEtiquetes.executar(idAlbara) : List.of();

        boolean isMedical = Empresa.getByClau(empresa.codi()).equals(Empresa.MEDICAL);
        boolean mostrarPreu = !albara.isNoValorat() && !isCopia;
        boolean mostrarImport = isMedical && mostrarPreu;

        var bean = JasperAlbaraImpl.builder()
                .logoEmpresaPath(resolveLogoPath(empresa.codi()))
                .nomEmpresa(empresa.descripcio())
                .adresaEmpresa(buildAdresaEmpresa(empresa))
                .telFaxFacturEmpresa(buildContacteEmpresa(empresa))
                .data("<b>" + getLiteral(locale, "data") + "</b> " + buildData(albara.data(), locale))
                .albaraEspecial(albara.numeroAlbaraEspecial().map(n -> "(" + n + ")").orElse(""))
                .titolNumeroAlbara(buildTitolNumeroAlbara(albara, locale))
                .titolNomDestinatari(buildTitolNomDestinatari(albara, locale))
                .adresaDestinatari(buildAdresaDestinatari(albara))
                .poblacioDestinatari(buildPoblacioDestinatari(albara))
                .paisVatDestinatari(buildPaisVatDestinatari(albara, locale))
                .titolNomTransportista(buildTitolNomTransportista(albara, locale))
                .adresaTransportista(buildAdresaTransportista(albara))
                .poblacioTransportista(buildPoblacioTransportista(albara))
                .infoFormaEnviament(buildInfoFormaEnviament(albara, locale))
                .infoBultos(buildInfoBultos(albara, locale))
                .infoPesBrut(buildInfoPesBrut(albara, locale))
                .infoTipusBultos(buildInfoTipusBultos(albara))
                .infoDretaClient(buildInfoDretaClient(albara, locale))
                .infoDretaMagatzem(buildInfoDretaMagatzem(albara, locale))
                .infoDretaProveidor(buildInfoDretaProveidor(albara, locale))
                .isCopia(isCopia)
                .titolMatriu(getLiteral(locale, "matriu"))
                .titolDenominacio(getLiteral(locale, "denominacio"))
                .titolQuantitat(getLiteral(locale, "quantitat"))
                .titolPreu(mostrarPreu ? getLiteral(locale, "preu") : "")
                .titolImport(mostrarImport ? getLiteral(locale, "import") : "")
                .mostrarImport(mostrarImport)
                .titolTotal(mostrarImport ? getLiteral(locale, "total") : "")
                .total(mostrarImport ? buildTotal(linies, numImportFormat) : "")
                .linies(buildLinies(linies, etiquetes, numDecimalFormat, buildNumberFormat(formatDecimal), numImportFormat, mostrarPreu, mostrarImport, getLiteral(locale, "lit_comanda"), getLiteral(locale, "hs_code"), locale))
                .isObservacionsVisible(albara.observacionsImpressio().isPresent())
                .titolObservacions(getLiteral(locale, "observacions"))
                .listObservacions(albara.observacionsImpressio().orElse(""))
                .isCostosVisible(albara.costTransport().isPresent() || albara.costMoq().isPresent() || albara.referenciaTransport().isPresent())
                .infoCostos(buildCostos(albara, numDecimalFormat, locale))
                .pagina(getLiteral(locale, "pagina"))
                .peuRegistre(getLiteral(locale, "peu_registre"))
                .codiBarres(buildCodiBarres(albara))
                .isMedical(isMedical)
                .build();

        try (InputStream inputAlbara = getClass().getResourceAsStream("/jasper/albara/Albara.jasper");
             InputStream inputLinia = getClass().getResourceAsStream("/jasper/albara/AlbaraLinia.jasper");
             InputStream inputLiniaValorat = getClass().getResourceAsStream("/jasper/albara/AlbaraLiniaValorat.jasper");
             InputStream inputEtiqueta = getClass().getResourceAsStream("/jasper/albara/AlbaraEtiquetaLinia.jasper")) {
            JasperReport rep = (JasperReport) JRLoader.loadObject(inputAlbara);
            JasperReport repLinia = (JasperReport) JRLoader.loadObject(inputLinia);
            JasperReport repLiniaValorat = (JasperReport) JRLoader.loadObject(inputLiniaValorat);
            JasperReport repEtiqueta = (JasperReport) JRLoader.loadObject(inputEtiqueta);
            var params = Maps.<String, Object>newHashMap();
            params.put("subreportLiniaAlbara", mostrarImport ? repLiniaValorat : repLinia);
            params.put("subreportEtiquetaLinia", repEtiqueta);
            params.put("fontName", "zh".equals(idioma) ? "WenQuanYi Micro Hei Mono" : "ArialFamily");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(JasperFillManager.fillReport(rep, params, new JRBeanCollectionDataSource(List.of(bean)))));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return outStream.toByteArray();
        } catch (Exception err) {
            log.error("ERROR GENERANT PDF ALBARA: ", err);
            throw new PdfErrorGeneracio(err);
        }
    }

    private String getLiteral(Locale locale, String key, Object... params) {
        return I18N.getLiteral(PROPERTIES_FILE, locale, key, params);
    }

    private NumberFormat buildDecimalNumberFormat(TipusFormatDecimal tipusFormatDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(tipusFormatDecimal.decimalFormatSymbols());
        format.applyPattern("#,##0.000");
        return format;
    }

    private NumberFormat buildImportNumberFormat(TipusFormatDecimal tipusFormatDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(tipusFormatDecimal.decimalFormatSymbols());
        format.applyPattern("#,##0.00");
        return format;
    }

    private NumberFormat buildNumberFormat(TipusFormatDecimal tipusFormatDecimal) {
        DecimalFormat format = new DecimalFormat();
        format.setGroupingUsed(true);
        format.setDecimalFormatSymbols(tipusFormatDecimal.decimalFormatSymbols());
        return format;
    }

    private String resolveLogoPath(String codiEmpresa) {
        if (Empresa.getByClau(codiEmpresa).equals(Empresa.MEDICAL)) {
            return "img/logo-medical.bmp";
        }
        return "img/logo-ames.bmp";
    }

    private String buildAdresaEmpresa(EmpresaAds empresa) {
        var parts = new ArrayList<String>();
        if (!empresa.adresaAlbara().isBlank()) parts.add(empresa.adresaAlbara());
        if (!empresa.poblacioAlbara().isBlank()) parts.add(empresa.poblacioAlbara());
        if (!empresa.paisAlbara().isBlank()) parts.add(empresa.paisAlbara());
        return String.join("<br>", parts);
    }

    private String buildContacteEmpresa(EmpresaAds empresa) {
        var parts = new ArrayList<String>();
        if (!empresa.telalb().isBlank()) parts.add(empresa.telalb());
        parts.add("email: factur@ames.group");
        var nif = empresa.nif().startsWith("A") || empresa.nif().startsWith("B")
                ? "NIF/VAT: ES " + empresa.nif()
                : "NIF/VAT: " + empresa.nif();
        parts.add("<b>" + nif + "</b>");
        return String.join("<br>", parts);
    }

    private String buildTitolNumeroAlbara(Albara albara, Locale locale) {
        return getLiteral(locale, "titol_client", albara.id().codiFormat());
    }

    private String buildData(LocalDate data, Locale locale) {
        var monthWords = data.getMonth().getDisplayName(TextStyle.SHORT, locale).split(" ");
        var monthName = monthWords[monthWords.length - 1];
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        return String.format("%d %s %d", data.getDayOfMonth(), monthName, data.getYear());
    }

    private String buildTitolNomDestinatari(Albara albara, Locale locale) {
        return "<b>" + getLiteral(locale, "consignatari") + ":</b> " + albara.adresa().destinatari();
    }

    private String buildAdresaDestinatari(Albara albara) {
        return albara.adresa().adresa();
    }

    private String buildPoblacioDestinatari(Albara albara) {
        var adresa = albara.adresa();
        return adresa.codiPostal().isBlank()
                ? adresa.poblacio()
                : (adresa.codiPostal() + " " + adresa.poblacio()).trim();
    }

    private String buildPaisVatDestinatari(Albara albara, Locale locale) {
        var adresa = albara.adresa();
        if (adresa.pais().isBlank()) return "";
        var nomPais = obtenirPaisosAds.get(adresa.pais()).map(PaisAds::nom).orElse(adresa.pais());
        var sb = new StringBuilder(nomPais);
        albara.client()
                .flatMap(cli -> obtenirClientAds.get(cli))
                .map(c -> c.nif())
                .filter(nif -> !nif.isBlank())
                .ifPresent(nif -> sb.append(" | <b>VAT:</b> ").append(nif));
        return sb.toString();
    }

    private String buildTitolNomTransportista(Albara albara, Locale locale) {
        return albara.informacioEnviament().transportista()
                .flatMap(obtenirTransportistesAds::get)
                .map(tra -> "<b>" + getLiteral(locale, "transportista") + ":</b> " + tra.descripcio())
                .orElse("");
    }

    private String buildAdresaTransportista(Albara albara) {
        return albara.informacioEnviament().transportista()
                .flatMap(obtenirTransportistesAds::get)
                .map(tra -> tra.adreca())
                .filter(a -> !a.isBlank())
                .orElse("");
    }

    private String buildPoblacioTransportista(Albara albara) {
        return albara.informacioEnviament().transportista()
                .flatMap(obtenirTransportistesAds::get)
                .map(tra -> tra.codiPostal().isBlank() ? tra.poblacio() : (tra.codiPostal() + " " + tra.poblacio()).trim())
                .filter(p -> !p.isBlank())
                .orElse("");
    }

    private String buildInfoFormaEnviament(Albara albara, Locale locale) {
        var env = albara.informacioEnviament();
        var sb = new StringBuilder();
        sb.append("<b>").append(getLiteral(locale, "forma_enviament")).append(": </b>");
        sb.append(env.formaEnviament().descripcio(locale));
        sb.append(" ").append(env.incoterm().name());
        var nomDesti = obtenirDestins.get(env.desti()).map(DestiAds::nom).orElse(env.desti());
        if (!nomDesti.isBlank()) sb.append(" ").append(nomDesti);
        return sb.toString();
    }

    private String buildInfoBultos(Albara albara, Locale locale) {
        return albara.informacioMagatzem().bultos()
                .map(b -> "<b>" + getLiteral(locale, "bultos") + ": " + b + "</b>")
                .orElse("");
    }

    private String buildInfoPesBrut(Albara albara, Locale locale) {
        return albara.informacioMagatzem().pesBrut()
                .map(p -> "<b>" + getLiteral(locale, "pes_brut") + ": " + p + " kg</b>")
                .orElse("");
    }

    private String buildInfoTipusBultos(Albara albara) {
        var mag = albara.informacioMagatzem();
        var tipus = new ArrayList<String>();
        mag.paletsTipus1().filter(p -> p.numero() != 0).ifPresent(p -> tipus.add(p.numero() + " palet 120x100cm"));
        mag.paletsTipus2().filter(p -> p.numero() != 0).ifPresent(p -> tipus.add(p.numero() + " palet 120x80cm"));
        mag.paletsTipus3().filter(p -> p.numero() != 0).ifPresent(p -> tipus.add(p.numero() + " palet 80x60cm"));
        albara.numeroCaixes().filter(n -> n != 0).ifPresent(n -> tipus.add(n + " box"));
        return tipus.isEmpty()
                ? ""
                : "<b>" + String.join(" + ", tipus) + "</b>";
    }

    private String buildInfoDretaClient(Albara albara, Locale locale) {
        return albara.client().map(c -> getLiteral(locale, "codi_client") + ": " + c).orElse("");
    }

    private String buildInfoDretaMagatzem(Albara albara, Locale locale) {
        return getLiteral(locale, "magatzem") + ": " + albara.magatzem();
    }

    private String buildInfoDretaProveidor(Albara albara, Locale locale) {
        return albara.numeroProveidor().map(n -> getLiteral(locale, "proveidor") + ": " + n).orElse("");
    }

    private String buildCodiBarres(Albara albara) {
        return albara.id().empresa() + albara.id().codiFormat();
    }

    private List<JasperLiniaAlbara> buildLinies(List<LiniaAlbara> linies, List<EtiquetaTransportAlbara> etiquetes,
                                                NumberFormat numFormat, NumberFormat numIntFormat, NumberFormat numImportFormat, boolean mostrarPreu, boolean mostrarImport,
                                                String litComanda, String litHsCode, Locale locale) {
        return linies.stream()
                .map(l -> buildJasperLinia(l, etiquetes, numFormat, numIntFormat, numImportFormat, mostrarPreu, mostrarImport, litComanda, litHsCode, locale))
                .toList();
    }

    private JasperLiniaAlbara buildJasperLinia(LiniaAlbara linia, List<EtiquetaTransportAlbara> etiquetes,
                                               NumberFormat numFormat, NumberFormat numIntFormat, NumberFormat numImportFormat, boolean mostrarPreu, boolean mostrarImport,
                                               String litComanda, String litHsCode, Locale locale) {
        var etiquetesLinia = etiquetes.stream()
                .filter(e -> e.clauLiniaAlbara().equals(linia.id()))
                .map(e -> buildJasperEtiquetaLinia(e, locale))
                .toList();
        var hsCode = linia.informacioPesa().partidaArantzelaria();
        return JasperLiniaAlbaraImpl.builder()
                .comandaClient(linia.infoComanda().map(c -> litComanda + ": " + c.comandaClient()).orElse(""))
                .codiPartidaArantzelaria(hsCode.isBlank() ? "" : litHsCode + ": " + hsCode)
                .matriu(linia.informacioPesa().matriu())
                .quantitat(numIntFormat.format(linia.quantitat()))
                .denominacio(linia.informacioPesa().denominacio())
                .preu(mostrarPreu ? buildPreuUnitari(linia.preu(), numFormat, mostrarImport) : "")
                .importLinia(mostrarImport ? numImportFormat.format(linia.importBrut()) + " " + linia.preu().divisa().base() : "")
                .referenciaNivellTecnic(buildReferenciaNivellTecnic(linia))
                .etiquetes(etiquetesLinia)
                .build();
    }

    private String buildPreuUnitari(Preu preu, NumberFormat numFormat, boolean mostrarImport) {
        var valor = numFormat.format(preu.valor());
        // En mode valorat la divisa base ja apareix a l'import de la línia, per tant no la concatenem al preu unitari;
        // només afegim " /%" quan el preu està expressat en cèntims (%).
        if (mostrarImport) {
            return preu.divisa().isCent() ? valor + " /%" : valor;
        }
        return valor + " " + preu.divisa();
    }

    private String buildTotal(List<LiniaAlbara> linies, NumberFormat numImportFormat) {
        var total = linies.stream()
                .map(LiniaAlbara::importBrut)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var divisa = linies.stream().findFirst().map(l -> l.preu().divisa().base().toString()).orElse("");
        return (numImportFormat.format(total) + " " + divisa).trim();
    }

    private JasperEtiquetaLinia buildJasperEtiquetaLinia(EtiquetaTransportAlbara e, Locale locale) {
        var rang = e.etiquetaProduccioDesde() + "—" + e.etiquetaProduccioFins();
        return JasperEtiquetaLiniaImpl.builder()
                .labelRang(getLiteral(locale, "etq_rang"))
                .rang(rang)
                .labelBatch(getLiteral(locale, "etq_lot"))
                .batch(e.lot())
                .labelQuantCaixa(getLiteral(locale, "etq_quant_caixa"))
                .quantCaixa(String.valueOf(e.quantitatPecesPerCaixa()))
                .labelNumCaixes(getLiteral(locale, "etq_num_caixes"))
                .numCaixes(String.valueOf(e.quantitatCaixes()))
                .labelQuantitat(getLiteral(locale, "etq_quantitat"))
                .quantitat(String.valueOf(e.quantitatPecesTotal()))
                .build();
    }

    private String buildReferenciaNivellTecnic(LiniaAlbara linia) {
        var referencia = linia.informacioPesa().referencia();
        var nivellTecnic = linia.informacioPesa().nivellTecnic();
        return nivellTecnic.isBlank() ? referencia : referencia + "     " + nivellTecnic;
    }

    private String buildCostos(Albara albara, NumberFormat numFormat, Locale locale) {
        var parts = new ArrayList<String>();
        albara.costTransport().ifPresent(c ->
                parts.add(getLiteral(locale, "cost_transport") + ": " + numFormat.format(c.imp())));
        albara.costMoq().ifPresent(c ->
                parts.add(getLiteral(locale, "cost_moq") + ": " + numFormat.format(c)));
        albara.referenciaTransport().ifPresent(r ->
                parts.add(getLiteral(locale, "ref_transport") + ": " + r));
        return String.join(" | ", parts);
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperAlbara {
        String getLogoEmpresaPath();
        String getNomEmpresa();
        String getAdresaEmpresa();
        String getTelFaxFacturEmpresa();
        String getData();
        String getAlbaraEspecial();
        String getTitolNumeroAlbara();
        String getTitolNomDestinatari();
        String getAdresaDestinatari();
        String getPoblacioDestinatari();
        String getPaisVatDestinatari();
        String getTitolNomTransportista();
        String getAdresaTransportista();
        String getPoblacioTransportista();
        String getInfoFormaEnviament();
        String getInfoBultos();
        String getInfoPesBrut();
        String getInfoTipusBultos();
        String getInfoDretaClient();
        String getInfoDretaMagatzem();
        String getInfoDretaProveidor();
        boolean getIsCopia();
        String getTitolMatriu();
        String getTitolDenominacio();
        String getTitolQuantitat();
        String getTitolPreu();
        String getTitolImport();
        boolean getMostrarImport();
        String getTitolTotal();
        String getTotal();
        List<JasperLiniaAlbara> getLinies();
        boolean getIsObservacionsVisible();
        String getTitolObservacions();
        String getListObservacions();
        boolean getIsCostosVisible();
        String getInfoCostos();
        String getPagina();
        String getPeuRegistre();
        String getCodiBarres();
        boolean getIsMedical();
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperLiniaAlbara {
        String getComandaClient();
        String getCodiPartidaArantzelaria();
        String getMatriu();
        String getQuantitat();
        String getDenominacio();
        String getPreu();
        String getImportLinia();
        String getReferenciaNivellTecnic();
        List<JasperEtiquetaLinia> getEtiquetes();
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperEtiquetaLinia {
        String getLabelRang();
        String getRang();
        String getLabelBatch();
        String getBatch();
        String getLabelQuantCaixa();
        String getQuantCaixa();
        String getLabelNumCaixes();
        String getNumCaixes();
        String getLabelQuantitat();
        String getQuantitat();
    }

}
