package ames.comercial.edi.response;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.sql.Date;
import java.util.Optional;

@JsonDeserialize(builder = LiniaEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaEDIResponse {

	Optional<Long> codi();
	Long codiComanda();
	Optional<Long> codiComandaProcessat();
	Optional<Long> codiLiniaProcessat();
	Optional<String> codiComandaClient();
	Optional<String> codiComandaClientProcessat();
	Optional<String> codiLiniaClientProcessat();
	Optional<String> codiLiniaClient();
	Optional<Date> dataClient();
	Optional<Date> dataAMES();
	Optional<Date> dataMagatzem();
	Optional<Date> dataConfirmadaFabrica();
	Integer quantitat ();
	Optional<Integer> quantitatProcessat ();
	Optional<Integer> quantitatPendent();
	Optional<Integer> quantitatAcumulada();
	Optional<Integer> quantitatAcumuladaProcessat();
	Optional<Integer> quantitatAcumuladaPendent();
	Optional<TipusLiniaComanda> tipusProcessat();
	TipusLiniaComanda tipus();
	Optional<String> observacions();
	Optional<String> comentarisAMES();
	Optional<String> alertesAMES();
	Optional<String> ultimAlbara();
	Optional<String> nad();
	String status();
	String codiArticle ();
	String codiArticleAmes ();
	Optional<String> comentarisInterns();
	Optional<String> comentarisClient();
	@Value.Default
	default Boolean processable() {
		return false; // Valor por defecto
	}
	Optional<Date> dataInicial();
	Optional<Date> dataFinal();
	Optional<String> kanban();
	Optional<String> ran();
//	Long stock();


}
