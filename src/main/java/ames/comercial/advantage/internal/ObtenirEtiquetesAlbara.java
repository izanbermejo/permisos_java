package ames.comercial.advantage.internal;

import ames.comercial.advantage.EtiquetaTransportAlbaraImpl;
import ames.comercial.advantage.IObtenirEtiquetesAlbara;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObtenirEtiquetesAlbara implements IObtenirEtiquetesAlbara {

    @Override
    public List<EtiquetaTransportAlbara> executar(KeyAlbara keyAlbara) {
        PreparedStatementProvider prep = conn -> {
            var ps = conn.prepareStatement("""
                SELECT *
                FROM etiqalbs
                WHERE empcod = ? AND albcod = ?;
            """);
            ps.setString(1, keyAlbara.empresa());
            ps.setString(2, keyAlbara.codiFormat());
            return ps;
        };
        return new AdvantageDao().query(prep, mapeig());
    }

    private ResultSetAction<List<EtiquetaTransportAlbara>> mapeig() {
        return rs -> {
            var resultat = new ArrayList<EtiquetaTransportAlbara>();
            while (rs.next()) {
                resultat.add(EtiquetaTransportAlbaraImpl.builder()
                        .clauLiniaAlbara(KeyLiniaAlbara.of(rs.getString("empcod"), rs.getLong("albcod"), rs.getLong("alblin")))
                        .etiquetaTransport(rs.getLong("etitransp"))
                        .etiquetaProduccioDesde(rs.getLong("eticajd"))
                        .etiquetaProduccioFins(rs.getLong("eticajh"))
                        .lot(rs.getString("lot"))
                        .quantitatPecesPerCaixa(rs.getLong("qcaja"))
                        .codiFabrica(rs.getString("fabcod"))
                        .dataFabricacio(rs.getDate("datafab").toLocalDate())
                        .build());
            }
            return resultat;
        };
    }

}
