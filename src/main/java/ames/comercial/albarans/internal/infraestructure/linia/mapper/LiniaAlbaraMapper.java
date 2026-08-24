package ames.comercial.albarans.internal.infraestructure.linia.mapper;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.*;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LiniaAlbaraMapper implements RowMapper<LiniaAlbara> {

    @Override
    public LiniaAlbara mapRow(ResultSet rs, int rowNum) throws SQLException {
        Optional<InformacioComanda> infoComanda = mapInformacioComanda(rs);
        InformacioPesa infoPesa = mapInformacioPesa(rs);

        return LiniaAlbaraImpl.builder()
                .id(KeyLiniaAlbara.of(
                        KeyAlbara.of(rs.getLong("codi_albara"), rs.getString("empresa")),
                        rs.getLong("linia")
                ))
                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                .infoComanda(infoComanda)
                .informacioPesa(infoPesa)
                .quantitat(rs.getLong("quantitat"))
                .quantitatPendentFacturar(rs.getLong("quantitat_pendent_facturar"))
                .quantitatPendentConsumir(rs.getLong("quantitat_pendent_consumir"))
                .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                .isPreuFixat(rs.getBoolean("is_preu_fixat"))
                // Les línies anteriors a la columna (i les de traspàs i consum) no en tenen: queden a 0
                .descompte(Optional.ofNullable(rs.getBigDecimal("descompte")).orElse(BigDecimal.ZERO))
                .comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
                .observacionsImpressio(Optional.ofNullable(rs.getString("observacions_impressio")))
                .observacionsInternes(Optional.ofNullable(rs.getString("observacions_internes")))
                .codiEmbalatge(Optional.ofNullable(rs.getString("codi_embalatge")))
                .identificadorConsum(Optional.ofNullable(rs.getString("identificador_consum")))
                .isUrgent(rs.getBoolean("is_urgent"))
                .build();
    }

    private Optional<InformacioComanda> mapInformacioComanda(ResultSet rs) throws SQLException {
        var comanda = rs.getLong("comanda");
        if (rs.wasNull()) {
            return Optional.empty();
        }
        return Optional.of(InformacioComandaImpl.builder()
                .comanda(comanda)
                .comandaClient(rs.getString("comanda_client"))
                .programa(rs.getString("programa"))
                .build());
    }

    private InformacioPesa mapInformacioPesa(ResultSet rs) throws SQLException {
        return InformacioPesaImpl.builder()
                .matriu(rs.getString("pesa_matriu"))
                .referencia(rs.getString("pesa_referencia"))
                .nivellTecnic(rs.getString("pesa_nivell_tecnic"))
                .denominacio(rs.getString("pesa_denominacio"))
                .codiPartidaArantzelaria(rs.getString("pesa_codi_partida_arantzelaria"))
                .partidaArantzelaria(rs.getString("pesa_partida_arantzelaria"))
                .codiEan13(Optional.ofNullable(rs.getString("pesa_codi_ean13")))
                .isVolUdi(rs.getBoolean("pesa_is_vol_udi"))
                .diesCaducitat(rs.getInt("pesa_dies_caducitat"))
                .codiFamilia(Optional.ofNullable(rs.getString("pesa_codi_familia")))
                // Les línies migrades poden no tenir-los informats, per això es tracta el NULL
                .pesUnitari(Optional.ofNullable(rs.getBigDecimal("pesa_pes_unitari")).orElse(BigDecimal.ZERO))
                .unitatsEmbalatge(rs.getLong("pesa_unitats_embalatge"))
                .bossesCaixa(rs.getLong("pesa_bosses_caixa"))
                .caixesPalet(rs.getLong("pesa_caixes_palet"))
                .build();
    }

}

