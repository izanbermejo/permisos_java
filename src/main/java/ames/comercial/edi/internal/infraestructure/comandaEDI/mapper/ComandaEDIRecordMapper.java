package ames.comercial.edi.internal.infraestructure.comandaEDI.mapper;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDIImpl;
import ames.comercial.server.I18N;
import ames.comercial.server.Json;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ComandaEDIRecordMapper implements RowMapper<DadesComandaEDI> {

    private Json json;

    public ComandaEDIRecordMapper(Json json) {
        this.json = json;
    }

    @Override
    public DadesComandaEDI mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Dades de la comanda
        DadesComandaEDIImpl.Builder builder = DadesComandaEDIImpl.builder()
                .codi(rs.getLong("codi"))
                .json(json.deserialize(rs.getString("json"), ComandaMissatgeEDI.class));
        if (rs.getString("path_pdf") != null)
            builder.pathPDF(rs.getString("path_pdf"));

        builder.pathEDI(rs.getString("path_edi"))
                .numeroEnviament(rs.getString("enviament_numero"))
                .missatgeTipus(rs.getString("missatge_tipus"))
                .missatgeNumero(rs.getString("missatge_numero"))
                .data(rs.getTimestamp("data").toLocalDateTime())
                .referencia(rs.getString("referencia"))
                .observacions(Optional.ofNullable(I18N.getLiteral(rs.getString("observacions"))))
                .usuariLogistica(rs.getString("usuari_logistica"))
                .codiClientAmes(rs.getString("codi_client_ames"))
                .nomClientAmes(rs.getString("nom_client_ames"))
                .deleted(rs.getBoolean("deleted"))
                .insertedBy(rs.getString("inserted_by"))
                .insertedAt(rs.getTimestamp("inserted_at").toLocalDateTime())
                .status(rs.getString("status"))
                .nad(rs.getString("nad"))
                .nadPath(rs.getString("nad_path"))
                .bustia(Optional.ofNullable(rs.getString("bustia"))).
                document(rs.getString("document"));

        if (rs.getTimestamp("updated_at") != null) {
            builder.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .updatedBy(rs.getString("updated_by"))
                    .updatedReason(rs.getString("updated_reason"));
        }

        if (rs.getTimestamp("deleted_at") != null) {
            builder.deletedAt(rs.getTimestamp("deleted_at").toLocalDateTime())
                    .deletedBy(rs.getString("deleted_by"))
                    .deletedReason(rs.getString("deleted_reason"));
        }
        builder.tipus(rs.getString("document"));

        ObtenirClientEDIAds.ClientEDIAds clientProfile = json.deserialize(rs.getString("client_profile"), ObtenirClientEDIAds.ClientEDIAds.class);
        if (clientProfile!=null)
            builder.clientProfile(clientProfile);

        return builder.build();

    }

}
