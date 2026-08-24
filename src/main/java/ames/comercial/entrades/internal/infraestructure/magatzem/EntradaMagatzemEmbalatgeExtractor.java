package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;
import ames.comercial.entrades.internal.domain.EntradaMagatzemEmbalatgeImpl;
import ames.comercial.server.MapperUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EntradaMagatzemEmbalatgeExtractor implements ResultSetExtractor<Map<String, List<EntradaMagatzemEmbalatge>>> {

    @Override
    public Map<String, List<EntradaMagatzemEmbalatge>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, List<EntradaMagatzemEmbalatge>> result = new HashMap<>();

        while (rs.next()) {
            String id = rs.getString("id");
            var emb = EntradaMagatzemEmbalatgeImpl.builder()
                    .article(rs.getString("article"))
                    .client(rs.getString("client"))
                    .codiElement(rs.getString("codi_element"))
                    .etiquetaCaixa(MapperUtils.readOptionalLong(rs, "etiqueta_caixa"))
                    .etiquetaPalet(rs.getLong("etiqueta_palet"))
                    .descripcio(rs.getString("descripcio"))
                    .quantitat(rs.getLong("quantitat"))
                    .build();
            result.computeIfAbsent(id, k -> new ArrayList<>()).add(emb);
        }

        return result;
    }

}
