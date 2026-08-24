package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = QueryComandesPerEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryComandesPerEDIResponse {
	LocalDate dataClient();
	LocalDate dataAMES();
//	LocalDate dataMagatzem();
	Integer quantitat();
	String tipus();
	Long codiComanda();
	Long codiLinia();
	String codiComandaClient();
//	Optional<String> comentaris();

//		public static QueryClientEDIResponse mapTo(ResultSet resultSet) throws SQLException {
//			return QueryClientEDIResponseImpl.builder()
//					.codi(resultSet.getString("codcli"))
//					.nom(resultSet.getString("nom"))
////					.usuariLogistica(resultSet.getString("usulogis"))
//					.edibox(resultSet.getString("ediboxe"))
//					.nad(resultSet.getString("NAD02"))
//					.diesDeSortida(resultSet.getString("diessor"))
//					.build();
//		}
}
