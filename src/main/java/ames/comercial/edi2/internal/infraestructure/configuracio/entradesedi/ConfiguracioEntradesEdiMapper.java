package ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi;

import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComandaImpl;
import ames.comercial.entrades.internal.domain.InformacioSortidaEdi;
import ames.comercial.entrades.internal.domain.InformacioSortidaEdiImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ConfiguracioEntradesEdiMapper implements RowMapper<ConfiguracioEntradaComanda> {


    public ConfiguracioEntradesEdiMapper() {}

    @Override
    public ConfiguracioEntradaComanda mapRow(ResultSet rs, int rowNum) throws SQLException {
        Array arrayLlocEntrega = rs.getArray("lloc_entrega");

        List<String> llocsEntrega = Collections.emptyList();

        if (arrayLlocEntrega != null) {
            llocsEntrega = Arrays.stream((Object[]) arrayLlocEntrega.getArray())
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .toList();
        }

        var builder = ConfiguracioEntradaComandaImpl.builder()
                .codiClient(rs.getString("codi_client"))
                .tipusMissatge(rs.getString("tipus_missatge"))
                .fermOrientatiu(rs.getString("ferm_orientatiu"))
                .ediBox(rs.getString("edibox"))
                .nad02(rs.getString("nad02"))
                .codiProveidor(rs.getString("codi_proveidor"))
                .informacioSortida(mapSortida(rs))
                .considerarAlbarans(rs.getBoolean("considerar_albarans"))
                .considerarDuesDates(rs.getBoolean("considerar_dues_dates"))
                .estrategiaEdi(rs.getInt("estrategia_edi"))
                .diesTall(rs.getInt("dies_tall"))
                .llocsEntrega((llocsEntrega))
                .tipusDocumentEdi(Optional.ofNullable(rs.getString("tipus_document_edi")))
                .comentaris(Optional.ofNullable(rs.getString("comentaris")))
                .isActiu(rs.getBoolean("is_actiu"));

        return builder.build();
    }

    private InformacioSortidaEdi mapSortida(ResultSet rs) throws SQLException {
        int diesRestar = rs.getInt("dies_restar");
        Array dies_sortida = rs.getArray("dies_sortida");
        List<Integer> diesSortida = Collections.emptyList();

        if (dies_sortida != null) {
            diesSortida = Arrays.stream((Integer[]) dies_sortida.getArray()).filter(Objects::nonNull).toList();
        }

        return InformacioSortidaEdiImpl.builder()
                .diesRestar(diesRestar)
                .diesSortida(diesSortida)
                .build();
    }
}
