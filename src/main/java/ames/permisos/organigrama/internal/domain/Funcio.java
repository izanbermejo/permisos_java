package ames.permisos.organigrama.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Map;
import java.util.Optional;

@JsonDeserialize(builder = FuncioImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Funcio {
    Optional<Integer> idCentre();
    Optional<String> nomCentre();
    Optional<Integer> idDepartament();
    Optional<Map<String, String>> nomDepartament();
    Optional<Integer> idFuncio();
    Optional<Map<String, String>> nomFuncio();
}
