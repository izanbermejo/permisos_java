package ames.comercial.edi2.internal.domain;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniaMergeImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaMerge {

    LocalDate data();
    Optional<IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse> actual();
    Optional<LiniaEdi> edi();
    Optional<LiniesDeute> deute();
    long quantitatNova();
    boolean artificial();

}