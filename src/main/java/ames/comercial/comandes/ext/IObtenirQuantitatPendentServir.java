package ames.comercial.comandes.ext;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;

public interface IObtenirQuantitatPendentServir {

    long executar (KeyArticleClient articleClient, Empresa empresa);

}
