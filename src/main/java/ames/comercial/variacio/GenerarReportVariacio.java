package ames.comercial.variacio;

import ames.comercial.shared.Divisa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GenerarReportVariacio {

    @Autowired NamedParameterJdbcTemplate jdbc;

    public byte[] executar (LocalDate dataInici, LocalDate dataFi, List<String> fabriques, Divisa divisa,
                            LocalDate dataSolicitadaInici, LocalDate dataSolicitadaFi) {
        try {
            ReportVariacioXls report = new ReportVariacioXls(jdbc);
            return report.generar(dataInici, dataFi, divisa, fabriques, dataSolicitadaInici, dataSolicitadaFi);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
