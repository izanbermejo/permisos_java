package ames.comercial.albarans.internal.domain.albara;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = IncidenciaTransportImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface IncidenciaTransport {

    String rao();

}
