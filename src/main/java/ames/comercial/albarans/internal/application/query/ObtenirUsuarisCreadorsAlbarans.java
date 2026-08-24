package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.ext.IObtenirUsuarisCreadorsAlbarans;
import ames.comercial.cache.CacheConfig;
import ames.comercial.shared.SimpleItem;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Obté els usuaris que han creat albarans en els darrers dos anys, per poder-los filtrar al frontend.
 * <p>
 * La clau és el codi de l'empleat a fàbrica ({@code cache.cache_empleats.usufab}, el mateix codi que
 * guarda {@code albarans.albara.id_usuari_creacio}) i el valor el nom i els cognoms de l'empleat.
 * Els albarans que no tenen l'usuari de creació informat (id nul o 0, típicament els creats per
 * processos automàtics) s'agrupen sota la clau 0 amb el valor {@code AMES}; també hi cauen els codis
 * que no tenen cap empleat a la cache.
 * <p>
 * La consulta ha de recórrer tota la taula d'albarans (no hi ha índex per data de creació) i triga
 * mig segon, massa per a la metadata, que ha de ser àgil; per això el resultat es manté a la cache
 * de Caffeine ({@link CacheConfig#USUARIS_CREADORS_ALBARANS}) igual que l'stock satèl·lit, i la
 * tasca {@code TascaCacheUsuarisCreadorsAlbarans} la manté escalfada perquè cap petició no l'hagi de
 * calcular. Amb {@code sync = true}, si tot i això la cache és buida, només una petició la recalcula.
 */
@Service
public class ObtenirUsuarisCreadorsAlbarans implements IObtenirUsuarisCreadorsAlbarans {

    /** Valor que es mostra per als albarans sense usuari de creació informat */
    private static final String USUARI_AMES = "AMES";

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    @Cacheable(value = CacheConfig.USUARIS_CREADORS_ALBARANS, sync = true)
    public List<SimpleItem> all() {
        return jdbcAmes.query(
                """
                    SELECT COALESCE(a.id_usuari_creacio, 0) AS usufab,
                           MAX(TRIM(CONCAT_WS(' ', e.nom, e.cognoms))) AS nom_cognoms
                    FROM albarans.albara a
                    LEFT JOIN cache.cache_empleats e
                        ON e.usufab = a.id_usuari_creacio
                    WHERE a.data_creacio >= CURRENT_DATE - INTERVAL '2 years'
                    GROUP BY 1
                """,
                (rs, rowNum) -> {
                    var nomCognoms = rs.getString("nom_cognoms");
                    return SimpleItem.of(
                            String.valueOf(rs.getLong("usufab")),
                            Strings.isNullOrEmpty(nomCognoms) ? USUARI_AMES : nomCognoms);
                });
    }

}
