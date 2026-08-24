package ames.comercial.albarans.internal.domain.albara;

import ames.comercial.shared.Divisa;
import ames.comercial.shared.MotiuEnviamentExpres;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.Optional;

@JsonDeserialize(builder = CostEnviamentExpressImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CostEnviamentExpress {

    BigDecimal imp();
    MotiuEnviamentExpres motiu();
    Optional<String> detallMotiu();
    Divisa divisa();

}