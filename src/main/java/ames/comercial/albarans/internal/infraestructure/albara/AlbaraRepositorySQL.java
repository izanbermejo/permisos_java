package ames.comercial.albarans.internal.infraestructure.albara;

import ames.comercial.albarans.AlbaransException.AlbaraChanged;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.mapper.AlbaraMapper;
import ames.comercial.server.ETag;
import ames.comercial.server.Json;
import ames.comercial.server.RequestThread;
import ames.comercial.server.RequestThread.Entity;
import ames.comercial.server.SimpleJdbcUpsert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class AlbaraRepositorySQL implements AlbaraRepository {

    private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(Albara albara) {
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("albarans")
                .withTableName("albara")
                .onConflictColumns("codi", "empresa")
                .execute(mapAlbaraToParams(albara));
    }

    /**
     * TODO: Eliminar després de la migració
     */
    public void saveBatch(List<Albara> albarans) {
        if (albarans.isEmpty()) {
            return;
        }
        var insertBatch = new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("albarans")
                .withTableName("albara");
        // Processar en lotes per evitar problemes de memòria
        for (int i = 0; i < albarans.size(); i += 2000) {
            int end = Math.min(i + 2000, albarans.size());
            var subList = albarans.subList(i, end);
            var batchParams = subList.stream()
                    .map(this::mapAlbaraToParams)
                    .toArray(HashMap[]::new);
            insertBatch.executeBatch(batchParams);
        }
    }

    private HashMap<String, Object> mapAlbaraToParams(Albara albara) {
        var params = new HashMap<String, Object>();
        params.put("codi", albara.id().codi());
        params.put("empresa", albara.id().empresa());
        params.put("tipus", albara.tipus());
        params.put("client", albara.client().orElse(null));
        params.put("data", albara.data());
        params.put("magatzem", albara.magatzem());
        params.put("adresa", json.serialize(albara.adresa()));
        params.put("informacio_enviament", json.serialize(albara.informacioEnviament()));
        params.put("desti_alternatiu", albara.destiAlternatiu().orElse(null));
        params.put("adresa_broker", json.serialize(albara.adresaBroker().orElse(null)));
        params.put("adresa_factura_proforma", json.serialize(albara.adresaFacturaProforma().orElse(null)));
        params.put("informacio_magatzem", json.serialize(albara.informacioMagatzem()));
        params.put("informacio_traspas", json.serialize(albara.informacioTraspas().orElse(null)));
        params.put("informacio_edi", json.serialize(albara.informacioEdi().orElse(null)));
        params.put("cost_logistic", json.serialize(albara.costLogistic().orElse(null)));
        params.put("cost_transport", json.serialize(albara.costTransport().orElse(null)));
        params.put("cost_enviament_express", json.serialize(albara.costEnviamentExpress().orElse(null)));
        params.put("observacions_impressio", albara.observacionsImpressio().orElse(null));
        params.put("observacions_internes", albara.observacionsInternes().orElse(null));
        params.put("observacions_proforma", albara.observacionsProforma().orElse(null));
        params.put("data_creacio", albara.dataCreacio());
        params.put("usuari_creacio", albara.usuariCreacio());
        params.put("numero_proveidor", albara.numeroProveidor().orElse(null));
        params.put("numero_albara_especial", albara.numeroAlbaraEspecial().orElse(null));
        params.put("is_tancat", albara.isTancat());
        params.put("is_facturat", albara.isFacturat());
        params.put("is_facturacio_automatica", albara.isFacturacioAutomatica());
        params.put("is_enviat_email", albara.isEnviatEmail());
        params.put("is_normalitzats", albara.isNormalitzats());
        params.put("is_cal_pagar_ports", albara.isCalPagarPorts());
        params.put("is_no_valorat", albara.isNoValorat());
        params.put("is_enviat_hisenda", albara.isEnviatHisenda());
        params.put("numero_factura_transport", albara.numeroFacturaTransport().orElse(null));
        params.put("numero_caixes", albara.numeroCaixes().orElse(null));
        params.put("alsada_caixes", albara.alsadaCaixes().orElse(null));
        params.put("cost_moq", albara.costMoq().orElse(null));
        params.put("referencia_transport", albara.referenciaTransport().orElse(null));
        params.put("etag", json.serialize(ETag.generate()));
        params.put("is_urgent", albara.isUrgent());
        return params;
    }

    @Override
    public Optional<Albara> find(KeyAlbara id) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject(
                    "SELECT * FROM albarans.albara WHERE codi = ? AND empresa = ?",
                    new AlbaraMapper(json),
                    id.codi(),
                    id.empresa()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Albara> findObertsClient(String codiClient) {
        return jdbcAmes.query("SELECT * FROM albarans.albara WHERE client = ? AND NOT is_tancat", new AlbaraMapper(json), codiClient);
    }

    @Override
    public void delete(KeyAlbara id) {
        jdbcAmes.update(
                "DELETE FROM albarans.albara WHERE codi = ? AND empresa = ?",
                id.codi(),
                id.empresa()
        );
    }

    @Override
    public void checkEtag(KeyAlbara id) {
        var expectedVersion = RequestThread.etag(Entity.ALBARA);
        if (!expectedVersion.isEmpty()) {
            etag(id).ifPresent(etag -> {
                if (!expectedVersion.equals(etag.versio()))
                    throw new AlbaraChanged(etag);
            });
        }
    }

    @Override
    public Optional<ETag> etag(KeyAlbara id) {
        try {
            var currentETag = jdbcAmes.queryForObject(
                    "SELECT etag FROM albarans.albara WHERE codi = ? AND empresa = ?",
                    (rs, rowNum) -> json.deserialize(rs.getString("etag"), ETag.class),
                    id.codi(),
                    id.empresa()
            );
            return Optional.ofNullable(currentETag);
        } catch (EmptyResultDataAccessException empty) {
            return Optional.empty();
        }
    }

}
