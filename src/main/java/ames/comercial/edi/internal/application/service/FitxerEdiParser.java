package ames.comercial.edi.internal.application.service;

import ames.comercial.edi.ComandaEDIConfig;
import ames.comercial.edi.beans.*;
import ames.comercial.server.exception.AppException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FitxerEdiParser {

    File fitxerEdi;

    public FitxerEdiParser(File fitxerEdi) {
        this.fitxerEdi = fitxerEdi;
    }

    public List<ComandaMissatgeEDI> parse () {
        String nombre_fichero = fitxerEdi.getName();
//        System.out.println("Vaig a llegir: " + path.toString());
        List<ComandaMissatgeEDI> pedidos = new ArrayList<ComandaMissatgeEDI>();
        ComandaMissatgeEDI pedido = null;
        Linea linea = null;
        List<Linea> lineas = null;
        List<CT> CTs = null;
        List<LE> LEs = null;
        List<LT> LTs = null;
        List<LL> LLs = null;
        LineaPedidoPrevio lineaPedidoPrevio = null;
        List<LineaPedidoPrevio> lineasPedidoPrevio = null;
        AlbaranPrevio albaranPrevio = null;
        List<AlbaranPrevio> albaranesPrevios = null;
        DA da = null;
        Detalle detalle = null;
        List<Detalle> detalles = null;
        String numero_documento = "";
        int iLine = 1;

        List<List<String>> partes = readAndSplitFiles();

        for (var parte : partes) {
            for (var line : parte) {
                if (line.startsWith("CA")) {
                    // Inicialitzo el "pedido" i els grups (lineaspedidosPrevios, alabaranes i detalles) donat un nou "pedido" dins un mateix fitxer
                    pedido = new ComandaMissatgeEDI();
//                    pedido.setNombreFicheroEDI(nombre_fichero);
                    CTs = new ArrayList<CT>();
                    lineas = new ArrayList<Linea>();
                    CA ca = new CA();
                    ca.setInicio(line.substring(0, CA.i_inicio).trim());
                    ca.setTipo(line.substring(CA.i_inicio, CA.i_tipo).trim());
                    ca.setNumeroEnvio(line.substring(CA.i_tipo, CA.i_numeroEnvio).trim());
                    ca.setBuzonOrigen(line.substring(CA.i_numeroEnvio, CA.i_buzonOrigen).trim());
                    ca.setBuzonDestino(line.substring(CA.i_buzonOrigen, CA.i_buzonDestino).trim());
                    numero_documento = line.substring(CA.i_buzonDestino, CA.i_numeroDocumento).trim();
                    ca.setNumeroDocumento(numero_documento);
                    ca.setCodigoDocumento(line.substring(CA.i_numeroDocumento, CA.i_codigoDocumento).trim());
                    ca.setDocumento(line.substring(CA.i_codigoDocumento, CA.i_documento).trim());
                    ca.setLogistica(line.substring(CA.i_documento, CA.i_logistica).trim());
                    ca.setFuncion(line.substring(CA.i_logistica, CA.i_funcion).trim());
                    ca.setReferencia(line.substring(CA.i_funcion, CA.i_referencia).trim());
                    pedido.setCA(ca);
                } else if (line.startsWith("CB")) {
                    CB cb = new CB();
                    cb.setInicio(line.substring(0, CB.i_inicio).trim());
                    cb.setFechaMensaje(line.substring(CB.i_inicio, CB.i_fechaMensaje).trim());
                    cb.setHoraMensaje(line.substring(CB.i_fechaMensaje, CB.i_horaMensaje).trim());
                    cb.setFechaInicioHorizonte(line.substring(CB.i_horaMensaje, CB.i_fechaInicioHorizonte).trim());
                    cb.setFechaFinalHorizonte(line.substring(CB.i_fechaInicioHorizonte, CB.i_fechaFinalHorizonte).trim());
                    cb.setTipoFecha(line.substring(CB.i_fechaFinalHorizonte, CB.i_tipoFecha).trim());
                    cb.setIdTransportista(line.substring(CB.i_tipoFecha, CB.i_idTransportista).trim());
                    cb.setTipoTransporte(line.substring(CB.i_idTransportista, CB.i_tipoTransporte).trim());
                    pedido.setCB(cb);
                } else if (line.startsWith("CC")) {
                    CC cc = new CC();
                    cc.setInicio(line.substring(0, CC.i_inicio).trim());
                    cc.setNombreComprador(line.substring(CC.i_inicio, CC.i_nombreComprador).trim());
                    cc.setDireccionComprador(line.substring(CC.i_nombreComprador, CC.i_direccionComprador).trim());
                    cc.setLocalidadComprador(line.substring(CC.i_direccionComprador, CC.i_localidadComprador).trim());
                    cc.setProvinciaComprador(line.substring(CC.i_localidadComprador, CC.i_provinciaComprador).trim());
                    cc.setCodigoPostalComprador(line.substring(CC.i_provinciaComprador, CC.i_codigoPostalComprador).trim());
                    pedido.setCC(cc);
                } else if (line.startsWith("CD")) {
                    CD cd = new CD();
                    cd.setInicio(line.substring(0, CD.i_inicio).trim());
                    cd.setPaisComprador(line.substring(CD.i_inicio, CD.i_paisComprador).trim());
                    cd.setPersonaContactoComprador(line.substring(CD.i_paisComprador, CD.i_personaContactoComprador).trim());
                    cd.setTelefonoComprador(line.substring(CD.i_personaContactoComprador, CD.i_telefonoComprador).trim());
                    cd.setFaxComprador(line.substring(CD.i_telefonoComprador, CD.i_faxComprador).trim());
                    cd.setEmailComprador(line.substring(CD.i_faxComprador, CD.i_emailComprador).trim());
                    pedido.setCD(cd);
                } else if (line.startsWith("CI")) {
                    CI ci = new CI();
                    ci.setInicio(line.substring(0, CI.i_inicio).trim());
                    ci.setIdComprador(line.substring(CI.i_inicio, CI.i_idComprador).trim());
                    ci.setIdProveedor(line.substring(CI.i_idComprador, CI.i_idProveedor).trim());
                    ci.setNumCuentaInternaProveedor(line.substring(CI.i_idProveedor, CI.i_numCuentaInternaProveedor).trim());
                    ci.setIdExpedidor(line.substring(CI.i_numCuentaInternaProveedor, CI.i_idExpedidor).trim());
                    ci.setIdFacturado(line.substring(CI.i_idExpedidor, CI.i_idFacturado).trim());
                    pedido.setCI(ci);
                } else if (line.startsWith("CP")) {
                    CP cp = new CP();
                    cp.setInicio(line.substring(0, CP.i_inicio).trim());
                    cp.setNombreProveedor(line.substring(CP.i_inicio, CP.i_nombreProveedor).trim());
                    cp.setDireccionProveedor(line.substring(CP.i_nombreProveedor, CP.i_direccionProveedor).trim());
                    cp.setLocalidadProveedor(line.substring(CP.i_direccionProveedor, CP.i_localidadProveedor).trim());
                    cp.setProvinciaProveedor(line.substring(CP.i_localidadProveedor, CP.i_provinciaProveedor).trim());
                    cp.setCodigoPostalProveedor(line.substring(CP.i_provinciaProveedor, CP.i_codigoPostalProveedor).trim());
                    pedido.setCP(cp);
                } else if (line.startsWith("CQ")) {
                    CQ cq = new CQ();
                    cq.setInicio(line.substring(0, CQ.i_inicio).trim());
                    cq.setPaisProveedor(line.substring(CQ.i_inicio, CQ.i_paisProveedor).trim());
                    cq.setPersonaContactoProveedor(line.substring(CQ.i_paisProveedor, CQ.i_personaContactoProveedor).trim());
                    cq.setTelefonoProveedor(line.substring(CQ.i_personaContactoProveedor, CQ.i_telefonoProveedor).trim());
                    cq.setFaxProveedor(line.substring(CQ.i_telefonoProveedor, CQ.i_faxProveedor).trim());
                    pedido.setCQ(cq);
                } else if (line.startsWith("CF")) {
                    CF cf = new CF();
                    cf.setInicio(line.substring(0, CF.i_inicio).trim());
                    cf.setNombreFacturacion(line.substring(CF.i_inicio, CF.i_nombreFacturacion).trim());
                    cf.setDepartamentFacturacion(line.substring(CF.i_nombreFacturacion, CF.i_departamentFacturacion).trim());
                    pedido.setCF(cf);
                } else if (line.startsWith("CT")) {
                    CT ct = new CT();
                    ct.setInicio(line.substring(0, CT.i_inicio).trim());
                    ct.setTextoLibre(line.substring(CT.i_inicio, CT.i_textoLibre).trim());
                    CTs.add(ct);
                } else if (line.startsWith("LA")) {
                    linea = new Linea();
                    lineasPedidoPrevio = new ArrayList<LineaPedidoPrevio>();
                    albaranesPrevios = new ArrayList<AlbaranPrevio>();
                    detalles = new ArrayList<Detalle>();
                    LA la = new LA();
                    LEs = new ArrayList<LE>();
                    LTs = new ArrayList<LT>();
                    LLs = new ArrayList<LL>();
                    la.setInicio(line.substring(0, LA.i_inicio).trim());
                    la.setIdArticuloComprador(line.substring(LA.i_inicio, LA.i_idArticuloComprador).trim());
                    la.setEstadoArticulo(line.substring(LA.i_idArticuloComprador, LA.i_estadoArticulo).trim());
                    la.setCodigoAccion(line.substring(LA.i_estadoArticulo, LA.i_codigoAccion).trim());
                    la.setUnidadMedida(line.substring(LA.i_codigoAccion, LA.i_unidadMedida).trim());
                    la.setLugarEntrega(line.substring(LA.i_unidadMedida, LA.i_lugarEntrega).trim());
                    la.setLugarDestinoFinal(line.substring(LA.i_lugarEntrega, LA.i_lugarDestinoFinal).trim());
                    la.setCodigoAlmacen(line.substring(LA.i_lugarDestinoFinal, LA.i_codigoAlmacen).trim());
                    la.setFechaCubierta(line.substring(LA.i_codigoAlmacen, LA.i_fechaCubierta).trim());
                    la.setPaisOrigenCodificado(line.substring(LA.i_fechaCubierta, LA.i_paisOrigenCodificado).trim());
                    la.setFechaLimiteEntrega(line.substring(LA.i_paisOrigenCodificado, LA.i_fechaLimiteEntrega).trim());
                    la.setFechaCalculoActual(line.substring(LA.i_fechaLimiteEntrega, LA.i_fechaCalculoActual).trim());
                    la.setFechaInicioCalculo(line.substring(LA.i_fechaCalculoActual, LA.i_fechaInicioCalculo).trim());
                    la.setCodigoFrecuenciaEntrega(line.substring(LA.i_fechaInicioCalculo, LA.i_codigoFrecuenciaEntrega).trim());
                    la.setIndicadorRequerimiento(line.substring(LA.i_codigoFrecuenciaEntrega, LA.i_indicadorRequerimiento).trim());
                    la.setCodigoCaracteristicaItem(line.substring(LA.i_indicadorRequerimiento, LA.i_codigoCaracteristicaItem).trim());
                    la.setHoraLimiteEntrega(line.substring(LA.i_codigoCaracteristicaItem, LA.i_horaLimiteEntrega).trim());
                    linea.setLA(la);
                } else if (line.startsWith("LB")) {
                    LB lb = new LB();
                    lb.setInicio(line.substring(0, LB.i_inicio).trim());
                    lb.setStockActual(line.substring(LB.i_inicio, LB.i_stockActual).trim());
                    lb.setStockSeguridad(line.substring(LB.i_stockActual, LB.i_stockSeguridad).trim());
                    lb.setIdArticuloProveedor(line.substring(LB.i_stockSeguridad, LB.i_idArticuloProveedor).trim());
                    lb.setPaisConsignatario(line.substring(LB.i_idArticuloProveedor, LB.i_paisConsignatario).trim());
                    lb.setPrecio(line.substring(LB.i_paisConsignatario, LB.i_precio).trim());
                    lb.setDivisa(line.substring(LB.i_precio, LB.i_divisa).trim());
                    linea.setLB(lb);
                } else if (line.startsWith("LC")) {
                    LC lc = new LC();
                    lc.setInicio(line.substring(0, LC.i_inicio).trim());
                    lc.setCodigoConsignatario(line.substring(LC.i_inicio, LC.i_codigoConsignatario).trim());
                    lc.setNombreConsignatario(line.substring(LC.i_codigoConsignatario, LC.i_nombreConsignatario).trim());
                    lc.setPersonaContactoConsignatario(line.substring(LC.i_nombreConsignatario, LC.i_personaContactoConsignatario).trim());
                    lc.setTelefonoConsignatario(line.substring(LC.i_personaContactoConsignatario, LC.i_telefonoConsignatario).trim());
                    linea.setLC(lc);
                } else if (line.startsWith("LD")) {
                    LD ld = new LD();
                    ld.setInicio(line.substring(0, LD.i_inicio).trim());
                    ld.setDireccionConsignatario(line.substring(LD.i_inicio, LD.i_direccionConsignatario).trim());
                    ld.setLocalidadConsignatario(line.substring(LD.i_direccionConsignatario, LD.i_localidadConsignatario).trim());
                    ld.setProvinciaConsignatario(line.substring(LD.i_localidadConsignatario, LD.i_provinciaConsignatario).trim());
                    ld.setCodigoPostalConsignatario(line.substring(LD.i_provinciaConsignatario, LD.i_codigoPostalConsignatario).trim());
                    ld.setFaxConsignatario(line.substring(LD.i_codigoPostalConsignatario, LD.i_faxConsignatario).trim());
                    linea.setLD(ld);
                } else if (line.startsWith("LS")) {
                    LS ls = new LS();
                    ls.setInicio(line.substring(0, LS.i_inicio).trim());
                    ls.setCodigoSolicitante(line.substring(LS.i_inicio, LS.i_codigoSolicitante).trim());
                    ls.setNombreSolicitante(line.substring(LS.i_codigoSolicitante, LS.i_nombreSolicitante).trim());
                    ls.setPersonaAprovisionamiento(line.substring(LS.i_nombreSolicitante, LS.i_personaAprovisionamiento).trim());
                    ls.setTelefonoSolicitante(line.substring(LS.i_personaAprovisionamiento, LS.i_telefonoSolicitante).trim());
                    linea.setLS(ls);
                } else if (line.startsWith("LE")) {
                    LE le = new LE();
                    le.setInicio(line.substring(0, LE.i_inicio).trim());
                    le.setTipoBulto(line.substring(LE.i_inicio, LE.i_tipoBulto).trim());
                    le.setReferenciaEmbalaje(line.substring(LE.i_tipoBulto, LE.i_referenciaEmalaje).trim());
                    le.setPiezasPorEmbalaje(line.substring(LE.i_referenciaEmalaje, LE.i_piezasPorEmbalaje).trim());
                    le.setTipoContenedor(line.substring(LE.i_piezasPorEmbalaje, LE.i_tipoContenedor).trim());
                    le.setNumeroEmbalajes(line.substring(LE.i_tipoContenedor, LE.i_numeroEmbalajes).trim());
                    le.setNivelEmpaquetamiento(line.substring(LE.i_numeroEmbalajes, LE.i_nivelEmpaquetamiento).trim());
                    LEs.add(le);
                    linea.setEmpaquetamientos(LEs);
                } else if (line.startsWith("LT")) {
                    LT lt = new LT();
                    lt.setInicio(line.substring(0, LT.i_inicio).trim());
                    lt.setTexto1(line.substring(LT.i_inicio, LT.i_texto1).trim());
                    lt.setTexto2(line.substring(LT.i_texto1, LT.i_texto2).trim());
                    lt.setTexto3(line.substring(LT.i_texto2, LT.i_texto3).trim());
                    lt.setTexto4(line.substring(LT.i_texto3, LT.i_texto4).trim());
                    LTs.add(lt);
                    linea.setObservaciones(LTs);
                } else if (line.startsWith("LG")) {
                    LG lg = new LG();
                    lg.setInicio(line.substring(0, LG.i_inicio).trim());
                    lg.setDescripcionArticulo(line.substring(LG.i_inicio, LG.i_descripcionArticulo).trim());
                    lg.setNumeroContrato(line.substring(LG.i_descripcionArticulo, LG.i_numeroContrato).trim());
                    lg.setFechaContrato(line.substring(LG.i_numeroContrato, LG.i_fechaContrato).trim());
                    lg.setNumeroDocumentoAnterior(line.substring(LG.i_fechaContrato, LG.i_numeroDocumentoAnterior).trim());
                    lg.setFechaDocumentoAnterior(line.substring(LG.i_numeroDocumentoAnterior, LG.i_fechaDocumentoAnterior).trim());
                    lg.setNumeroLineaContrato(line.substring(LG.i_fechaDocumentoAnterior, LG.i_numeroLineaContrato).trim());
                    lg.setNumeroDePlano(line.substring(LG.i_numeroLineaContrato, LG.i_numeroDeplano).trim());
                    linea.setLG(lg);
                } else if (line.startsWith("LH")) {
                    LH lh = new LH();
                    lh.setInicio(line.substring(0, LH.i_inicio).trim());
                    lh.setNumPedidoPrevio(line.substring(LH.i_inicio, LH.i_numeroPedidoPrevio).trim());
                    lh.setNumeroLab(line.substring(LH.i_numeroPedidoPrevio, LH.i_numeroLab).trim());
                    lh.setNumeroLote(line.substring(LH.i_numeroLab, LH.i_numeroLote).trim());
                    lh.setFechaLab(line.substring(LH.i_numeroLote, LH.i_fechaLab).trim());
                    lh.setFechaPedidoPrevio(line.substring(LH.i_fechaLab, LH.i_fechaPedidoPrevio).trim());
                    lh.setNumeroPedidoNuevo(line.substring(LH.i_fechaPedidoPrevio, LH.i_numeroPedidoNuevo).trim());
                    lh.setFechaPedidoNuevo(line.substring(LH.i_numeroPedidoPrevio, LH.i_fechaPedidoNuevo).trim());
                    linea.setLH(lh);
                } else if (line.startsWith("LI")) {
                    LI li = new LI();
                    li.setInicio(line.substring(0, LI.i_inicio).trim());
                    li.setCambioIngenieria(line.substring(LI.i_inicio, LI.i_cambioIngenieria).trim());
                    li.setFechaCambioIngerieria(line.substring(LI.i_cambioIngenieria, LI.i_fechaCambioIngerieria).trim());
                    li.setNumeroRuta(line.substring(LI.i_fechaCambioIngerieria, LI.i_numeroRuta).trim());
                    li.setNumeroSufijoRuta(line.substring(LI.i_numeroRuta, LI.i_numeroSufijoRuta).trim());
                    li.setNumeroTransporte(line.substring(LI.i_numeroSufijoRuta, LI.i_numeroTransporte).trim());
                    linea.setLI(li);
                } else if (line.startsWith("LL")) {
                    LL ll = new LL();
                    ll.setInicio(line.substring(0, LL.i_inicio).trim());
                    ll.setCodigoEtiqueta(line.substring(LL.i_inicio, LL.i_codigoEtiqueta).trim());
                    ll.setTexto(line.substring(LL.i_codigoEtiqueta, LL.i_texto).trim());
                    LLs.add(ll);
                    linea.setEtiquetas(LLs);
                } else if (line.startsWith("LQ")) {
                    lineaPedidoPrevio = new LineaPedidoPrevio();
                    LQ lq = new LQ();
                    lq.setInicio(line.substring(0, LQ.i_inicio).trim());
                    lq.setCantidadBalance(line.substring(LQ.i_inicio, LQ.i_cantidadBalance).trim());
                    lq.setFechaCantidadBalance(line.substring(LQ.i_cantidadBalance, LQ.i_fechaCantidadBalance).trim());
                    lq.setCantidadAtraso(line.substring(LQ.i_fechaCantidadBalance, LQ.i_cantidadAtraso).trim());
                    lq.setFechaCantidadAtraso(line.substring(LQ.i_cantidadAtraso, LQ.i_fechaCantidadAtraso).trim());
                    lq.setCantidadUrgente(line.substring(LQ.i_fechaCantidadAtraso, LQ.i_cantidadUrgente).trim());
                    lq.setFechaCantidadUrgente(line.substring(LQ.i_cantidadUrgente, LQ.i_fechaCantidadUrgente).trim());
                    lq.setCantidadTransito(line.substring(LQ.i_fechaCantidadUrgente, LQ.i_cantidadTransito).trim());
                    lq.setFechaCantidadTransito(line.substring(LQ.i_cantidadTransito, LQ.i_fechaCantidadTransito).trim());
                    lq.setCantidadAcumuladaRecibida(line.substring(LQ.i_fechaCantidadTransito, LQ.i_cantidadAcumuladaRecibida).trim());
                    lq.setCantidadAcumuladaProgramada(line.substring(LQ.i_cantidadAcumuladaRecibida, LQ.i_cantidadAcumuladaProgramada).trim());
                    lq.setInicioPeriodoAcumulada(line.substring(LQ.i_cantidadAcumuladaProgramada, LQ.i_inicioPeriodoAcumulada).trim());
                    lq.setFinPeriodoAcumulada(line.substring(LQ.i_inicioPeriodoAcumulada, LQ.i_finPeriodoAcumulada).trim());
                    lq.setCantidadAcumuladaPeriodoAnterior(line.substring(LQ.i_finPeriodoAcumulada, LQ.i_cantidadAcumuladaPeriodoAnterior).trim());
                    lineaPedidoPrevio.setLQ(lq);
                    lineasPedidoPrevio.add(lineaPedidoPrevio);
                } else if (line.startsWith("AA")) {
                    albaranPrevio = new AlbaranPrevio();
                    AA aa = new AA();
                    aa.setInicio(line.substring(0, AA.i_inicio).trim());
                    aa.setReferenciaAlbaranEntrada(line.substring(AA.i_inicio, AA.i_referenciaAlbaranEntrada).trim());
                    aa.setFechaAlbaran(line.substring(AA.i_referenciaAlbaranEntrada, AA.i_fechaAlbaran).trim());
                    aa.setHoraAlbaran(line.substring(AA.i_fechaAlbaran, AA.i_horaAlbaran).trim());
                    aa.setCantidadEnviadaAlbaran(line.substring(AA.i_horaAlbaran, AA.i_cantidadEnviadaAlbaran).trim());
                    aa.setCantidadRecibidaAlbaran(line.substring(AA.i_cantidadEnviadaAlbaran, AA.i_cantidadRecibidaAlbaran).trim());
                    aa.setFechaRecepcion(line.substring(AA.i_cantidadRecibidaAlbaran, AA.i_fechaRecepcion).trim());
                    albaranPrevio.setAA(aa);
                    albaranesPrevios.add(albaranPrevio);
                } else if (line.startsWith("DA")) {
                    da = new DA();
                    da.setInicio(line.substring(0, DA.i_inicio).trim());
                    da.setTipo(line.substring(DA.i_inicio, DA.i_tipo).trim());
                    da.setCantidad(line.substring(DA.i_tipo, DA.i_cantidad).trim());
                    da.setUnidadMedida(line.substring(DA.i_cantidad, DA.i_unidadMedida).trim());
                    da.setFechaInicial(line.substring(DA.i_unidadMedida, DA.i_fechaInicial).trim());
                    da.setHoraInicial(line.substring(DA.i_fechaInicial, DA.i_horaInicial).trim());
                    da.setFechaFinal(line.substring(DA.i_horaInicial, DA.i_fechaFinal).trim());
                    da.setHoraFinal(line.substring(DA.i_fechaFinal, DA.i_horaFinal).trim());
                    da.setRazonInstruccion(line.substring(DA.i_horaFinal, DA.i_razonInstruccion).trim());
                    da.setNumeroRAN(line.substring(DA.i_razonInstruccion, DA.i_numeroRAN).trim());
                    da.setFechaRAN(line.substring(DA.i_numeroRAN, DA.i_fechaRAN).trim());
                    da.setFrecuenciaEnvio(line.substring(DA.i_fechaRAN, DA.i_frecuenciaEnvio).trim());
                    da.setNumeroTarjetaKanban(line.substring(DA.i_frecuenciaEnvio, DA.i_numeroTarjetaKanban).trim());
                    da.setUltimoNumeroRAN(line.substring(DA.i_numeroTarjetaKanban, DA.i_ultimoNumeroKanban).trim());
                    lineas.remove(linea);
                    detalle = new Detalle();
                    detalle.setDA(da);
                    detalles.add(detalle);
                    linea.setLineasPedidoPrevio(lineasPedidoPrevio);
                    linea.setAlbaranesPrevios(albaranesPrevios);
                    linea.setDetalles(detalles);
                    lineas.add(linea);
                } else if (line.startsWith("DR")) {
                    lineas.remove(linea);
                    detalles.remove(detalle);
                    DR dr = new DR();
                    dr.setInicio(line.substring(0, DR.i_inicio).trim());
                    dr.setFechaEntradaEnLinea(line.substring(DR.i_inicio, DR.i_fechaEntradaEnlinea).trim());
                    dr.setHoraEntradaEnlinea(line.substring(DR.i_fechaEntradaEnlinea, DR.i_horaEntradaEnlinea).trim());
                    detalle.setDA(da);
                    detalle.setDR(dr);
                    detalles.add(detalle);
                    linea.setDetalles(detalles);
                    lineas.add(linea);
                } else if (line.startsWith("ZZ")) {
//                    linea.setLineasPedidoPrevio(lineasPedidoPrevio);
//                    linea.setAlbaranesPrevios(albaranesPrevios);
//                    linea.setDetalles(detalles);
                    if (detalle != null && detalle.getDA() == null)
                        lineas.add(linea);
                    pedido.setLineas(lineas);
                    pedido.setTextosLibres(CTs);
                    ZZ zz = new ZZ();
                    zz.setInicio(extractString(line));
                    zz.setNumeroRegistros(line.substring(ZZ.i_inicio, ZZ.i_numeroRegitros).trim());
                    pedido.setZZ(zz);
                }
                iLine++;
            }
            pedidos.add(pedido);
        }
        return pedidos;
    }

    private String extractString(String linia) {
        return linia.substring(0, ZZ.i_inicio).trim();
    }

    private Optional<LocalDate> extracData(){
        return null;
    }

    private List<List<String>> readAndSplitFiles(){
        List<List<String>> partes = new ArrayList<>();
        List<String> contenidoParteActual = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(fitxerEdi.toPath(), StandardCharsets.ISO_8859_1)) {
            String linea;
            while ((linea = br.readLine()) != null) {
                contenidoParteActual.add(linea);
                if (linea.trim().startsWith(ComandaEDIConfig.DELIMITER)) {
                    partes.add(new ArrayList<>(contenidoParteActual));
                    contenidoParteActual.clear();
                }
            }
            // Añadir la última parte si no está vacía
            if (!contenidoParteActual.isEmpty()) {
                partes.add(new ArrayList<>(contenidoParteActual));
            }
        } catch (IOException e) {
            throw new AppException("Error dividint el fitxer EDI: {}", e);
        }

        return partes;
    }

}
