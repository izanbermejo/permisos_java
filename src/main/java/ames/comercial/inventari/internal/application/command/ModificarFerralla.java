package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.InventariException;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.domain.service.CrearMovimentAltres;
import ames.comercial.inventari.internal.domain.service.CrearMovimentAltresRequestImpl;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ModificarFerralla {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    public void executar(long id, ModificarFerrallaRequest request) {
        if (request.quantitat() <= 0) throw new InventariException.QuantitatHaDeSerPositiva();
        var old = movimentRepository.findById(id)
                .orElseThrow(InventariException.MovimentNoTrobat::new);
        if (!TipusMoviment.FERRALLA.equals(old.tipus())) {
            throw new InventariException.NoEsFerralla();
        }
        actualitzarFitxa.executar(old, true);
        movimentRepository.deleteById(id);
        var crearRequest = CrearMovimentAltresRequestImpl.builder()
                .articleClient(old.articleClient())
                .empresa(old.empresa())
                .magatzem(old.magatzem())
                .data(request.data())
                .quantitat(-request.quantitat())
                .tipus(TipusMoviment.FERRALLA)
                .observacions(request.observacions())
                .build();
        var nouMoviment = new CrearMovimentAltres().executar(crearRequest);
        movimentRepository.save(nouMoviment);
        actualitzarFitxa.executar(nouMoviment);
    }

    @JsonDeserialize(builder = ModificarFerrallaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ModificarFerrallaRequest {
        LocalDate data();
        Long quantitat();
        Optional<String> observacions();
    }

}
