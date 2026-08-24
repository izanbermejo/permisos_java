package ames.comercial.cache.clients;

import ames.comercial.shared.Adresa;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.SimpleItem;
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
public class ReompleCacheClients {

    static final Logger log = LogManager.getLogger(ReompleCacheClients.class.getName());

    @Autowired ObtenirClientsCache obtenirClientsCache;
    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    public void executar() {
        log.info("INI - Refresh cache clients");
        var listRegistres =  obtenirClientsCache.executar();
        // S'eliminen les taules temporals
        jdbcTemplate.getJdbcTemplate().execute ("DROP TABLE IF EXISTS cache.cache_client_old");
        jdbcTemplate.getJdbcTemplate().execute ("DROP TABLE IF EXISTS cache.cache_client_new");
        // Creació de la taula temporal a partir da la existent
        jdbcTemplate.getJdbcTemplate().execute("CREATE TABLE cache.cache_client_new (LIKE cache.cache_client INCLUDING ALL)");

        // INSERT dels registres
        String sql = """
                    INSERT INTO cache.cache_client_new (clicod, nom, alias, estat, codi_proveidor, empresa, data_bloqueig,divisa, responsable,
                        delegat_codi, usuari_bloqueig, pais, pais_nom, forma_enviament, incoterm, desti, transportista_codi,
                        transportista_desc, zonatra_codi, zonatra_desc, dies_sortida, num_dies_transit,
                        adresa_nom, adresa_adresa, adresa_poblacio, adresa_codi_postal, adresa_pais, adresaenv_nom,
                        adresaenv_adresa, adresaenv_poblacio, adresaenv_codi_postal, adresaenv_pais, notes_client,
                        notes_logistica, notes_morositat)
                    VALUES(:clicod, :nom, :alias, :estat, :codi_proveidor, :empresa, :data_bloqueig, :divisa, :responsable, :delegat_codi, :usuari_bloqueig, :pais,
                        :pais_nom, :forma_enviament, :incoterm, :desti, :transportista_codi, :transportista_desc,
                        :zonatra_codi, :zonatra_desc, :dies_sortida, :num_dies_transit, :adresa_nom,
                        :adresa_adresa, :adresa_poblacio, :adresa_codi_postal, :adresa_pais, :adresaenv_nom,
                        :adresaenv_adresa, :adresaenv_poblacio, :adresaenv_codi_postal, :adresaenv_pais,
                        :notes_client, :notes_logistica, :notes_morositat);
                """;
        List<MapSqlParameterSource> batchValues = new ArrayList<>();
        for (var r : listRegistres) {
            batchValues.add(new MapSqlParameterSource()
                    .addValue("clicod", r.clicod())
                    .addValue("nom", r.nom())
                    .addValue("alias", r.alias())
                    .addValue("estat", r.estat())
                    .addValue("codi_proveidor", r.codiProveidor())
                    .addValue("empresa", r.empresa())
                    .addValue("data_bloqueig", r.dataBloqueig().orElse(null))
                    .addValue("divisa", r.divisa().map(Divisa::toString).orElse(null))
                    .addValue("responsable", r.responsableLogistica())
                    .addValue("delegat_codi", r.codiDelegat().orElse(null))
                    .addValue("usuari_bloqueig", r.usuariBloqueig().orElse(null))
                    .addValue("pais", r.pais())
                    .addValue("pais_nom", r.nomPais())
                    .addValue("forma_enviament", r.formaEnviament().map(Enum::name).orElse(null))
                    .addValue("incoterm", r.incoterm().map(Enum::name).orElse(null))
                    .addValue("desti", r.desti())
                    .addValue("transportista_codi", r.codiTransportista())
                    .addValue("transportista_desc", r.descTransportista())
                    .addValue("zonatra_codi", r.zonaTransport().map(SimpleItem::codi).orElse(null))
                    .addValue("zonatra_desc", r.zonaTransport().map(SimpleItem::nom).orElse(null))
                    .addValue("dies_sortida", r.diesSortida())
                    .addValue("num_dies_transit", r.diesTransitClient())
                    .addValue("adresa_nom", r.adresa().destinatari())
                    .addValue("adresa_adresa", r.adresa().adresa())
                    .addValue("adresa_poblacio", r.adresa().poblacio())
                    .addValue("adresa_codi_postal", r.adresa().codiPostal())
                    .addValue("adresa_pais", r.adresa().pais())
                    .addValue("adresaenv_nom", r.adresaEnviament().map(Adresa::destinatari).orElse(null))
                    .addValue("adresaenv_adresa", r.adresaEnviament().map(Adresa::adresa).orElse(null))
                    .addValue("adresaenv_poblacio", r.adresaEnviament().map(Adresa::poblacio).orElse(null))
                    .addValue("adresaenv_codi_postal", r.adresaEnviament().map(Adresa::codiPostal).orElse(null))
                    .addValue("adresaenv_pais", r.adresaEnviament().map(Adresa::pais).orElse(null))
                    .addValue("notes_client", r.notesClient())
                    .addValue("notes_logistica", r.notesLogistica())
                    .addValue("notes_morositat", r.notesMorositat()));
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
            ALTER TABLE cache.cache_client RENAME TO cache_client_old;
            ALTER TABLE cache.cache_client_new RENAME TO cache_client;
            DROP TABLE IF EXISTS cache.cache_client_old;
            COMMIT;
            """);
        log.info("FI - Refresh cache clients");
    }

}
