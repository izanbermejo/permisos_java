package ames.comercial.advantage;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.auxiliar.generators.*;

import java.util.ArrayList;
import java.util.List;

public class ReplicaAdvantageExecutador {

    AdvantageDao adsDao = new AdvantageDao();
    List<PreparedStatementProvider> statements = new ArrayList<PreparedStatementProvider>();
    ReplicaAdvantageData data = ReplicaAdvantage.instance();

    public void executar() {
        // DELETE al albcap
        if (!data.albaraDelete().isEmpty())
            statements.add(AlbCapGenerator.delete(data.albaraDelete()));
        // INSERT al albcap
        if (!data.albaraInsert().isEmpty())
            statements.add(AlbCapGenerator.insert(data.albaraInsert()));
        // DELETE al alblin
        if (!data.liniaAlbaraDelete().isEmpty())
            statements.add(AlbLinGenerator.delete(data.liniaAlbaraDelete()));
        // INSERT al alblin
        if (!data.liniaAlbaraInsert().isEmpty())
            statements.add(AlbLinGenerator.insert(data.liniaAlbaraInsert()));
        // UPDATE al albcap per canvi de facturat
        if (!data.updateFacturatAlbara().isEmpty())
            statements.add(AlbCapGenerator.updateFacturat(data.updateFacturatAlbara()));
        // UPDATE al albcap per canvi d'autofacturable
        if (!data.updateFacturacioAutomaticaAlbara().isEmpty())
            statements.add(AlbCapGenerator.updateFacturacioAutomatica(data.updateFacturacioAutomaticaAlbara()));
        // DELETE al penabo (pendents d'abonar dels traspassos abonables); abans dels inserts, perquè
        // canviar la data d'un albarà es replica esborrant amb la data antiga i inserint amb la nova
        if (!data.penAboDelete().isEmpty())
            statements.add(PenAboGenerator.delete(data.penAboDelete()));
        // INSERT al penabo
        if (!data.penAboInsert().isEmpty())
            statements.add(PenAboGenerator.insert(data.penAboInsert()));
        // UPDATE al penabo per increment de quantitat d'una línia ja existent
        if (!data.penAboIncrement().isEmpty())
            statements.add(PenAboGenerator.incrementar(data.penAboIncrement()));
        try {
            adsDao.executeUpdate(statements);
        } finally {
            ReplicaAdvantage.cleanThreadLocal();
        }
    }

}
