package ames.comercial.comandes.internal.infraestructure.editoalbarans;

import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDateTime;
import java.util.List;

public interface EdiToAlbaransRepository {

    void save (KeyArticleClient articleClient, String comandaClient, ComandaMissatgeEDI missatge);

    List<EdiToAlbaransRecord> pendingEdiToAlbarans();

    void marcaProcessat(EdiToAlbaransRecord reg);

    record EdiToAlbaransRecord(KeyArticleClient articleClient, String comandaClient, ComandaMissatgeEDI missatge, LocalDateTime datareg) {}
}
