package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class ObtenirClientAds implements IObtenirClientAds {

    @Override
    public Optional<ClientAds> get(String clicod) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select cli6.clicod, cli6.clinom, empcod, cli4.clinif, cli6.clidiv, cli4.clipai, tpaisos.nom as nomPais, cli6.cliidi, cli6.clibra, cli6.flgcost,
						    cli6.e_mail,
						    SUBSTRING(clienv, 1, 2) "formaEnviament",
						    SUBSTRING(clienv, 3, 3) "incoterm",
                            SUBSTRING(clienv, 6, 4) "desti",
                            zontra, z.deszon,
                            diessor, diesres,
						    clidtonc, clidtonf, dtenorfe,
						    clidel='0090' as clientWeb, -- Els clients webs tenen marcat el delegat 0090
							tra.tracod, tra.descrip, cli6.clienv,
							adrenv.clinom "envdestinatari", adrenv.clidre "envadre", adrenv.clipob "envpob", adrenv.clipos "envpos", adrenv.clipai "envpais",
							cli4.clinom as "nomFiscal", cli4.clidre, cli4.clipob, cli4.clipos, cli4.clipai,
							cli6.obsmor, cli6.notes, cli6.notalb, cli6.alertcom, cli6.cliflg, cli6.bloimp, cli6.factri,
							cli6.clilalb, cli6.clilcom, cli6.autofac
						from comundb.cli6
						INNER JOIN comundb.cli4 ON SUBSTRING(cli6.clicod,1,4) = cli4.clicod4
						INNER JOIN comundb.tpaisos ON clipai = codpais
						LEFT JOIN comundb.tra ON cli6.clitra = tra.tracod
						LEFT JOIN comundb.cliadr adrenv ON adrenv.clicod = cli6.clicod AND adrenv.clitip = 'EN'
						LEFT JOIN zontra z ON z.codzon = zontra AND z.codpai = codpais AND z.codtra = tra.tracod
						WHERE cli6.clicod = ?
					""");
            statement.setString(1, clicod);
            return statement;
        };
        ResultSetAction<Optional<ClientAds>> rsAction = rs -> {
            if (rs.next()) {
                return Optional.of(
                        ClientAdsImpl.builder()
                                .clicod(rs.getString("clicod"))
                                .nom(rs.getString("clinom"))
                                .empresa(rs.getString("empcod"))
                                .nif(rs.getString("clinif"))
                                .divisa(Divisa.getBySymbol(rs.getString("clidiv")))
                                .pais(rs.getString("clipai"))
                                .nomPais(rs.getString("nomPais"))
                                .idioma(idioma(rs))
                                .brancaProfesional(rs.getString("clibra"))
                                .isAplicaCostTransport(readOptionalString(rs, "flgcost").map(flgcost -> flgcost.equals("S")).orElse(false))
                                .email(rs.getString("e_mail"))
                                .formaEnviament(FormaEnviament.getByCodi(rs.getString("formaEnviament")))
                                .incoterm(Incoterm.valueOf(rs.getString("incoterm")))
                                .desti(rs.getString("desti"))
                                .codiTransportista(rs.getString("tracod"))
                                .zonaTransport(zonaTransport(rs))
                                .diesSortida(rs.getString("diessor"))
                                .diesTransitClient(rs.getInt("diesres"))
                                .dtoCoixBronze(MapperUtils.readOptionalBigDecimal(rs, "clidtonc").orElse(BigDecimal.ZERO))
                                .dtoFiltres(MapperUtils.readOptionalBigDecimal(rs, "clidtonf").orElse(BigDecimal.ZERO))
                                .dtoCoixFerro(MapperUtils.readOptionalBigDecimal(rs, "dtenorfe").orElse(BigDecimal.ZERO))
                                .adresa(adresa(rs))
                                .adresaEnviament(adresaEnviament(rs))
                                .notesClient(readOptionalString(rs, "notes").orElse(""))
                                .notesLogistica(readOptionalString(rs, "notalb").orElse(""))
                                .notesMorositat(readOptionalString(rs, "obsmor").orElse(""))
                                .isClientWeb(rs.getBoolean("clientWeb"))
                                .comentariInternClient(readOptionalString(rs, "alertcom").orElse(""))
                                .estatClient(rs.getString("cliflg"))
                                .isImpagament(MapperUtils.readOptionalString(rs, "bloimp").map(bloimp -> bloimp.equals("S")).orElse(false))
                                .isClientProforma(MapperUtils.readOptionalString(rs, "factri").map(v -> v.equals("S")).orElse(false))
                                .isAlbaraPerPesa("S".equals(rs.getString("clilalb")))
                                .isAlbaraPerComanda("S".equals(rs.getString("clilcom")))
                                .isFacturacioAutomatica(MapperUtils.readOptionalString(rs, "autofac").map(v -> v.equals("S")).orElse(false))
                                .build());
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private Adresa adresa(ResultSet rs) throws SQLException {
        return AdresaImpl.builder()
                .destinatari(rs.getString("nomFiscal"))
                .adresa(rs.getString("clidre"))
                .poblacio(rs.getString("clipob"))
                .codiPostal(rs.getString("clipos"))
                .pais(rs.getString("clipai"))
                .build();
    }

    private Optional<Adresa> adresaEnviament(ResultSet rs) throws SQLException {
        String adr = rs.getString("envadre");
        if (rs.wasNull())
            return Optional.empty();
        return Optional.of(AdresaImpl.builder()
                .destinatari(rs.getString("envdestinatari"))
                .adresa(adr)
                .poblacio(rs.getString("envpob"))
                .codiPostal(rs.getString("envpos"))
                .pais(rs.getString("envpais"))
                .build());
    }

    private Optional<SimpleItem> zonaTransport(ResultSet rs ) throws SQLException {
        String adr = rs.getString("zontra");
        if (rs.wasNull() || adr.isBlank())
            return Optional.empty();
        return Optional.of(SimpleItem.of(adr, readOptionalString(rs, "deszon").orElse("")));
    }

    private String idioma(ResultSet rs) throws SQLException {
        var optIdioma = readOptionalString(rs, "cliidi");
        // En cas que no estigui definit l'idioma al client es retorna l'anglès
        if (optIdioma.isEmpty())
            return "en";
        var codiIdioma = optIdioma.get();
        return switch (codiIdioma) {
            case "00" -> "es";
            case "01" -> "en";
            case "02" -> "fr";
            case "03" -> "de";
            case "04" -> "it";
            case "05" -> "ca";
            default -> "en";
        };
    }

    private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
        var s = rs.getString(column);
        return Strings.isNullOrEmpty(s) ? Optional.empty() : Optional.of(s);
    }

    @JsonDeserialize(builder = ClientAdsImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ClientAds {
        String clicod();
        String nom();
        String empresa();
        String nif();
        Divisa divisa();
        String pais();
        String nomPais();
        String idioma();
        String brancaProfesional();
        boolean isAplicaCostTransport();
        String email();
        FormaEnviament formaEnviament();
        Incoterm incoterm();
        String desti();
        String codiTransportista();
        Optional<SimpleItem> zonaTransport();
        String diesSortida();
        int diesTransitClient();
        BigDecimal dtoCoixBronze();
        BigDecimal dtoFiltres();
        BigDecimal dtoCoixFerro();
        Adresa adresa();
        Optional<Adresa> adresaEnviament();
        String notesClient();
        String notesLogistica();
        String notesMorositat();
        boolean isClientWeb();
        String comentariInternClient();
        String estatClient();
        boolean isImpagament();
        boolean isClientProforma();
        boolean isAlbaraPerPesa();
        boolean isAlbaraPerComanda();

        /** Client marcat com a autofacturable a Advantage (cli6.autofac = 'S'). */
        boolean isFacturacioAutomatica();

        @Derived
        default boolean isDistribuidor() {
            String[] brancasProfessionalsDistribudors = {"0800", "0801"};
            return Arrays.asList(brancasProfessionalsDistribudors).contains(brancaProfesional());
        }

        @Derived
        default boolean isClientFiltres() {
            return "1500".equals(brancaProfesional());
        }

        @Derived
        default boolean isSuministramentIndustrial() {
            return "1200".equals(brancaProfesional());
        }

        @Derived
        default boolean isNacional() {
            // Son nacionals si el codi de páis es menor que 0100 (provincies)
            return Integer.parseInt(pais()) <= 99;
        }

        @Derived
        default Adresa adresaEnviamentCalculada() {
            return adresaEnviament().orElse(adresa());
        }

    }

}
