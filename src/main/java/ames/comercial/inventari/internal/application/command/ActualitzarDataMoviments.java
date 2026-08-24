package ames.comercial.inventari.internal.application.command;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.inventari.ext.IActualitzarDataMoviments;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.MovimentImpl;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Canvia la data dels moviments d'inventari associats a unes línies d'albarà. S'usa quan es rectifica
 * la data d'un albarà, perquè els seus moviments (que en van heretar la data en crear-se) no quedin amb
 * la data antiga.
 * <p>
 * Segueix el mateix patró que la modificació d'altres moviments ({@code ModificarRegularitzacio},
 * {@code ModificarFerralla}): s'obtenen els moviments, es reconstrueixen en memòria amb la nova data i
 * es tornen a desar eliminant els antics, de manera que passen per les validacions del domini. A
 * diferència d'aquells, no cal revertir ni reaplicar la fitxa d'estoc: només canvia la data, i article,
 * empresa, magatzem, tipus i quantitat es mantenen, per la qual cosa l'efecte sobre l'estoc és idèntic.
 */
@Service
public class ActualitzarDataMoviments implements IActualitzarDataMoviments {

    @Autowired MovimentRepository movimentRepository;

    @Override
    public void executar(List<KeyLiniaAlbara> idLiniesAlbara, LocalDate data) {
        var moviments = movimentRepository.findByLiniaAlbara(idLiniesAlbara);
        if (moviments.isEmpty()) {
            return;
        }
        var actualitzats = moviments.stream()
                .map(moviment -> (Moviment) MovimentImpl.builder().from(moviment).data(data).build())
                .toList();
        movimentRepository.delete(idLiniesAlbara);
        movimentRepository.save(actualitzats);
    }

}
