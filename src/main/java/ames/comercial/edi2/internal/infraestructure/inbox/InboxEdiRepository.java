package ames.comercial.edi2.internal.infraestructure.inbox;

import ames.comercial.edi2.internal.domain.Inbox;

import java.util.Map;
import java.util.Optional;

public interface InboxEdiRepository {

    int nextId();
    Optional<Inbox> get (long id);
    boolean exists (String id);

    void save (Inbox inbox);
    void marcaPdfLligat (long id, String path);
    void marcaProcessat(long id);
    void marcaError (long id, String error);
    Map<Integer, String> obtenirPendentsPerLligar();
    Map<Integer, String> obtenirPendentsPerProcessar();
}
