package ames.comercial.edi.internal.infraestructure.comandaEDI.mapper;

import ames.comercial.edi.internal.domain.DadesComandaEDI;
import org.immutables.value.Value;

@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ComandaEDIRecord {

	DadesComandaEDI dades();

}
