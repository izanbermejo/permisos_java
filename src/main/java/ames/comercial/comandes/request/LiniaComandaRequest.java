package ames.comercial.comandes.request;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.server.I18N;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import org.immutables.value.Value;
import org.immutables.value.Value.Check;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaComandaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaComandaRequest {

	long quantitat();
	KeyArticleClient articleClient();
	LocalDate dataSolicitada();
	LocalDate dataPrevistaSortida();
	Optional<LocalDate> dataPrevistaSortidaInterna();
	Optional<LocalDate> dataConfirmadaFabrica();
	TipusLiniaComanda tipus();
	BigDecimal preu();
	String divisa();
	boolean isPreuFixat();
	Optional<Long> comandaBlanca();
	Optional<String> comentarisClient();
	Optional<String> comentarisInterns();
	
	@Check
	default void check () {
		Preconditions.checkArgument(quantitat() >= 0, I18N.getLiteral("quantitat_major_zero"));
	}
	
}
