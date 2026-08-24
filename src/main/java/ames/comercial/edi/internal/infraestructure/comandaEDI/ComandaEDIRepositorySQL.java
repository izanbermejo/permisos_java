package ames.comercial.edi.internal.infraestructure.comandaEDI;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.server.Json;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Dates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class ComandaEDIRepositorySQL implements ComandaEDIRepository {

    private @Autowired
    @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    static final Logger log = LogManager.getLogger(ComandaEDIRepositorySQL.class.getName());

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    @Override
    public long nextId() {
        return jdbcAmes.queryForObject("SELECT nextval('edi.seq_comanda')", Long.class);
    }

    public void saveLinies(List<DadesLiniaEDI> linies) {

        String sql = "INSERT INTO edi.linia (codi_comanda, quantitat, data_inicial, data_final, codi_article, codi_article_ames,ultim_albara, tipus,observacions,status,deleted,inserted_at,inserted_by,codi_linia_client,codi_comanda_client,codi_article_fab,acum_article,programa) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        jdbcAmes.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                DadesLiniaEDI linia = linies.get(i);
                ps.setLong(1, linia.codi_comanda());
                ps.setLong(2, linia.quantitat());
                if (linia.dataInicial().isEmpty())
                    ps.setDate(3, Dates.getNowSQLDate());
                else
                    ps.setDate(3, linia.dataInicial().get());
                if (linia.dataFinal().isEmpty())
                    ps.setDate(4, null);
                else
                    ps.setDate(4, linia.dataFinal().get());
                ps.setString(5, linia.codiArticle());
                ps.setString(6, linia.codiArticleAmes());
                if (linia.ultimAlbara().isEmpty())
                    ps.setString(7, "");
                else
                    ps.setString(7, linia.ultimAlbara().get());
                ps.setString(8, linia.tipus());
                ps.setString(9, linia.observacions());
                ps.setString(10, linia.status());
                ps.setBoolean(11, false);
                ps.setTimestamp(12, Timestamp.valueOf(linia.insertedAt()));
                ps.setString(13, linia.insertedBy());
                if (linia.codiLiniaClient().isEmpty())
                    ps.setString(14, "");
                else
                    ps.setString(14, linia.codiLiniaClient().get());
                if (linia.codiComandaClient().isEmpty())
                    ps.setString(15, "");
                else
                    ps.setString(15, linia.codiComandaClient().get());
                ps.setString(16,linia.codiArticleFab());
                ps.setInt(17,linia.acumulatArticle());
                if (linia.programa().isEmpty())
                    ps.setString(18, "");
                else
                    ps.setString(18, linia.programa().get());
//                ps.setString(17,linia.ca);
//					for (Detalle detalle : detalles) {
//						ps.setLong(1, codi_comanda);
//						ps.setLong(2, Long.parseLong((detalle.getDA().getCantidad())));
//						if (detalle.getDA().getFechaInicial().isEmpty() ||detalle.getDA().getFechaInicial().equals(""))
//							ps.setDate(3, null);
//						else
//							ps.setDate(3, new java.sql.Date(Dates.formatter_date.parse(detalle.getDA().getFechaInicial()).getTime()));
//						if (detalle.getDA().getFechaFinal().isEmpty() ||detalle.getDA().getFechaFinal().equals(""))
//							ps.setDate(4, null);
//						else
//							ps.setDate(4, new java.sql.Date(Dates.formatter_date.parse(detalle.getDA().getFechaFinal()).getTime()));
//						ps.setLong(5, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).artint());
//						ps.setString(6, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).aclfab());
//						ps.setString(7, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).observacions());
//					}
//				} catch (ParseException pe) {
//					log.error("Error de processament d'una data. Detalls: " + pe.getMessage());
//				}
            }

            @Override
            public int getBatchSize() {
                return linies.size();
            }
        });
    }

    @Override
    public Long save(DadesComandaEDI dadesComandaEDI) {
        Optional<Long> codi = dadesComandaEDI.codi();

        Long newCodi = nextId();

        var params = new HashMap<String, Object>();
        if (codi.isEmpty())
            params.put("codi", newCodi);
        else {
            remove(codi.get());
            params.put("codi", codi);
        }

        ComandaMissatgeEDI pedido = dadesComandaEDI.json();
        String sortedJson="";

        try {
            sortedJson = convertPedidoToJson(pedido);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        params.put("json", sortedJson);
        if (!dadesComandaEDI.pathPDF().isEmpty())
            params.put("path_pdf", dadesComandaEDI.pathPDF());
        params.put("enviament_numero", dadesComandaEDI.numeroEnviament());
        params.put("missatge_tipus", dadesComandaEDI.missatgeTipus());
        params.put("missatge_numero", dadesComandaEDI.missatgeNumero());
        params.put("data", dadesComandaEDI.data());
        params.put("referencia", dadesComandaEDI.referencia());
        params.put("path_edi", dadesComandaEDI.pathEDI());
        params.put("observacions", dadesComandaEDI.observacions().get());
        params.put("usuari_logistica", dadesComandaEDI.usuariLogistica());
        params.put("codi_client_ames", dadesComandaEDI.codiClientAmes());
        params.put("nom_client_ames", dadesComandaEDI.nomClientAmes());
        params.put("deleted", dadesComandaEDI.deleted());
        params.put("inserted_at", dadesComandaEDI.insertedAt());
        params.put("inserted_by", dadesComandaEDI.insertedBy());
        params.put("status", dadesComandaEDI.status());
        params.put("nad", dadesComandaEDI.nad());
        params.put("nad_path", dadesComandaEDI.nadPath());
        params.put("document", dadesComandaEDI.tipus());
        params.put("bustia", dadesComandaEDI.bustia().get());
        if (!dadesComandaEDI.clientProfile().isEmpty())
            params.put("client_profile", json.serialize(dadesComandaEDI.clientProfile()));

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("edi")
                .withTableName("comanda")
                .execute(params);
        return newCodi;
    }

    @Override
    public int updateComandaEDIFields(Long codi, HashMap<String, Object> fields) {
        StringJoiner setClause = new StringJoiner(", ");
        fields.forEach((key, value) -> setClause.add(key + " = ?"));

        String sql = "UPDATE edi.comanda SET " + setClause.toString() + ", updated_at = ? WHERE codi = ?";

        Object[] params = fields.values().toArray(new Object[fields.size() + 2]);
        params[fields.size()] = LocalDateTime.now();
        params[fields.size() + 1] = codi;

        return jdbcAmes.update(sql, params);
    }

    // TODO falta posar camps audit (updated_by)
    @Override
    public int updateLiniaComandaEDIFields(Long codi, HashMap<String, Object> fields) {
        StringJoiner setClause = new StringJoiner(", ");
        fields.forEach((key, value) -> setClause.add(key + " = ?"));

        String sql = "UPDATE edi.linia SET " + setClause.toString() + ", updated_at = ? WHERE codi = ?";

        Object[] params = fields.values().toArray(new Object[fields.size() + 2]);
        params[fields.size()] = LocalDateTime.now();
        params[fields.size() + 1] = codi;

        return jdbcAmes.update(sql, params);
    }


    @Override
    public int setPathPDFofPathEDIFiles(String pathPDF, String pathEDI) {

        String sql = "UPDATE edi.comanda SET path_pdf=?, updated_at = ?, updated_by ='<SYSTEM_CHECKPDF>', updated_reason='<SYSTEM_CHECKPDF>' WHERE path_edi=?";

        return jdbcAmes.update(sql, pathPDF, LocalDateTime.now(), pathEDI);
    }

    //	@Override
    // Codi comentat doncs es complicat gestionar els comentaris del processament i d'altres processos com el check del PDF forma simultanea. Sobretot per que el proces de check es passa cada dia i aniria omplint aquest camp de forma indefinida apart de fer un update innecessari
//	public int updateAppendingObservacions(Long codi_comanda,String observacio) {
//		//TODO millorar aquest update doncs estan harcoded els camps update_by i update_reason
//		observacio="\n"+observacio;
//		String sql = "UPDATE edi.comanda SET observacions= observacions || ?, updated_at = ? , updated_by ='<SYSTEM_CHECKPDF>', updated_reason='Observacions actualitzades per proces de check del path PDF' WHERE codi=?";
//
//		return jdbcAmes.update(sql, observacio, RequestThread.dateLocal(),codi_comanda);
//	}

//	public List<DadesComandaEDINoJSON> listComandesSensePathPDF() {
//		String sql = "SELECT * " +
//				"FROM edi.comanda where path_pdf is null and deleted = false";
//
//		List<DadesComandaEDINoJSON> list = jdbcAmes.query(sql,new ComandaEDIRecordMapperNoJSON(json));
//
//		return list;
//	}

    @Transactional
    public void delete (long codi) {
        jdbcAmes.update("UPDATE edi.linia SET deleted=true, deleted_by=?, deleted_at=NOW(), deleted_reason='PROCESSED' WHERE codi_comanda = ?", RequestThread.nomUsuari(), codi);
        updateComandaSetProcessada(codi);
    }

    @Transactional
    public void updateUltimAlbara(Long codiComanda,String codiArticle,String ultimAlbara) {
        jdbcAmes.update("UPDATE edi.linia SET ultim_albara=?, updated_at = NOW(), updated_by ='<SYSTEM>', updated_reason='<UPDATE_ULTIM_ALBARA>' WHERE codi_comanda = ? AND codi_article = ? AND deleted = false",ultimAlbara, codiComanda, codiArticle);
    }

    public void deleteLine (long codi) {
        jdbcAmes.update("DELETE FROM edi.linia WHERE codi = ?", codi);
    }

    private void remove(Long codi) {
        jdbcAmes.update("DELETE FROM edi.comanda WHERE codi = ?", codi);
    }

    public void deleteLiniesArticle(Long codiComanda,String codiArticle) {
        jdbcAmes.update("UPDATE edi.linia SET deleted=true, deleted_by=?, deleted_at=NOW(), deleted_reason='PROCESSED' WHERE codi_comanda = ? AND codi_article = ?", RequestThread.nomUsuari(), codiComanda, codiArticle);
    }

    public void updateComandaSetProcessada(Long codiComanda) {
        jdbcAmes.update("update edi.comanda set status =?, deleted =true , deleted_by = ?, deleted_at = NOW(), deleted_reason='PROCESSED' where codi = ?", DadesComandaEDI.ENUM_STATUS_PROCESSED, RequestThread.nomUsuari(),codiComanda);
    }

    public void updateLiniaSetProcessada(Long codiComanda, String codiArticle) {
      jdbcAmes.update("update edi.linia set status =?, deleted =true , deleted_by = ?, deleted_at = NOW(), deleted_reason='PROCESSED' where codi_comanda = ? and codi_article = ? ", DadesComandaEDI.ENUM_STATUS_PROCESSED, RequestThread.nomUsuari(),codiComanda,codiArticle);
    }

    public void updateClientProfile(Long codiComanda, ObtenirClientEDIAds.ClientEDIAds clientProfile) {
        jdbcAmes.update("update edi.comanda set clientProfile =?, updated_by = '<SYSTEM>', updated_at = NOW() where codi = ?", json.serialize(clientProfile),codiComanda);
    }

    public void fixComandaWithNADIssue(Long codiComanda, String codiClientAmes, String nomClientAmes) {
        jdbcAmes.update("update edi.comanda set codi_client_ames = ?, nom_client_ames =?,  updated_by = '<SYSTEM>', status='DRAFT', observacions='', updated_at = NOW() where codi = ?", codiClientAmes,nomClientAmes,codiComanda);
    }

    public void fixLiniaWithNADIssue(Long codiLinia, String codiArticle, String codiArticleAmes) {
        jdbcAmes.update("update edi.linia set codi_article = ?, codi_article_ames =?,  updated_by = '<SYSTEM>', status='DRAFT', observacions='', updated_at = NOW() where codi = ?", codiArticle,codiArticleAmes,codiLinia);
    }

    public static String convertPedidoToJson(ComandaMissatgeEDI pedido) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Convertir el objeto 'Pedido' en un JSON (sin ordenar)
        String jsonString = objectMapper.writeValueAsString(pedido);

        // Convertir el JSON en un Map para ordenar las claves del primer nivel
        Map<String, Object> map = objectMapper.readValue(jsonString, Map.class);

        // Ordenar las claves del primer nivel con TreeMap (que ordena alfabéticamente)
        Map<String, Object> sortedMap = new TreeMap<>(map);

        // Convertir el Map ordenado de vuelta a un JSON
        return objectMapper.writeValueAsString(sortedMap);
    }

//	@Override
//	public void checkEtag (long codi) {
//		var expectedVersion = RequestThread.etag(Entity.COMANDA);
//		if (!expectedVersion.isEmpty()) {
//			etag(codi).ifPresent(etag -> {
//				if (!expectedVersion.equals(etag.versio()))
//					throw new ComandaChanged(etag);
//			});
//		}
//	}

//	@Override
//	public Optional<ETag> etag (long codi) {
//		try {
//			var type = new TypeReference<ETag>() {};
//			var currentETag = jdbcAmes.queryForObject(
//				"SELECT etag FROM comandes.comanda WHERE codi = ?",
//				(rs, rowNum) -> json.deserialize(rs.getString(1), type),
//				codi
//			);
//			return Optional.of(currentETag);
//		} catch (EmptyResultDataAccessException empty) {
//			return Optional.empty();
//		}
//	}

}
