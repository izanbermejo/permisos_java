package ames.permisos.shared;

import ames.permisos.server.I18N;
import ames.permisos.server.exception.AppException;

public class OFExceptions {

    public static class OFNoExisteixPerArticleClient extends AppException {
        public OFNoExisteixPerArticleClient (KeyArticleClient numero) {
            super(I18N.getLiteral("of_no_existeix_artcli", numero));
        }
    }

    public static class OFNoExisteixPerNumero extends AppException {
        public OFNoExisteixPerNumero(long numero) {
            super(I18N.getLiteral("of_no_existeix_numero", numero));
        }
    }

    public static class NoEsUltimaOF extends AppException {
        public NoEsUltimaOF(long numero) {
            super(I18N.getLiteral("of_no_ultima", numero));
        }
    }

    public static class OFNoNormalitzada extends AppException {
        public OFNoNormalitzada(long numero) {
            super(I18N.getLiteral("of_no_normalitzada", numero));
        }
    }

    public static class OFEstaAnulada extends AppException {
        public OFEstaAnulada() {
            super(I18N.getLiteral("of_ja_esta_anulada"));
        }
    }

}
