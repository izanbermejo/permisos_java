package ames.permisos.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.Optional;

@JsonDeserialize(builder = InformacioEnviamentImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioEnviament {

	FormaEnviament formaEnviament();
	Incoterm incoterm();
	String desti();
	Optional<String> transportista();
	Optional<String> zonaTransport();

	/**
	 * @return Condicions enviament en el format advantage
	 * FFIIIDDDD on FF es la forma d'enviament, III l'incoterm i DDDD el destí
	 */
	@Derived
	default String comenv() {
		return formaEnviament().codiAdvantage() + incoterm() + desti();
	}

	static InformacioEnviament desconegut() {
		return InformacioEnviamentImpl.builder()
				.formaEnviament(FormaEnviament.DESCONEGUT)
				.incoterm(Incoterm.EXW)
				.desti("")
				.build();
	}

}
