package ames.comercial.edi2.internal.domain.embalatgeexpedicio;

public enum TipusElementEmbalatgeExpedicio {

    CAIXA (1),
    PALET (2),
    TAPA_PALET (3),
    SEPARADOR_CAIXA (4),
    TAPA_CAIXA (5);

    private final int nivell;

    TipusElementEmbalatgeExpedicio(int nivell) {
        this.nivell = nivell;
    }

    public int nivell () {
        return nivell;
    }

    public static TipusElementEmbalatgeExpedicio fromNivell(int nivell) {
        for (TipusElementEmbalatgeExpedicio tipus : values()) {
            if (tipus.nivell == nivell) {
                return tipus;
            }
        }
        throw new IllegalArgumentException("Nivell no válido: " + nivell);
    }
}
