package ames.comercial.albarans.internal.infraestructure.facturacio;

import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbara;
import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbaraImpl;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public class FacturacioLiniaAlbaraSQL implements FacturacioLiniaAlbaraRepository {

    @Autowired NamedParameterJdbcTemplate jdbc;

    @Override
    public void save(FacturacioLiniaAlbara facturacioLiniaAlbara) {
        new SimpleJdbcInsert(jdbc.getJdbcTemplate())
                .withSchemaName("albarans")
                .withTableName("facturacio")
                .execute(mapLiniaAlbaraToParams(facturacioLiniaAlbara));
    }

    /**
     * TODO: Eliminar després de la migració
     */
    @SuppressWarnings("unchecked")
    public void saveBatch(List<FacturacioLiniaAlbara> linies) {
        if (linies.isEmpty()) {
            return;
        }
        var insertBatch = new SimpleJdbcInsert(jdbc.getJdbcTemplate())
                .withSchemaName("albarans")
                .withTableName("facturacio");
        // Processar en lotes per evitar problemes de memòria
        for (int i = 0; i < linies.size(); i += 2000) {
            int end = Math.min(i + 2000, linies.size());
            var subList = linies.subList(i, end);
            var batchParams = subList.stream()
                    .map(this::mapLiniaAlbaraToParams)
                    .toArray(HashMap[]::new);
            insertBatch.executeBatch(batchParams);
        }
    }

    private HashMap<String, Object> mapLiniaAlbaraToParams(FacturacioLiniaAlbara facturacioLiniaAlbara) {
        var params = new HashMap<String, Object>();
        params.put("empresa", facturacioLiniaAlbara.liniaAlbara().idAlbara().empresa());
        params.put("codi_factura", facturacioLiniaAlbara.codiFactura());
        params.put("codi_albara", facturacioLiniaAlbara.liniaAlbara().idAlbara().codi());
        params.put("linia_albara", facturacioLiniaAlbara.liniaAlbara().linia());
        params.put("quantitat", facturacioLiniaAlbara.quantitat());
        return params;
    }

    @Override
    public List<FacturacioLiniaAlbara> findByFactura(String empresa, String codiFactura) {
        return jdbc.query(
                "SELECT * FROM albarans.facturacio WHERE empresa = :empresa AND codi_factura = :codi_factura",
                new MapSqlParameterSource()
                        .addValue("empresa", empresa)
                        .addValue("codi_factura", codiFactura),
                (rs, rowNum) -> FacturacioLiniaAlbaraImpl.builder()
                        .codiFactura(rs.getString("codi_factura"))
                        .liniaAlbara(KeyLiniaAlbara.of(
                                rs.getString("empresa"),
                                rs.getLong("codi_albara"),
                                rs.getLong("linia_albara")
                        ))
                        .quantitat(rs.getLong("quantitat"))
                        .build()
        );
    }

    @Override
    public void deleteByFactura(String empresa, String codiFactura) {
        jdbc.getJdbcTemplate()
                .update("DELETE FROM albarans.facturacio WHERE empresa = ? AND codi_factura = ?", empresa, codiFactura);
    }

}
