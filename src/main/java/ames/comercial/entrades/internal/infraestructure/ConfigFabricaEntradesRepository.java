package ames.comercial.entrades.internal.infraestructure;

import ames.comercial.entrades.internal.domain.ConfigFabricaEntrades;

import java.util.Optional;

public interface ConfigFabricaEntradesRepository {
    Optional<ConfigFabricaEntrades> get(String codiFabrica);
}
