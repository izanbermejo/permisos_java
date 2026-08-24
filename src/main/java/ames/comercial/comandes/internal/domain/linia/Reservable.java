package ames.comercial.comandes.internal.domain.linia;

public enum Reservable {

    NO,
    TOT,
    PARCIAL,
    RES,
    NO_APLICA;

    public static Reservable of(long quantitatReservada, long quantitatPendent) {
        if (quantitatPendent == 0) return Reservable.NO_APLICA;
        if (quantitatReservada==0) return Reservable.RES;
        else if (quantitatPendent > quantitatReservada) return Reservable.PARCIAL;
        return Reservable.TOT;
    }

}
