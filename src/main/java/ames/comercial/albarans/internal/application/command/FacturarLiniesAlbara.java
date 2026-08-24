package ames.comercial.albarans.internal.application.command;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.AlbaransException.LiniaAlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbara;
import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbaraImpl;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.facturacio.FacturacioLiniaAlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.server.BeanUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FacturarLiniesAlbara {

    @Autowired FacturacioLiniaAlbaraRepository facturacioLiniaAlbaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;
    @Autowired CanviarFacturatAlbara canviarFacturatAlbara;

    @Transactional
    public void executar(List<FacturarLiniesAlbaraItemRequest> liniesFacturar) {
        for (var liniaFacturar : liniesFacturar) {
            // Anotació de la línia facturada a la taula de facturació de línies d'albarà
            var facturacioLiniaAlbara = toFacturacioLiniaAlbara(liniaFacturar);
            facturacioLiniaAlbaraRepository.save(facturacioLiniaAlbara);
            // Recuperació de la línia d'albarà a facturar
            var liniaAlbara = liniaAlbaraRepository.find(liniaFacturar.clauLiniaAlbara())
                    .orElseThrow(() -> new LiniaAlbaraNoExisteix(liniaFacturar.clauLiniaAlbara()));
            // Facturació de la línia
            var liniaAlbaraFacturada = liniaAlbara.facturar(liniaFacturar.quantitat());
            liniaAlbaraRepository.save(liniaAlbaraFacturada);
            // Rèplica a l'Advantage de la línia facturada
            ReplicaAdvantage.instance().addLiniaAlbaraDeleteInsert(liniaAlbaraFacturada);
        }
        // Es marquen tots els albarans com a facturats
        liniesFacturar.stream()
                .map(FacturarLiniesAlbaraItemRequest::clauLiniaAlbara)
                .map(KeyLiniaAlbara::idAlbara)
                .distinct()
                .forEach(codiAlbara -> BeanUtils.getBean(CanviarFacturatAlbara.class).executar(codiAlbara, true));
    }

    private FacturacioLiniaAlbara toFacturacioLiniaAlbara(FacturarLiniesAlbaraItemRequest liniaFacturar) {
        return FacturacioLiniaAlbaraImpl.builder()
                .liniaAlbara(liniaFacturar.clauLiniaAlbara())
                .codiFactura(liniaFacturar.codiFactura())
                .quantitat(liniaFacturar.quantitat())
                .build();
    }

    @JsonDeserialize(builder = FacturarLiniesAlbaraItemRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface FacturarLiniesAlbaraItemRequest {
        String empresa();
        String codiFactura();
        long codiAlbara();
        long liniaAlbara();
        long quantitat();

        @Derived
        default KeyLiniaAlbara clauLiniaAlbara() {
            return KeyLiniaAlbara.of(empresa(), codiAlbara(), liniaAlbara());
        }
    }

}
