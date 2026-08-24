package ames.comercial.inventari.internal.domain.moviment;

import ames.comercial.albarans.internal.domain.albara.CostLogisticImpl;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = CostLogisticImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioDevolucio {

    String parteDevolucio();

}
