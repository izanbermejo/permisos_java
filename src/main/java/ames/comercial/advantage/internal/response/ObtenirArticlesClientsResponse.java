package ames.comercial.advantage.internal.response;

import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@JsonDeserialize(builder = ObtenirArticlesClientsResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ObtenirArticlesClientsResponse {

	String artInt();
	String flag();
	String referencia();
	String article();
	String codiClient();
	String nomClient();
	String aliasClient();
	String formaEnviament();
	String codiProveidor();
	String codiEmpresa();
	String denominacio();
	String nivellTecnic();
	boolean pesaSeguretat();
	String codiFabrica();
	BigDecimal preu();
	String divisa();
	int stock();
	boolean bloquejatStock();
	String usuariBloqueigStock();
	int unitatsEmbalatge();
	int bossesCaixa();
	int caixesPalet();
	TipusArticleClient tipus();
	Optional<String> partidaArantzelariaCodi();
	Optional<String> partidaArantzelariaCodiAmes();
	Optional<String> partidaArantzelariaDescripcio();
	
	@Derived
	default String articleClient() {
		return article() + codiClient();
	}
	
	static ObtenirArticlesClientsResponse mapTo(ResultSet resultSet) throws SQLException {
		return ObtenirArticlesClientsResponseImpl.builder()
				.artInt(resultSet.getString("artint"))
				.flag(resultSet.getString("aclflg"))
				.referencia(resultSet.getString("aclref"))
				.article(resultSet.getString("aclfab"))
				.codiClient(resultSet.getString("clicod"))
				.nomClient(resultSet.getString("clinom"))
				.aliasClient(resultSet.getString("cliali"))
				.formaEnviament(resultSet.getString("aclfenv"))
				.codiProveidor(Optional.ofNullable(resultSet.getString("clipro")).orElse(""))
				.codiEmpresa(resultSet.getString("empcod"))
				.denominacio(resultSet.getString("aclden"))
				.nivellTecnic(resultSet.getString("aclnvt"))
				.pesaSeguretat("S".equalsIgnoreCase(resultSet.getString("aclsec")))
				.codiFabrica(resultSet.getString("codfab"))
				.preu(resultSet.getBigDecimal("aclpre") != null ? resultSet.getBigDecimal("aclpre") : BigDecimal.ZERO)
				.divisa(resultSet.getString("acldiv"))
				.stock(resultSet.getInt("aclstk"))
				.bloquejatStock(resultSet.getBoolean("bloqueostk"))
				.usuariBloqueigStock(resultSet.getString("usubloq"))
				.unitatsEmbalatge(resultSet.getInt("aclucai"))
				.bossesCaixa(resultSet.getInt("bosxcai"))
				.caixesPalet(resultSet.getInt("aclucap"))
				.tipus(TipusArticleClient.getByTipus(resultSet.getString("tipus")))
				.partidaArantzelariaCodi(Optional.ofNullable(resultSet.getString("partida")))
				.partidaArantzelariaCodiAmes(Optional.ofNullable(resultSet.getString("codiPartida")))
				.partidaArantzelariaDescripcio(Optional.ofNullable(resultSet.getString("descpartara")))
				.build();
	}
	
}
