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

    public static class FuncioModulJaExisteix extends AppException {
        public FuncioModulJaExisteix() {
            super(I18N.getLiteral("funcio_modul_ja_existeix"));
        }
    }

    public static class EmpleatModulJaExisteix extends AppException {
        public EmpleatModulJaExisteix() {
            super(I18N.getLiteral("empleat_modul_ja_existeix"));
        }
    }

}
