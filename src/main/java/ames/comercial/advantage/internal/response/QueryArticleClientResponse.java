package ames.comercial.advantage.internal.response;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@JsonDeserialize(builder = QueryClientResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryArticleClientResponse {

	String artInt();
	String flag();
	String referencia();
	String article();
	String codiClient();
	Empresa empresa();
	String denominacio();
	String nivellTecnic();
	boolean pesaSeguretat();
	String codiFabrica();
	String nomFabrica();
	BigDecimal preu();
	String divisa();
	boolean bloquejatStock();
	String usuariBloqueigStock();
	int unitatsEmbalatge();
	int bossesCaixa();
	int caixesPalet();
	TipusArticleClient tipus();
	String codiProjectManager();
	Optional<String> partidaArantzelariaCodi();
	Optional<String> partidaArantzelariaCodiAmes();
	Optional<String> partidaArantzelariaDescripcio();
	
	@Derived
	public default String articleClient() {
		return article() + codiClient();
	}

	@Derived
	default KeyArticleClient clau() { return  KeyArticleClient.of(artInt(), codiClient()); }

	@Derived
	default boolean isActiu() { return "A".equalsIgnoreCase(flag()); }
	
	public static QueryArticleClientResponse mapTo(ResultSet resultSet) throws SQLException {
		return QueryArticleClientResponseImpl.builder()
				.artInt(resultSet.getString("artint"))
				.flag(resultSet.getString("aclflg"))
				.referencia(resultSet.getString("aclref"))
				.article(resultSet.getString("aclfab"))
				.codiClient(resultSet.getString("clicod"))
				.empresa(Empresa.getByClau(resultSet.getString("empcod")))
				.denominacio(resultSet.getString("aclden"))
				.nivellTecnic(resultSet.getString("aclnvt"))
				.pesaSeguretat("S".equalsIgnoreCase(resultSet.getString("aclsec")))
				.codiFabrica(resultSet.getString("codfab"))
				.nomFabrica(resultSet.getString("descrip"))
				.preu(resultSet.getBigDecimal("aclpre") != null ? resultSet.getBigDecimal("aclpre") : BigDecimal.ZERO)
				.divisa(resultSet.getString("acldiv"))
				.bloquejatStock(resultSet.getBoolean("bloqueostk"))
				.usuariBloqueigStock(resultSet.getString("usubloq"))
				.unitatsEmbalatge(resultSet.getInt("aclucai"))
				.bossesCaixa(resultSet.getInt("bosxcai"))
				.caixesPalet(resultSet.getInt("aclucap"))
				.tipus(TipusArticleClient.getByTipus(resultSet.getString("tipus")))
				.codiProjectManager(resultSet.getString("acldel"))
				.partidaArantzelariaCodi(Optional.ofNullable(resultSet.getString("partida")))
				.partidaArantzelariaCodiAmes(Optional.ofNullable(resultSet.getString("codiPartida")))
				.partidaArantzelariaDescripcio(Optional.ofNullable(resultSet.getString("descpartara")))
				.build();
	}
	
}
