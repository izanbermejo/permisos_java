package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IProcessarLiniesComanda {

    void executar (KeyArticleClient articleClient, KeyComandaEdi comandaEdi, List<ProcessarLiniesComandaReq> reqLinies);

    @JsonDeserialize(builder = ProcessarLiniesComandaReqImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ProcessarLiniesComandaReq {

        Optional<KeyLiniaComanda> clauLinia();
        String comandaClient();
        Long quantitat();
        TipusLiniaComanda tipus();
        LocalDate dataSolicitada();
        LocalDate dataPrevistaSortida();
        Optional<LocalDate> dataPrevistaSortidaInterna();
        Optional<String> comentarisClient();
        Optional<String> comentarisInterns();
    }

}
