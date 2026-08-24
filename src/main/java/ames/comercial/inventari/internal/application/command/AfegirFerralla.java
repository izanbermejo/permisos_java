package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.domain.service.CrearMovimentAltres;
import ames.comercial.inventari.internal.domain.service.CrearMovimentAltresRequestImpl;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class AfegirFerralla {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    public void executar(AfegirFerrallaRequest request) {
        if (request.quantitat() <= 0) throw new InventariException.QuantitatHaDeSerPositiva();
        var crearRequest = CrearMovimentAltresRequestImpl.builder()
                .articleClient(KeyArticleClient.of(request.artint(), request.clicod()))
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .quantitat(-request.quantitat())
                .tipus(TipusMoviment.FERRALLA)
                .observacions(request.observacions())
                .build();
        var moviment = new CrearMovimentAltres().executar(crearRequest);
        movimentRepository.save(moviment);
        actualitzarFitxa.executar(moviment);
    }

    @JsonDeserialize(builder = AfegirFerrallaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AfegirFerrallaRequest {
        String artint();
        String clicod();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();
        Optional<String> observacions();
    }

}
