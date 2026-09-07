package ames.permisos.permisos;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class PermisosException {

    public static class NomPermisJaExisteix extends AppException {
        public NomPermisJaExisteix() {
            super(I18N.getLiteral("nom_permis_ja_existeix"));
        }
    }

    public static class FuncioPermisJaExisteix extends AppException {
        public FuncioPermisJaExisteix() {
            super(I18N.getLiteral("funcio_permis_ja_existeix"));
        }
    }

    public static class EmpleatPermisJaExisteix extends AppException {
        public EmpleatPermisJaExisteix() {
            super(I18N.getLiteral("empleat_permis_ja_existeix"));
        }
    }
}
