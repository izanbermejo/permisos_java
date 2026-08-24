package ames.comercial.server;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class I18N {

	private static final String PROPERTIES_FILE = "i18n/traduccions";

	private I18N () {} 
	
	public static String getLiteral (Locale locale, String key, Object... params) {
		return getLiteral(PROPERTIES_FILE, locale, key, params);
	}
	
	public static String getLiteral (String propertiesFile, Locale locale, String key, Object... params) {
		String value = keyValue(propertiesFile, key, locale, params);
		
		// Si la clau no existeix o no té cap valor assignat, ho provem amb l'idioma per defecte 
		if (value.isEmpty() && !locale.equals(Languages.getDefault()))
			value = keyValue(propertiesFile, key, Languages.getDefault(), params);
		
		// Si no existeix cap valor per la clau demanada, retornem la clau 
		if (value.isEmpty())
			value = (key.startsWith("{")) ? key : ("{" + key + "}");
		return value;
	}
	
	public static String getLiteral (String key, Object... params) {
		Locale locale = RequestThread.idioma() == null ? Languages.getDefault() : RequestThread.idioma();
		return getLiteral(locale, key, params);
	}

	public static NumberFormat getNumberFormat() {
		Locale locale = RequestThread.idioma() == null ? Languages.getDefault() : RequestThread.idioma();
		return NumberFormat.getNumberInstance(locale);
	}

	private static String keyValue (String propertiesFile, String key, Locale locale, Object... params) {
		try {
			ResourceBundle messages = ResourceBundle.getBundle(propertiesFile, locale);
			if (params == null) 
				return messages.getString(key);
			
			// Apliquem els paràmetres al missatge
			MessageFormat formatter = new MessageFormat("", locale);
			formatter.applyPattern(messages.getString(key));
			return formatter.format(params);
		} catch (MissingResourceException error) {
			return "";
		}
	}
	
}
