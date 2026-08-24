package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.IObtenirInformacioAcumulatsArticleClient;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ObtenirQuantitatServidaAcumulada {

    @Autowired IObtenirInformacioAcumulatsArticleClient obtenirInformacioAcumulatsArticleClient;
    @Autowired NamedParameterJdbcTemplate jdbcAmes;

    public long executar (KeyArticleClient articleClient, KeyAlbara clauAlbaraLimit) {
        // Obtenció de l'informació d'acumulats per article client
        var optInformacioAcumulats = obtenirInformacioAcumulatsArticleClient.get(articleClient);

        // Obtenció de la quantitat acumulada a partir de l'informació d'acumulats i de la data límit de l'albarà
        var quantitatAcumulada = obtenirAlbarans(articleClient,
                optInformacioAcumulats.data().orElse(LocalDate.MIN),
                optInformacioAcumulats.numAlbaraAsLong(),
                clauAlbaraLimit);

        // El resultat és la suma de la quantitat acumulada obtinguda a partir dels albarans i de la quantitat
        // acumulada obtinguda a partir de l'informació d'acumulats
        return quantitatAcumulada + optInformacioAcumulats.quantitatAcumulada();
    }

    private long obtenirAlbarans(KeyArticleClient articleClient, LocalDate dataAcumulats, long numeroAlbaraAcumulats,
                                 KeyAlbara clauAlbaraLimit) {
        var params = new MapSqlParameterSource();
        params.addValue("empresa", clauAlbaraLimit.empresa());
        params.addValue("artint", articleClient.artint());
        params.addValue("clicod", articleClient.clicod());
        params.addValue("dataAcumulats", dataAcumulats);
        params.addValue("numeroAlbaraAcumulats", numeroAlbaraAcumulats);
        params.addValue("numeroAlbaraLimit", clauAlbaraLimit.codi());
        var sql = """
                WITH linies AS (
                        SELECT
                            codi_albara,
                            empresa,
                            SUM(quantitat) AS quantitat
                        FROM albarans.linia_albara
                        WHERE artint = :artint
                            AND clicod = :clicod
                        GROUP BY codi_albara, empresa
                    )
                    SELECT
                        SUM(l.quantitat)
                    FROM linies l
                    LEFT JOIN albarans.albara a
                        ON l.codi_albara = a.codi AND l.empresa = a.empresa
                    WHERE
                      a.empresa = :empresa
                      AND a.client = :clicod
                      AND a.tipus IN ('CLIENT', 'TRASPAS_PLATAFORMA')
                      AND a.is_tancat
                      AND a.data > :dataAcumulats
                      AND a.codi > :numeroAlbaraAcumulats
                      AND a.codi <= :numeroAlbaraLimit;
            """;
        return Optional.ofNullable(jdbcAmes.queryForObject(sql, params, Long.class))
                .orElse(0L);
    }

}
