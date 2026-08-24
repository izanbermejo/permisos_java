package ames.comercial.cache.articlesclient;

import ames.comercial.shared.Preu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReompleCacheArticlesClient {

    static final Logger log = LogManager.getLogger(ReompleCacheArticlesClient.class.getName());

    @Autowired ObtenirArticlesClientCache obtenirArticlesClientCache;
    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    public void executar() {
        log.info("INI - Refresh cache articlesclient");
        var listRegistres = obtenirArticlesClientCache.executar();
        // S'eliminen les taules temporals
        jdbcTemplate.getJdbcTemplate().execute("DROP TABLE IF EXISTS cache.cache_article_client_old");
        jdbcTemplate.getJdbcTemplate().execute("DROP TABLE IF EXISTS cache.cache_article_client_new");
        // Creació de la taula temporal a partir da la existent
        jdbcTemplate.getJdbcTemplate().execute("CREATE TABLE cache.cache_article_client_new (LIKE cache.cache_article_client INCLUDING ALL)");

        // INSERT dels registres
        String sql = """
                    INSERT INTO cache.cache_article_client_new (artint, clicod, codi_fabrica, flag, referencia, empresa,
                        empresa_desc, empresa_entrega, empresa_entrega_desc, denominacio, nivell_tecnic, pesa_seguretat,
                        forma_enviament, fabrica_codi, fabrica_desc,
                        preu, divisa, cost, divisa_cost, preu_ames, divisa_ames,
                        stock, stock_minim, mag_entrada_codi, mag_entrada_desc, mag_sortida_codi, mag_sortida_desc,
                        pes, bloquejat, usuari_bloqueig, unitats_embalatge, bosses_caixa, caixes_palet, tipus,
                        projectman_codi, partida_arant_codi, partida_arant_desc, partida_arant_partida, planificador, notes_embalatge,
                        codi_ean13, is_vol_udi, dies_caducitat, codi_familia, nom_familia)
                    VALUES(:artint, :clicod, :codi_fabrica, :flag, :referencia, :empresa, :empresa_desc,
                        :empresa_entrega, :empresa_entrega_desc, :denominacio, :nivell_tecnic, :pesa_seguretat, :forma_enviament,
                        :fabrica_codi, :fabrica_desc, :preu, :divisa, :cost, :divisa_cost, :preu_ames, :divisa_ames,
                        :stock, :stock_minim, :mag_entrada_codi, :mag_entrada_desc,
                        :mag_sortida_codi, :mag_sortida_desc, :pes, :bloquejat, :usuari_bloqueig,
                        :unitats_embalatge, :bosses_caixa, :caixes_palet, :tipus, :projectman_codi,
                        :partida_arant_codi, :partida_arant_desc, :partida_arant_partida, :planificador, :notes_embalatge,
                        :codi_ean13, :is_vol_udi, :dies_caducitat, :codi_familia, :nom_familia);
                """;
        List<MapSqlParameterSource> batchValues = new ArrayList<>();
        for (var r : listRegistres) {
            batchValues.add(new MapSqlParameterSource()
                    .addValue("artint", r.artint())
                    .addValue("clicod", r.codiClient())
                    .addValue("codi_fabrica", r.article())
                    .addValue("flag", r.flag())
                    .addValue("referencia", r.referencia())
                    .addValue("empresa", r.empresa())
                    .addValue("empresa_desc", r.empresaDesc())
                    .addValue("empresa_entrega", r.empresaEntrega())
                    .addValue("empresa_entrega_desc", r.empresaEntregaDesc())
                    .addValue("denominacio", r.denominacio())
                    .addValue("nivell_tecnic", r.nivellTecnic())
                    .addValue("pesa_seguretat", r.isPesaSeguretat())
                    .addValue("forma_enviament", r.formaEnviament())
                    .addValue("fabrica_codi", r.codiFabrica())
                    .addValue("fabrica_desc", r.nomFabrica())
                    .addValue("preu", r.preu())
                    .addValue("divisa", r.divisa())
                    .addValue("cost", r.cost().map(Preu::valor).orElse(null))
                    .addValue("divisa_cost", r.cost().map(c -> c.divisa().symbol()).orElse(null))
                    .addValue("preu_ames", r.preuAmes().map(Preu::valor).orElse(null))
                    .addValue("divisa_ames", r.preuAmes().map(c -> c.divisa().symbol()).orElse(null))
                    .addValue("stock", r.stock())
                    .addValue("stock_minim", r.stockMinim())
                    .addValue("pes", r.pes())
                    .addValue("bloquejat", r.bloquejatStock())
                    .addValue("usuari_bloqueig", r.usuariBloqueigStock())
                    .addValue("mag_entrada_codi", r.magatzemEntrada())
                    .addValue("mag_entrada_desc", r.magatzemEntradaDesc())
                    .addValue("mag_sortida_codi", r.magatzemSortida())
                    .addValue("mag_sortida_desc", r.magatzemSortidaDesc())
                    .addValue("unitats_embalatge", r.unitatsEmbalatge())
                    .addValue("bosses_caixa", r.bossesCaixa())
                    .addValue("caixes_palet", r.caixesPalet())
                    .addValue("tipus", r.tipus().name())
                    .addValue("projectman_codi", r.codiProjectManager())
                    .addValue("partida_arant_codi", r.partidaArantCodi())
                    .addValue("partida_arant_desc", r.partidaArantDesc())
                    .addValue("partida_arant_partida", r.partidaArantPartida())
                    .addValue("planificador", r.planificador())
                    .addValue("notes_embalatge", r.notesEmbalatge().orElse(null))
                    .addValue("codi_ean13", r.codiEan13().orElse(null))
                    .addValue("is_vol_udi", r.isVolUdi())
                    .addValue("dies_caducitat", r.diesCaducitat())
                    .addValue("codi_familia", r.codiFamilia().orElse(null))
                    .addValue("nom_familia", r.nomFamilia().orElse(null))
            );
            if (batchValues.size() >= 1000) {
                jdbcTemplate.batchUpdate(sql, batchValues.toArray(new SqlParameterSource[0]));
                batchValues.clear();
            }
        }
        if (!batchValues.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchValues.toArray(new SqlParameterSource[0]));
            batchValues.clear();
        }
        jdbcTemplate.getJdbcTemplate().execute("""
            BEGIN;
            ALTER TABLE cache.cache_article_client RENAME TO cache_article_client_old;
            ALTER TABLE cache.cache_article_client_new RENAME TO cache_article_client;
            DROP TABLE cache.cache_article_client_old;
            COMMIT;
            """);
        log.info("FI - Refresh cache articlesclient");
    }

}
