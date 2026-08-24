package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.InformacioMagatzem;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.server.Json;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Albarans que el magatzem ja ha servit però que el transportista encara no ha recollit: els que tenen
 * {@code isServit} cert i {@code isEntregat} fals a la informació de magatzem.
 * <p>
 * És la consulta que alimenta l'avís diari de {@code EnviarAvisAlbaransNoRecollitsComercial} (un correu
 * per a cada persona que els ha creat) i el de {@code EnviarAvisAlbaransNoRecollitsMagatzem} (un correu
 * per a cada magatzem). Per això retorna tant les dades de l'albarà com l'empleat que el va crear, i
 * els dos avisos es poden muntar a partir d'una sola lectura.
 * <p>
 * Hi entren tots els tipus d'albarà, també els de traspàs i els de consum, que no tenen client i per
 * tant deixen el codi i el nom del client buits.
 */
@Service
public class ObtenirAlbaransServitsNoRecollits {

    /** Valor que es mostra quan l'albarà no té l'usuari de creació informat (processos automàtics) */
    private static final String USUARI_AMES = "AMES";

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    /**
     * S'ordena per data i no per magatzem perquè els dos avisos agrupen després el resultat (per empleat
     * l'un, per magatzem l'altre) i l'agrupació manté l'ordre d'arribada: així les dues taules queden
     * ordenades per data amb una sola ordenació.
     */
    public List<AlbaraNoRecollit> executar() {
        return jdbcAmes.query("""
                    SELECT a.codi, a.empresa, a.data, a.magatzem,
                           a.client, c.nom AS client_nom,
                           a.informacio_enviament, a.informacio_magatzem,
                           COALESCE(a.id_usuari_creacio, 0) AS usufab,
                           TRIM(CONCAT_WS(' ', e.nom, e.cognoms)) AS nom_cognoms, e.email
                    FROM albarans.albara a
                    LEFT JOIN cache.cache_client c
                        ON a.client = c.clicod
                    LEFT JOIN cache.cache_empleats e
                        ON e.usufab = a.id_usuari_creacio
                    WHERE COALESCE((a.informacio_magatzem ->> 'isServit')::boolean, false)
                      AND NOT COALESCE((a.informacio_magatzem ->> 'isEntregat')::boolean, false)
                    ORDER BY a.data, a.empresa, a.codi
                """,
                (rs, rowNum) -> {
                    var nomCognoms = rs.getString("nom_cognoms");
                    return new AlbaraNoRecollit(
                            KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa")),
                            rs.getDate("data").toLocalDate(),
                            rs.getString("magatzem"),
                            Optional.ofNullable(rs.getString("client")),
                            Optional.ofNullable(rs.getString("client_nom")),
                            json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class),
                            json.deserialize(rs.getString("informacio_magatzem"), InformacioMagatzem.class),
                            rs.getLong("usufab"),
                            Strings.isNullOrEmpty(nomCognoms) ? USUARI_AMES : nomCognoms,
                            Optional.ofNullable(rs.getString("email")));
                });
    }

    /**
     * @param usufabCreacio codi a fàbrica de l'empleat que va crear l'albarà
     *                      ({@code cache_empleats.usufab}); 0 si l'albarà no en té cap informat
     * @param nomCreacio    nom i cognoms de l'empleat que va crear l'albarà, o {@code AMES} si no es
     *                      pot identificar
     * @param emailCreacio  adreça de l'empleat que va crear l'albarà; buida si no en té o si no es pot
     *                      identificar, i aleshores l'albarà només surt a l'avís del magatzem
     */
    public record AlbaraNoRecollit(
            KeyAlbara id,
            LocalDate data,
            String magatzem,
            Optional<String> client,
            Optional<String> clientNom,
            InformacioEnviament informacioEnviament,
            InformacioMagatzem informacioMagatzem,
            long usufabCreacio,
            String nomCreacio,
            Optional<String> emailCreacio) {
    }

}
