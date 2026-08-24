package ames.comercial.comandes.internal.domain.service;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;

public interface IRecalculReservesArticleclient {
    void executar(KeyLiniaComanda linia);
    void executar(KeyArticleClient articleClient, Empresa empresa);
}
