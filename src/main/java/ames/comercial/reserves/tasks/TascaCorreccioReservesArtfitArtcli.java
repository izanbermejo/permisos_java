package ames.comercial.reserves.tasks;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.shared.Empresa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Tasca per corregir cada nit les quantitats reservades d'artfit i artcli.
 * Aquesta tasca s'ha d'eliminar quan es posi en marxa el mòdul d'albarans ja que l'stock
 * es controlarà a través del nou mòdul d'inventari
 */
@Component
@ConditionalOnProperty(name = "tasca.recalculreserves", havingValue = "true", matchIfMissing = false)
public class TascaCorreccioReservesArtfitArtcli {

    private static final Logger logger = LoggerFactory.getLogger(TascaCorreccioReservesArtfitArtcli.class);

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Transactional
    @Scheduled(cron="0 0 3 * * *")
    public void executar() {
        logger.info("INICI TASCA CORRECCIÓ RESSERVES ARTFIT ARTCLI");

        // Posar a 0 les reserves d'artfit
        new AdvantageDao().executeUpdate(connection -> connection.prepareStatement("""
                UPDATE artfit
                SET comqres = 0
                """));

        // Obtenció dels registres a corregir
        var registres = jdbcAmes.query("""
                SELECT artint, c.empresa, SUM(quantitat_reservada)
                FROM comandes.linia_comanda lc
                LEFT JOIN comandes.comanda c ON c.codi = lc.comanda
                WHERE actual
                	AND clicod = '000000'
                	AND NOT lc.servida
                GROUP BY artint, c.empresa
                HAVING SUM(quantitat_reservada)>0;
                """, (rs, rowNum) -> new RegActualitzar(rs.getString("empresa"), rs.getString("artint"), rs.getInt("sum")));

        AdvantageDao adsDao = new AdvantageDao();
        List<PreparedStatementProvider> statements = new ArrayList<PreparedStatementProvider>();
        statements.add(batchCorreccioArtfit(registres));
        adsDao.executeUpdate(statements);

        // Actualitzar l'artcli amb les reserves correctes
        new AdvantageDao().executeUpdate(connection -> connection.prepareStatement("""
                UPDATE comundb.artcli
                SET aclqre = 0
                """));
        new AdvantageDao().executeUpdate(connection -> connection.prepareStatement("""
                UPDATE comundb.artcli
                SET aclqre = COALESCE((
                    SELECT SUM(f.comqres)
                    FROM artfit f
                    WHERE f.artint = artcli.artint
                ), 0)
                WHERE artcli.clicod = '000000';
                """));

        logger.info("FI TASCA CORRECCIÓ RESSERVES ARTFIT ARTCLI");
    }

    private record RegActualitzar (String empcod, String artint, int reserva) {}

    private PreparedStatementProviderBatch batchCorreccioArtfit(List<RegActualitzar> registres) {
        return connection -> {
            var prep = connection.prepareStatement("""
                    UPDATE artfit
                    SET comqres = ?
                    WHERE clicod = '000000' AND artint = ? AND empcod = ? AND magcod = ?;
                    """);
            for (RegActualitzar r : registres) {
                var empresa = Empresa.getByClau(r.empcod);
                prep.setInt(1, r.reserva);
                prep.setString(2, r.artint);
                prep.setString(3, empresa.clau());
                prep.setString(4, empresa.magatzem());
                prep.addBatch();
            }
            return prep;
        };
    }

}
