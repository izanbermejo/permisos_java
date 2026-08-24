package ames.comercial.edi2.internal.domain.embalatgeexpedicio;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = EmbalatgeExpedicioImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EmbalatgeExpedicio {

    // Retornables
    Optional<ElementEmbalatgeExpedicio> nivell1Retornable();
    Optional<ElementEmbalatgeExpedicio> nivell2Retornable();
    Optional<ElementEmbalatgeExpedicio> nivell3Retornable();
    Optional<ElementEmbalatgeExpedicio> nivell4Retornable();
    Optional<ElementEmbalatgeExpedicio> nivell5Retornable();

    // No retornables
    Optional<ElementEmbalatgeExpedicio> nivell1NoRetornable();
    Optional<ElementEmbalatgeExpedicio> nivell2NoRetornable();
    Optional<ElementEmbalatgeExpedicio> nivell3NoRetornable();
    Optional<ElementEmbalatgeExpedicio> nivell4NoRetornable();
    Optional<ElementEmbalatgeExpedicio> nivell5NoRetornable();

}
