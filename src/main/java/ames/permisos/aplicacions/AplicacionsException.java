package ames.permisos.aplicacions;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class AplicacionsException {
    public static class AplicacioAssignada extends AppException {
        public AplicacioAssignada() {
            super(I18N.getLiteral("aplicacio_assignada"));
        }
    }
}
