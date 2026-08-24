package ames.comercial.cache.clients;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirClientsCache {

    public List<RegistreCacheClient> executar() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                select cli6.clicod, cli6.clinom, cli6.cliali, cli6.empcod, cliflg, cli6.clidiv, usuresp, cli6.datblo, cli6.climor, cli6.clidel,
                    SUBSTRING(clienv, 1, 2) "formaEnviament",
                    SUBSTRING(clienv, 3, 3) "incoterm",
                    SUBSTRING(clienv, 6, 4) "desti",
                    zontra, z.deszon,
                    diessor, diesres,
                    tra.tracod, tra.descrip,
                    adrenv.clinom "envdestinatari", adrenv.clidre "envadre", adrenv.clipob "envpob", adrenv.clipos "envpos", adrenv.clipai "envpais",
                    cli4.clinom as "nomFiscal", cli4.clidre, cli4.clipob, cli4.clipos, cli4.clipai, tpaisos.nom as nomPais,
                    cli8.clipro,
                    cli6.obsmor, cli6.notes, cli6.notalb
                from comundb.cli6
                INNER JOIN comundb.cli4 ON SUBSTRING(cli6.clicod,1,4) = cli4.clicod4
                INNER JOIN comundb.tpaisos ON clipai = codpais
                LEFT JOIN comundb.cli8 ON cli8.clicod = cli6.clicod AND cli8.empcod = cli6.empcod
                LEFT JOIN comundb.tra ON cli6.clitra = tra.tracod
                LEFT JOIN comundb.cliadr adrenv ON adrenv.clicod = cli6.clicod AND adrenv.clitip = 'EN'
                LEFT JOIN zontra z ON z.codzon = zontra AND z.codpai = codpais AND z.codtra = tra.tracod;
						""");
        AdvantageDao.ResultSetAction<List<RegistreCacheClient>> rsAction = rs -> {
            var result = new ArrayList<RegistreCacheClient>();
            while (rs.next()) {
                result.add(RegistreCacheClientImpl.builder()
                        .clicod(rs.getString("clicod"))
                        .nom(rs.getString("clinom"))
                        .alias(rs.getString("cliali"))
                        .estat(rs.getString("cliflg"))
                        .codiProveidor(readOptionalString(rs, "clipro").orElse(""))
                        .empresa(rs.getString("empcod"))
                        .dataBloqueig(readOptionalDate(rs, "datblo"))
                        .usuariBloqueig(readOptionalString(rs, "climor"))
                        .divisa(Optional.ofNullable(rs.getString("clidiv"))
                                .filter(s -> !s.isBlank())
                                .map(Divisa::getBySymbol))
                        .responsableLogistica(rs.getString("usuresp"))
                        .codiDelegat(readOptionalString(rs, "clidel"))
                        .pais(rs.getString("clipai"))
                        .nomPais(rs.getString("nomPais"))
                        .formaEnviament(Optional.ofNullable(rs.getString("formaEnviament"))
                                .filter(s -> !s.isBlank())
                                .map(FormaEnviament::getByCodi))
                        .incoterm(Optional.ofNullable(rs.getString("incoterm"))
                                .filter(s -> !s.isBlank())
                                .map(Incoterm::valueOf))
                        .desti(rs.getString("desti"))
                        .codiTransportista(readOptionalString(rs, "tracod").orElse(""))
                        .descTransportista(readOptionalString(rs,"descrip").orElse(""))
                        .zonaTransport(zonaTransport(rs))
                        .diesSortida(rs.getString("diessor"))
                        .diesTransitClient(rs.getInt("diesres"))
                        .adresa(adresa(rs))
                        .adresaEnviament(adresaEnviament(rs))
                        .notesClient(readOptionalString(rs, "notes").orElse(""))
                        .notesLogistica(readOptionalString(rs, "notalb").orElse(""))
                        .notesMorositat(readOptionalString(rs, "obsmor").orElse(""))
                        .build());
            }
            return result;
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

    private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
        var s = rs.getString(column);
        return Strings.isNullOrEmpty(s) ? Optional.empty() : Optional.of(s);
    }

    private Optional<LocalDate> readOptionalDate(ResultSet rs, String colum) throws  SQLException {
        var d = rs.getDate(colum);
        return rs.wasNull() ? Optional.empty() : Optional.of(d.toLocalDate());
    }

    @JsonDeserialize(builder = RegistreCacheClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RegistreCacheClient {
        String clicod();
        String nom();
        String alias();
        String estat();
        String codiProveidor();
        String empresa();
        Optional<LocalDate> dataBloqueig();
        Optional<String> usuariBloqueig();
        Optional<Divisa> divisa();
        String pais();
        String nomPais();
        String responsableLogistica();
        Optional<String> codiDelegat();
        Optional<FormaEnviament> formaEnviament();
        Optional<Incoterm> incoterm();
        String desti();
        String codiTransportista();
        String descTransportista();
        Optional<SimpleItem> zonaTransport();
        String diesSortida();
        int diesTransitClient();
        Adresa adresa();
        Optional<Adresa> adresaEnviament();
        String notesClient();
        String notesLogistica();
        String notesMorositat();
    }

}
