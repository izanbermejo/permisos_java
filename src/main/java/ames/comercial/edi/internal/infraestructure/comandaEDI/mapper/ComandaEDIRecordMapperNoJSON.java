package ames.comercial.edi.internal.infraestructure.comandaEDI.mapper;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSONImpl;
import ames.comercial.server.I18N;
import ames.comercial.server.Json;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ComandaEDIRecordMapperNoJSON implements RowMapper<DadesComandaEDINoJSON> {

    private Json json;

    public ComandaEDIRecordMapperNoJSON(Json json) {
        this.json = json;
    }

    @Override
    public DadesComandaEDINoJSON mapRow(ResultSet rs, int rowNum) throws SQLException {
        // Dades de la comanda
        DadesComandaEDINoJSONImpl.Builder builder = DadesComandaEDINoJSONImpl.builder()
                .codi(rs.getLong("codi"));
//				.json(json.deserialize(rs.getString("json"), Pedido.class))
        if (rs.getString("path_pdf") != null)
            builder.pathPDF(rs.getString("path_pdf"));
        builder.pathEDI(rs.getString("path_edi"))
                .numeroEnviament(rs.getString("enviament_numero"))
                .missatgeTipus(rs.getString("missatge_tipus"))
                .missatgeNumero(rs.getString("missatge_numero"))
                .data(rs.getTimestamp("data").toLocalDateTime())
                .referencia(rs.getString("referencia"));
        if (rs.getString("observacions").startsWith("edi.comanda.observacions"))
            builder.observacions(Optional.ofNullable(I18N.getLiteral(rs.getString("observacions"))));
        else
            builder.observacions(Optional.ofNullable(rs.getString("observacions")));
        builder.usuariLogistica(rs.getString("usuari_logistica"))
                .codiClientAmes(rs.getString("codi_client_ames"))
                .nomClientAmes(rs.getString("nom_client_ames"))
                .deleted(rs.getBoolean("deleted"))
                .insertedBy(rs.getString("inserted_by"))
                .insertedAt(rs.getTimestamp("inserted_at").toLocalDateTime())
                .status(rs.getString("status"))
                .nad(rs.getString("nad"))
                .nadPath(rs.getString("nad_path"))
                .tipus(rs.getString("document"))
                .bustia(rs.getString("bustia"));

        if (rs.getTimestamp("updated_at") != null) {
            builder.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .updatedBy(rs.getString("updated_by"))
                    .updatedReason(rs.getString("updated_reason"));
        }

        if (rs.getTimestamp("deleted_at") != null) {
            builder.updatedAt(rs.getTimestamp("deleted_at").toLocalDateTime())
                    .updatedBy(rs.getString("deleted_by"))
                    .updatedReason(rs.getString("deleted_reason"));
        }
        builder.document(rs.getString("document"));
        if (rs.getString("client_profile") != null && !rs.getString("client_profile").equals("null"))
            builder.clientProfile(json.deserialize(rs.getString("client_profile"), ObtenirClientEDIAds.ClientEDIAds.class));
        return builder.build();
    }
}
