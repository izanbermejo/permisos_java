package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.entrades.internal.domain.EntradaMagatzemDetallImpl;
import ames.comercial.entrades.internal.domain.EntradaMagatzemImpl;
import ames.comercial.server.MapperUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntradaMagatzemExtractor implements ResultSetExtractor<List<EntradaMagatzem>> {

    @Override
    public List<EntradaMagatzem> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<EntradaMagatzem> resultat = new ArrayList<>();
        EntradaMagatzemBuilderState current = null;
        while (rs.next()) {
            String idEntrada = rs.getString("id");
            // Comprovar si és una nova entrada
            if (current == null || !current.id.equals(idEntrada)) {
                if (current != null) {
                    resultat.add(current.build());
                }
                current = new EntradaMagatzemBuilderState(rs);
            }

            current.addDetall(rs);
        }
        // S'afegeix l'última entrada
        if (current != null) {
            resultat.add(current.build());
        }
        return  resultat;
    }

    private static class EntradaMagatzemBuilderState {
        final String id;
        final EntradaMagatzemImpl.Builder builder;
        final List<EntradaMagatzemDetall> detalls = new ArrayList<>();

        EntradaMagatzemBuilderState(ResultSet rs) throws SQLException {
            this.id = rs.getString("id");
            this.builder = EntradaMagatzemImpl.builder()
                    .id(this.id)
                    .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                    .articleFabrica(rs.getString("article"))
                    .client(rs.getString("client"))
                    .magatzem(rs.getString("magatzem"))
                    .fabrica(rs.getString("fabrica"))
                    .quantitat(rs.getLong("quantitat"))
                    .quantitatCaixa(rs.getLong("quantitat_caixa"))
                    .dataEtiqueta(rs.getDate("data_etiqueta").toLocalDate())
                    .lot(rs.getString("lot"))
                    .of(rs.getLong("of"))
                    .etiquetaCaixa(rs.getLong("etiqueta_caixa"))
                    .etiquetaPalet(rs.getLong("etiqueta_palet"))
                    .nivellTecnic(rs.getString("nivell_tecnic"))
                    .codiSeguretat(rs.getString("codi_seguretat"))
                    .codiCal(rs.getString("codi_cal"))
                    .dataEntrada(rs.getDate("data_entrada").toLocalDate())
                    .pesPremsat(rs.getBigDecimal("pes_premsat"))
                    .pesFinal(rs.getBigDecimal("pes_final"))
                    .dataAlta(rs.getTimestamp("data_alta").toLocalDateTime())
                    .dataProcessat(MapperUtils.readOptionalLocalDateTime(rs, "data_processat"))
                    .error(Optional.ofNullable(rs.getString("error")));
        }

        void addDetall(ResultSet rs) throws  SQLException {
            var etiquetaCaixa = rs.getLong("detall_etiqueta_caixa");
            // No totes les capçaleres tenen un detall
            if (rs.wasNull())
                return;
            // S'afegeix el detall a la llista
            EntradaMagatzemDetall detall = EntradaMagatzemDetallImpl.builder()
                    .etiquetaCaixa(etiquetaCaixa)
                    .quantitat(rs.getLong("detall_quantitat"))
                    .dataEtiqueta(rs.getDate("detall_data_etiqueta").toLocalDate())
                    .lot(rs.getString("detall_lot"))
                    .of(rs.getLong("detall_of"))
                    .error(Optional.ofNullable(rs.getString("detall_error")))
                    .build();
            detalls.add(detall);
        }

        EntradaMagatzem build() {
            return builder.detalls(this.detalls).build();
        }

    }

}
