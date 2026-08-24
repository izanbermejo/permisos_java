package ames.comercial.inventari.internal.application.query;

import ames.comercial.inventari.ext.IObtenirStockMagatzemsIntermig;
import ames.comercial.inventari.ext.StockMagatzemIntermigResponseImpl;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ObtenirStockMagatzemsIntermig implements IObtenirStockMagatzemsIntermig {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public List<StockMagatzemIntermigResponse> perArticleClient(KeyArticleClient articleClient) {
        return query("f.artint = ? AND f.clicod = ?",
                new Object[]{articleClient.artint(), articleClient.clicod()});
    }

    @Override
    public List<StockMagatzemIntermigResponse> perClients(List<String> clicods) {
        if (clicods.isEmpty()) {
            return List.of();
        }
        var placeholders = String.join(",", clicods.stream().map(c -> "?").toList());
        return query("f.clicod IN (%s)".formatted(placeholders), clicods.toArray());
    }

    /**
     * Retorna, per cada (empresa, magatzem), l'stock propi i l'stock que té en trànsit als seus
     * magatzems intermitjos. La clau és el LEFT JOIN amb {@code com_magatzem.magatzems_intermitjos}:
     * <ul>
     *   <li>La CTE {@code intermig_agg} suma, per cada magatzem FINAL, l'stock de tots els seus
     *       magatzems intermitjos (files de fitxa el magatzem de les quals és un {@code intermig}).</li>
     *   <li>El FULL OUTER JOIN garanteix que també apareix un magatzem final que no té stock propi
     *       però sí que en té en trànsit al seu intermig.</li>
     *   <li>{@code is_intermig} marca les files que corresponen a un magatzem intermig, perquè els
     *       consumidors que no vulguin comptar-lo dos cops el puguin descartar.</li>
     * </ul>
     * El filtre final {@code stock_propi <> 0 OR stock_intermig <> 0} només retorna magatzems amb
     * stock (propi o en trànsit al seu intermig), evitant files a 0 que només afegirien soroll.
     *
     * @param filtreFitxa condició SQL sobre l'àlies {@code f} (fitxa) — p. ex. per article/client.
     * @param params      paràmetres posicionals de la condició, en ordre.
     */
    private List<StockMagatzemIntermigResponse> query(String filtreFitxa, Object[] params) {
        var sql = """
            WITH fitxa_f AS (
                SELECT f.artint, f.clicod, f.empresa, f.magatzem, f.stock, f.stock_reservat
                FROM inventari.fitxa f
                WHERE %s
            ),
            intermig_agg AS (
                SELECT ff.artint,
                       ff.clicod,
                       ff.empresa,
                       mi.final AS magatzem,
                       SUM(ff.stock) AS stock_intermig,
                       string_agg(DISTINCT mi.intermig, ',') AS magatzems_intermitjos
                FROM com_magatzem.magatzems_intermitjos mi
                JOIN fitxa_f ff ON ff.magatzem = mi.intermig
                GROUP BY ff.artint, ff.clicod, ff.empresa, mi.final
            )
            SELECT COALESCE(f.artint, ia.artint)     AS artint,
                   COALESCE(f.clicod, ia.clicod)     AS clicod,
                   COALESCE(f.empresa, ia.empresa)   AS empresa,
                   COALESCE(f.magatzem, ia.magatzem) AS magatzem,
                   COALESCE(f.stock, 0)              AS stock_propi,
                   COALESCE(f.stock_reservat, 0)     AS stock_reservat,
                   (mi_self.intermig IS NOT NULL)    AS is_intermig,
                   COALESCE(ia.stock_intermig, 0)    AS stock_intermig,
                   ia.magatzems_intermitjos          AS magatzems_intermitjos
            FROM fitxa_f f
            FULL OUTER JOIN intermig_agg ia
                ON  ia.artint   = f.artint
                AND ia.clicod   = f.clicod
                AND ia.empresa  = f.empresa
                AND ia.magatzem = f.magatzem
            LEFT JOIN com_magatzem.magatzems_intermitjos mi_self
                ON mi_self.intermig = COALESCE(f.magatzem, ia.magatzem)
            WHERE COALESCE(f.stock, 0) <> 0 OR COALESCE(ia.stock_intermig, 0) <> 0
            """.formatted(filtreFitxa);

        return jdbcAmes.query(sql, (rs, i) -> {
            var magatzemsIntermitjos = rs.getString("magatzems_intermitjos");
            return StockMagatzemIntermigResponseImpl.builder()
                    .artint(rs.getString("artint"))
                    .clicod(rs.getString("clicod"))
                    .empresa(rs.getString("empresa"))
                    .magatzem(rs.getString("magatzem"))
                    .stockPropi(rs.getLong("stock_propi"))
                    .stockReservat(rs.getLong("stock_reservat"))
                    .isIntermig(rs.getBoolean("is_intermig"))
                    .addAllMagatzemsIntermitjos(magatzemsIntermitjos == null
                            ? List.of()
                            : Arrays.asList(magatzemsIntermitjos.split(",")))
                    .stockIntermig(rs.getLong("stock_intermig"))
                    .build();
        }, params);
    }

}
