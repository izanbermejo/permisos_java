package ames.comercial.inventari.ext;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;

public interface IIncrementarReservaArticleclient {

    void executar (KeyArticleClient articleClient, Empresa empresa, long quantitat);

}
