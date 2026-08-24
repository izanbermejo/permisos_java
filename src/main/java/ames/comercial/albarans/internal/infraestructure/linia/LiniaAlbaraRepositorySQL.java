package ames.comercial.albarans.internal.infraestructure.linia;

import ames.comercial.albarans.AlbaransException.LiniaAlbaraChanged;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.linia.mapper.LiniaAlbaraMapper;
import ames.comercial.server.ETag;
import ames.comercial.server.RequestThread;
import ames.comercial.server.RequestThread.Entity;
import ames.comercial.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class LiniaAlbaraRepositorySQL implements LiniaAlbaraRepository {

    private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public long nextNumero(KeyAlbara idAlbara) {
        return jdbcAmes.queryForObject(
                "SELECT coalesce(MAX(linia),0)+1 FROM albarans.linia_albara WHERE codi_albara = ? AND empresa = ?",
                Long.class,
                idAlbara.codi(),
                idAlbara.empresa()
        );
    }

    @Override
    public void save(LiniaAlbara linia) {
        checkEtag(linia.id());

        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("albarans")
                .withTableName("linia_albara")
                .onConflictColumns("codi_albara", "empresa", "linia")
                .execute(params(linia));
    }

    @Override
    public void save(List<LiniaAlbara> linies) {
        linies.forEach(this::save);
    }

    /**
     * TODO: Eliminar després de la migració
     */
    @SuppressWarnings("unchecked")
    public void saveBatch(List<LiniaAlbara> linies) {
        if (linies.isEmpty()) {
            return;
        }
        var insertBatch = new org.springframework.jdbc.core.simple.SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("albarans")
                .withTableName("linia_albara");
        // Processar en lotes per evitar problemes de memòria
        for (int i = 0; i < linies.size(); i += 2000) {
            int end = Math.min(i + 2000, linies.size());
            var subList = linies.subList(i, end);
            var batchParams = subList.stream()
                    .map(this::params)
                    .toArray(HashMap[]::new);
            insertBatch.executeBatch(batchParams);
        }
    }

    private HashMap<String, Object> params(LiniaAlbara l) {
        var params = new HashMap<String, Object>();
        //var newEtag = ETag.generate();
        // TODO Eliminar després de migració, només necessari per mantenir la data de registre original en les línies migrades
        var newEtag = l.dataregMigracio().isPresent()
                ? ETag.of(l.dataregMigracio().get().atStartOfDay(), "[MIGRACIO]", l.usuregMigracio().orElse("[MIGRACIO]"))
                : ETag.generate();

        params.put("codi_albara", l.id().idAlbara().codi());
        params.put("empresa", l.id().idAlbara().empresa());
        params.put("linia", l.id().linia());
        params.put("artint", l.articleClient().artint());
        params.put("clicod", l.articleClient().clicod());

        params.put("comanda", l.infoComanda().flatMap(InformacioComanda::comanda).orElse(null));
        params.put("comanda_client", l.infoComanda().map(InformacioComanda::comandaClient).orElse(null));
        params.put("programa", l.infoComanda().map(InformacioComanda::programa).orElse(null));

        params.put("pesa_matriu", l.informacioPesa().matriu());
        params.put("pesa_referencia", l.informacioPesa().referencia());
        params.put("pesa_nivell_tecnic", l.informacioPesa().nivellTecnic());
        params.put("pesa_denominacio", l.informacioPesa().denominacio());
        params.put("pesa_codi_partida_arantzelaria", l.informacioPesa().codiPartidaArantzelaria());
        params.put("pesa_partida_arantzelaria", l.informacioPesa().partidaArantzelaria());
        params.put("pesa_codi_ean13", l.informacioPesa().codiEan13().orElse(null));
        params.put("pesa_is_vol_udi", l.informacioPesa().isVolUdi());
        params.put("pesa_dies_caducitat", l.informacioPesa().diesCaducitat());
        params.put("pesa_codi_familia", l.informacioPesa().codiFamilia().orElse(null));
        params.put("pesa_pes_unitari", l.informacioPesa().pesUnitari());
        params.put("pesa_unitats_embalatge", l.informacioPesa().unitatsEmbalatge());
        params.put("pesa_bosses_caixa", l.informacioPesa().bossesCaixa());
        params.put("pesa_caixes_palet", l.informacioPesa().caixesPalet());

        params.put("quantitat", l.quantitat());
        params.put("quantitat_pendent_facturar", l.quantitatPendentFacturar());
        params.put("quantitat_pendent_consumir", l.quantitatPendentConsumir());
        params.put("preu", l.preu().valor());
        params.put("divisa", l.preu().divisa());
        params.put("is_preu_fixat", l.isPreuFixat());
        params.put("descompte", l.descompte());
        params.put("comanda_blanca", l.comandaBlanca().orElse(null));
        params.put("observacions_impressio", l.observacionsImpressio().orElse(null));
        params.put("observacions_internes", l.observacionsInternes().orElse(null));
        params.put("codi_embalatge", l.codiEmbalatge().orElse(null));
        params.put("identificador_consum", l.identificadorConsum().orElse(null));
        params.put("versio", newEtag.versio());
        params.put("usuari", newEtag.usuari());
        params.put("datareg", newEtag.data());
        // TODO Eliminar (i descomentar la següent) després de migració,
        //  només necessari para mantener la data de registre original en las líneas migradas
        params.put("datareg_local", newEtag.data().toLocalDate());
        //params.put("datareg_local", RequestThread.dateLocal());
        params.put("is_urgent", l.isUrgent());
        return params;
    }

    @Override
    public Optional<LiniaAlbara> find(KeyLiniaAlbara id) {
        return find(id.idAlbara(), id.linia());
    }

    @Override
    public Optional<LiniaAlbara> find(KeyAlbara idAlbara, long linia) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject(
                    "SELECT * FROM albarans.linia_albara WHERE codi_albara = ? AND empresa = ? AND linia = ?",
                    new LiniaAlbaraMapper(),
                    idAlbara.codi(),
                    idAlbara.empresa(),
                    linia
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LiniaAlbara> findByAlbara(KeyAlbara idAlbara) {
        return jdbcAmes.query(
                """
                SELECT * FROM albarans.linia_albara
                WHERE codi_albara = ? AND empresa = ?
                """,
                new LiniaAlbaraMapper(),
                idAlbara.codi(),
                idAlbara.empresa()
        );
    }

    @Override
    public void deleteByAlbara(KeyAlbara idAlbara) {
        jdbcAmes.update(
                "DELETE FROM albarans.linia_albara WHERE codi_albara = ? AND empresa = ?",
                idAlbara.codi(),
                idAlbara.empresa()
        );
    }

    @Override
    public boolean isHiHaLiniesFacturades(KeyAlbara idAlbara) {
        return Boolean.TRUE.equals(jdbcAmes.query(
                """
                        SELECT EXISTS (
                            SELECT 1
                            FROM albarans.linia_albara
                            WHERE
                              codi_albara = ?
                              AND empresa = ?
                              AND quantitat_pendent_facturar < quantitat
                        )
                        """,
                rs -> rs.next() && rs.getBoolean(1),
                idAlbara.codi(),
                idAlbara.empresa()
        ));
    }

    @Override
    public void delete(KeyLiniaAlbara id) {
        jdbcAmes.update(
                "DELETE FROM albarans.linia_albara WHERE codi_albara = ? AND empresa = ? AND linia = ?",
                id.idAlbara().codi(),
                id.idAlbara().empresa(),
                id.linia()
        );
    }

    @Override
    public void checkEtag(KeyLiniaAlbara id) {
        var partsExpectedVersion = RequestThread.etag(Entity.LINIA_ALBARA).split("#");
        if (partsExpectedVersion.length == 4) {
            var empresaRevisar = partsExpectedVersion[0];
            var albaraRevisar = Long.valueOf(partsExpectedVersion[1]);
            var liniaRevisar = Long.valueOf(partsExpectedVersion[2]);
            var expectedVersion = partsExpectedVersion[3];

            if (!expectedVersion.isEmpty()
                    && id.idAlbara().empresa().equals(empresaRevisar)
                    && id.idAlbara().codi() == albaraRevisar
                    && id.linia() == liniaRevisar) {
                etag(id).ifPresent(etag -> {
                    if (!expectedVersion.equals(etag.versio()))
                        throw new LiniaAlbaraChanged(etag);
                });
            }
        }
    }

    @Override
    public Optional<ETag> etag(KeyLiniaAlbara id) {
        try {
            var currentETag = jdbcAmes.queryForObject(
                    "SELECT versio, usuari, datareg FROM albarans.linia_albara WHERE codi_albara = ? AND empresa = ? AND linia = ?",
                    (rs, rowNum) -> ETag.of(
                            rs.getTimestamp("datareg").toLocalDateTime(),
                            rs.getString("versio"),
                            rs.getString("usuari")
                    ),
                    id.idAlbara().codi(),
                    id.idAlbara().empresa(),
                    id.linia()
            );
            return Optional.ofNullable(currentETag);
        } catch (EmptyResultDataAccessException empty) {
            return Optional.empty();
        }
    }

}

