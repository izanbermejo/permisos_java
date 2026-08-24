package ames.comercial.albarans.internal.services;

/**
 * Avís que es genera quan la quantitat a servir d'una línia d'albarà no encaixa amb les unitats
 * d'embalatge de l'article-client. Reprodueix els controls del codi legacy {@code mantalb.pas}
 * (funció {@code TestQEmb}), que des del 10/11/2010 són avisos i no bloquejos.
 * <p>
 * Es calcula a la proposta de creació d'albarà i és merament informatiu: no impedeix la creació.
 */
public enum AvisEmbalatge {

    /** Les unitats per caixa (Advantage {@code ACLUCAI}) són zero: cal revisar l'article-client. */
    SENSE_EMBALATGE_DEFINIT(Severitat.ERROR),

    /** Hi ha caixes per palet definides i la quantitat no és múltiple de (unitats/caixa × caixes/palet). */
    NO_MULTIPLE_CONDICIONAMENT(Severitat.AVIS),

    /** No hi ha caixes per palet i la quantitat no és múltiple de les unitats per caixa, en un magatzem controlat internament (no es poden obrir caixes). */
    NO_MULTIPLE_CAIXA(Severitat.AVIS),

    /** No hi ha caixes per palet i la quantitat no és múltiple de les unitats per caixa, en un magatzem de plataforma (informatiu: la sortida es pot fer). */
    NO_MULTIPLE_CAIXA_PLATAFORMA(Severitat.INFO);

    private final Severitat severitat;

    AvisEmbalatge(Severitat severitat) {
        this.severitat = severitat;
    }

    public Severitat severitat() {
        return severitat;
    }

    public enum Severitat {
        INFO, AVIS, ERROR
    }

}
