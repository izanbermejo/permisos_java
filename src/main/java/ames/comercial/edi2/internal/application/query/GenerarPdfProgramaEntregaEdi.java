package ames.comercial.edi2.internal.application.query;

import ames.comercial.edi2.EDIException;
import ames.comercial.edi2.internal.domain.capsalera.CB;
import ames.comercial.edi2.internal.domain.capsalera.CD;
import ames.comercial.edi2.internal.domain.capsalera.CT;
import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.bloc.*;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.infraestructure.capsalera.CapsaleraEdiRepository;
import ames.comercial.edi2.internal.infraestructure.comanda.ComandaEdiRepository;
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
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class GenerarPdfProgramaEntregaEdi {

    static final Logger log = LogManager.getLogger(GenerarPdfProgramaEntregaEdi.class.getName());

    private static final String PROPERTIES_FILE = "i18n/edi2/report_programa_entrega_edi";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired CapsaleraEdiRepository capsaleraRepo;
    @Autowired ComandaEdiRepository comandaRepo;

    public byte[] run(long idMissatge, Locale locale) {
        var capsaleres = capsaleraRepo.obtenirCapsaleraEdi(idMissatge);
        if (capsaleres.isEmpty()) throw new EDIException.CapsaleraNoTrobada();
        var capsalera = capsaleres.get(0);

        var comandes = comandaRepo.obtenirComandesPerMissatge(idMissatge);
        if (comandes.isEmpty()) throw new EDIException.ComandaNoTrobada();

        List<JasperProgramaEntregaEdi> beans = new ArrayList<>();
        for (int i = 0; i < comandes.size(); i++) {
            beans.add(buildBean(capsalera, comandes.get(i), i, comandes.size(), locale));
        }

        try (InputStream input = getClass().getResourceAsStream("/jasper/ProgramaEntregaEdi.jasper")) {
            JasperReport rep = (JasperReport) JRLoader.loadObject(input);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(beans);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setExporterInput(new SimpleExporterInput(JasperFillManager.fillReport(rep, Maps.newHashMap(), dataSource)));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
            exporter.exportReport();
            return outStream.toByteArray();
        } catch (Exception err) {
            log.error("ERROR GENERANT PDF PROGRAMA ENTREGA EDI idMissatge={}: ", idMissatge, err);
            throw new PdfErrorGeneracio(err);
        }
    }

    private JasperProgramaEntregaEdi buildBean(CapsaleraEdi cap, ComandaEdi comanda, int index, int total, Locale locale) {
        boolean esPrimer = index == 0;
        return JasperProgramaEntregaEdiImpl.builder()
                // Control
                .mostrarCapsalera(esPrimer)
                .mostrarComentarisLinia(!comanda.lt().isEmpty())
                .lineaNum(lit(locale, "linea") + " " + (index + 1))
                // Header (only rendered when esPrimer, but always populated)
                .titolProgramaEntrega(lit(locale, "titol_programa_entrega"))
                .idEdiRow(buildIdEdiRow(cap, locale))
                .titolInterlocutors(lit(locale, "titol_interlocutors"))
                .buzoEmisorRow(boldLbl(lit(locale, "lbl_buzo_emisor")) + " " + cap.ca().buzonOrigen())
                .buzoReceptorRow(boldLbl(lit(locale, "lbl_buzo_receptor")) + " " + cap.ca().buzonDestino())
                .titolPrograma(lit(locale, "titol_programa"))
                .programaEsquerra(buildProgramaEsquerra(cap, comanda, locale))
                .programaDreta(buildProgramaDreta(cap, comanda, locale))
                .titolComprador(lit(locale, "titol_comprador"))
                .titolProveidor(lit(locale, "titol_proveidor"))
                .comprador(buildComprador(cap))
                .proveidor(buildProveidor(cap))
                .titolComentarisCapsalera(lit(locale, "titol_comentaris_capsalera"))
                .comentarisCapsalera(buildComentarisCapsalera(cap))
                // Per-comanda sections
                .titolConsignatari(lit(locale, "titol_consignatari"))
                .plantaClientRow(boldLbl(lit(locale, "lbl_planta_client")) + " " + buildPlantaClient(comanda))
                .llocEntregaRow(boldLbl(lit(locale, "lbl_lloc_entrega")) + " " + comanda.la().lugarEntrega().orElse(""))
                .magatzemRow(boldLbl(lit(locale, "lbl_magatzem")) + " " + comanda.la().codigoAlmacen().orElse(""))
                .destiFinalRow(comanda.la().lugarDestinoFinal().filter(s -> !s.isBlank())
                        .map(s -> boldLbl(lit(locale, "lbl_destinacio_final")) + " " + s).orElse(""))
                .solicitanteRow(buildSolicitante(comanda, locale))
                .titolArticle(lit(locale, "titol_article"))
                .codiCompradorRow(boldLbl(lit(locale, "lbl_codi_comprador")) + " " + comanda.la().idArticuloComprador())
                .codiVenedorRow(boldLbl(lit(locale, "lbl_codi_venedor")) + " " + comanda.lb().flatMap(LB::identificacionArticuloProveedor).orElse(""))
                .descripcioRow(boldLbl(lit(locale, "lbl_descripcio")) + " " + comanda.lg().flatMap(LG::descripcionArticulo).orElse(""))
                .titolContracte(lit(locale, "titol_contracte"))
                .numeroPedidoRow(boldLbl(lit(locale, "lbl_numero_pedido")) + " " + comanda.lg().map(LG::numeroContratoPedido).orElse(""))
                .dataComandaRow(boldLbl(lit(locale, "lbl_data_comanda")) + " " + comanda.lg().flatMap(LG::fechaContratoPedido).map(d -> d.format(DATE_FMT)).orElse(""))
                .numeroLiniaRow(boldLbl(lit(locale, "lbl_numero_linia")) + " " + comanda.lg().flatMap(LG::numeroLineaContrato).orElse(""))
                .numeroPlayRow(boldLbl(lit(locale, "lbl_numero_pla")) + " " + comanda.lg().flatMap(LG::numeroPlano).orElse(""))
                .titolComentarisLinia(lit(locale, "titol_comentaris_linia"))
                .comentarisLinia(buildComentarisLinia(comanda))
                .mostrarEndarreriments(hasEndarreriments(comanda))
                .titolEndarreriments(lit(locale, "titol_endarreriments"))
                .lblQuantitatEndarrerida(lit(locale, "lbl_quantitat_endarrerida"))
                .lblDataEndarreriment(lit(locale, "lbl_data_endarreriment"))
                .quantitatEndarrerida(buildEndarrerimentsQuantitat(comanda, locale))
                .dataEndarreriment(buildEndarrerimentsData(comanda))
                .mostrarQuantitatsAcumulades(comanda.lq().stream().anyMatch(q -> q.cantidadAcumuladaRecibida().isPresent() || q.cantidadAcumuladaProgramada().isPresent()))
                .titolQuantitatsAcumulades(lit(locale, "titol_quantitats_acumulades"))
                .lblQuantitatRebuda(lit(locale, "lbl_quantitat_rebuda"))
                .lblQuantitatProgramada(lit(locale, "lbl_quantitat_programada"))
                .quantitatRebuda(comanda.lq().stream().flatMap(q -> q.cantidadAcumuladaRecibida().stream()).findFirst().map(q -> fmtNum(q, locale)).orElse(""))
                .quantitatProgramada(comanda.lq().stream().flatMap(q -> q.cantidadAcumuladaProgramada().stream()).findFirst().map(q -> fmtNum(q, locale)).orElse(""))
                .titolAlbarans(lit(locale, "titol_albarans"))
                .colAlbaraEntrada(lit(locale, "col_albara_entrada"))
                .colAlbaraData(lit(locale, "col_albara_data"))
                .colAlbaraQuantitatEnviada(lit(locale, "col_albara_quantitat_enviada"))
                .colAlbaraQuantitatRebuda(lit(locale, "col_albara_quantitat_rebuda"))
                .colAlbaraDataRecepcio(lit(locale, "col_albara_data_recepcio"))
                .albaransReferencia(joinAA(comanda, aa -> aa.referenciaAlbaranEntrada().orElse("")))
                .albaransData(joinAA(comanda, aa -> aa.fechaAlbaran().map(d -> d.format(DATE_FMT)).orElse("")))
                .albaransQuantitatEnviada(joinAA(comanda, aa -> fmtNumStr(aa.cantidadEnviadaAlbaran().orElse(""), locale)))
                .albaransQuantitatRebuda(joinAA(comanda, aa -> fmtNumStr(aa.cantidadRecibidaAlbaran().orElse(""), locale)))
                .albaransDataRecepcio(joinAA(comanda, aa -> aa.fechaRecepcion().map(d -> d.format(DATE_FMT)).orElse("")))
                .titolQuantitatPedida(lit(locale, "titol_quantitat_pedida"))
                .colDaEstat(lit(locale, "col_da_estat"))
                .colDaQuantitat(lit(locale, "col_da_quantitat"))
                .colDaUnitat(lit(locale, "col_da_unitat"))
                .colDaDataInici(lit(locale, "col_da_data_inici"))
                .colDaDataFi(lit(locale, "col_da_data_fi"))
                .colDaTipusFecha(lit(locale, "col_da_tipus_fecha"))
                .colDaFreqEnvio(lit(locale, "col_da_freq_envio"))
                .colDaRan(lit(locale, "col_da_ran"))
                .colDaUltimRan(lit(locale, "col_da_ultim_ran"))
                .colDaKanban(lit(locale, "col_da_kanban"))
                .daEstat(joinDA(comanda, da -> mapEstatDA(da.tipoDetalle(), locale)))
                .daQuantitat(joinDA(comanda, da -> fmtNum(da.cantidad(), locale)))
                .daUnitat(joinDA(comanda, da -> da.unidadMedida().trim()))
                .daDataInici(joinDA(comanda, da -> da.fechaInicial().map(d -> d.format(DATE_FMT)).orElse("")))
                .daDataFi(joinDA(comanda, da -> da.fechaFinal().map(d -> d.format(DATE_FMT)).orElse("")))
                .daTipusFecha(joinDA(comanda, da -> mapTipusFecha(da.razonInstruccion(), locale)))
                .daFreqEnvio(joinDA(comanda, da -> da.frecuenciaEnvio().map(String::trim).orElse("")))
                .daRan(joinDA(comanda, da -> da.numeroRan().map(String::trim).orElse("")))
                .daUltimRan(joinDA(comanda, da -> da.ultimoNumeroRanEmitido().map(String::trim).orElse("")))
                .daKanban(joinDA(comanda, da -> da.numTarjetaKanban().map(String::trim).orElse("")))
                .mostrarEmbalatges(!comanda.le().isEmpty())
                .titolEmbalatges(lit(locale, "titol_embalatges"))
                .colLeTipoBulto(lit(locale, "col_le_tipo_bulto"))
                .colLeReferencia(lit(locale, "col_le_referencia"))
                .colLePieces(lit(locale, "col_le_peces_paquet"))
                .colLeNumero(lit(locale, "col_le_numero"))
                .colLeNivell(lit(locale, "col_le_nivell"))
                .leTipoBulto(joinLE(comanda, LE::tipoBulto))
                .leReferencia(joinLE(comanda, LE::referenciaEmbalajeCliente))
                .lePieces(joinLE(comanda, le -> le.cantPiezasPorEmbalaje().map(n -> fmtNum(n, locale)).orElse("")))
                .leNumero(joinLE(comanda, le -> le.numeroEmbalajes().map(n -> fmtNum(n, locale)).orElse("")))
                .leNivell(joinLE(comanda, le -> le.nivelEmpaquetamiento().orElse("")))
                .build();
    }

    // ─── Header builders ────────────────────────────────────────────────────

    private String buildIdEdiRow(CapsaleraEdi cap, Locale locale) {
        String idEdi = cap.ca().numeroDocumento();
        String data = cap.cb().map(cb -> cb.fechaMensaje().format(DATE_FMT)).orElse("");
        String hora = cap.cb().flatMap(CB::horaMensaje).map(t -> t.format(TIME_FMT)).orElse("");
        return lit(locale, "lbl_id_edi") + " " + idEdi
                + "     " + lit(locale, "lbl_data_emissio") + " " + data
                + "     " + lit(locale, "lbl_hora_emissio") + " " + hora;
    }

    private String buildProgramaEsquerra(CapsaleraEdi cap, ComandaEdi comanda, Locale locale) {
        String numDelforAnterior = comanda.lg().flatMap(LG::numeroDocumentoAnterior).orElse("");
        return boldLbl(lit(locale, "lbl_numero_delfor")) + " " + cap.ca().numeroDocumento() + "<br>"
                + boldLbl(lit(locale, "lbl_codi_delfor")) + " " + cap.ca().codigoDocumento() + "<br>"
                + boldLbl(lit(locale, "lbl_logistica")) + " " + cap.ca().logisticaNombreMensaje().orElse("") + "<br>"
                + boldLbl(lit(locale, "lbl_funcio_document")) + " " + cap.ca().funcionMensaje().orElse("") + "<br>"
                + boldLbl(lit(locale, "lbl_itinerari_transport")) + " " + cap.cb().flatMap(CB::idTransportista).orElse("") + "<br>"
                + boldLbl(lit(locale, "lbl_numero_delfor_anterior")) + " " + numDelforAnterior;
    }

    private String buildProgramaDreta(CapsaleraEdi cap, ComandaEdi comanda, Locale locale) {
        String dataDoc = cap.cb().map(cb -> cb.fechaMensaje().format(DATE_FMT)).orElse("");
        String dataIniciHoritz = cap.cb().flatMap(CB::fechaInicioHorizonte).map(d -> d.format(DATE_FMT)).orElse("");
        String dataFiHoritz = cap.cb().flatMap(CB::fechaFinalHorizonte).map(d -> d.format(DATE_FMT)).orElse("");
        String refApp = cap.ca().referenciaAplicacion().orElse("");
        String dataDocAnterior = comanda.lg().flatMap(LG::fechaDocumentoAnterior).map(d -> d.format(DATE_FMT)).orElse("");
        return boldLbl(lit(locale, "lbl_data_document")) + " " + dataDoc + "<br>"
                + boldLbl(lit(locale, "lbl_data_inici_horitz")) + " " + dataIniciHoritz + "<br>"
                + boldLbl(lit(locale, "lbl_data_fi_horitz")) + " " + dataFiHoritz + "<br>"
                + boldLbl(lit(locale, "lbl_referencia_aplicacio")) + " " + refApp + "<br>"
                + "<br>"
                + boldLbl(lit(locale, "lbl_data_document")) + " " + dataDocAnterior;
    }

    private String buildComprador(CapsaleraEdi cap) {
        var sb = new StringBuilder(cap.ci().idComprador());
        cap.cc().ifPresent(cc -> {
            sb.append("<br>").append(cc.nombreComprador());
            cc.direccionComprador().ifPresent(d -> sb.append("<br>").append(d));
            var localitat = cc.localidadComprador().orElse("");
            var cp = cc.codigoPostalComprador().map(c -> " " + c).orElse("");
            if (!localitat.isBlank() || !cp.isBlank()) sb.append("<br>").append(localitat).append(cp);
        });
        cap.cd().flatMap(CD::personaContactoComprador)
                .filter(c -> !c.isBlank())
                .ifPresent(c -> sb.append("<br>").append(c));
        return sb.toString();
    }

    private String buildProveidor(CapsaleraEdi cap) {
        var sb = new StringBuilder(cap.ci().idProveedor());
        sb.append(cap.ci().idExpedidor().trim().isBlank() ? "" : " / " + cap.ci().idExpedidor().trim());
        sb.append(cap.ci().numCuentaInternaProveedor()
                .map(String::trim).filter(s -> !s.isBlank())
                .map(s -> " / " + s).orElse(""));
        cap.cp().ifPresent(cp -> {
            cp.nombreProveedor().filter(n -> !n.isBlank()).ifPresent(n -> sb.append("<br>").append(n));
            cp.direccionProveedor().filter(d -> !d.isBlank()).ifPresent(d -> sb.append("<br>").append(d));
            var localitat = cp.localidadProveedor().orElse("");
            var cp2 = cp.codPostalProveedor().map(c -> " " + c).orElse("");
            if (!localitat.isBlank() || !cp2.isBlank()) sb.append("<br>").append(localitat).append(cp2);
        });
        return sb.toString();
    }

    private String buildComentarisCapsalera(CapsaleraEdi cap) {
        return cap.ct().stream()
                .map(CT::textoLibre)
                .collect(Collectors.joining("<br>"));
    }

    // ─── Comanda-specific builders ──────────────────────────────────────────

    private String buildSolicitante(ComandaEdi comanda, Locale locale) {
        return comanda.ls().map(ls -> {
            var parts = new ArrayList<String>();
            ls.codigoSolicitante().filter(s -> !s.isBlank()).ifPresent(parts::add);
            ls.nombreSolicitante().filter(s -> !s.isBlank()).ifPresent(parts::add);
            ls.personaContacto().filter(s -> !s.isBlank()).ifPresent(parts::add);
            ls.telefono().filter(s -> !s.isBlank()).ifPresent(parts::add);
            if (parts.isEmpty()) return "";
            return boldLbl(lit(locale, "lbl_solicitant")) + " " + String.join(" / ", parts);
        }).orElse("");
    }

    private String buildPlantaClient(ComandaEdi comanda) {
        var lc = comanda.lc();
        var sb = new StringBuilder(lc.codigoConsignatario()).append(" -- ").append(lc.nombreConsignatario());
        comanda.ld().ifPresent(ld -> {
            ld.direccion().filter(d -> !d.isBlank()).ifPresent(d -> sb.append(",").append(d));
            var localitat = ld.localidad().orElse("");
            var prov = ld.provincia().map(p -> " / " + p).orElse("");
            if (!localitat.isBlank() || !prov.isBlank()) sb.append(",").append(localitat).append(prov);
        });
        return sb.toString();
    }

    private String buildComentarisLinia(ComandaEdi comanda) {
        if (comanda.lt().isEmpty()) return "";
        var parts = new ArrayList<String>();
        for (var lt : comanda.lt()) {
            lt.texto1().filter(t -> !t.isBlank()).ifPresent(parts::add);
            lt.texto2().filter(t -> !t.isBlank()).ifPresent(parts::add);
            lt.texto3().filter(t -> !t.isBlank()).ifPresent(parts::add);
            lt.texto4().filter(t -> !t.isBlank()).ifPresent(parts::add);
        }
        return String.join("<br>", parts);
    }

    // ─── DA table builders ──────────────────────────────────────────────────

    @FunctionalInterface
    private interface DaExtractor {
        String extract(ames.comercial.edi2.internal.domain.linia.bloc.DA da);
    }

    @FunctionalInterface
    private interface AaExtractor {
        String extract(AA aa);
    }

    @FunctionalInterface
    private interface LeExtractor {
        String extract(LE le);
    }

    private String joinDA(ComandaEdi comanda, DaExtractor extractor) {
        return comanda.linies().stream()
                .map(LiniaEdi::da)
                .map(extractor::extract)
                .collect(Collectors.joining("<br>"));
    }

    private String joinLE(ComandaEdi comanda, LeExtractor extractor) {
        return comanda.le().stream()
                .map(extractor::extract)
                .collect(Collectors.joining("<br>"));
    }

    private String joinAA(ComandaEdi comanda, AaExtractor extractor) {
        if (comanda.aa().isEmpty()) return "";
        return comanda.aa().stream()
                .map(extractor::extract)
                .collect(Collectors.joining("<br>"));
    }

    private String mapEstatDA(String tipoDetalle, Locale locale) {
        try {
            int tipus = Integer.parseInt(tipoDetalle.trim());
            return switch (tipus) {
                case 2 -> lit(locale, "da_auto_fab");
                case 3 -> lit(locale, "da_auto_mat_prim");
                case 4 -> lit(locale, "da_estat_prev");
                default -> lit(locale, "da_estat_firme"); //L'1 o desconegut és ferm
            };
        } catch (NumberFormatException e) {
            return tipoDetalle;
        }
    }

    private String mapTipusFecha(Optional<String> razonInstruccion, Locale locale) {
        return razonInstruccion
                .filter(r -> !r.isBlank())
                .map(r -> "E".equals(r.trim()) ? lit(locale, "da_tipus_fecha_entrega") : r.trim())
                .orElse(lit(locale, "da_tipus_fecha_entrega"));
    }

    // ─── Endarreriments builders ─────────────────────────────────────────────

    private boolean hasEndarreriments(ComandaEdi comanda) {
        return comanda.lq().stream()
            .anyMatch(q -> q.cantidadAtraso().isPresent() || q.fechaCantidadAtraso().isPresent());
    }

    private String buildEndarrerimentsQuantitat(ComandaEdi comanda, Locale locale) {
        return comanda.lq().stream()
            .filter(q -> q.cantidadAtraso().isPresent() || q.fechaCantidadAtraso().isPresent())
            .map(q -> q.cantidadAtraso().map(n -> fmtNum(n, locale)).orElse(""))
            .collect(Collectors.joining("<br>"));
    }

    private String buildEndarrerimentsData(ComandaEdi comanda) {
        return comanda.lq().stream()
            .filter(q -> q.cantidadAtraso().isPresent() || q.fechaCantidadAtraso().isPresent())
            .map(q -> q.fechaCantidadAtraso().map(d -> d.format(DATE_FMT)).orElse(""))
            .collect(Collectors.joining("<br>"));
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String lit(Locale locale, String key) {
        return I18N.getLiteral(PROPERTIES_FILE, locale, key);
    }

    private String boldLbl(String label) {
        return "<b>" + label + "</b>";
    }

    private String fmtNum(long n, Locale locale) {
        return NumberFormat.getInstance(locale).format(n);
    }

    private String fmtNumStr(String s, Locale locale) {
        if (s.isBlank()) return "";
        try {
            long n = Long.parseLong(s.trim());
            return n == 0 ? "" : fmtNum(n, locale);
        } catch (NumberFormatException e) {
            return s.trim();
        }
    }

    // ─── Bean ───────────────────────────────────────────────────────────────

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface JasperProgramaEntregaEdi {
        // Control
        boolean getMostrarCapsalera();
        boolean getMostrarComentarisLinia();
        String getLineaNum();
        // Header
        String getTitolProgramaEntrega();
        String getIdEdiRow();
        String getTitolInterlocutors();
        String getBuzoEmisorRow();
        String getBuzoReceptorRow();
        String getTitolPrograma();
        String getProgramaEsquerra();
        String getProgramaDreta();
        String getTitolComprador();
        String getTitolProveidor();
        String getComprador();
        String getProveidor();
        String getTitolComentarisCapsalera();
        String getComentarisCapsalera();
        // Consignatari
        String getTitolConsignatari();
        String getPlantaClientRow();
        String getLlocEntregaRow();
        String getMagatzemRow();
        String getDestiFinalRow();
        String getSolicitanteRow();
        // Article
        String getTitolArticle();
        String getCodiCompradorRow();
        String getCodiVenedorRow();
        String getDescripcioRow();
        // Contracte
        String getTitolContracte();
        String getNumeroPedidoRow();
        String getDataComandaRow();
        String getNumeroLiniaRow();
        String getNumeroPlayRow();
        // Comentaris linia
        String getTitolComentarisLinia();
        String getComentarisLinia();
        // Endarreriments
        boolean getMostrarEndarreriments();
        String getTitolEndarreriments();
        String getLblQuantitatEndarrerida();
        String getLblDataEndarreriment();
        String getQuantitatEndarrerida();
        String getDataEndarreriment();
        // Quantitats acumulades
        boolean getMostrarQuantitatsAcumulades();
        String getTitolQuantitatsAcumulades();
        String getLblQuantitatRebuda();
        String getLblQuantitatProgramada();
        String getQuantitatRebuda();
        String getQuantitatProgramada();
        // Albarans
        String getTitolAlbarans();
        String getColAlbaraEntrada();
        String getColAlbaraData();
        String getColAlbaraQuantitatEnviada();
        String getColAlbaraQuantitatRebuda();
        String getColAlbaraDataRecepcio();
        String getAlbaransReferencia();
        String getAlbaransData();
        String getAlbaransQuantitatEnviada();
        String getAlbaransQuantitatRebuda();
        String getAlbaransDataRecepcio();
        // DA table headers
        String getTitolQuantitatPedida();
        String getColDaEstat();
        String getColDaQuantitat();
        String getColDaUnitat();
        String getColDaDataInici();
        String getColDaDataFi();
        String getColDaTipusFecha();
        String getColDaFreqEnvio();
        String getColDaRan();
        String getColDaUltimRan();
        String getColDaKanban();
        // DA table data (<br>-joined)
        String getDaEstat();
        String getDaQuantitat();
        String getDaUnitat();
        String getDaDataInici();
        String getDaDataFi();
        String getDaTipusFecha();
        String getDaFreqEnvio();
        String getDaRan();
        String getDaUltimRan();
        String getDaKanban();
        // Embalatges
        boolean getMostrarEmbalatges();
        String getTitolEmbalatges();
        String getColLeTipoBulto();
        String getColLeReferencia();
        String getColLePieces();
        String getColLeNumero();
        String getColLeNivell();
        String getLeTipoBulto();
        String getLeReferencia();
        String getLePieces();
        String getLeNumero();
        String getLeNivell();
    }
}
