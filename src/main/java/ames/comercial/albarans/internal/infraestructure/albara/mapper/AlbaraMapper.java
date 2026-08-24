package ames.comercial.albarans.internal.infraestructure.albara.mapper;

import ames.comercial.albarans.internal.domain.albara.*;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AlbaraMapper implements RowMapper<Albara> {

    private final Json json;

    public AlbaraMapper(Json json) {
        this.json = json;
    }

    @Override
    public Albara mapRow(ResultSet rs, int rowNum) throws SQLException {
        return AlbaraImpl.builder()
                .id(KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa")))
                .tipus(TipusAlbara.valueOf(rs.getString("tipus")))
                .client(MapperUtils.readOptionalString(rs, "client"))
                .data(rs.getDate("data").toLocalDate())
                .magatzem(rs.getString("magatzem"))
                .adresa(json.deserialize(rs.getString("adresa"), Adresa.class))
                .informacioEnviament(json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class))
                .destiAlternatiu(Optional.ofNullable(rs.getString("desti_alternatiu")))
                .adresaBroker(Optional.ofNullable(json.deserialize(rs.getString("adresa_broker"), Adresa.class)))
                .adresaFacturaProforma(Optional.ofNullable(json.deserialize(rs.getString("adresa_factura_proforma"), Adresa.class)))
                .informacioMagatzem(json.deserialize(rs.getString("informacio_magatzem"), InformacioMagatzem.class))
                .informacioTraspas(Optional.ofNullable(json.deserialize(rs.getString("informacio_traspas"), InformacioTraspas.class)))
                .informacioEdi(Optional.ofNullable(json.deserialize(rs.getString("informacio_edi"), InformacioEdi.class)))
                .costLogistic(Optional.ofNullable(json.deserialize(rs.getString("cost_logistic"), CostLogistic.class)))
                .costTransport(Optional.ofNullable(json.deserialize(rs.getString("cost_transport"), CostTransport.class)))
                .costEnviamentExpress(Optional.ofNullable(json.deserialize(rs.getString("cost_enviament_express"), CostEnviamentExpress.class)))
                .observacionsImpressio(Optional.ofNullable(rs.getString("observacions_impressio")))
                .observacionsInternes(Optional.ofNullable(rs.getString("observacions_internes")))
                .observacionsProforma(Optional.ofNullable(rs.getString("observacions_proforma")))
                .dataCreacio(rs.getDate("data_creacio").toLocalDate())
                .usuariCreacio(rs.getString("usuari_creacio"))
                .numeroProveidor(Optional.ofNullable(rs.getString("numero_proveidor")))
                .numeroAlbaraEspecial(Optional.ofNullable(rs.getString("numero_albara_especial")))
                .isTancat(rs.getBoolean("is_tancat"))
                .isFacturat(rs.getBoolean("is_facturat"))
                .isFacturacioAutomatica(rs.getBoolean("is_facturacio_automatica"))
                .isEnviatEmail(rs.getBoolean("is_enviat_email"))
                .isNormalitzats(rs.getBoolean("is_normalitzats"))
                .isCalPagarPorts(rs.getBoolean("is_cal_pagar_ports"))
                .isNoValorat(rs.getBoolean("is_no_valorat"))
                .isEnviatHisenda(rs.getBoolean("is_enviat_hisenda"))
                .numeroFacturaTransport(Optional.ofNullable(rs.getString("numero_factura_transport")))
                .numeroCaixes(Optional.ofNullable(rs.getObject("numero_caixes", Long.class)))
                .alsadaCaixes(Optional.ofNullable(rs.getString("alsada_caixes")))
                .costMoq(Optional.ofNullable(rs.getBigDecimal("cost_moq")))
                .referenciaTransport(Optional.ofNullable(rs.getString("referencia_transport")))
                .isUrgent(rs.getBoolean("is_urgent"))
                .build();
    }

}

