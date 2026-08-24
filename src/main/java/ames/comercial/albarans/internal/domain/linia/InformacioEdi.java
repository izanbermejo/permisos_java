package ames.comercial.albarans.internal.domain.linia;

import ames.comercial.albarans.internal.domain.albara.InformacioEdiImpl;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = InformacioEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioEdi {

    String punto();
    String aclgat();
    String csg3921();

}
