package ames.comercial.inventari.internal.infraestructure.moviment;

import ames.comercial.advantage.internal.auxiliar.ConditionGenerator;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.infraestructure.moviment.mapper.MovimentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import org.springframework.dao.EmptyResultDataAccessException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class MovimentRepositorySQL implements MovimentRepository {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public List<Moviment> findByLiniaAlbara(KeyLiniaAlbara keyLiniaAlbara) {
        return findByLiniaAlbara(List.of(keyLiniaAlbara));
    }

    @Override
    public List<Moviment> findByLiniaAlbara(List<KeyLiniaAlbara> liniesAlbara) {
        if (liniesAlbara == null || liniesAlbara.isEmpty()) {
            return List.of();
        }

        String placeholders = ConditionGenerator.generate(liniesAlbara, 3);
        String sql = """
                SELECT * FROM inventari.moviment
                WHERE (linia_albara_empresa, linia_albara_numero, linia_albara_linia) IN (%s)
                """.formatted(placeholders);

        Object[] params = liniesAlbara.stream()
                .flatMap(key -> java.util.stream.Stream.of(
                        key.idAlbara().empresa(),
                        key.idAlbara().codi(),
                        key.linia()
                ))
                .toArray();

        return jdbcAmes.query(sql, new MovimentMapper(), params);
    }

    @Override
    public Optional<Moviment> findById(long id) {
        try {
            return Optional.of(jdbcAmes.queryForObject(
                    "SELECT * FROM inventari.moviment WHERE id = ?",
                    new MovimentMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void save(List<Moviment> moviment) {
        moviment.forEach(this::save);
    }

    /**
     * TODO: Eliminar després de la migració
     */
    public void saveBatch(List<Moviment> moviments) {
        if (moviments.isEmpty()) {
            return;
        }
        var insertBatch = new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("inventari")
                .withTableName("moviment")
                .usingGeneratedKeyColumns("id");
        // Processar en lotes per evitar problemes de memòria
        for (int i = 0; i < moviments.size(); i += 2000) {
            int end = Math.min(i + 2000, moviments.size());
            var subList = moviments.subList(i, end);
            var batchParams = subList.stream()
                    .map(this::mapMovimentToParams)
                    .toArray(HashMap[]::new);
            insertBatch.executeBatch(batchParams);
        }
    }

    @Override
    public void save(Moviment moviment) {
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("inventari")
                .withTableName("moviment")
                .usingGeneratedKeyColumns("id")
                .execute(mapMovimentToParams(moviment));
    }

    @Override
    public void delete(KeyLiniaAlbara idLiniaAlbara) {
        delete(List.of(idLiniaAlbara));
    }

    @Override
    public void delete(List<KeyLiniaAlbara> liniesAlbara) {
        if (liniesAlbara == null || liniesAlbara.isEmpty()) {
            return;
        }

        String placeholders = ConditionGenerator.generate(liniesAlbara, 3);
        String sql = """
                DELETE FROM inventari.moviment
                WHERE (linia_albara_empresa, linia_albara_numero, linia_albara_linia) IN (%s)
                """.formatted(placeholders);

        Object[] params = liniesAlbara.stream()
                .flatMap(key -> java.util.stream.Stream.of(
                        key.idAlbara().empresa(),
                        key.idAlbara().codi(),
                        key.linia()
                ))
                .toArray();

        jdbcAmes.update(sql, params);
    }

    @Override
    public void deleteById(long id) {
        jdbcAmes.update("DELETE FROM inventari.moviment WHERE id = ?", id);
    }

    private HashMap<String, Object> mapMovimentToParams(Moviment moviment) {
        var params = new HashMap<String, Object>();

        // Camps bàsics
        params.put("artint", moviment.articleClient().artint());
        params.put("clicod", moviment.articleClient().clicod());
        params.put("empresa", moviment.empresa());
        params.put("magatzem", moviment.magatzem());
        params.put("data", moviment.data());
        params.put("tipus", moviment.tipus().name());
        params.put("quantitat", moviment.quantitat());

        // Línia albarà associada
        moviment.liniaAlbara().ifPresent(liniaAlbara -> {
            params.put("linia_albara_empresa", liniaAlbara.idAlbara().empresa());
            params.put("linia_albara_numero", liniaAlbara.idAlbara().codi());
            params.put("linia_albara_linia", liniaAlbara.linia());
        });

        // InformacioEntrada
        moviment.entrada().ifPresent(entrada -> {
            params.put("entrada_of", entrada.of());
            params.put("entrada_id_entrada", entrada.idEntrada());
            params.put("entrada_id_entrada_fabrica", entrada.idEntradaFabrica());
        });

        // InformacioSortida
        moviment.sortida().ifPresent(sortida -> {
            params.put("sortida_client", sortida.client());
            params.put("sortida_comanda_codi", sortida.liniaComanda().comanda());
            params.put("sortida_comanda_numero", sortida.liniaComanda().numero());
        });

        // InformacioDevolucio
        moviment.devolucio().ifPresent(devolucio ->
            params.put("devolucio_parte_devolucio", devolucio.parteDevolucio())
        );

        // InformacioTraspasClient
        moviment.traspasClient().ifPresent(traspasClient -> {
            params.put("traspas_client_receptor", traspasClient.clientReceptor());
            params.put("traspas_client_traspas_empresa", traspasClient.isVaImplicarTraspasEmpresa());
        });

        // InformacioTraspasMagatzem
        moviment.traspasMagatzem().ifPresent(traspasMagatzem -> {
            params.put("traspas_magatzem_receptor", traspasMagatzem.magatzemReceptor());
            params.put("traspas_magatzem_traspas_empresa", traspasMagatzem.isVaImplicarTraspasEmpresa());
        });

        // InformacioTraspasEmpresa
        moviment.traspasEmpresa().ifPresent(traspasEmpresa -> {
            params.put("traspas_empresa_receptora", traspasEmpresa.empresaReceptora());
            params.put("traspas_empresa_traspas_magatzem", traspasEmpresa.isVaImplicarTraspasMagatzem());
        });

        // Camps d'auditoria
        moviment.observacions().ifPresent(obs -> params.put("observacions", obs));
        params.put("data_creacio", moviment.dataCreacio());
        params.put("usuari", moviment.usuari());

        return params;
    }

}

