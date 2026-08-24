package ames.comercial.albarans.internal.domain.embalatgeexpedicio;

public enum TipusElementEmbalatgeExpedicio {

    CAIXA (1),
    PALET (2),
    TAPA_PALET (3),
    SEPARADPOR_CAIXA (4),
    TAPA_CAIXA (5);

    private final int nivell;

    TipusElementEmbalatgeExpedicio(int nivell) {
        this.nivell = nivell;
    }

    public int nivell () {
        return nivell;
    }

}
