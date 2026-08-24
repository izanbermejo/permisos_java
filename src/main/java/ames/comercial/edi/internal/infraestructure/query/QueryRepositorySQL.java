package ames.comercial.edi.internal.infraestructure.query;

import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.edi.internal.domain.*;
import ames.comercial.edi.internal.infraestructure.comandaEDI.mapper.ComandaEDIRecordMapper;
import ames.comercial.edi.internal.infraestructure.comandaEDI.mapper.ComandaEDIRecordMapperNoJSON;
import ames.comercial.edi.internal.infraestructure.comandaEDI.mapper.LiniaMapper;
import ames.comercial.server.Json;
import ames.comercial.shared.KeyArticleAmes;
import ames.comercial.shared.Usuari;
import ames.comercial.shared.mapper.KeyArticleAmesMapper;
import ames.comercial.shared.mapper.UsuariMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class QueryRepositorySQL implements QueryRepository {

    private @Autowired
    @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    static final Logger log = LogManager.getLogger(QueryRepositorySQL.class.getName());

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

//    public void saveLinies(List<DadesLiniaEDI> linies) {
//
//        String sql = "INSERT INTO edi.linia (codi_comanda, quantitat, data_inicial, data_final, codi_article, codi_article_ames,ultim_albara, tipus,observacions,status,deleted,inserted_at,inserted_by) " + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
//
//        jdbcAmes.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                DadesLiniaEDI linia = linies.get(i);
//                ps.setLong(1, linia.codi_comanda());
//                ps.setLong(2, linia.quantitat());
//                if (linia.dataInicial().isEmpty()) ps.setDate(3, Dates.getNowSQLDate());
//                else ps.setDate(3, linia.dataInicial().get());
//                if (linia.dataFinal().isEmpty()) ps.setDate(4, null);
//                else ps.setDate(4, linia.dataFinal().get());
//                ps.setString(5, linia.codiArticle());
//                ps.setString(6, linia.codiArticleAmes());
//                if (linia.ultimAlbara().isEmpty()) ps.setString(7, "");
//                else ps.setString(7, linia.ultimAlbara().get());
//                ps.setString(8, linia.tipus());
//                ps.setString(9, linia.observacions());
//                ps.setString(10, linia.status());
//                ps.setBoolean(11, false);
//                ps.setTimestamp(12, Timestamp.valueOf(linia.insertedAt()));
//                ps.setString(13, linia.insertedBy());
////					for (Detalle detalle : detalles) {
////						ps.setLong(1, codi_comanda);
////						ps.setLong(2, Long.parseLong((detalle.getDA().getCantidad())));
////						if (detalle.getDA().getFechaInicial().isEmpty() ||detalle.getDA().getFechaInicial().equals(""))
////							ps.setDate(3, null);
////						else
////							ps.setDate(3, new java.sql.Date(Dates.formatter_date.parse(detalle.getDA().getFechaInicial()).getTime()));
////						if (detalle.getDA().getFechaFinal().isEmpty() ||detalle.getDA().getFechaFinal().equals(""))
////							ps.setDate(4, null);
////						else
////							ps.setDate(4, new java.sql.Date(Dates.formatter_date.parse(detalle.getDA().getFechaFinal()).getTime()));
////						ps.setLong(5, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).artint());
////						ps.setString(6, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).aclfab());
////						ps.setString(7, codisArticlesAmes.get(linia.getLA().getIdArticuloComprador()).observacions());
////					}
////				} catch (ParseException pe) {
////					log.error("Error de processament d'una data. Detalls: " + pe.getMessage());
////				}
//            }
//
//            @Override
//            public int getBatchSize() {
//                return linies.size();
//            }
//        });
//    }

    @Override
    public Optional<DadesComandaEDI> find(long codi) {
        try {
            var rowComanda = jdbcAmes.queryForObject("SELECT * FROM edi.comanda WHERE codi = ?", new ComandaEDIRecordMapper(json), codi);
            DadesComandaEDIImpl.Builder builder = DadesComandaEDIImpl.builder();

            builder.codi(rowComanda.codi()).json(rowComanda.json());
            ObtenirClientEDIAds.ClientEDIAds clientProfile = null;
            if (rowComanda.clientProfile() != null && !rowComanda.clientProfile().isEmpty())
                clientProfile = rowComanda.clientProfile().get();
            if (rowComanda.pathPDF() != null && !rowComanda.pathPDF().isEmpty()) builder.pathPDF(rowComanda.pathPDF());
            builder.pathEDI(rowComanda.pathEDI())
                    .numeroEnviament(rowComanda.numeroEnviament())
                    .missatgeTipus(rowComanda.missatgeTipus())
                    .missatgeNumero(rowComanda.missatgeNumero())
                    .data(rowComanda.data())
                    .referencia(rowComanda.referencia())
                    .document(rowComanda.document())
                    .usuariLogistica(rowComanda.usuariLogistica())
                    .codiClientAmes(rowComanda.codiClientAmes())
                    .nomClientAmes(rowComanda.nomClientAmes())
//					.codiArticle(rowComanda.codiArticle())
//					.codiArticleAmes(rowComanda.codiArticleAmes())
                    .deleted(rowComanda.deleted())
                    .insertedAt(rowComanda.insertedAt())
                    .insertedBy(rowComanda.insertedBy())
                    .nad(rowComanda.nad())
                    .nadPath(rowComanda.nadPath())
                    .status(rowComanda.status())
                    .tipus(rowComanda.tipus());
            if (clientProfile != null) builder.clientProfile(clientProfile);
//					.build();
            if (rowComanda.observacions() != null && !rowComanda.observacions().isEmpty())
                builder.observacions(rowComanda.observacions());
            if (rowComanda.updatedAt() != null && !rowComanda.updatedAt().isEmpty())
                builder.updatedAt(rowComanda.updatedAt());
            if (rowComanda.updatedBy() != null && !rowComanda.updatedBy().isEmpty())
                builder.updatedBy(rowComanda.updatedBy());
            if (rowComanda.updatedReason() != null && !rowComanda.updatedReason().isEmpty())
                builder.updatedReason(rowComanda.updatedReason());
            if (rowComanda.deletedAt() != null && !rowComanda.deletedAt().isEmpty())
                builder.deletedAt(rowComanda.deletedAt());
            if (rowComanda.deletedBy() != null && !rowComanda.deletedBy().isEmpty())
                builder.deletedBy(rowComanda.deletedBy());
            if (rowComanda.deletedReason() != null && !rowComanda.deletedReason().isEmpty())
                builder.deletedReason(rowComanda.deletedReason());

            var props = builder.build();
            return Optional.of(props);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<DadesComandaEDINoJSON> findNoJSON(long codi) {
        try {
            var rowComanda = jdbcAmes.queryForObject("SELECT * FROM edi.comanda WHERE codi = ?", new ComandaEDIRecordMapperNoJSON(json), codi);
            DadesComandaEDINoJSONImpl.Builder builder = DadesComandaEDINoJSONImpl.builder();

            builder.codi(rowComanda.codi());
            ObtenirClientEDIAds.ClientEDIAds clientProfile = null;
            if (rowComanda.clientProfile() != null && !rowComanda.clientProfile().isEmpty())
                clientProfile = rowComanda.clientProfile().get();
            if (rowComanda.pathPDF() != null && !rowComanda.pathPDF().isEmpty()) builder.pathPDF(rowComanda.pathPDF());
            builder.pathEDI(rowComanda.pathEDI())
                    .numeroEnviament(rowComanda.numeroEnviament())
                    .missatgeTipus(rowComanda.missatgeTipus())
                    .missatgeNumero(rowComanda.missatgeNumero())
                    .data(rowComanda.data())
                    .referencia(rowComanda.referencia())
                    .document(rowComanda.document())
                    .usuariLogistica(rowComanda.usuariLogistica())
                    .codiClientAmes(rowComanda.codiClientAmes())
                    .nomClientAmes(rowComanda.nomClientAmes())
//					.codiArticle(rowComanda.codiArticle())
//					.codiArticleAmes(rowComanda.codiArticleAmes())
                    .deleted(rowComanda.deleted())
                    .insertedAt(rowComanda.insertedAt())
                    .insertedBy(rowComanda.insertedBy())
                    .nad(rowComanda.nad())
                    .nadPath(rowComanda.nadPath())
                    .status(rowComanda.status())
                    .tipus(rowComanda.tipus())
                    .bustia(rowComanda.bustia());
            if (clientProfile != null) builder.clientProfile(clientProfile);
//					.build();
            if (rowComanda.observacions() != null && !rowComanda.observacions().isEmpty())
                builder.observacions(rowComanda.observacions());
            if (rowComanda.updatedAt() != null && !rowComanda.updatedAt().isEmpty())
                builder.updatedAt(rowComanda.updatedAt());
            if (rowComanda.updatedBy() != null && !rowComanda.updatedBy().isEmpty())
                builder.updatedBy(rowComanda.updatedBy());
            if (rowComanda.updatedReason() != null && !rowComanda.updatedReason().isEmpty())
                builder.updatedReason(rowComanda.updatedReason());
            if (rowComanda.deletedAt() != null && !rowComanda.deletedAt().isEmpty())
                builder.deletedAt(rowComanda.deletedAt());
            if (rowComanda.deletedBy() != null && !rowComanda.deletedBy().isEmpty())
                builder.deletedBy(rowComanda.deletedBy());
            if (rowComanda.deletedReason() != null && !rowComanda.deletedReason().isEmpty())
                builder.deletedReason(rowComanda.deletedReason());

            var props = builder.build();
            return Optional.of(props);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
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

    public List<DadesComandaEDINoJSON> list() {
        String sql = "SELECT *  FROM edi.comanda where deleted = false order by data asc";

        List<DadesComandaEDINoJSON> list = jdbcAmes.query(sql, new ComandaEDIRecordMapperNoJSON(json));

        return list;
    }

    public List<DadesComandaEDINoJSON> list(String usuari) {
        if (usuari == null || usuari.isBlank()) {
            return list(); // Devuelve todas las órdenes si no hay usuarios
        }

        // Convertir el string en una lista de usuarios (eliminando espacios en blanco)
        List<String> usuaris = Arrays.stream(usuari.split(","))
                .map(String::trim)
                .collect(Collectors.toList());

        // Construir los placeholders dinámicamente (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(usuaris.size(), "?"));

        // Construir la consulta con IN
        String sql = "SELECT * FROM edi.comanda " +
                "WHERE deleted = false AND usuari_logistica IN (" + placeholders + ") " +
                "ORDER BY data ASC";

        // Ejecutar la consulta pasando los valores como parámetros
        return jdbcAmes.query(sql, new ComandaEDIRecordMapperNoJSON(json), usuaris.toArray());
    }

    public boolean existLiniaComandaEDI(Long codi) {
        try {
            var registreComanda = jdbcAmes.queryForObject("SELECT count(*) FROM edi.linia where codi = ?", Long.class, codi);
            return (registreComanda > 0);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public boolean existComandaWithPathEDI(String pathEDI) {
        try {
            var registreComanda = jdbcAmes.queryForObject("SELECT count(*) FROM edi.comanda where path_edi like ?", Long.class, "%" + pathEDI);
            return (registreComanda > 0);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Transactional
    public void delete(long codi) {
        jdbcAmes.update("DELETE FROM edi.linia WHERE codi_comanda = ?", codi);
        jdbcAmes.update("DELETE FROM edi.comanda WHERE codi = ?", codi);
    }

    public void deleteLine(long codi) {
        jdbcAmes.update("DELETE FROM edi.linia WHERE codi = ?", codi);
    }

    public Optional<KeyArticleAmes> getFirstArticleOfOrderLines(Long codi_comanda) {
        String sql = "select codi_article,codi_article_ames from edi.linia where codi_comanda = ? and deleted=false order by data_inicial LIMIT 1";
        try {
            return Optional.of(jdbcAmes.queryForObject(sql, keyArticleAmesRowMapper(), codi_comanda));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<DadesLiniaEDI> listLinies(Long codi_comanda) {
        return listLinies(codi_comanda, null, null);
    }

    public List<DadesLiniaEDI> listLinies(Long codi_comanda, String diesPrevisio) {
        return listLinies(codi_comanda, diesPrevisio, null);
    }

    public List<DadesLiniaEDI> listLinies(Long codi_comanda, String diesPrevisio, String codi_article) {
        List<DadesLiniaEDI> list = new ArrayList<DadesLiniaEDI>();
        String sql = "SELECT * ";
        if (diesPrevisio != null)
            sql += "FROM edi.linia where codi_comanda = ? and deleted=false and data_inicial<= (CURRENT_DATE + INTERVAL '" + diesPrevisio + " days')::DATE";
        else
            sql += "FROM edi.linia where codi_comanda = ? and deleted=false";

        if (codi_article != null) {
            sql += " and codi_article= ?";
            list = jdbcAmes.query(sql, new LiniaMapper(json), codi_comanda, codi_article);
        } else
            list = jdbcAmes.query(sql, new LiniaMapper(json), codi_comanda);

        return list;
    }

    public List<KeyArticleAmes> listArticles(Long codi_comanda, String diesPrevisio) {
        List<KeyArticleAmes> list = new ArrayList<KeyArticleAmes>();
        String sql = "select codi_article,codi_article_ames from edi.linia  " +
                "where codi_comanda = ? and data_inicial<= (CURRENT_DATE + INTERVAL '" + diesPrevisio + " days')::DATE and deleted=false group by codi_article,codi_article_ames";

        list = jdbcAmes.query(sql, new KeyArticleAmesMapper(json), codi_comanda);

        return list;
    }

    private RowMapper<KeyArticleAmes> keyArticleAmesRowMapper() {
        return (rs, rowNum) -> KeyArticleAmes.of(
                rs.getString("codi_article"),
                rs.getString("codi_article_ames")
        );
    }

    public List<KeyArticleAmes> listArticles(Long codi_comanda) {
        List<KeyArticleAmes> list = new ArrayList<KeyArticleAmes>();
        String sql = "select codi_article,codi_article_ames from edi.linia where codi_comanda = ? and deleted = false group by codi_article,codi_article_ames";

        list = jdbcAmes.query(sql, new KeyArticleAmesMapper(json), codi_comanda);

        return list;
    }

    public List<Usuari> listUsuaris() {
        List<Usuari> list = new ArrayList<Usuari>();
        String sql = "select distinct c.usuari_logistica as usuari from edi.comanda c where c.usuari_logistica!='' order by c.usuari_logistica";

        list = jdbcAmes.query(sql, new UsuariMapper(json));

        return list;
    }

    private void remove(Long codi) {
        jdbcAmes.update("DELETE FROM edi.comanda WHERE codi = ?", codi);
    }

    public boolean ultimArticleDeComanda(Long codiComanda) {
        String sql = "SELECT COUNT(DISTINCT l.codi_article) " +
                "FROM edi.linia l " +
                "WHERE codi_comanda = ?";
        Integer distinctCount = jdbcAmes.queryForObject(sql, Integer.class, codiComanda);
        return distinctCount != null && distinctCount == 1;
    }

    public List<String> listComandesSensePathPDF() {
        String sql = "SELECT path_edi " +
//                "FROM edi.comanda where path_pdf is null and deleted = false group by path_edi";
                "FROM edi.comanda where path_pdf is null group by path_edi";

        List<String> list = jdbcAmes.queryForList(sql, String.class);

        return list;
    }
}
