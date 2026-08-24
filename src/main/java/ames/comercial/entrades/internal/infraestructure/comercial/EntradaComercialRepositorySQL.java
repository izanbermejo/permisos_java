package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.EntradaComercial;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class EntradaComercialRepositorySQL implements EntradaComercialRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(EntradaComercial entradaComercial) {
        delete(entradaComercial.id());

        // INSERT entrada_comercial
        var params = new HashMap<String, Object>();
        params.put("id", entradaComercial.id());
        params.put("id_entrada_fabrica", entradaComercial.idEntradaFabrica());
        params.put("article", entradaComercial.articleFabrica());
        params.put("client", entradaComercial.client());
        params.put("magatzem", entradaComercial.magatzem());
        params.put("fabrica", entradaComercial.fabrica());
        params.put("quantitat", entradaComercial.quantitat());
        params.put("quantitat_caixa", entradaComercial.quantitatCaixa());
        params.put("of", entradaComercial.of());
        params.put("data_entrada", entradaComercial.dataEntrada());
        params.put("pes_premsat", entradaComercial.pesPremsat());
        params.put("pes_final", entradaComercial.pesFinal());
        params.put("data_alta", entradaComercial.dataAlta());
        params.put("data_processat", entradaComercial.dataProcessat().orElse(null));
        params.put("error", entradaComercial.error().orElse(null));
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("entrada_comercial")
                .execute(params);
    }

    @Override
    public void save(List<EntradaComercial> entradaComercial) {
        entradaComercial.forEach(this::save);
    }

    private void delete(String id) {
        jdbcAmes.update("DELETE FROM entrades.entrada_comercial WHERE id = ?", id);
    }

    @Override
    public List<EntradaComercial> pendentsProcessar() {
        return jdbcAmes.query("""
                SELECT * 
                FROM entrades.entrada_comercial 
                WHERE data_processat IS NULL
                ORDER BY data_alta ASC
                """, new EntradaComercialMapper());
    }

    @Override
    public List<EntradaComercial> obtenirByIdFabrica(String idEntradaFabrica) {
        return jdbcAmes.query("""
                        SELECT *
                        FROM entrades.entrada_comercial
                        WHERE id_entrada_fabrica = ?
                        ORDER BY data_entrada DESC;
                """,new EntradaComercialMapper(), idEntradaFabrica);
    }

    @Override
    public Optional<EntradaComercial> find(String id) {
        return Optional.ofNullable(jdbcAmes.queryForObject("""
                        SELECT *
                        FROM entrades.entrada_comercial
                        WHERE id = ?;
                """, new EntradaComercialMapper(), id));
    }
}
