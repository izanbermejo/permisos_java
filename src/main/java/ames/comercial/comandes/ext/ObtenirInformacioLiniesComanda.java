package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.comanda.InformacioClient;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ObtenirInformacioLiniesComanda implements IObtenirInformacioLiniesComanda {

    private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public List<InformacioLiniaComandaDTO> executar(List<KeyLiniaComanda> ids) {
        if (ids.isEmpty()) return List.of();

        // PostgreSQL permet passar claus compostes en clàusules IN mitjançant la sintaxi (col1, col2) IN ((val1, val2), (val3, val4), ...)
        String placeholders = ids.stream()
                .map(id -> "(?, ?)")
                .collect(Collectors.joining(", "));
        String sql = """
                SELECT c.empresa, c.informacio_client, c.adresa, c.informacio_enviament,
                ac.codi_fabrica, ac.nivell_tecnic, ac.denominacio, ac.partida_arant_codi,
                ac.partida_arant_partida,
                ac.codi_ean13, ac.is_vol_udi, ac.dies_caducitat, ac.codi_familia,
                ac.unitats_embalatge, ac.bosses_caixa, ac.caixes_palet, ac.pes,
                COALESCE((lc.dades_calcul ->> 'descompte')::numeric, 0) AS descompte,
                lc.*
                FROM comandes.linia_comanda lc
                    LEFT JOIN "cache".cache_article_client ac ON ac.artint = lc.artint AND ac.clicod = lc.clicod
                    LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
                WHERE actual
                    AND (comanda, numero) IN ( %s )
        """.formatted(placeholders);
        Object[] params = ids.stream()
                .flatMap(id -> Stream.of(id.comanda(), id.numero()))
                .toArray();

        return jdbcAmes.query(sql, this::mapper, params);
    }

    private InformacioLiniaComandaDTO mapper (ResultSet rs, int rowNum) throws SQLException {
        // Informació client comanda
        var infoClientComanda = json.deserialize(rs.getString("informacio_client"), InformacioClient.class);
        // Adreça comanda
        Adresa adresa = json.deserialize(rs.getString("adresa"), Adresa.class);
        // Informació enviament
        InformacioEnviament informacioEnviament = json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class);
        return InformacioLiniaComandaDTOImpl.builder()
                .id(KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero")))
                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                .empresa(rs.getString("empresa"))
                .projecte(rs.getString("codi_fabrica"))
                .programa(infoClientComanda.programa())
                .comandaSegonsClient(infoClientComanda.identificador())
                .adresa(adresa)
                .informacioEnviament(informacioEnviament)
                .tipusArticleClient(TipusArticleClient.valueOf(rs.getString("tipus_article_client")))
                .referencia(rs.getString("referencia"))
                .nivellTecnic(rs.getString("nivell_tecnic"))
                .denominacio(rs.getString("denominacio"))
                .codiPartidaArantzelaria(rs.getString("partida_arant_codi"))
                .partidaArantzelaria(rs.getString("partida_arant_partida"))
                .codiEan13(Optional.ofNullable(rs.getString("codi_ean13")))
                .isVolUdi(rs.getBoolean("is_vol_udi"))
                .diesCaducitat(rs.getInt("dies_caducitat"))
                .codiFamilia(Optional.ofNullable(rs.getString("codi_familia")))
                .tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
                .quantitat(rs.getLong("quantitat"))
                .quantitatServida(rs.getLong("quantitat_servida"))
                .quantitatReservada(rs.getLong("quantitat_reservada"))
                .unitatsEmbalatge(rs.getLong("unitats_embalatge"))
                .bossesCaixa(rs.getLong("bosses_caixa"))
                .caixesPalet(rs.getLong("caixes_palet"))
                .pesUnitari(Optional.ofNullable(rs.getBigDecimal("pes")).orElse(BigDecimal.ZERO))
                .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                .isPreuFixat(rs.getBoolean("preu_fixat"))
                .descompte(Optional.ofNullable(rs.getBigDecimal("descompte")).orElse(BigDecimal.ZERO))
                .dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
                .comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
                .build();
    }

}
