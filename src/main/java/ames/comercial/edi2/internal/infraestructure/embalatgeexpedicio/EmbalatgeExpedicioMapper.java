package ames.comercial.edi2.internal.infraestructure.embalatgeexpedicio;

import ames.comercial.edi2.internal.domain.embalatgeexpedicio.*;
import ames.comercial.server.Json;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class EmbalatgeExpedicioMapper implements ResultSetExtractor<EmbalatgeExpedicio> {

    private final Json json;

    public EmbalatgeExpedicioMapper(Json json) {
        this.json = json;
    }

    @Override
    public EmbalatgeExpedicio extractData(ResultSet rs) throws SQLException {
        EmbalatgeExpedicioImpl.Builder builder = EmbalatgeExpedicioImpl.builder();

        while (rs.next()) {

            ElementEmbalatgeExpedicio element = ElementEmbalatgeExpedicioImpl.builder()
                    .tipus(TipusElementEmbalatgeExpedicio.fromNivell(rs.getInt("nivell")))
                    .isRetornable(rs.getBoolean("is_retornable"))
                    .referencia(rs.getString("referencia"))
                    .descripcio(rs.getString("descripcio"))
                    .codiElement(rs.getString("codi_element"))
                    .factorMultiplicador(Optional.ofNullable(rs.getObject("factor_multiplicador", Integer.class)))
                    .numElementsFixes(Optional.ofNullable(rs.getObject("elements_fixes", Integer.class)))
                    .build();

            boolean retornable = rs.getBoolean("is_retornable");
            int nivell = rs.getInt("nivell");

            switch (nivell) {
                case 1 -> {
                    if (retornable) builder.nivell1Retornable(element);
                    else builder.nivell1NoRetornable(element);
                }
                case 2 -> {
                    if (retornable) builder.nivell2Retornable(element);
                    else builder.nivell2NoRetornable(element);
                }
                case 3 -> {
                    if (retornable) builder.nivell3Retornable(element);
                    else builder.nivell3NoRetornable(element);
                }
                case 4 -> {
                    if (retornable) builder.nivell4Retornable(element);
                    else builder.nivell4NoRetornable(element);
                }
                case 5 -> {
                    if (retornable) builder.nivell5Retornable(element);
                    else builder.nivell5NoRetornable(element);
                }
            }
        }

        return builder.build();
    }
}
