package ames.comercial.comandes.internal.infraestructure.liniacomandacomentaris;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;

public interface LiniaComandaComentarisRepository {

    void updateComentarisInterns (KeyLiniaComanda clauLinia, String text);
    void updateComentarisClient (KeyLiniaComanda clauLinia, String text);

}
