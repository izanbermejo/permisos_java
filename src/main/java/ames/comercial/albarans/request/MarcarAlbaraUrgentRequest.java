package ames.comercial.albarans.request;

import ames.comercial.albarans.internal.domain.albara.CostEnviamentExpress;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = MarcarAlbaraUrgentRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface MarcarAlbaraUrgentRequest {

    CostEnviamentExpress costEnviamentExpress();
    boolean isUrgent();
}
