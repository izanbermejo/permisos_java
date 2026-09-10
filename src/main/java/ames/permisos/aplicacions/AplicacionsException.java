package ames.permisos.aplicacions;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class AplicacionsException {
    public static class AplicacioAssignada extends AppException {
        public AplicacioAssignada() {
            super(I18N.getLiteral("aplicacio_assignada"));
        }
    }

    public static class NomAplicacioJaExisteix extends AppException {
        public NomAplicacioJaExisteix() {
            super(I18N.getLiteral("nom_aplicacio_ja_existeix"));
        }
    }

    public static class AplicacioNoTrobadaPermisos extends AppException {
        public AplicacioNoTrobadaPermisos() {
            super(I18N.getLiteral("aplicacio_no_trobada_permisos"));
        }
    }

}
