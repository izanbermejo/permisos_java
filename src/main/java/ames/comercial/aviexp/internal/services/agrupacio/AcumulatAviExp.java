package ames.comercial.aviexp.internal.services.agrupacio;

import ames.comercial.advantage.IObtenirEtiquetesAlbara.EtiquetaTransportAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.*;
import java.util.stream.Collectors;

@JsonDeserialize(builder = AcumulatAviExpImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface AcumulatAviExp {

    long totalCaixes();
    long totalPeces();
    Set<Long> etiquetaTransport();
    Set<KeyLiniaAlbara> clauLiniesAlbara();
    List<EtiquetaTransportAlbara> etiquetes();

    static AcumulatAviExp empty(KeyLiniaAlbara liniaAlbara) {
        return AcumulatAviExpImpl.builder()
                .totalCaixes(0)
                .totalPeces(0)
                .etiquetaTransport(Set.of())
                .clauLiniesAlbara(Set.of(liniaAlbara))
                .etiquetes(List.of())
                .build();
    }

    default AcumulatAviExp acumular(EtiquetaTransportAlbara etiqueta, KeyLiniaAlbara liniaAlbara) {
        // S'afegeix la nova etiqueta de transport al conjunt d'etiquetes de transport existents.
        var nousPalets = new LinkedHashSet<>(etiquetaTransport());
        nousPalets.add(etiqueta.etiquetaTransport());
        // S'afegeix la nova etiqueta de transport a la llista d'etiquetes existents.
        var novesEtiquetes = new ArrayList<>(etiquetes());
        novesEtiquetes.add(etiqueta);
        // S'afegeix la clau de línia d'albarà associada a la nova etiqueta de transport al conjunt de claus de línies d'albarà existents.
        var novesClauLiniesAlbara = new LinkedHashSet<>(clauLiniesAlbara());
        novesClauLiniesAlbara.add(liniaAlbara);
        // S'acumulen els totals i s'actualitzen les llistes
        return AcumulatAviExpImpl.builder()
                .totalCaixes(totalCaixes() + etiqueta.quantitatCaixes())
                .totalPeces(totalPeces() + etiqueta.quantitatPecesTotal())
                .etiquetaTransport(nousPalets)
                .clauLiniesAlbara(novesClauLiniesAlbara)
                .etiquetes(novesEtiquetes)
                .build();
    }

    default Set<Long> etiquetesTransportOrdenades() {
        return etiquetaTransport().stream()
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    default List<EtiquetaTransportAlbara> etiquetesProduccioOrdenades(long etiquetaTransport) {
        return etiquetes().stream()
                .filter(e -> e.etiquetaTransport() == etiquetaTransport)
                .sorted(Comparator.comparingLong(EtiquetaTransportAlbara::etiquetaProduccioDesde))
                .toList();
    }

    @Derived
    default boolean isPaletHomogeni() {
        return etiquetes().stream()
                .noneMatch(EtiquetaTransportAlbara::isCaixaSuelta);
    }

    @Derived
    default long numPalets() {
        return etiquetaTransport().size();
    }

}
