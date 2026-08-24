package ames.comercial.entrades.internal.infraestructure;

import ames.comercial.shared.KeyArticleClient;

import java.util.Optional;

public interface ConfigArticleClientEntradesRepository {
    Optional<String> getEmpresa(KeyArticleClient articleClient, String codiFabrica);
}
