package ames.comercial.reserves.tasks;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.calculadorareserves.request.CalculadoraReservesReqImpl;
import ames.comercial.calculadorareserves.service.CalculadoraReserves;
import ames.comercial.comandes.internal.domain.comanda.Servible;
import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.internal.service.ICalcularServible;
import ames.comercial.comandes.service.IProviderDiesReserva;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "tasca.recalculreserves", havingValue = "true", matchIfMissing = false)
public class TascaRecalculReserves {

    private static final Logger logger = LoggerFactory.getLogger(TascaRecalculReserves.class);

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    @Autowired IProviderDiesReserva providerDiesReserva;
    @Autowired LiniaComandaRepository liniaRepo;
    @Autowired ComandaRepository comandaRepo;
    @Autowired IObtenirStocks obtenirStocks;
    @Autowired ICalcularServible calcularServible;

    @Transactional
    @Scheduled(cron="0 0 2 * * *")
    public void executar() {
        logger.info("INICI TASCA RECALCUL RESERVES");
        // Obtenció de la data màxima de reserva
        var dataMaximaReserva = RequestThread.dateLocal().plusDays(providerDiesReserva.provide());
        // Obtenció de tots els articles-clients de normalitzats
        var articlesClient = obtenirArticlesClientsNormalitzats();
        // Correcció de cada empresa que gestiona normalitzats
        correcioReserves(articlesClient, Empresa.GROUP, dataMaximaReserva);
        correcioReserves(articlesClient, Empresa.MONTERREY, dataMaximaReserva);
        correcioReserves(articlesClient, Empresa.PORE, dataMaximaReserva);
        correcioReserves(articlesClient, Empresa.CMA, dataMaximaReserva);
        correcioReserves(articlesClient, Empresa.MEDICAL, dataMaximaReserva);
        logger.info("FI TASCA RECALCUL RESERVES");
    }

    private void correcioReserves(List<KeyArticleClient> articlesClient, Empresa empresa, LocalDate dataMaximaReserva) {
        // 1.- Es posen a 0 totes les reserves i estat de la reserva 'RES' de totes les
        // línies de comandes de normalitzats que estan pendents i no superen els dies de reserva
        logger.debug("INICI " + empresa);
        jdbcAmes.update("""
                UPDATE comandes.linia_comanda
                SET
                    quantitat_reservada = 0,
                    estat_reserva = 'RES'
                FROM comandes.comanda c
                WHERE c.codi = comandes.linia_comanda.comanda
                  AND c.empresa = ?
                  AND comandes.linia_comanda.actual
                  AND comandes.linia_comanda.clicod = '000000'
                  AND comandes.linia_comanda.data_solicitada <= ?
                  AND comandes.linia_comanda.quantitat_pendent > 0;
                """, empresa.clau(), dataMaximaReserva);

        // 2.- Es posen a 0 i que supera els dies de reserva 'NO' de totes les
        // línies de comandes de normalitzats que estan pendents i superen els dies de reserva
        jdbcAmes.update("""
                UPDATE comandes.linia_comanda
                SET
                    quantitat_reservada = 0,
                    estat_reserva = 'NO'
                FROM comandes.comanda c
                WHERE c.codi = comandes.linia_comanda.comanda
                  AND c.empresa = ?
                  AND comandes.linia_comanda.actual
                  AND comandes.linia_comanda.clicod = '000000'
                  AND comandes.linia_comanda.data_solicitada > ?
                  AND comandes.linia_comanda.quantitat_pendent > 0;
                """, empresa.clau(), dataMaximaReserva);

        Set<Long> setComandes = new HashSet<Long>();
        // Per cada article de normalitzats s'ha de calcular les reserves per a fer-ho via UPDATE
        for (var article : articlesClient) {
            // Obtenció de l'stock total de la peça
            var stockPesa = obtenirStocks.query(article, empresa);
            long stockTotal = stockPesa.map(Stock::stock).orElse(0L);
            if (stockTotal > 0) {
                // Línies pendents de servir de l'article client
                var liniesPendents = liniaRepo.findByArticlePendent(article, empresa);
                // Reserves actuals
                var stockReservat = stockReservat(liniesPendents);
                // Stock disponible (l'stock total menys l'stock reservat)
                var stockDisponible = stockTotal - stockReservat;
                // Càlcul de les línies que han de sol·licitar reserves (tenen pendent de reservar i no superen la data)
                var liniesPendentsReserva = liniesPendents.stream()
                        .filter(LiniaComanda::teQuantitatPendentReservar)
                        .filter(l -> l.dataSolicitada().isBefore(dataMaximaReserva))
                        .toList();
                // Càlcul de les reserves
                var respServ = new CalculadoraReserves(CalculadoraReservesReqImpl.builder()
                        .stock(stockDisponible)
                        .linies(liniesPendentsReserva.stream().map(CalculadoraReserves::buildRequest).toList())
                        .build()).executar();
                // UPDATE per cada línia que té reserva
                var liniesUpdate = respServ.linies()
                        .stream()
                        .filter(l -> l.quantitatReservada() > 0)
                        .toList();
                for (var linia : liniesUpdate) {
                    setComandes.add(linia.clauLinia().comanda());
                    var liniaOriginal = liniesPendentsReserva.stream().filter(l -> l.id().equals(linia.clauLinia())).findFirst().orElseThrow();
                    jdbcAmes.update("""
                            UPDATE comandes.linia_comanda SET
                              quantitat_reservada = ?,
                              estat_reserva = ?
                            WHERE comanda = ? AND numero = ? AND actual
                            """, linia.quantitatReservada(),
                            Reservable.of(linia.quantitatReservada(), liniaOriginal.quantitatPendent()).toString(),
                            linia.clauLinia().comanda(),
                            linia.clauLinia().numero());
                }
            }
        }
        updateComandes(setComandes);
        logger.debug("FI {}", empresa);
    }

    private void updateComandes(Set<Long> setComandes) {
        for (var codComanda : setComandes) {
            Servible serv = calcularServible.calcula(liniaRepo.findByComanda(codComanda));
            jdbcAmes.update("""
                            UPDATE comandes.comanda SET
                              servible = ?
                            WHERE codi = ?
                            """, serv.toString(), codComanda);
        }
    }

    private long stockReservat(List<LiniaComanda> linies) {
        return linies.stream()
                .map(LiniaComanda::reserva)
                .map(reserva -> reserva.map(InformacioReserva::quantitat).orElse(0L))
                .mapToLong(Long::longValue)
                .sum();
    }

    private List<KeyArticleClient> obtenirArticlesClientsNormalitzats() {
        PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                        SELECT a.artint
                        FROM comundb.artcli a
                        LEFT JOIN dummy d ON a.clicod = d.str1
                        WHERE clicod = '000000'
                        """);
        ResultSetAction<List<KeyArticleClient>> rsAction = rs -> {
            List<KeyArticleClient> resultat = new ArrayList<KeyArticleClient>();
            while (rs.next()) {
                resultat.add(KeyArticleClient.of(rs.getString(1), "000000"));
            }
            return resultat;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

}
