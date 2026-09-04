package ames.permisos.moduls;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class ModulsException {
    public static class ModulAssignat extends AppException {
        public ModulAssignat() {
            super(I18N.getLiteral("modul_assignat"));
        }
    }

    public static class NomModulJaExisteix extends AppException {
        public NomModulJaExisteix() {
            super(I18N.getLiteral("nom_modul_ja_existeix"));
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
