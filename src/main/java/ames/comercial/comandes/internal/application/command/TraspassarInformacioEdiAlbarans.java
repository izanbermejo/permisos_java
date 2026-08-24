package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.editoalbarans.EdiToAlbaransRepository;
import ames.comercial.comandes.internal.infraestructure.editoalbarans.EdiToAlbaransRepository.EdiToAlbaransRecord;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.edi.beans.*;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class TraspassarInformacioEdiAlbarans {

    @Autowired ComandaRepository comandaRepo;
    @Autowired LiniaComandaRepository liniaComandaRepo;
    @Autowired EdiToAlbaransRepository ediToAlbaransRepo;

    @Transactional
    public void executar (EdiToAlbaransRecord e) {
        // Obtenció de l'article-client
        var articleClient = new ObtenirArticleClientAds().query(e.articleClient()).orElseThrow();
        // Obtenció de les línies de comanda
        var liniesComanda = obtenirLiniesComanda(e.articleClient(), e.comandaClient(), articleClient.empresa());
        // Obtenció les línies de la referència en el missatge
        var liniesReferencia = e.missatge().getLineas().stream()
                .filter(l -> l.getLA().getIdArticuloComprador().equals(articleClient.referencia()))
                .toList();
        // Boolean per saber si el missatge es de tipus DELJIT
        var isMissatgeDeljit = "DELJIT".equals(e.missatge().getCA().getDocumento());
        // Boolean per saber si el client és DELJIT (té un registre amb docum DELJIT a la taula flagsedi)
        var isClientDeljit = isClientDeljit(e.articleClient().clicod());
        // Per cada línia pot haver varies dates
        var listInsertDadesEdi = new ArrayList<DadesEDI>();
        for (var l : liniesReferencia) {
            for (var d : l.getDetalles()) {
                var dataSolicitada = dataSolicitada(d.getDA());
                var optLinia = findLiniaComanda(liniesComanda, dataSolicitada);
                // En cas que es trobi la línia
                optLinia.ifPresent(liniaComanda -> {
                    // Si el client és DELJIT només cal afegir dades EDI quan el missatge sigui DELJIT.
                    // En cas que el client no sigui DELJIT sempre s'afegiran les dades EDI
                    // Això serveix per evitar que els clients que tenen DELJIT i DELFOR
                    // es sobreescrigui l'informació de l'EDI amb els DELFOR
                    if (!isClientDeljit || isMissatgeDeljit) {
                        listInsertDadesEdi.add(buildDadesEdi(liniaComanda, e.missatge(), l, d));
                    }
                });
            }
        }
        // Marcar com a processat la tasca
        ediToAlbaransRepo.marcaProcessat(e);
        // Statements a executar
        List<PreparedStatementProvider> statements = new ArrayList<PreparedStatementProvider>();
        statements.add(updateHcomLin(listInsertDadesEdi));
        statements.add(deleteDadesEdi(listInsertDadesEdi));
        statements.add(insertDadesEdi(listInsertDadesEdi));
        new AdvantageDao().executeUpdate(statements);
    }

    private Optional<LiniaComanda> findLiniaComanda(List<LiniaComanda> linies, LocalDate data) {
        return linies.stream()
                .filter(l -> l.dataSolicitada().equals(data))
                .findFirst();
    }

    private List<LiniaComanda> obtenirLiniesComanda(KeyArticleClient articleClient, String comandaClient, Empresa empresa) {
        var optComanda = comandaRepo.findByComandaClient(articleClient.clicod(), comandaClient, empresa);
        if (optComanda.isEmpty())
            return List.of();
        var comanda = optComanda.get();
        var liniesComanda = liniaComandaRepo.findByComanda(comanda.codi());
        return liniesComanda.stream()
                .filter(l -> l.articleClient().equals(articleClient))
                .toList();
    }

    private LocalDate dataSolicitada(DA da) {
        var dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (StringUtils.hasText(da.getFechaFinal()))
            return LocalDate.parse(da.getFechaFinal(), dateFormatter);
        return LocalDate.parse(da.getFechaInicial(), dateFormatter);
    }

    private boolean isClientDeljit(String codiClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("""
							select f.codcli
							from flagsedi f
							left join dummy d ON f.codcli = d.str1
							where f.docum = 'DELJ' and f.codcli = ?
							""");
            statement.setString(1, codiClient);
            return statement;
        };
        return new AdvantageDao().query(prep, ResultSet::next);
    }

    private PreparedStatementProviderBatch insertDadesEdi(List<DadesEDI> dades) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return conn -> new MultipleInsertStatement<DadesEDI>(conn, "dadesedi")
                .add("COMCOD", DadesEDI::COMCOD)
                .add("HCOMLIN", DadesEDI::HCOMLIN)
                .add("CI_IDCOMPR", DadesEDI::CI_IDCOMPR)
                .add("CI_IDEXPED", DadesEDI::CI_IDEXPED)
                .add("CI_IDPROVE", DadesEDI::CI_IDPROVE)
                .add("CI_NUMCUEN", DadesEDI::CI_NUMCUEN)
                .add("DA_DATAINI", DadesEDI::DA_DATAINI)
                .add("DA_HORAINI", DadesEDI::DA_HORAINI)
                .add("DA_NUMKANB", DadesEDI::DA_NUMKANB)
                .add("DA_NUMRAN", DadesEDI::DA_NUMRAN)
                .add("LA_CODIALM", DadesEDI::LA_CODIALM)
                .add("LA_CODICAR", DadesEDI::LA_CODICAR)
                .add("LA_DATALIM", DadesEDI::LA_DATALIM)
                .add("LA_HORLIM", DadesEDI::LA_HORLIM)
                .add("LA_LUGAREN", DadesEDI::LA_LUGAREN)
                .add("LA_LUGARDE", DadesEDI::LA_LUGARDE)
                .add("LC_CODICON", DadesEDI::LC_CODICON)
                .add("LE_CANTPIE", DadesEDI::LE_CANTPIE)
                .add("LE_REFBULT", DadesEDI::LE_REFBULT)
                .add("LG_NUMCONT", DadesEDI::LG_NUMCONT)
                .add("LH_NUMLAB", DadesEDI::LH_NUMLAB)
                .add("LI_CINGENI", DadesEDI::LI_CINGENI)
                .add("LI_IDARTIC", DadesEDI::LI_IDARTIC)
                .add("LI_NUMRUTA", DadesEDI::LI_NUMRUTA)
                .add("LS_CODISOL", DadesEDI::LS_CODISOL)
                .add("CLICOD", DadesEDI::CLICOD)
                .add("DATAREG", d -> d.DATAREG().format(formatter))
                .add("DA_DATAFIN", d -> dataDaFin(d.DA_DATAFIN))
                .build(dades);
    }

    private static String dataDaFin(String value) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (ObjectUtils.isEmpty(value))
            return "";
        return LocalDate.parse(value, inputFormatter).format(formatter);
    }

    public static PreparedStatementProviderBatch updateHcomLin(List<DadesEDI> dadesEdi) {
        return conn -> {
            var statement = conn.prepareStatement("UPDATE hcomlin SET numran = ?, CODIRUTA = ?, PUNTO = ? WHERE comcod = ? AND hcomlin = ? AND actual = 'S'");
            for (DadesEDI d : dadesEdi) {
                int param = 1;
                statement.setString(param++, d.DA_NUMRAN);
                statement.setString(param++, d.LI_NUMRUTA);
                statement.setString(param++, d.LA_LUGAREN);
                statement.setString(param++, d.COMCOD);
                statement.setString(param++, d.HCOMLIN);
                statement.addBatch();
            }
            return statement;
        };
    }

    public static PreparedStatementProviderBatch deleteDadesEdi(List<DadesEDI> dadesEdi) {
        return conn -> {
            var statement = conn.prepareStatement("DELETE FROM dadesedi WHERE comcod = ? AND HCOMLIN = ?");
            for (DadesEDI d : dadesEdi) {
                statement.setString(1, d.COMCOD);
                statement.setString(2, d.HCOMLIN);
                statement.addBatch();
            }
            return statement;
        };
    }

    private DadesEDI buildDadesEdi(LiniaComanda liniaComanda, ComandaMissatgeEDI missatge, Linea linia, Detalle detall) {
        var optCi = Optional.ofNullable(missatge.getCI());
        var optDa = Optional.ofNullable(detall.getDA());
        var optLa = Optional.ofNullable(linia.getLA());
        var optLc = Optional.ofNullable(linia.getLC());
        var optLg = Optional.ofNullable(linia.getLG());
        var optLh = Optional.ofNullable(linia.getLH());
        var optLi = Optional.ofNullable(linia.getLI());
        var optLs = Optional.ofNullable(linia.getLS());
        var optLe = Optional.ofNullable(ObjectUtils.isEmpty(linia.getEmpaquetamientos()) ? null : linia.getEmpaquetamientos().get(0));
        return new DadesEDI(
                liniaComanda.codiFormat(),
                liniaComanda.numeroFormat(),
                optCi.map(CI::getIdComprador).orElse(""),
                optCi.map(CI::getIdExpedidor).orElse(""),
                optCi.map(CI::getIdProveedor).orElse(""),
                optCi.map(CI::getNumCuentaInternaProveedor).orElse(""),
                optDa.map(DA::getFechaInicial).orElse(""),
                optDa.map(DA::getHoraInicial).orElse(""),
                optDa.map(DA::getNumeroTarjetaKanban).orElse(""),
                optDa.map(DA::getNumeroRAN).orElse(""),
                optLa.map(LA::getCodigoAlmacen).orElse(""),
                optLa.map(LA::getCodigoCaracteristicaItem).orElse(""),
                optLa.map(LA::getFechaLimiteEntrega).orElse(""),
                optLa.map(LA::getHoraLimiteEntrega).orElse(""),
                optLa.map(LA::getLugarEntrega).orElse(""),
                optLa.map(LA::getLugarDestinoFinal).orElse(""),
                optLc.map(LC::getCodigoConsignatario).orElse(""),
                optLe.map(LE::getPiezasPorEmbalaje).orElse(""),
                optLe.map(LE::getReferenciaEmbalaje).orElse(""),
                optLg.map(LG::getNumeroContrato).orElse(""),
                optLh.map(LH::getNumeroLab).orElse(""),
                optLi.map(LI::getCambioIngenieria).orElse(""),
                optLi.map(LI::getNumeroSufijoRuta).orElse(""),
                optLi.map(LI::getNumeroRuta).orElse(""),
                optLs.map(LS::getCodigoSolicitante).orElse(""),
                liniaComanda.articleClient().clicod(),
                RequestThread.dateLocal(),
                optDa.map(DA::getFechaFinal).orElse(""));

    }

    private record DadesEDI (String COMCOD, String HCOMLIN,
                             String CI_IDCOMPR, String CI_IDEXPED, String CI_IDPROVE, String CI_NUMCUEN,
                             String DA_DATAINI, String DA_HORAINI, String DA_NUMKANB, String DA_NUMRAN,
                             String LA_CODIALM, String LA_CODICAR, String LA_DATALIM, String LA_HORLIM, String LA_LUGAREN, String LA_LUGARDE,
                             String LC_CODICON,
                             String LE_CANTPIE, String LE_REFBULT,
                             String LG_NUMCONT,
                             String LH_NUMLAB,
                             String	LI_CINGENI, String	LI_IDARTIC, String	LI_NUMRUTA,
                             String LS_CODISOL,
                             String	CLICOD, LocalDate DATAREG, String DA_DATAFIN) {}
}
