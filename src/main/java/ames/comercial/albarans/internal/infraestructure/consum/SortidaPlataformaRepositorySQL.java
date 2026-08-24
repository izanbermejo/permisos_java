package ames.comercial.albarans.internal.infraestructure.consum;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.consum.SortidaPlataforma;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class SortidaPlataformaRepositorySQL implements SortidaPlataformaRepository {

    private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    private final RowMapper<SortidaPlataforma> mapper = (rs, rowNum) -> new SortidaPlataforma(
            KeyLiniaAlbara.of(rs.getString("albconsum_empresa"), Long.parseLong(rs.getString("albconsum_codi")), rs.getLong("albconsum_linia")),
            rs.getString("albconsum_magatzem"),
            rs.getDate("albconsum_data").toLocalDate(),
            KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")),
            rs.getLong("quantitat"),
            KeyLiniaAlbara.of(rs.getString("albtraspas_empresa"), Long.parseLong(rs.getString("albtraspas_codi")), rs.getLong("albtraspas_linia")),
            rs.getString("albtraspas_magatzem"),
            rs.getDate("albtraspas_data").toLocalDate(),
            rs.getLong("albtraspas_quantitat"));

    @Override
    public void save(SortidaPlataforma s) {
        jdbcAmes.update(
                """
                INSERT INTO albarans.sortides_plataforma (
                    albconsum_empresa, albconsum_codi, albconsum_linia, albconsum_magatzem, albconsum_data,
                    artint, clicod, quantitat,
                    albtraspas_empresa, albtraspas_codi, albtraspas_linia, albtraspas_magatzem, albtraspas_data,
                    albtraspas_quantitat
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                s.albaraConsum().idAlbara().empresa(),
                String.valueOf(s.albaraConsum().idAlbara().codi()),
                s.albaraConsum().linia(),
                s.magatzemConsum(),
                s.dataConsum(),
                s.articleClient().artint(),
                s.articleClient().clicod(),
                s.quantitat(),
                s.albaraTraspas().idAlbara().empresa(),
                String.valueOf(s.albaraTraspas().idAlbara().codi()),
                s.albaraTraspas().linia(),
                s.magatzemTraspas(),
                s.dataTraspas(),
                s.albtraspasQuantitat()
        );
    }

    @Override
    public void updateDataConsum(KeyAlbara albaraConsum, LocalDate dataConsum) {
        jdbcAmes.update(
                """
                UPDATE albarans.sortides_plataforma SET albconsum_data = ?
                WHERE albconsum_empresa = ? AND albconsum_codi = ?
                """,
                dataConsum,
                albaraConsum.empresa(),
                String.valueOf(albaraConsum.codi()));
    }

    @Override
    public void updateDataTraspas(KeyAlbara albaraTraspas, LocalDate dataTraspas) {
        jdbcAmes.update(
                """
                UPDATE albarans.sortides_plataforma SET albtraspas_data = ?
                WHERE albtraspas_empresa = ? AND albtraspas_codi = ?
                """,
                dataTraspas,
                albaraTraspas.empresa(),
                String.valueOf(albaraTraspas.codi()));
    }

    @Override
    public List<SortidaPlataforma> findByAlbaraConsum(KeyAlbara albaraConsum) {
        return jdbcAmes.query(
                "SELECT * FROM albarans.sortides_plataforma WHERE albconsum_empresa = ? AND albconsum_codi = ?",
                mapper,
                albaraConsum.empresa(),
                String.valueOf(albaraConsum.codi()));
    }

    @Override
    public List<SortidaPlataforma> findByLiniaConsum(KeyLiniaAlbara liniaConsum) {
        return jdbcAmes.query(
                "SELECT * FROM albarans.sortides_plataforma WHERE albconsum_empresa = ? AND albconsum_codi = ? AND albconsum_linia = ?",
                mapper,
                liniaConsum.idAlbara().empresa(),
                String.valueOf(liniaConsum.idAlbara().codi()),
                liniaConsum.linia());
    }

    @Override
    public void deleteByAlbaraConsum(KeyAlbara albaraConsum) {
        jdbcAmes.update(
                "DELETE FROM albarans.sortides_plataforma WHERE albconsum_empresa = ? AND albconsum_codi = ?",
                albaraConsum.empresa(),
                String.valueOf(albaraConsum.codi()));
    }

    @Override
    public void deleteByLiniaConsum(KeyLiniaAlbara liniaConsum) {
        jdbcAmes.update(
                "DELETE FROM albarans.sortides_plataforma WHERE albconsum_empresa = ? AND albconsum_codi = ? AND albconsum_linia = ?",
                liniaConsum.idAlbara().empresa(),
                String.valueOf(liniaConsum.idAlbara().codi()),
                liniaConsum.linia());
    }

}
