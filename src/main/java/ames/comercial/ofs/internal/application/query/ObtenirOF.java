package ames.comercial.ofs.internal.application.query;

import ames.comercial.ofs.response.ObtenirOFResponse;
import ames.comercial.ofs.response.ObtenirOFResponseImpl;
import ames.comercial.ofs.response.TerminiResponse;
import ames.comercial.ofs.response.TerminiResponseImpl;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class ObtenirOF {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    /**
     * Mètode per buscar el detall d'una OF donada pel número de l'OF
     * @param numero
     * @return detall de l'OF
     */

    public Optional<ObtenirOFResponse> get(long numero) {
        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """ 
                    SELECT of.*, t.*, t.quantitat as qtatTermini
                    FROM ofs.ordre_fabricacio of
                    LEFT JOIN ofs.termini t ON of.numero = t.numero
                    WHERE of.numero = :numero
                    ORDER BY t.data asc
                """,
                new MapSqlParameterSource("numero", numero),
                this::mapOrdreFabricacio);
    }

    private Optional<ObtenirOFResponse> mapOrdreFabricacio(ResultSet rs) throws SQLException {
        // Si no hi ha registres es retorna buit
        if (!rs.next()) return  Optional.empty();

        // Lectura de la capçalera a la primera fila
        var ofBuilder = ObtenirOFResponseImpl.builder();
        ofBuilder.numero(rs.getLong("numero"));
        ofBuilder.articleClient(KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")));
        ofBuilder.fabrica(rs.getString("fabrica"));
        ofBuilder.dataEmissio(rs.getDate("data_emissio").toLocalDate());
        ofBuilder.ofAnterior(MapperUtils.readOptionalLong(rs, "of_anterior"));
        ofBuilder.ofPosterior(MapperUtils.readOptionalLong(rs, "of_posterior"));
        ofBuilder.quantitatRebudaEntrades(rs.getLong("quantitat_rebuda_entrades"));
        ofBuilder.quantitatRebudaEntradesAcumulatTotal(rs.getLong("quantitat_rebuda_entrades_total"));
        ofBuilder.ultimaQuantitatRebudaAbansCreacio(rs.getLong("ultima_quantitat_rebuda_abans_creacio"));
        ofBuilder.dataUltimaQuantitatRebudaAbansCreacio(MapperUtils.readOptionalDate(rs,"data_ultima_quantitat_rebuda_abans_creacio"));
        ofBuilder.ultimaQuantitatRebuda(rs.getLong("ultima_quantitat_rebuda"));
        ofBuilder.dataUltimaQuantitatRebuda(MapperUtils.readOptionalDate(rs,"data_ultima_quantitat_rebuda"));
        ofBuilder.dataAnulacio(MapperUtils.readOptionalDate(rs, "data_anulacio"));
        ofBuilder.increment(rs.getBoolean("existeix_increment"));
        ofBuilder.canviFabrica(rs.getBoolean("canvi_fabrica"));
        ofBuilder.diesCalculIncrement(rs.getInt("dies_calcul_increment"));
        long acumulatActual = 0L;
        long acumulatAnterior = 0L;
        var lisTerminis = new ArrayList<TerminiResponse>();
        do {
            long quantitat = rs.getLong("qtatTermini");
            long quantitatAnterior = rs.getLong("quantitat_anterior");
            acumulatActual += quantitat;
            acumulatAnterior += quantitatAnterior;
            lisTerminis.add(TerminiResponseImpl.builder()
                    .quantitat(quantitat)
                    .quantitatAnterior(quantitatAnterior)
                    .quantitatRebuda(rs.getLong("quantitat_rebuda"))
                    .data(rs.getDate("data").toLocalDate())
                    .dataSortida(rs.getDate("data_sortida").toLocalDate())
                    .isStockSeguretat(rs.getBoolean("is_stock_seguretat"))
                    .acumulatActual(acumulatActual)
                    .acumulatAnterior(acumulatAnterior)
                    .build());
        } while (rs.next());
        ofBuilder.terminis(lisTerminis);
        return Optional.of(ofBuilder.build());
    }

}