package ames.comercial.inventari.internal.infraestructure.fitxa.mapper;

import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.domain.fitxa.FitxaImpl;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FitxaMapper implements RowMapper<Fitxa> {

    @Override
    public Fitxa mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FitxaImpl.builder()
                .id(KeyFitxa.of(
                        KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")),
                        rs.getString("empresa"),
                        rs.getString("magatzem")))
                .stock(rs.getLong("stock"))
                .stockReservat(rs.getLong("stock_reservat"))
                .isActiu(rs.getBoolean("actiu"))
                .dataCreacio(rs.getDate("data_creacio").toLocalDate())
                .build();
    }

}

