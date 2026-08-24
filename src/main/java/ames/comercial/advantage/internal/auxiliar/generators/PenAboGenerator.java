package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.ReplicaAdvantageData.RegistreAbonament;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.server.RequestThread;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Replica els pendents d'abonar dels traspassos abonables a la taula {@code penabo} de l'Advantage, que és
 * la que consumeix el mòdul de facturació per anar abonant.
 * <p>
 * Rèplica de la lògica del legacy {@code reportlegacy/mantalb.pas} (insert a la línia ~5459 i delete a la
 * ~5526). Com allà, el registre es penja de l'<b>empresa de destí</b> ({@code empcod}) i no de la que emet
 * l'albarà, i {@code clifit} porta el mateix valor que {@code codcli}.
 * <p>
 * {@code penabo} no té número de línia: la clau efectiva és
 * {@code (ndevol, empcod, data, artint, codcli)}. Per això no es permet tenir dues línies de la mateixa
 * peça al mateix albarà de traspàs (veure {@code AlbaransException.PecaJaAlAlbara}).
 */
public class PenAboGenerator {

    public static PreparedStatementProviderBatch insert(List<RegistreAbonament> registres) {
        return conn -> new MultipleInsertStatement<RegistreAbonament>(conn, "penabo")
                .add("ndevol", r -> r.albara().id().codiFormat())
                // Empresa de destí: és qui rep la mercaderia i a qui se li abonarà
                .add("empcod", r -> r.albara().empresaTraspas())
                .add("data", r -> r.albara().data())
                .add("codcli", r -> r.linia().articleClient().clicod())
                .add("artint", r -> r.linia().articleClient().artint())
                // Client definitiu: el legacy hi posa el mateix que a codcli
                .add("clifit", r -> r.linia().articleClient().clicod())
                .add("ndevcl", r -> "")
                .add("devq", RegistreAbonament::quantitat)
                .add("preu", r -> r.linia().preu().valor())
                .add("coddiv", r -> r.linia().preu().divisa().symbol())
                .add("usuari", r -> RequestThread.nomUsuari())
                .add("diareg", r -> RequestThread.dateLocal())
                // En crear-lo, tot està pendent d'abonar; el mòdul de facturació és qui va baixant aquest valor
                .add("penabo", RegistreAbonament::quantitat)
                .build(registres);
    }

    public static PreparedStatementProviderBatch delete(List<RegistreAbonament> registres) {
        return conn -> {
            var statement = conn.prepareStatement(
                    "DELETE FROM penabo WHERE ndevol = ? AND empcod = ? AND data = ? AND artint = ? AND codcli = ?");
            for (var r : registres) {
                omplirClau(statement, r);
                statement.addBatch();
            }
            return statement;
        };
    }

    /**
     * Suma una quantitat al registre existent, tant a la quantitat traspassada com al pendent d'abonar.
     * S'utilitza quan la creació automàtica de traspàs incrementa una línia d'un albarà abonable ja obert:
     * no es pot refer el registre de zero perquè es perdria el que el mòdul de facturació ja hagi abonat.
     */
    public static PreparedStatementProviderBatch incrementar(List<RegistreAbonament> registres) {
        return conn -> {
            var statement = conn.prepareStatement("""
                    UPDATE penabo SET devq = devq + ?, penabo = penabo + ?
                    WHERE ndevol = ? AND empcod = ? AND data = ? AND artint = ? AND codcli = ?
                    """);
            for (var r : registres) {
                statement.setLong(1, r.quantitat());
                statement.setLong(2, r.quantitat());
                statement.setString(3, r.albara().id().codiFormat());
                statement.setString(4, r.albara().empresaTraspas());
                statement.setDate(5, Date.valueOf(r.albara().data()));
                statement.setString(6, r.linia().articleClient().artint());
                statement.setString(7, r.linia().articleClient().clicod());
                statement.addBatch();
            }
            return statement;
        };
    }

    private static void omplirClau(PreparedStatement statement, RegistreAbonament r) throws SQLException {
        statement.setString(1, r.albara().id().codiFormat());
        statement.setString(2, r.albara().empresaTraspas());
        statement.setDate(3, Date.valueOf(r.albara().data()));
        statement.setString(4, r.linia().articleClient().artint());
        statement.setString(5, r.linia().articleClient().clicod());
    }

}
