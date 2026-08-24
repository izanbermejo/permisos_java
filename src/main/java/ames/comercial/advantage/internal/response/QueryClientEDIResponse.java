package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@JsonDeserialize(builder = QueryClientEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryClientEDIResponse {
	String codi();
	String document();
	String nom();
//	String usuariLogistica();
	String edibox();
	String nad();
	String diesDeSortida();
	Boolean encAlbara();
	Integer diesARestar();
	Boolean plataforma();
	Boolean dosDates();
	Boolean restarFerm();
	Boolean restarPrevisio();
	String fermOrientatiu();
	Boolean editant();

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
