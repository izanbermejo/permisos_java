package ames.comercial.albarans.internal.domain.albara;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = InformacioEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioEdi {

    String punto();
    String aclgat();
    String csg3921();
    String mrnNum();
    LocalDate mrnData();
    String mrnType();

}
