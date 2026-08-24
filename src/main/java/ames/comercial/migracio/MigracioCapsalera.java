package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.albarans.internal.domain.albara.*;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepositorySQL;
import ames.comercial.shared.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class MigracioCapsalera {

    @Autowired AlbaraRepositorySQL albaraRepository;

    /**
     * Magatzems que no són de plataforma a efectes de tipificar els albarans històrics: les seves
     * sortides són albarans de client (no consums) i els traspassos que hi entren són traspassos de
     * magatzem (no de plataforma). Són els magatzems propis d'AMES més els que estan marcats com a
     * plataforma a l'Advantage sense ser-ho ({@link MigracioMagatzems#MAGATZEMS_TRANSIT}, que es migren
     * com a {@code TRANSIT}); es comparteix la llista per no mantenir-la en dos llocs.
     */
    private List<String> magatzemsAmes = Stream.concat(
            Stream.of("0001",
                    "0005",
                    "0006",
                    "0007",
                    "0008",
                    "0037",
                    "0038",
                    "0040",
                    "0045",
                    "0070",
                    "0077",
                    "0080"),
            MigracioMagatzems.MAGATZEMS_TRANSIT.stream()).toList();

    public void migracio() {
        System.out.println(LocalDateTime.now() + " Iniciant obtenció moviments històrics");
        var albaraHistorics = obtenirAlbaraHistoric();

        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                select *
                from albcap a
                LEFT JOIN dummy d ON a.empcod = d.str1
                WHERE 1=1 AND year(albdat) >= 2026 AND albcod <> ''
                ORDER BY diareg ASC, empcod ASC, albcod ASC
                """);
        AdvantageDao.ResultSetAction<List<Albcap>> rsAction = rs -> {
            var result = new ArrayList<Albcap>();
            while (rs.next()) {
                result.add(new Albcap(
                        rs.getLong("albcod"),
                        rs.getString("empcod"),
                        rs.getString("clicod"),
                        rs.getDate("albdat") != null ? rs.getDate("albdat").toLocalDate() : null,
                        rs.getString("magcod"),
                        rs.getString("albnom"),
                        rs.getString("albdre"),
                        rs.getString("albpob"),
                        rs.getString("codpos"),
                        rs.getString("albpai"),
                        rs.getString("albenv"),
                        rs.getString("zontra"),
                        rs.getString("desti"),
                        rs.getString("albtra"),
                        rs.getString("nomb"),
                        rs.getString("dreb"),
                        rs.getString("pobb"),
                        rs.getString("posb"),
                        rs.getString("paib"),
                        rs.getString("nomf"),
                        rs.getString("dref"),
                        rs.getString("pobf"),
                        rs.getString("posf"),
                        rs.getString("paif"),
                        rs.getString("numalbmix") != null && !rs.getString("numalbmix").isBlank() ? rs.getString("numalbmix") : null,
                        rs.getLong("pesbru"),
                        rs.getLong("bultos"),
                        rs.getString("albserv"),
                        rs.getString("albenprep"),
                        rs.getString("albenserv"),
                        rs.getString("entregat"),
                        rs.getDate("dataprev") != null ? rs.getDate("dataprev").toLocalDate() : null,
                        rs.getDate("dataenv") != null ? rs.getDate("dataenv").toLocalDate() : null,
                        rs.getLong("numpal1"),
                        rs.getString("altpalet1"),
                        rs.getLong("numpal2"),
                        rs.getString("altpalet2"),
                        rs.getLong("numpal3"),
                        rs.getString("altpalet3"),
                        rs.getString("matricula"),
                        rs.getString("avinum"),
                        rs.getString("avienviat"),
                        rs.getString("empcodr"),
                        rs.getString("magcodr"),
                        rs.getString("trasabon"),
                        rs.getString("punto"),
                        rs.getString("aclgat"),
                        rs.getString("csg3921"),
                        rs.getString("mrnnum"),
                        rs.getDate("mrndata") != null ? rs.getDate("mrndata").toLocalDate() : null,
                        rs.getString("mrntype"),
                        rs.getBigDecimal("implogi"),
                        rs.getString("textlogi"),
                        rs.getBigDecimal("costra"),
                        rs.getString("tecostra"),
                        rs.getBigDecimal("costexpres"),
                        rs.getString("raoexpres"),
                        rs.getString("observ"),
                        rs.getString("notes"),
                        rs.getString("notaprof"),
                        rs.getString("raoinci"),
                        rs.getDate("diareg") != null ? rs.getDate("diareg").toLocalDate() : null,
                        rs.getString("usuari"),
                        rs.getString("albpro"),
                        rs.getString("albesp"),
                        rs.getString("albtancat"),
                        rs.getString("albfac1"),
                        rs.getString("factauto"),
                        rs.getString("albfax"),
                        rs.getString("albnor"),
                        rs.getString("pagatports"),
                        rs.getString("novalorat"),
                        rs.getString("envhis"),
                        rs.getString("facttrans"),
                        rs.getLong("numbox"),
                        rs.getString("altbox"),
                        rs.getBigDecimal("costmoq"),
                        rs.getString("reftransp"),
                        rs.getString("albexpres")
                ));
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant obtenció capçaleres ALBCAP");
        var listAlbcaps = new AdvantageDao().query(prep, rsAction);
        System.out.println(LocalDateTime.now() + " Total registres albcap a migrar: " + listAlbcaps.size());
        var listAlbarans = new ArrayList<Albara>();
        int contador = 0;
        for (var albcap : listAlbcaps) {
            try {
                var albara = toAlbara(albcap, albaraHistorics);
                listAlbarans.add(albara);
            } catch (Exception e) {
                System.err.println("Error migrant Albarà: " + albcap);
                e.printStackTrace();
            }
            contador++;
            if (contador % 5000 == 0) {
                System.out.println("Processats " + contador + " registres...");
            }
        }
        albaraRepository.saveBatch(listAlbarans);
    }

    private List<AlbaraHistoric> obtenirAlbaraHistoric() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                SELECT empcod, hisalb, h.magcod, h.trasmag, histip
                FROM his h
                where (histip = 2 or histip = 4 or histip = 7) AND hisalb <> ''
                group by empcod, hisalb, h.magcod, h.trasmag, histip;
                """);
        AdvantageDao.ResultSetAction<List<AlbaraHistoric>> rsAction = rs -> {
            var result = new ArrayList<AlbaraHistoric>();
            while (rs.next()) {
                result.add(new AlbaraHistoric(
                        rs.getString("empcod"),
                        rs.getString("hisalb"),
                        rs.getString("magcod"),
                        rs.getString("trasmag"),
                        rs.getString("histip")));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private Albara toAlbara(Albcap albcap, List<AlbaraHistoric> albaraHistorics) {
        var keyAlbara = KeyAlbara.of(albcap.albnum(), albcap.empcod());

        // Processar adreça principal
        var adresa = AdresaImpl.builder()
                .destinatari(nvl(albcap.albnom(), ""))
                .adresa(nvl(albcap.albdre(), ""))
                .poblacio(nvl(albcap.albpob(), ""))
                .codiPostal(nvl(albcap.codpos(), ""))
                .pais(nvl(albcap.albpai(), ""))
                .build();

        // Processar informació enviament
        var informacioEnviament = parseInformacioEnviament(albcap.albenv(), albcap.zontra(), albcap.desti(), albcap.albtra());

        // Processar adreça broker (si existeix)
        Optional<Adresa> adresaBroker = Optional.empty();
        if (albcap.nomb() != null && !albcap.nomb().isBlank()) {
            adresaBroker = Optional.of(AdresaImpl.builder()
                    .destinatari(nvl(albcap.nomb(), ""))
                    .adresa(nvl(albcap.dreb(), ""))
                    .poblacio(nvl(albcap.pobb(), ""))
                    .codiPostal(nvl(albcap.posb(), ""))
                    .pais(nvl(albcap.paib(), ""))
                    .build());
        }

        // Processar adreça factura proforma (si existeix)
        Optional<Adresa> adresaFacturaProforma = Optional.empty();
        if (albcap.nomf() != null && !albcap.nomf().isBlank()) {
            adresaFacturaProforma = Optional.of(AdresaImpl.builder()
                    .destinatari(nvl(albcap.nomf(), ""))
                    .adresa(nvl(albcap.dref(), ""))
                    .poblacio(nvl(albcap.pobf(), ""))
                    .codiPostal(nvl(albcap.posf(), ""))
                    .pais(nvl(albcap.paif(), ""))
                    .build());
        }

        // Processar informació magatzem
        var informacioMagatzemBuilder = InformacioMagatzemImpl.builder()
                .isEnServei(isTrue(albcap.albenserv()))
                .isEnPreparacio(isTrue(albcap.albenprep()))
                .isServit(isTrue(albcap.albserv()))
                .isEntregat(isTrue(albcap.entregat()))
                .isAvisEnviat(isTrue(albcap.avienviat()));

        if (albcap.numalbmix() != null) {
            informacioMagatzemBuilder.numAlbaraMix(albcap.numalbmix());
        }
        if (albcap.pesbru() != null && albcap.pesbru() > 0) {
            informacioMagatzemBuilder.pesBrut(albcap.pesbru());
        }
        if (albcap.bultos() != null && albcap.bultos() > 0) {
            informacioMagatzemBuilder.bultos(albcap.bultos());
        }
        if (albcap.dataprev() != null) {
            informacioMagatzemBuilder.dataPrevista(albcap.dataprev().atStartOfDay());
        }
        if (albcap.dataenv() != null) {
            informacioMagatzemBuilder.dataEnviament(albcap.dataenv().atStartOfDay());
        }
        if (albcap.numpal1() != null && albcap.numpal1() > 0) {
            informacioMagatzemBuilder.paletsTipus1(InformacioPaletImpl.builder()
                    .numero(albcap.numpal1())
                    .alsada(nvl(albcap.altpalet1(), ""))
                    .build());
        }
        if (albcap.numpal2() != null && albcap.numpal2() > 0) {
            informacioMagatzemBuilder.paletsTipus2(InformacioPaletImpl.builder()
                    .numero(albcap.numpal2())
                    .alsada(nvl(albcap.altpalet2(), ""))
                    .build());
        }
        if (albcap.numpal3() != null && albcap.numpal3() > 0) {
            informacioMagatzemBuilder.paletsTipus3(InformacioPaletImpl.builder()
                    .numero(albcap.numpal3())
                    .alsada(nvl(albcap.altpalet3(), ""))
                    .build());
        }
        if (albcap.matricula() != null && !albcap.matricula().isBlank()) {
            informacioMagatzemBuilder.matricula(albcap.matricula());
        }
        if (albcap.avinum() != null && !albcap.avinum().isBlank()) {
            informacioMagatzemBuilder.numeroAvis(albcap.avinum());
        }

        var informacioMagatzem = informacioMagatzemBuilder.build();

        // Processar informació traspàs (si existeix)
        Optional<InformacioTraspas> informacioTraspas = Optional.empty();
        if (albcap.empcodr() != null && !albcap.empcodr().isBlank()) {
            informacioTraspas = Optional.of(InformacioTraspasImpl.builder()
                    .empresaReceptora(albcap.empcodr())
                    .magatzemReceptor(nvl(albcap.magcodr(), ""))
                    .isTraspasAbonable(isTrue(albcap.trasabon()))
                    .build());
        }

        // Processar informació EDI (si existeix)
        Optional<InformacioEdi> informacioEdi = Optional.empty();
        if (albcap.punto() != null && !albcap.punto().isBlank()) {
            informacioEdi = Optional.of(InformacioEdiImpl.builder()
                    .punto(nvl(albcap.punto(), ""))
                    .aclgat(nvl(albcap.aclgat(), ""))
                    .csg3921(nvl(albcap.csg3921(), ""))
                    .mrnNum(nvl(albcap.mrnnum(), ""))
                    .mrnData(nvl(albcap.mrndata(), LocalDate.now()))
                    .mrnType(nvl(albcap.mrntype(), ""))
                    .build());
        }

        // Processar costs
        Optional<CostLogistic> costLogistic = Optional.empty();
        if (albcap.implogi() != null && albcap.implogi().compareTo(BigDecimal.ZERO) != 0) {
            costLogistic = Optional.of(CostLogisticImpl.builder()
                    .imp(albcap.implogi())
                    .comentaris(nvl(albcap.textlogi(), ""))
                    .build());
        }

        Optional<CostTransport> costTransport = Optional.empty();
        if (albcap.costra() != null && albcap.costra().compareTo(BigDecimal.ZERO) != 0) {
            costTransport = Optional.of(CostTransportImpl.builder()
                    .imp(albcap.costra())
                    .comentaris(nvl(albcap.raocostra(), ""))
                    .build());
        }

        Optional<CostEnviamentExpress> costEnviamentExpress = Optional.empty();
        if (albcap.costexpres() != null && albcap.costexpres().compareTo(BigDecimal.ZERO) != 0) {
            costEnviamentExpress = Optional.of(CostEnviamentExpressImpl.builder()
                    .imp(albcap.costexpres())
                    .motiu(MotiuEnviamentExpres.ALTRES)
                    .detallMotiu(nvl(albcap.raoexpres(), ""))
                    .divisa(Divisa.EURO)
                    .build());
        }

        // Construir Albara
        var albaraBuilder = AlbaraImpl.builder()
                .id(keyAlbara)
                .tipus(tipusAlbara(albcap, albaraHistorics))
                .data(nvl(albcap.albdat(), LocalDate.now()))
                .magatzem(nvl(albcap.magcod(), ""))
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .informacioMagatzem(informacioMagatzem)
                .dataCreacio(nvl(albcap.diareg(), LocalDate.now()))
                .usuariCreacio(nvl(albcap.usuari(), "[MIGRACIO]"))
                .isTancat(isTrue(albcap.albtancat()))
                .isFacturat(isTrue(albcap.albfac1()))
                .isFacturacioAutomatica(isTrue(albcap.factauto()))
                .isEnviatEmail(isTrue(albcap.albfax()))
                .isNormalitzats(isTrue(albcap.albnor()))
                .isCalPagarPorts(isTrue(albcap.pagatports()))
                .isNoValorat(isTrue(albcap.novalorat()))
                .isEnviatHisenda(isTrue(albcap.envhis()))
                .isUrgent(isTrue(albcap.albexpres()));

        // Camps opcionals
        if (albcap.clicod() != null && !albcap.clicod().isBlank()) {
            albaraBuilder.client(albcap.clicod());
        }
        adresaBroker.ifPresent(albaraBuilder::adresaBroker);
        adresaFacturaProforma.ifPresent(albaraBuilder::adresaFacturaProforma);
        informacioTraspas.ifPresent(albaraBuilder::informacioTraspas);
        informacioEdi.ifPresent(albaraBuilder::informacioEdi);
        costLogistic.ifPresent(albaraBuilder::costLogistic);
        costTransport.ifPresent(albaraBuilder::costTransport);
        costEnviamentExpress.ifPresent(albaraBuilder::costEnviamentExpress);
        if (albcap.observ() != null && !albcap.observ().isBlank()) {
            albaraBuilder.observacionsImpressio(albcap.observ());
        }
        if (albcap.notes() != null && !albcap.notes().isBlank()) {
            albaraBuilder.observacionsInternes(albcap.notes());
        }
        if (albcap.notaprof() != null && !albcap.notaprof().isBlank()) {
            albaraBuilder.observacionsProforma(albcap.notaprof());
        }
        if (albcap.raoinci() != null && !albcap.raoinci().isBlank()) {
            albaraBuilder.incidenciaTransport(albcap.raoinci());
        }
        if (albcap.albpro() != null && !albcap.albpro().isBlank()) {
            albaraBuilder.numeroProveidor(albcap.albpro());
        }
        if (albcap.albesp() != null && !albcap.albesp().isBlank()) {
            albaraBuilder.numeroAlbaraEspecial(albcap.albesp());
        }
        if (albcap.facttrans() != null && !albcap.facttrans().isBlank()) {
            albaraBuilder.numeroFacturaTransport(albcap.facttrans());
        }
        if (albcap.numbox() != null && albcap.numbox() > 0) {
            albaraBuilder.numeroCaixes(albcap.numbox());
        }
        if (albcap.altbox() != null && !albcap.altbox().isBlank()) {
            albaraBuilder.alsadaCaixes(albcap.altbox());
        }
        if (albcap.costmoq() != null && albcap.costmoq().compareTo(BigDecimal.ZERO) != 0) {
            albaraBuilder.costMoq(albcap.costmoq());
        }
        if (albcap.reftransp() != null && !albcap.reftransp().isBlank()) {
            albaraBuilder.referenciaTransport(albcap.reftransp());
        }

        return albaraBuilder.build();
    }

    private InformacioEnviament parseInformacioEnviament(String albenv, String zontra, String desti, String albtra) {
        // Format albenv: FFIIIDDDD on FF=forma enviament, III=incoterm, DDDD=destí
        // Però el destí alternatiu està en el camp 'desti' separat
        var transpAdvantage = nvl(albtra, "");
        Optional<String> transportista = (transpAdvantage.isBlank() || transpAdvantage.equals("000") || transpAdvantage.equals("0000"))
                ? Optional.empty()
                : Optional.of(transpAdvantage);
        if (albenv == null || albenv.length() < 5) {
            return InformacioEnviamentImpl.builder()
                    .formaEnviament(FormaEnviament.DESCONEGUT)
                    .incoterm(Incoterm.EXW)
                    .desti("")
                    .transportista(transportista)
                    .build();
        }

        String formaEnviamentCodi = albenv.substring(0, 2);
        String incotermStr = albenv.substring(2, 5);
        String destiFromEnv = albenv.length() > 5 ? albenv.substring(5) : "";

        FormaEnviament formaEnviament;
        try {
            formaEnviament = FormaEnviament.getByCodi(formaEnviamentCodi);
        } catch (Exception e) {
            formaEnviament = FormaEnviament.DESCONEGUT;
        }

        Incoterm incoterm;
        try {
            incoterm = Incoterm.valueOf(incotermStr.trim());
        } catch (Exception e) {
            incoterm = Incoterm.EXW;
        }

        var builder = InformacioEnviamentImpl.builder()
                .formaEnviament(formaEnviament)
                .incoterm(incoterm)
                .desti(destiFromEnv != null ? destiFromEnv.trim() : "")
                .transportista(nvl(albtra, ""));

        if (zontra != null && !zontra.isBlank()) {
            builder.zonaTransport(zontra);
        }

        return builder.build();
    }

    private TipusAlbara tipusAlbara (Albcap alb, List<AlbaraHistoric> albaraHistorics) {
        // Tots els moviments històrics que coincideixin amb el número d'albarà i empresa
        var movimentsHistoric = albaraHistorics.stream()
                .filter(h -> h.empcod().equals(alb.empcod()) && compareAlbnum(alb.albnum, h.albnum))
                .toList();

        // Moviment principal
        var optMovimentPrincipal = movimentsHistoric.stream()
                .filter(h -> h.magcod.equals(alb.magcod))
                .findFirst();

        if (optMovimentPrincipal.isPresent()) {
            var movimentPrincipal = optMovimentPrincipal.get();
            if (movimentPrincipal.histip.equals("2")) {
                return tipus2(movimentPrincipal);
            } else if (movimentPrincipal.histip.equals("4")) {
                return tipus4(movimentPrincipal, movimentsHistoric);
            } else if (movimentPrincipal.histip.equals("7")) {
                return TipusAlbara.TRASPAS_EMPRESA;
            }
        }

        return TipusAlbara.DESCONEGUT;

    }

    /**
     * En cas que el moviment principal sigui de tipus 2 es pot determinar si el tipus es de client (surt de magatzem AMES) o de consum (surt de magatzem Plataforma)
     */
    private TipusAlbara tipus2(AlbaraHistoric albaraHistoric) {
        return magatzemsAmes.stream().anyMatch(m -> m.equals(albaraHistoric.magcod)) ? TipusAlbara.CLIENT : TipusAlbara.CONSUM;
    }

    private TipusAlbara tipus4(AlbaraHistoric albaraHistoric, List<AlbaraHistoric> albaraHistoricsRelacionat) {
        var movimentRelacionatEmpresa = albaraHistoricsRelacionat.stream()
                .filter(h -> h.histip.equals("7") && h.magcod.equals(albaraHistoric.trasmag))
                .findAny();
        if (movimentRelacionatEmpresa.isPresent()) {
            return TipusAlbara.TRASPAS_MAGATZEM_EMPRESA;
        } else {
            // En cas que el moviment sigui de tipus 4 i no hi hagi cap moviment relacionat de tipus 7 amb el magatzem de destí, es considera que és un traspàs entre magatzems (sense empresa de destí)
            // I cal determinar si és un traspàs entre magatzems d'AMES o un traspàs entre magatzem d'AMES i magatzem de la plataforma
            return magatzemsAmes.stream().anyMatch(m -> m.equals(albaraHistoric.trasmag)) ? TipusAlbara.TRASPAS_MAGATZEM : TipusAlbara.TRASPAS_PLATAFORMA;
        }
    }

    private boolean compareAlbnum (Long albNumLong, String albNumStr) {
        if (albNumStr == null || albNumStr.isBlank()) {
            return false;
        }
        try {
            Long albNumStrLong = Long.parseLong(albNumStr);
            return albNumLong.equals(albNumStrLong);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isTrue(String value) {
        return "S".equalsIgnoreCase(value) || "T".equalsIgnoreCase(value) || "1".equals(value);
    }

    private <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public record Albcap(
            Long albnum,
            String empcod,
            String clicod,
            LocalDate albdat,
            String magcod,
            String albnom,
            String albdre,
            String albpob,
            String codpos,
            String albpai,
            String albenv,
            String zontra,
            String desti,
            String albtra,
            String nomb,
            String dreb,
            String pobb,
            String posb,
            String paib,
            String nomf,
            String dref,
            String pobf,
            String posf,
            String paif,
            String numalbmix,
            Long pesbru,
            Long bultos,
            String albserv,
            String albenprep,
            String albenserv,
            String entregat,
            LocalDate dataprev,
            LocalDate dataenv,
            Long numpal1,
            String altpalet1,
            Long numpal2,
            String altpalet2,
            Long numpal3,
            String altpalet3,
            String matricula,
            String avinum,
            String avienviat,
            String empcodr,
            String magcodr,
            String trasabon,
            String punto,
            String aclgat,
            String csg3921,
            String mrnnum,
            LocalDate mrndata,
            String mrntype,
            BigDecimal implogi,
            String textlogi,
            BigDecimal costra,
            String raocostra,
            BigDecimal costexpres,
            String raoexpres,
            String observ,
            String notes,
            String notaprof,
            String raoinci,
            LocalDate diareg,
            String usuari,
            String albpro,
            String albesp,
            String albtancat,
            String albfac1,
            String factauto,
            String albfax,
            String albnor,
            String pagatports,
            String novalorat,
            String envhis,
            String facttrans,
            Long numbox,
            String altbox,
            BigDecimal costmoq,
            String reftransp,
            String albexpres
    ) {}

    public record AlbaraHistoric (
            String empcod,
            String albnum,
            String magcod,
            String trasmag,
            String histip
    ) {}

}

