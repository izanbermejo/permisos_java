package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.InsertStatement;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDate;

public class HisGenerator {

    public static PreparedStatementProviderBatch crearRegistre(KeyArticleClient articleClient, long quantitat,
                                                               String empresa, String magatzem, LocalDate dataEntrada,
                                                               String fabrica, long of, String entrada) {
        return conn -> new InsertStatement(conn, "his")
                .add("artint", articleClient.artint())
                .add("clicod", articleClient.clicod())
                .add("empcod", empresa)
                .add("magcod", magatzem)
                .add("hisdia", dataEntrada)
                .add("histip", 1)
                .add("hisqua", quantitat)
                .add("hisfab", fabrica)
                .add("hisof", of)
                .add("hisalb", entrada)
                .add("hislalb", "000")
                .add("diareg", LocalDate.now())
                .add("usuari", "[ENTRADES]")
                .build();
    }

}
