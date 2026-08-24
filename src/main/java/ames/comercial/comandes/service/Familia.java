package ames.comercial.comandes.service;

import java.util.HashMap;
import java.util.Map;

public enum Familia {

    O ("O", "Marketing", false, false, false, true, false),
    A ("A", "Coixinet bronze", true, false, false, false, false),
    p ("p", "Coixinet bronze", true, false, false, false, false),
    b ("b", "Coixinet bronze", true, false, false, false, false),
    g ("g", "Coixinet bronze", true, false, false, false, false),
    a ("a", "Coixinet ferro", true, false, false, false, false),
    J ("J", "Filtres", false, false, true, false, false),
    j ("j", "Filtres", false, false, true, false, false),
    l ("l", "Filtres", false, false, true, false, false),
    I ("I", "Barres de bronze normalitzades", true, false, false, false, true),
    i ("i", "Barres de ferro normalitzades", true, false, false, false, true),
    h ("h", "Barres de bronze normalitzades 90/10", true, false, false, false, true),
    r ("r", "Titani Normalitzat", true, false, false, false, true),
    s ("s", "Kit instrumental", true, false, false, false, true),
    DESCONECGUT ("DESCONEGUT", "DESCONEGUT", false, false, false, false, false);

    private static final Map<String, Familia> LOOKUP_MAP;
    public String familia;
    public String descripcio;
    public boolean isNormalitzat;
    public boolean isMedical;
    public boolean isFiltres;
    public boolean isMarketing;
    public boolean isBarra;

    Familia(String familia, String descripcio, boolean isNormalitzat, boolean isMedical, boolean isFiltres, boolean isMarketing, boolean isBarra) {
        this.familia = familia;
        this.descripcio = descripcio;
        this.isNormalitzat = isNormalitzat;
        this.isMedical = isMedical;
        this.isFiltres = isFiltres;
        this.isMarketing = isMarketing;
        this.isBarra = isBarra;
    }

    static {
        LOOKUP_MAP = new HashMap<String, Familia>();
        for (Familia f : Familia.values()) {
            LOOKUP_MAP.put(f.familia, f);
        }
    }

    public static Familia getByFamilia(String familia) {
        var f = LOOKUP_MAP.get(familia);
        return f != null ? f : Familia.DESCONECGUT;
    }

}
