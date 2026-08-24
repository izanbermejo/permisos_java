package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirLiniesComandaPendents implements IObtenirLiniesComandaPendents {

    @Autowired NamedParameterJdbcTemplate jdbc;

    @Override
    public List<ObtenirLiniesComandaPendentsResponse> executar(KeyArticleClient articleClient) {
        return executar(articleClient, false);
    }

    @Override
    public List<ObtenirLiniesComandaPendentsResponse> executar(KeyArticleClient articleClient, boolean incloureStockSeguretat) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());
        List<ObtenirLiniesComandaPendentsResponse> result = jdbc.query("""
                SELECT lc.comanda, lc.numero, c.empresa, lc.quantitat, lc.quantitat_pendent, lc.tipus, lc.data_solicitada,
                	lc.data_prevista_sortida, lc.data_prevista_sortida_interna, lc.data_confirmada_fabrica,
                	c.informacio_client->>'identificador' AS "comanda_client",
                	com.intern as "comIntern", com.client as "comClient", lc.preu, lc.divisa, lc.comanda_blanca,
                	lc.referencia, c.empresa, c.client_nom, lc.preu_fixat
                FROM comandes.linia_comanda lc
                LEFT JOIN comandes.comanda c ON c.codi = lc.comanda
                LEFT JOIN comandes.linia_comanda_comentaris com ON lc.comanda = com.comanda AND lc.numero = com.numero
                WHERE actual
                	AND quantitat_pendent > 0
                	AND lc.artint = :artint
                	AND lc.clicod = :clicod
                """, params,
                (rs, rowNum) -> ObtenirLiniesComandaPendentsResponseImpl.builder()
                        .clauLinia(KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero")))
                        .empresa(Empresa.getByClau(rs.getString("empresa")))
                        .quantitatSolicitada(rs.getLong("quantitat"))
                        .quantitatPendent(rs.getLong("quantitat_pendent"))
                        .tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
                        .dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
                        .dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
                        .dataPrevistaSortidaInterna(Optional.ofNullable(rs.getDate("data_prevista_sortida_interna"))
                                .map(Date::toLocalDate))
                        .dataConfirmadaFabrica(Optional.ofNullable(rs.getDate("data_confirmada_fabrica"))
                                .map(Date::toLocalDate))
                        .comandaClient(rs.getString("comanda_client"))
                        .comentarisInterns(Optional.ofNullable(rs.getString("comIntern")))
                        .comentarisClient(Optional.ofNullable(rs.getString("comClient")))
                        .preu(Preu.of(rs.getBigDecimal("preu"), rs.getString("divisa")))
                        .comandaBlanca(Optional.ofNullable(rs.getLong("comanda_blanca")))
                        .referencia(rs.getString("referencia"))
                        .clientNom(rs.getString("client_nom"))
                        .isPreuFixat(rs.getBoolean("preu_fixat"))
                        .build());
        // Es filtre per incloure o no les línies que son stock de seguretat (per exemple en l'EDI no cal, però
        // pel càlcul d'OFs si que cal)
        return result.stream()
                .filter(l -> !l.isStockSeguretat() || (incloureStockSeguretat && l.isStockSeguretat()))
                .toList();
    }

}
