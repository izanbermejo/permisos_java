package ames.comercial.migracio;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbara;
import ames.comercial.albarans.internal.domain.facturacio.FacturacioLiniaAlbaraImpl;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.facturacio.FacturacioLiniaAlbaraSQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MigracioFacturacioLiniesAlbara {

    @Autowired
    FacturacioLiniaAlbaraSQL facturacioLiniaAlbaraRepository;

    public void migracio() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                SELECT c.empcod, l.fccnum, l.fclalb, l.alblin, l.fclq
                 FROM faclin l
                 LEFT JOIN faccap c ON l.empcod = c.empcod AND l.fccnum = c.fccnum AND l.fcctip = c.fcctip
                 WHERE l.fcctip < '5'
                 AND l.tiplin = '0001'
                 AND c.fccdat >= '2026-01-04';
                """);
        AdvantageDao.ResultSetAction<List<FacturacioLiniaAlbara>> rsAction = rs -> {
            var result = new ArrayList<FacturacioLiniaAlbara>();
            while (rs.next()) {
                result.add(FacturacioLiniaAlbaraImpl.builder()
                                .codiFactura(rs.getString("fccnum"))
                                .liniaAlbara(KeyLiniaAlbara.of(
                                        KeyAlbara.of(rs.getLong("fclalb"), rs.getString("empcod")),
                                        rs.getLong("alblin")
                                ))
                                .quantitat(rs.getLong("fclq"))
                        .build()
                );
            }
            return result;
        };
        System.out.println(LocalDateTime.now() + " Iniciant obtenció línies ALBLIN");
        var listAlblin = new AdvantageDao().query(prep, rsAction);
        System.out.println(LocalDateTime.now() + " Total registres alblin a migrar: " + listAlblin.size());
        facturacioLiniaAlbaraRepository.saveBatch(listAlblin);
    }

}

