package ames.comercial.edi2;

import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;

@SuppressWarnings("serial")
public class EDIException {

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

    public static class ConfigEdiNoTrobada extends AppException {
        public ConfigEdiNoTrobada(String ediBox, String nad02, String codiProv, String tipus) {
            super(I18N.getLiteral("confEdi_no_existeix", ediBox, nad02, codiProv, tipus));
        }
    }

    public static class ComandaNoEsPotLligar extends AppException {
        public ComandaNoEsPotLligar(long idComanda) {
            super(I18N.getLiteral("confEdi_no_existeix", idComanda));
        }
    }

    public static class CapsaleraNoTrobada extends AppException {
        public CapsaleraNoTrobada() {
            super(I18N.getLiteral("comanda_no_existeix"));
        }
    }

    public static class ComandaNoTrobada extends AppException {
        public ComandaNoTrobada() {
            super(I18N.getLiteral("capsalera_no_existeix"));
        }
    }

    public static class ComandaPendentProcessar extends AppException {
        public ComandaPendentProcessar() {
            super(I18N.getLiteral("comanda_pendent_processar"));
        }
    }

    public static class ComandaNoEsPotProcessar extends AppException {
        public ComandaNoEsPotProcessar() {
            super(I18N.getLiteral("comanda_no_processar"));
        }
    }
}
