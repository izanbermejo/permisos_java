package ames.comercial.tarifes.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = DadesPreuImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DadesPreu {
	Long codi_tarifa();
	String artInt ();
	String aclFab ();
	String aclRef ();
	Double pr01();
	Double pr02();
	Double pr03();
	Double pr04();
	Double pr05();
	Double pr06();
	Double pr07();
	Double pr08();
	Double pr09();
	Double pr10();
	Double pr11();
	Double pr12();
	Integer unsBos();
	Integer unsCai();
}
