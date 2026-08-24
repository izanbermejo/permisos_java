package ames.comercial.edi2.internal.domain.linia;

import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.linia.bloc.DR;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaEdi {
    long idComanda();
    long idLinia();
    Optional<String> comentarisInterns();
    Optional<String> comentarisClient();
    DA da();
    Optional<DR> dr();

    @Value.Derived
    default long quantitatNova(){return da().cantidad();}

    //si el client te marcat com a dosdates cal mostrar com a data solicitada la dataFinal.
    default LocalDate dataSolicitada(boolean isDuesDates){
        if (isDuesDates){
            return da().fechaFinal().orElse(null);
        } else {
            return da().fechaInicial().orElse(null);

        }
    }

    //si el client te marcat com a dosdates cal mostrar com a data solicitada la dataInicial.
    default LocalDate dataSortidaAMES(boolean isDuesDates){
        if (isDuesDates){
            return da().fechaInicial().orElse(null);
        } else {
            return da().fechaFinal().orElse(null);
        }
    }

    default Optional<String> numeroComanda(){return da().numeroRan();}

}
