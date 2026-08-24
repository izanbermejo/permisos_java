package ames.comercial.edi2.internal.infraestructure.comanda;

import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi.Estat;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdiImpl;
import ames.comercial.edi2.internal.domain.comanda.bloc.*;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.server.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ComandaEdiMapper implements RowMapper<ComandaEdi> {

    private final Json json;
    private List<LiniaEdi> linies;

    public ComandaEdiMapper(Json json, List<LiniaEdi> linies) {
        this.json = json;
        this.linies = linies;
    }

    public ComandaEdiMapper(Json json) {
        //constructor a esborrar quan la part de pasar comandes del sergi s'esborri.
        this.json = json;
        this.linies = List.of();
    }

    @Override
    public ComandaEdi mapRow(ResultSet rs, int rowNum) throws SQLException {

        return ComandaEdiImpl.builder()
                .idMissatge(rs.getLong("id_missatge"))
                .idCapsalera(rs.getLong("id_capsalera"))
                .idComanda(rs.getLong("id_comanda"))
                .artInt(Optional.ofNullable(rs.getString("artint")))
                .cliCod(Optional.ofNullable(rs.getString("clicod")))
                .la(json.deserialize(rs.getString("la"), LA.class))
                .lb(rs.getString("lb") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("lb"), LB.class)))
                .lc(json.deserialize(rs.getString("lc"), LC.class))
                .ld(rs.getString("ld") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("ld"), LD.class)))
                .ls(rs.getString("ls") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("ls"), LS.class)))
                .le(rs.getString("le") == null
                        ? List.of()
                        : json.deserialize(rs.getString("le"), new TypeReference<List<LE>>() {}))
                .lt(rs.getString("lt") == null
                        ? List.of()
                        : json.deserialize(rs.getString("lt"), new TypeReference<List<LT>>() {}))
                .lg(rs.getString("lg") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("lg"), LG.class)))
                .lh(rs.getString("lh") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("lh"), LH.class)))
                .li(rs.getString("li") == null
                        ? Optional.empty()
                        : Optional.of(json.deserialize(rs.getString("li"), LI.class)))
                .ll(rs.getString("ll") == null
                        ? List.of()
                        : json.deserialize(rs.getString("ll"), new TypeReference<List<LL>>() {}))
                .lq(rs.getString("lq") == null
                        ? List.of()
                        : json.deserialize(rs.getString("lq"), new TypeReference<List<LQ>>() {}))
                .aa(rs.getString("aa") == null
                        ? List.of()
                        : json.deserialize(rs.getString("aa"), new TypeReference<List<AA>>() {}))
                .linies(linies.isEmpty() ? List.of() : linies)
                .estat(Estat.valueOf(rs.getString("estat")))
                .error(Optional.ofNullable(rs.getString("error")))
                .build();
    }
}
