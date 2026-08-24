package ames.comercial.edi2.internal.infraestructure.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.*;
import ames.comercial.server.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CapsaleraEdiMapper implements RowMapper<CapsaleraEdi> {

    private Json json;
    private TypeReference<List<CT>> ctTypeReference;

    public CapsaleraEdiMapper (Json json) {
        this.json = json;
        ctTypeReference = new TypeReference<>() {};
    }

    @Override
    public CapsaleraEdi mapRow(ResultSet rs, int rowNum) throws SQLException {
        return CapsaleraEdiImpl.builder()
                .idCapsalera(rs.getLong("id_capsalera"))
                .idMissatge(rs.getLong("id_missatge"))
                .ca(json.deserialize(rs.getString("ca"), CA.class))
                .cb(rs.getString("cb") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cb"), CB.class)))
                .cc(rs.getString("cc") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cc"), CC.class)))
                .cd(rs.getString("cd") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cd"), CD.class)))
                .ci(json.deserialize(rs.getString("ci"), CI.class))
                .cp(rs.getString("cp") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cp"), CP.class)))
                .cq(rs.getString("cq") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cq"), CQ.class)))
                .cf(rs.getString("cf") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("cf"), CF.class)))
                .ct(rs.getString("ct") == null
                        ? List.of()
                        : json.deserialize(rs.getString("ct"), ctTypeReference))
                .build();
    }
}
