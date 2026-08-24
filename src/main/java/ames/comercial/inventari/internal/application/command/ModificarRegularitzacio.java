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
public class ModificarRegularitzacio {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    public void executar(long id, ModificarRegularitzacioRequest request) {
        var old = movimentRepository.findById(id)
                .orElseThrow(InventariException.MovimentNoTrobat::new);
        if (!TipusMoviment.REGULARITZACIO.equals(old.tipus())) {
            throw new InventariException.NoEsRegularitzacio();
        }
        // Revertir l'efecte de l'antic moviment a la fitxa
        actualitzarFitxa.executar(old, true);
        // Eliminar l'antic moviment
        movimentRepository.deleteById(id);
        // Crear el nou moviment amb les dades actualitzades
        var crearRequest = CrearMovimentAltresRequestImpl.builder()
                .articleClient(old.articleClient())
                .empresa(old.empresa())
                .magatzem(old.magatzem())
                .data(request.data())
                .quantitat(request.quantitat())
                .tipus(TipusMoviment.REGULARITZACIO)
                .observacions(request.observacions())
                .build();
        var nouMoviment = new CrearMovimentAltres().executar(crearRequest);
        movimentRepository.save(nouMoviment);
        // Aplicar l'efecte del nou moviment a la fitxa
        actualitzarFitxa.executar(nouMoviment);
    }

    @JsonDeserialize(builder = ModificarRegularitzacioRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ModificarRegularitzacioRequest {
        LocalDate data();
        Long quantitat();
        Optional<String> observacions();
    }

}
