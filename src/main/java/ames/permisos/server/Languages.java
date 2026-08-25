package ames.permisos.server;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public enum Languages {

	CATALAN		(true,	new Locale("ca", "ES")),
	SPANISH		(true,	new Locale("es", "ES")),
	HUNGARIAN		(true,	new Locale("hu", "HU")),
	XINES		(true,	new Locale("zh", "CN")),
	ENGLISH_GB		(true,	new Locale("en", "GB")),
	ENGLISH_USA		(true,	new Locale("en", "US"));
	
	private final Locale locale;
	private final boolean enabled;
	
	private Languages (boolean enabled, Locale locale) {
		this.locale = locale;
		this.enabled = enabled;
	}
	
	/** 
	 * Retorna l'idioma acceptat que té més prefèrencia dels passats per paràmetres. Si no es troba cap idioma es retorna l'idioma per defecte.
	 * @param Llista de possibles idiomes acceptats ordenats per prioritat
	 * @return Idioma acceptat amb més prioritat
	 */
	public static Locale getPriorAccepted (List<Locale> acceptedLanguages) {
		if ((acceptedLanguages == null) || acceptedLanguages.isEmpty())
			return getDefault();

		return allEnabled().stream()
			.filter(lang -> acceptedLanguages.contains(lang))
			.findFirst()
			.orElse(getDefault());
	}

	/** Retorna el primer idioma actiu que tingui un llenguatge especificat. Si no se'n troba cap es retorna l'idioma per defecte. */
	public static Locale getPriorAccepted (String language) {
		if ((language == null) || language.isEmpty())
			return getDefault();

		return allEnabled().stream()
			.filter(lang -> lang.getLanguage().equalsIgnoreCase(language))
			.findFirst()
			.orElse(getDefault());
	}
	
	public Locale locale () {
		return locale;
	}
	
	/** Retorna els idiomes que estan habilitats per l'aplicació */
	public static List<Locale> allEnabled () {
		return Stream.of(Languages.values())
			.filter(lang -> lang.enabled)
			.map(lang -> lang.locale)
			.collect(toList());
	}
	
	/** Retorna l'idioma per defecte */
	public static Locale getDefault () {
		return CATALAN.locale;
	}
}
