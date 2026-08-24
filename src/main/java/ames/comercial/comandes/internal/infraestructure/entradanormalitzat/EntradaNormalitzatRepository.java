package ames.comercial.comandes.internal.infraestructure.entradanormalitzat;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Pair;

import java.util.List;

public interface EntradaNormalitzatRepository {

    void save (KeyArticleClient articleClient, Empresa empresa);
    List<Pair<KeyArticleClient, Empresa>> obtenirPendents();
    void delete (KeyArticleClient articleClient, Empresa empresa);

}
