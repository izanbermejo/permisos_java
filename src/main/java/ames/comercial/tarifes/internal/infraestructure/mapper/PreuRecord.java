package ames.comercial.tarifes.internal.infraestructure.mapper;

import ames.comercial.tarifes.internal.domain.DadesPreu;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import org.immutables.value.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface PreuRecord {
	DadesPreu dades();
}