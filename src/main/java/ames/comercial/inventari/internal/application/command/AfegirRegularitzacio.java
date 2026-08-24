package ames.comercial.inventari.internal.application.command;

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
public class AfegirRegularitzacio {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    public void executar(AfegirRegularitzacioRequest request) {
        var crearRequest = CrearMovimentAltresRequestImpl.builder()
                .articleClient(KeyArticleClient.of(request.artint(), request.clicod()))
                .empresa(request.empresa())
                .magatzem(request.magatzem())
                .data(request.data())
                .quantitat(request.quantitat())
                .tipus(TipusMoviment.REGULARITZACIO)
                .observacions(request.observacions())
                .build();
        var moviment = new CrearMovimentAltres().executar(crearRequest);
        movimentRepository.save(moviment);
        actualitzarFitxa.executar(moviment);
    }

    @JsonDeserialize(builder = AfegirRegularitzacioRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AfegirRegularitzacioRequest {
        String artint();
        String clicod();
        String empresa();
        String magatzem();
        LocalDate data();
        Long quantitat();
        Optional<String> observacions();
    }

}
