package ames.permisos.parametres;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class ParametresException {

    public static class NomParametreJaExisteix extends AppException {
        public NomParametreJaExisteix() {
            super(I18N.getLiteral("nom_parametre_ja_existeix"));
        }
    }

    public static class FuncioParametreJaExisteix extends AppException {
        public FuncioParametreJaExisteix() {
            super(I18N.getLiteral("funcio_parametre_ja_existeix"));
        }
    }

    public static class EmpleatParametreJaExisteix extends AppException {
        public EmpleatParametreJaExisteix() {
            super(I18N.getLiteral("empleat_parametre_ja_existeix"));
        }
    }
}
