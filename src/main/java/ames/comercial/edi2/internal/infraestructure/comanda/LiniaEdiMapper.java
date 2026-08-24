package ames.comercial.edi2.internal.infraestructure.comanda;

import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdiImpl;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.linia.bloc.DAImpl;
import ames.comercial.edi2.internal.domain.linia.bloc.DR;
import ames.comercial.edi2.internal.domain.linia.bloc.DRImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public class LiniaEdiMapper implements RowMapper<LiniaEdi> {

    @Override
    public LiniaEdi mapRow(ResultSet rs, int rowNum) throws SQLException {

        return LiniaEdiImpl.builder()
                .idComanda(rs.getLong("id_comanda"))
                .idLinia(rs.getLong("id_linia"))
                .comentarisInterns(Optional.ofNullable(rs.getString("comentari_intern")))
                .comentarisClient(Optional.ofNullable(rs.getString("comentari_client")))
                .da(mapDA(rs))
                .dr(mapDR(rs))
                .build();
    }

    private DA mapDA(ResultSet rs) throws SQLException {
        return DAImpl.builder()
                .inicioRegistro("0")
                .tipoDetalle(rs.getString("da_tipo_detalle"))
                .cantidad(rs.getLong("da_cantidad"))
                .unidadMedida(rs.getString("da_unidad_medida"))
                .fechaInicial(Optional.ofNullable(rs.getObject("da_fecha_inicial", LocalDate.class)))
                .horaInicial(Optional.ofNullable(rs.getObject("da_hora_inicial", LocalTime.class)))
                .fechaFinal(Optional.ofNullable(rs.getObject("da_fecha_final", LocalDate.class)))
                .horaFinal(Optional.ofNullable(rs.getObject("da_hora_final", LocalTime.class)))
                .razonInstruccion(Optional.ofNullable(rs.getString("da_razon_instruccion")))
                .numeroRan(Optional.ofNullable(rs.getString("da_numero_ran")))
                .fechaRan(Optional.ofNullable(rs.getObject("da_fecha_ran", LocalDate.class)))
                .frecuenciaEnvio(Optional.ofNullable(rs.getString("da_frecuencia_envio")))
                .numTarjetaKanban(Optional.ofNullable(rs.getString("da_num_tarjeta_kanban")))
                .ultimoNumeroRanEmitido(Optional.ofNullable(rs.getString("da_ultimo_numero_ran_emitido")))
                .build();
    }

    private Optional<DR> mapDR(ResultSet rs) throws SQLException{
        var fechaEntrada = Optional.ofNullable(rs.getObject("dr_fecha_entrada", LocalDate.class));
        var horaEntrada = Optional.ofNullable(rs.getObject("dr_hora_entrada", LocalTime.class));

        // Com que el bloc DR és opcional i no sempre s'informa. Donem per fet que s'ha informat
        // quan algun dels dos camps ho està
        if (fechaEntrada.isPresent() || horaEntrada.isPresent())
            return Optional.of(DRImpl.builder()
                    .fechaEntradaLinea(fechaEntrada)
                    .horaEntradaLinea(horaEntrada)
                    .build());
        // Si cap dels dos camps no està vol dir que no venia el DR
        return Optional.empty();
    }
}
