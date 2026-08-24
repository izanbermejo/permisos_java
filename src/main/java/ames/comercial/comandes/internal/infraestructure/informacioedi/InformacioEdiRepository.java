package ames.comercial.comandes.internal.infraestructure.informacioedi;

import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InformacioEdiRepository {

    void save (List<InformacioEdi> listInformacioEdi);
    Optional<KeyComandaEdi> findKeyComandaEdi(String comandaClient, LocalDate data, KeyArticleClient articleClient);

    record InformacioEdi(String comandaClient, LocalDate data, KeyArticleClient articleClient, long quantitat, KeyComandaEdi comandaEdi) {}
}
