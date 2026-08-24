package ames.comercial.entrades;

import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;

public class EntradesException {
    public static class NoPotReprocessar extends AppException {
        public NoPotReprocessar() { super(I18N.getLiteral("no_pot_reprocessar")); }
    }

    public static class NoTrobaEntrada extends AppException {
        public NoTrobaEntrada() { super(I18N.getLiteral("no_troba_entrada")); }
    }

    public static class ErrorEnviantCorreuErrorEntrada extends AppException {
        public ErrorEnviantCorreuErrorEntrada (Throwable cause) {
            super(I18N.getLiteral("error_enviant_correu_error_comanda"), cause);
        }
    }
}
