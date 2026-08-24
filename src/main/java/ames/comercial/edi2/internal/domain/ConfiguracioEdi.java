package ames.comercial.edi2.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(builder = ConfiguracioEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ConfiguracioEdi {

    ConfiguracioAviExp configuracioAviExp();
    List<ConfiguracioEntradaComanda> configuracioEntradaComandes();

}
