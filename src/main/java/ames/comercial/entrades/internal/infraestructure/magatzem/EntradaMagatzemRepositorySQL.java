package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;
import ames.comercial.entrades.internal.domain.EntradaMagatzemImpl;
import ames.comercial.server.BatchInsertHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class EntradaMagatzemRepositorySQL implements EntradaMagatzemRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(EntradaMagatzem entradaMagatzem) {
        // S'eliminen els registres
        delete(entradaMagatzem.id());

        // INSERT entrada_magatzem
        var params = new HashMap<String, Object>();
        params.put("id", entradaMagatzem.id());
        params.put("id_entrada_fabrica", entradaMagatzem.idEntradaFabrica());
        params.put("article", entradaMagatzem.articleFabrica());
        params.put("client", entradaMagatzem.client());
        params.put("magatzem", entradaMagatzem.magatzem());
        params.put("fabrica", entradaMagatzem.fabrica());
        params.put("quantitat", entradaMagatzem.quantitat());
        params.put("quantitat_caixa", entradaMagatzem.quantitatCaixa());
        params.put("data_etiqueta", entradaMagatzem.dataEtiqueta());
        params.put("lot", entradaMagatzem.lot());
        params.put("of", entradaMagatzem.of());
        params.put("etiqueta_caixa", entradaMagatzem.etiquetaCaixa());
        params.put("etiqueta_palet", entradaMagatzem.etiquetaPalet());
        params.put("nivell_tecnic", entradaMagatzem.nivellTecnic());
        params.put("codi_seguretat", entradaMagatzem.codiSeguretat());
        params.put("codi_cal", entradaMagatzem.codiCal());
        params.put("data_entrada", entradaMagatzem.dataEntrada());
        params.put("pes_premsat", entradaMagatzem.pesPremsat());
        params.put("pes_final", entradaMagatzem.pesFinal());
        params.put("data_alta", entradaMagatzem.dataAlta());
        params.put("data_processat", entradaMagatzem.dataProcessat().orElse(null));
        params.put("error", entradaMagatzem.error().orElse(null));
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("entrada_magatzem")
                .execute(params);

        // INSERT dels detalls
        saveDetall(entradaMagatzem.id(), entradaMagatzem.detalls());
        // INSERT dels embalatges
        saveEmbalatge(entradaMagatzem.id(), entradaMagatzem.embalatges());
    }

    @Override
    public void save(List<EntradaMagatzem> entradesMagatzem) {
        entradesMagatzem.forEach(this::save);
    }

    private void saveDetall(String id, List<EntradaMagatzemDetall> listDetalls) {
        var registres = new ArrayList<Map<String, Object>>();
        listDetalls.forEach(detall -> {
            var params = new HashMap<String, Object>();
            params.put("id", id);
            params.put("etiqueta_caixa", detall.etiquetaCaixa());
            params.put("quantitat", detall.quantitat());
            params.put("data_etiqueta", detall.dataEtiqueta());
            params.put("lot", detall.lot());
            params.put("of", detall.of());
            params.put("error",detall.error().orElse(null));
            registres.add(params);
        });
        new BatchInsertHelper(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("entrada_magatzem_detall")
                .execute(registres);
    }

    private void saveEmbalatge(String id, List<EntradaMagatzemEmbalatge> listEmbalatges) {
        var registres = new ArrayList<Map<String, Object>>();
        listEmbalatges.forEach(emb -> {
            var params = new HashMap<String, Object>();
            params.put("id", id);
            params.put("article", emb.article());
            params.put("client", emb.client());
            params.put("codi_element", emb.codiElement());
            params.put("etiqueta_caixa", emb.etiquetaCaixa().orElse(null));
            params.put("etiqueta_palet", emb.etiquetaPalet());
            params.put("descripcio", emb.descripcio());
            params.put("quantitat",emb.quantitat());
            registres.add(params);
        });
        new BatchInsertHelper(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("entrada_magatzem_embalatge")
                .execute(registres);
    }

    private void delete(String id) {
        jdbcAmes.update("DELETE FROM entrades.entrada_magatzem WHERE id = ?", id);
        jdbcAmes.update("DELETE FROM entrades.entrada_magatzem_detall WHERE id = ?", id);
        jdbcAmes.update("DELETE FROM entrades.entrada_magatzem_embalatge WHERE id = ?", id);
    }

    @Override
    public List<EntradaMagatzem> pendentsProcessar() {
        var sql = """
				SELECT em.*,
				    ed.etiqueta_caixa as detall_etiqueta_caixa,
				    ed.quantitat as detall_quantitat,
				    ed.data_etiqueta as detall_data_etiqueta,
				    ed.lot as detall_lot,
				    ed.of as detall_of,
				    ed.error as detall_error
				FROM entrades.entrada_magatzem em
				LEFT JOIN entrades.entrada_magatzem_detall ed ON em.id= ed.id
				WHERE data_processat IS NULL
				ORDER BY id_entrada_fabrica, em.etiqueta_palet, em.etiqueta_caixa, ed.etiqueta_caixa;
				""";
        // Obtenció de les entrades i el seu detall sense la part d'embalatges
        var entradesSenseEmbalatges = jdbcAmes.query(sql, new EntradaMagatzemExtractor());
        // Obtenció dels embalatges
        var mapEmbalatges = embalatgesPendents();
        // Merge de la llista d'entrades amb els embalatges
        List<EntradaMagatzem> resultat = new ArrayList<>();
        entradesSenseEmbalatges.forEach(e -> {
            List<EntradaMagatzemEmbalatge> listEmbalatges = Objects.requireNonNullElse(mapEmbalatges.get(e.id()), List.of());
            resultat.add(EntradaMagatzemImpl.builder()
                    .from(e)
                    .embalatges(listEmbalatges)
                    .build());
        });
        return  resultat;
    }

    private Map<String, List<EntradaMagatzemEmbalatge>> embalatgesPendents() {
        var sql = """
                SELECT emb.*
                FROM entrades.entrada_magatzem_embalatge emb
                LEFT JOIN entrades.entrada_magatzem em ON emb.id= em.id
                WHERE data_processat IS NULL
                """;
        return jdbcAmes.query(sql, new EntradaMagatzemEmbalatgeExtractor());
    }
}
