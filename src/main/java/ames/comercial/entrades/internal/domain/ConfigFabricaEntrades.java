package ames.comercial.entrades.internal.domain;

import org.immutables.value.Value;

@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ConfigFabricaEntrades {
    String codiFabrica();
    String magatzem();
    String empresa();
}
