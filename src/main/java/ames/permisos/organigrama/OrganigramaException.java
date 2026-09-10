package ames.permisos.organigrama;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class OrganigramaException {

    public static class EmpleatNoTrobatPermisos extends AppException {
        public EmpleatNoTrobatPermisos() {
            super(I18N.getLiteral("empleat_no_trobat_permisos"));
        }
    }
}
