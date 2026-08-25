package ames.permisos.shared.mapper;

import ames.permisos.server.Json;
import ames.permisos.shared.KeyArticleAmes;
import ames.permisos.shared.KeyArticleAmesImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class KeyArticleAmesMapper implements RowMapper<KeyArticleAmes> {

	private Json json;

	public KeyArticleAmesMapper(Json json) {
		this.json = json;
	}

	@Override
	public KeyArticleAmes mapRow(ResultSet rs, int rowNum) throws SQLException {
		if (rs.getString("codi_article_ames").equals(""))
			return KeyArticleAmesImpl.builder()
					.codiArticle("")
					.codiArticleAmes("")
					.build();
		else{
			return KeyArticleAmesImpl.builder()
					.codiArticle(rs.getString("codi_article"))
					.codiArticleAmes(rs.getString("codi_article_ames"))
					.build();
		}

	}

}
