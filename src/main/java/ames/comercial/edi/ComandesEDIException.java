package ames.comercial.edi;

import ames.comercial.server.exception.AppException;

@SuppressWarnings("serial")
public class ComandesEDIException {

    public static class ErrorDeConfiguracio extends AppException {
        public ErrorDeConfiguracio(String details) {
            super("Error de configuracio: " + details); // TODO i18n
        }
    }

    public static class ClientEDISenseProfile extends AppException {
        public ClientEDISenseProfile() {
            super("Perfil de client no trobat"); // TODO i18n
        }
    }

    public static class ReferenciaNoValida extends AppException {
        public ReferenciaNoValida() {
            super("Referencia no valida"); // TODO i18n
        }
    }

    public static class MissatgeNoTrobat extends AppException {
        public MissatgeNoTrobat(String pathEdi) {
            super("Missatge EDI no trobat: " + pathEdi); // TODO i18n
        }
    }

}
