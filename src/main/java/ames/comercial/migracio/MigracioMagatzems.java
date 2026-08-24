package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.magatzem.internal.domain.Magatzem;
import ames.comercial.magatzem.internal.domain.MagatzemImpl;
import ames.comercial.magatzem.internal.domain.TipusMagatzem;
import ames.comercial.magatzem.internal.infraestructure.MagatzemRepository;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.AdresaImpl;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Bolcat inicial de dades des de l'Advantage cap a PostgreSQL: {@code comundb.mag} →
 * {@code com_magatzem.magatzem}.
 * <p>
 * Segueix el mateix patró que {@link MigracioMoviments}: llegeix l'Advantage directament amb
 * {@link AdvantageDao} (sense passar pels ports de l'app) perquè aquesta migració sigui independent.
 * La taula {@code com_magatzem.magatzems_intermitjos} es migra manualment, no aquí.
 * TODO: Eliminar després de la migració.
 */
@Component
public class MigracioMagatzems {

    /**
     * Magatzems marcats com a plataforma ({@code mag.tipus = 'P'}) a l'Advantage que en realitat no ho
     * són: la mercaderia només hi està de pas, no s'hi fan consums i no s'hi manté pendent de consumir.
     * Es migren com a {@link TipusMagatzem#TRANSIT}. No es pot deduir de l'Advantage (no hi ha cap camp
     * que ho distingeixi), per això la llista és manual.
     * <p>
     * És la mateixa llista que {@link MigracioCapsalera} tracta com a no-plataforma per tipificar els
     * albarans històrics (les seves sortides són albarans de client i no consums, i els traspassos que hi
     * entren són traspassos de magatzem i no de plataforma), i és per això que la comparteixen.
     */
    public static final List<String> MAGATZEMS_TRANSIT = List.of(
            "0004",
            "0025",
            "0041",
            "0042",
            "0043",
            "0046",
            "0075",
            "0076",
            "0081",
            "0083",
            "0100",
            "9001",
            "9999");

    @Autowired MagatzemRepository magatzemRepository;

    public void migracio() {
        magatzemRepository.deleteAll();
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                select *
                FROM comundb.mag m
                LEFT JOIN dummy d ON m.magcod = d.str1
                """);
        AdvantageDao.ResultSetAction<List<Magatzem>> rsAction = rs -> {
            var result = new ArrayList<Magatzem>();
            while (rs.next()) {
                result.add(mapMagatzem(rs));
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant migració magatzems (comundb.mag)");
        var magatzems = new AdvantageDao().query(prep, rsAction);
        magatzems.forEach(magatzemRepository::save);
        System.out.println(LocalDateTime.now() + " Total magatzems migrats: " + magatzems.size());
    }

    private Magatzem mapMagatzem(ResultSet rs) throws SQLException {
        return MagatzemImpl.builder()
                .codi(rs.getString("magcod"))
                .tipus(tipusMagatzem(rs.getString("magcod"), rs.getString("tipus")))
                .descripcio(rs.getString("descrip"))
                .diesTransport(rs.getLong("diestra"))
                .informacioEnviament(mapInformacioEnviament(rs))
                .adresa(AdresaImpl.builder()
                        .destinatari(rs.getString("descrip"))
                        .adresa(rs.getString("adreca"))
                        .poblacio(rs.getString("poblacio"))
                        .codiPostal(rs.getString("codpos"))
                        .pais(rs.getString("pais"))
                        .build())
                .isFacturable(rs.getBoolean("facturable"))
                .isActiu(true)
                .isSii(isSii(rs))
                .build();
    }

    /**
     * Marca de declaració al SII del magatzem. A l'Advantage és el camp que el Delphi llegia com a
     * {@code tMagSII} i comparava amb {@code 'S'} (veure {@code EsMagSII} de {@code mantalb.pas}).
     */
    private boolean isSii(ResultSet rs) throws SQLException {
        return MapperUtils.readOptionalString(rs, "sii")
                .map(valor -> "S".equalsIgnoreCase(valor.trim()))
                .orElse(false);
    }

    /**
     * Els magatzems de {@link #MAGATZEMS_TRANSIT} es migren com a {@code TRANSIT} encara que a
     * l'Advantage estiguin marcats com a plataforma; la resta, segons el camp {@code mag.tipus}.
     */
    private TipusMagatzem tipusMagatzem(String codi, String tipusAdvantage) {
        if (MAGATZEMS_TRANSIT.contains(codi)) {
            return TipusMagatzem.TRANSIT;
        }
        return "P".equals(tipusAdvantage) ? TipusMagatzem.PLATAFORMA : TipusMagatzem.AMES;
    }

    private Optional<InformacioEnviament> mapInformacioEnviament(ResultSet rs) throws SQLException {
        String traenv = rs.getString("traenv");
        if (traenv == null || traenv.isBlank() || traenv.length() < 5)
            return Optional.empty();
        return Optional.of(InformacioEnviamentImpl.builder()
                .formaEnviament(FormaEnviament.getByCodi(traenv.substring(0, 2)))
                .incoterm(Incoterm.valueOf(traenv.substring(2, 5)))
                .desti(traenv.substring(5))
                .transportista(MapperUtils.readOptionalString(rs, "tracod").orElse(""))
                .zonaTransport(MapperUtils.readOptionalString(rs, "zontra"))
                .build());
    }

}
