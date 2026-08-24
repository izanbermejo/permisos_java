package ames.comercial.edi2.internal.domain.embalatgeexpedicio;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = ElementEmbalatgeExpedicioImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ElementEmbalatgeExpedicio {

    TipusElementEmbalatgeExpedicio tipus();
    boolean isRetornable();
    String referencia();
    String descripcio();
    String codiElement();
    Optional<Integer> factorMultiplicador();
    Optional<Integer> numElementsFixes();

    static ElementEmbalatgeExpedicio caixaDefecte() {
        return ElementEmbalatgeExpedicioImpl.builder()
                .tipus(TipusElementEmbalatgeExpedicio.CAIXA)
                .isRetornable(false)
                .referencia("CARTON BOX")
                .descripcio("Caixa de cartró estàndard")
                .codiElement("C")
                .build();
    }

    static ElementEmbalatgeExpedicio paletDefecte() {
        return ElementEmbalatgeExpedicioImpl.builder()
                .tipus(TipusElementEmbalatgeExpedicio.PALET)
                .isRetornable(false)
                .referencia("WOODEN PALET")
                .descripcio("Palet de fusta estàndard")
                .codiElement("C")
                .build();
    }

}
