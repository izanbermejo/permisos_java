package ames.comercial.advantage.internal.response;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@JsonDeserialize(builder = QueryArticleClientEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryArticleClientEDIResponse {

	String artInt();
	String aclFab();
	String referencia();
	String clicod();
	Optional<String> nomClient();
	Optional<String> usuariLogistica();
	String magatzemSortida();
	String magatzemEntrada();
	Long unitatsEmbalatge();
	int caixesPalet();
	int bossesCaixa();
	Long stock();

	@Value.Derived
	default KeyArticleClient clau() {
		return KeyArticleClient.of(artInt(), clicod());
	}

//	@Derived
//	public default String articleClient() {
//		return article() + codiClient();
//	}
	
	public static QueryArticleClientEDIResponse mapTo(ResultSet resultSet) throws SQLException {
		return QueryArticleClientEDIResponseImpl.builder()
				.artInt(resultSet.getString("artint"))
				.aclFab(resultSet.getString("aclfab"))
				.referencia(resultSet.getString("aclref"))
				.clicod(resultSet.getString("clicod"))
				.nomClient(resultSet.getString("clinom"))
				.usuariLogistica(resultSet.getString("usuresp"))
				.magatzemSortida(resultSet.getString("aclmags"))
				.magatzemEntrada(resultSet.getString("aclmage"))
				.build();
	}
	
}
