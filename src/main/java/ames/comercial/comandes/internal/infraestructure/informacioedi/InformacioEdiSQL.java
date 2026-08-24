package ames.comercial.comandes.internal.infraestructure.informacioedi;

import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.server.SimpleJdbcUpsert;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class InformacioEdiSQL implements InformacioEdiRepository {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

     @Override
     public void save(List<InformacioEdi> listInformacioEdi) {
        listInformacioEdi.forEach(this::save);
     }

    @Override
    public Optional<KeyComandaEdi> findKeyComandaEdi(String comandaClient, LocalDate data, KeyArticleClient articleClient) {
        var sql = """
            SELECT id_missatge_edi, id_comanda_edi
            FROM comandes.informacio_edi
            WHERE comanda = ?
              AND data = ?
              AND artint = ?
              AND clicod = ?
            """;
        return jdbcAmes.query(
                sql,
                rs -> {
                    if (rs.next()) {
                        return Optional.of(
                                KeyComandaEdi.of(rs.getLong("id_missatge_edi"), rs.getLong("id_comanda_edi"))
                        );
                    }
                    return Optional.empty();
                },
                comandaClient,
                data,
                articleClient.artint(),
                articleClient.clicod()
        );
    }

    private void save(InformacioEdi informacioEdi) {
         new SimpleJdbcUpsert(jdbcAmes)
                 .withSchemaName("comandes")
                 .withTableName("informacio_edi")
                 .onConflictColumns("comanda", "data", "artint", "clicod")
                 .execute(mapInformacioEdiToParams(informacioEdi));
     }

     private HashMap<String, Object> mapInformacioEdiToParams(InformacioEdi informacioEdi) {
         return new HashMap<>(){{
             put("comanda", informacioEdi.comandaClient());
             put("data", informacioEdi.data());
             put("artint", informacioEdi.articleClient().artint());
             put("clicod", informacioEdi.articleClient().clicod());
             put("quantitat", informacioEdi.quantitat());
             put("id_missatge_edi", informacioEdi.comandaEdi().idMissatge());
             put("id_comanda_edi", informacioEdi.comandaEdi().idComanda());
         }};
     }

}
