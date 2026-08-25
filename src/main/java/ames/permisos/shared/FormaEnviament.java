package ames.permisos.shared;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum FormaEnviament {

	DESCONEGUT			("00", "Mitjà de Transport desconegut", "Unknown System",       "Medio de transporte desconocido", "Moyen de Transport Inconu",   "Unbekannt Verkehrszweig",  "Modo di transporto sconosciuto"),
	AVIO				("01", "Per Avió",                     "By Air",                "Por Avión",                       "Par Avion",                   "Per Luftfracht",           "Per Aereo"),
	CAMIO				("02", "Per Camió",                    "By Truck",              "Por Camión",                      "Par Camion",                  "Per LKW",                  "Per Camion"),
	VAIXELL				("03", "Per Mar",                      "By Sea",                "Por Mar",                         "Par Bateau",                  "Seetransport",             "Trasporto Marittimo"),
	COURIER				("04", "Per Courrier",                 "By Courier",            "Por Courrier",                    "Par Courier",                 "Per Kurier",               "Per Courrier"),
	EQUIPATGES			("05", "Per Equipatges",               "By Luggage",            "Por Equipajes",                   "Par Bagage",                  "Per Gepäck",               "Per Bagagli"),
	CORREU				("06", "Per Correu",                   "By Mail",               "Por Correo",                      "Par Poste",                   "Per Post",                 "Per la Posta"),
	EN_MA				("07", "En Mà",                        "Hand Delivery",         "En Mano",                         "Livraison Spéciale",          "Sonderzustellung",         "Recapito in Mano"),
	TRANSPORT_ESPECIAL	("08", "Transport Especial",           "Special Transport",     "Transporte Especial",             "Par Transport Especial",      "Speciell Transport",       "Per Trasporto Speciale"),
	NOSTRES_MITJANS		("09", "Els Nostres Mitjans",          "By our Means",          "Nuestros Medios",                 "Nos Moyens",                  "Mit unserem Transport",    "Ns. mezzo"),
	SEUS_MITJANS		("10", "Els Seus Mitjans",             "By Your Means",         "Sus Medios",                      "Vos Moyens",                  "Mit Ihrem Transport",      "Vs. mezzo"),
	COURIER_AERI		("11", "Courrier Aeri",                "Aerial Courier",        "Courrier Aéreo",                  "Par Courier Aerien",          "Per Luftkurier",           "Courrier Aereo"),
	TREN				("12", "Per Tren",                     "By Train",              "Por Tren",                        "Par Train",                   "Trainieren",               "Per Treno");

	private static final Map<String, FormaEnviament> LOOKUP_MAP;
	private final String codiAdvantage;
	private final String ca, en, es, fr, de, it;

	FormaEnviament(String codiAdvantage, String ca, String en, String es, String fr, String de, String it) {
		this.codiAdvantage = codiAdvantage;
		this.ca = ca;
		this.en = en;
		this.es = es;
		this.fr = fr;
		this.de = de;
		this.it = it;
	}

	public String codiAdvantage() {
		return codiAdvantage;
	}

	public String descripcio(Locale locale) {
		return switch (locale.getLanguage()) {
			case "en" -> en;
			case "es" -> es;
			case "fr" -> fr;
			case "de" -> de;
			case "it" -> it;
			default  -> ca;
		};
	}

	static {
		LOOKUP_MAP = new HashMap<>();
		for (FormaEnviament t : FormaEnviament.values()) {
			LOOKUP_MAP.put(t.codiAdvantage, t);
		}
	}

	public static FormaEnviament getByCodi(String codi) {
		var f = LOOKUP_MAP.get(codi);
		if (f == null)
			throw new IllegalArgumentException(String.format("Forma enviament '%s' no mapejada", codi));
		return f;
	}

}
