package ames.comercial.edi2.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = AssignacioManualRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface AssignacioManualRequest {

    long idMissatge();
    long idComanda();
    String artInt();
    String cliCod();

}
