package ames.comercial.edi2.internal.infraestructure.inbox.mapper;


import ames.comercial.edi2.internal.domain.Inbox;
import ames.comercial.edi2.internal.domain.InboxImpl;
import ames.comercial.server.Json;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class InboxMapper implements RowMapper<Inbox> {

    private final Json json;

    public InboxMapper(Json json) {
        this.json = json;
    }

    @Override
    public Inbox mapRow(ResultSet rs, int rowNum) throws SQLException {
        return InboxImpl.builder()
                .missatge(rs.getString("missatge"))
                .path(rs.getString("path"))
                .pathPDF(Optional.ofNullable(rs.getString("path_pdf")))
                .dataReg(rs.getTimestamp("datareg").toLocalDateTime())
                .dataProcessat(Optional.ofNullable(rs.getTimestamp("data_processat").toLocalDateTime()))
                .id(rs.getInt("id"))
                .contingut(rs.getString("contingut"))
                .error(Optional.ofNullable(rs.getString("error")))
                .nomPdf(Optional.ofNullable(rs.getString("nom_pdf")))
                .build();
    }

}


