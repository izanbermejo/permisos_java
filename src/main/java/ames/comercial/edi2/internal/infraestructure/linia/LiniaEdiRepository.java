package ames.comercial.edi2.internal.infraestructure.linia;

import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;

public interface LiniaEdiRepository {

    long nextID();
    void updateComentarisInterns (KeyComandaEdi clauLinia, long idLinia, String text);
    void updateComentarisClient (KeyComandaEdi clauLinia, long idLinia, String text);
    void updateComentarisInterns (KeyComandaEdi clauLinia, String text);
    void updateComentarisClient (KeyComandaEdi clauLinia, String text);

}
