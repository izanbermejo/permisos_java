package ames.comercial.edi2.request;

import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(builder = ProcessarComandaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ProcessarComandaRequest {

    long idMissatge();
    long idComanda();
    String artInt();
    String cliCod();
    List<ProcessarLiniesComandaReq> linies();

}
