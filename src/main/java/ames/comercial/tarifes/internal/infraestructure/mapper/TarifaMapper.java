package ames.comercial.tarifes.internal.infraestructure.mapper;

import ames.comercial.server.Json;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import ames.comercial.tarifes.internal.domain.DadesTarifaImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TarifaMapper implements RowMapper<DadesTarifa> {

    private Json json;

    public TarifaMapper(Json json) {
        this.json = json;
    }

    @Override
    public DadesTarifa mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Dades de la comanda

        DadesTarifaImpl.Builder builder=DadesTarifaImpl.builder()
                .codi(rs.getLong("codi"))
                .nom(rs.getString("nom"))
                .divisa(rs.getString("divisa"))
                .enabled(rs.getBoolean("enabled"))
                .insertedAt(rs.getTimestamp("inserted_at").toLocalDateTime())
                .insertedBy(rs.getString("inserted_by"))
                .deleted(rs.getBoolean("deleted"))
                .status(rs.getString("status"))
                .tram01(rs.getInt("tram1"))
                .tram02(rs.getInt("tram2"))
                .tram03(rs.getInt("tram3"))
                .tram04(rs.getInt("tram4"))
                .tram05(rs.getInt("tram5"))
                .tram06(rs.getInt("tram6"))
                .tram07(rs.getInt("tram7"))
                .tram08(rs.getInt("tram8"))
                .tram09(rs.getInt("tram9"))
                .tram10(rs.getInt("tram10"))
                .tram11(rs.getInt("tram11"))
                .tram12(rs.getInt("tram12"))
                .descripcio(Optional.ofNullable(rs.getString("descripcio")).orElse(""))
                .tipusClient(Optional.ofNullable(rs.getString("tipus_client")).orElse(""))
                ;

        if ((Long) rs.getObject("vinculada") !=null)
            builder.vinculada(rs.getLong("vinculada"));

        if (rs.getTimestamp("updated_at") != null) {
            builder.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .updatedBy(rs.getString("updated_by"))
                    .updatedReason(rs.getString("updated_reason"));
        }
        if (rs.getTimestamp("deleted_at") != null) {
            builder.deletedAt(rs.getTimestamp("deleted_at").toLocalDateTime())
                    .deletedBy(rs.getString("deleted_at"));
        }
        return builder.build();
    }
}
