package ames.comercial.shared;

import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;

import java.io.File;

public class SharedExceptions {

    public static class ClientNoExisteix extends AppException {
        public ClientNoExisteix (String client) {
            super(I18N.getLiteral("client_no_existeix", client));
        }
    }

    public static class MagatzemNoExisteix extends AppException {
        public MagatzemNoExisteix (String magatzem) {
            super(I18N.getLiteral("magatzem_no_existeix", magatzem));
        }
    }

    public static class ArticleClientNotFound extends AppException {
        public ArticleClientNotFound (KeyArticleClient articleClient) {
            super(I18N.getLiteral("article_client_no_existeix", articleClient));
        }
        public ArticleClientNotFound (String articleClient) {
            super(I18N.getLiteral("article_client_no_existeix", articleClient.toUpperCase()));
        }
    }

    public static class ArticleNoCorrespon extends AppException {
        public ArticleNoCorrespon (ArticleClientInformacioComandaResponse articleClient, String client) {
            super(I18N.getLiteral("article_client_no_correspon", articleClient.aclfab(),client));
        }
        public ArticleNoCorrespon (String articleClient) {
            super(I18N.getLiteral("article_client_no_correspon", articleClient,articleClient.substring(7)));
        }
    }

    public static class ArticleJaExistent extends AppException {
        public ArticleJaExistent (KeyArticleClient articleClient) {
            super(I18N.getLiteral("article_client_ja_existent",articleClient.artint()+articleClient.clicod()));
        }
        public ArticleJaExistent (String articleClient) {
            super(I18N.getLiteral("article_client_ja_existent",articleClient));
        }
    }

    public static class AdjuntMateixNom extends AppException {
        public AdjuntMateixNom (String nom) {
            super(I18N.getLiteral("adjunt_mateix_nom", nom));
        }
    }

    public static class InvalidEmail extends AppException {
        public InvalidEmail (String emails) {
            super(I18N.getLiteral("emails_invalids", emails));
        }
    }

    public static class PathNotFound extends AppException {
        public PathNotFound (File file) {
            super(I18N.getLiteral("path_not_found", file.getAbsolutePath()), 404);
        }
    }

    public static class IOExceptionWrapper extends AppException {
        public IOExceptionWrapper (Throwable error, String file) {
            super(I18N.getLiteral("io_file_error", file), error);
        }
    }

    public static class PdfErrorGeneracio extends AppException {
        public PdfErrorGeneracio (Throwable error) {
            super(I18N.getLiteral("error_generacio_pdf", error));
        }
    }

    public static class TarifaFiltresNoDefinidaEmpresaDivisa extends AppException {
        public TarifaFiltresNoDefinidaEmpresaDivisa (Empresa empresa, Divisa divisa) {
            super(I18N.getLiteral("tarifa_filtres_no_definida_empresa_divisa", empresa.clau(), empresa.name(), divisa.symbol()));
        }
    }

}
