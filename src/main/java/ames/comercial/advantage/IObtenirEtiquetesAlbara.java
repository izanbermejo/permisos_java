package ames.comercial.advantage;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.util.List;

public interface IObtenirEtiquetesAlbara {

    List<EtiquetaTransportAlbara> executar (KeyAlbara keyAlbara);

    @JsonDeserialize(builder = EtiquetaTransportAlbaraImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface EtiquetaTransportAlbara {
        KeyLiniaAlbara clauLiniaAlbara();
        long etiquetaTransport();
        long etiquetaProduccioDesde();
        long etiquetaProduccioFins();
        String lot();
        long quantitatPecesPerCaixa();
        String codiFabrica();
        LocalDate dataFabricacio();

        @Derived
        default long quantitatCaixes() {
            return (etiquetaProduccioFins() - etiquetaProduccioDesde() + 1);
        }

        @Derived
        default long quantitatPecesTotal() {
            return quantitatCaixes() * quantitatPecesPerCaixa();
        }

        /**
         * En el cas que l'etiqueta de transport sigui igual a l'etiqueta de producció
         * @return true quan es tracta d'una caixa suelta
         */
        @Derived
        default boolean isCaixaSuelta() {
            return etiquetaTransport() == etiquetaProduccioDesde()
                    && etiquetaTransport() == etiquetaProduccioFins();
        }

    }

}
