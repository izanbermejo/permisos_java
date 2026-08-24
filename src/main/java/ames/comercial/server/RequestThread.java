package ames.comercial.server;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

public final class RequestThread {

	public enum Entity { COMANDA, LINIA_COMANDA, ALBARA, LINIA_ALBARA }
	public final static String UNKNOWN = "[DESCONEGUT]";
	
	private record Data (Locale idioma, String ipAddress, String codiUsuari, String nomUsuari, String email, LocalDateTime dateTimeLocal, Map<Entity,String> etags) {}

	private static final ThreadLocal<Data> dataThread = new ThreadLocal<>();
	
	private RequestThread () {}
	
	public static void set (Locale idioma, String ipAddress, String codiUsuari, String nomUsuari, String email, LocalDateTime dateTimeLocal, Map<Entity,String> etags) {
		Data data = new Data(
			(idioma == null ) ? Languages.getDefault() : idioma,
			(ipAddress == null) ? UNKNOWN : ipAddress,
			(codiUsuari == null) ? UNKNOWN : codiUsuari,
			(nomUsuari == null) ? UNKNOWN : nomUsuari,
			(email == null) ? UNKNOWN : email,
			(dateTimeLocal == null) ? LocalDateTime.now() : dateTimeLocal,
			(etags == null) ? Map.of() : etags
		);
		dataThread.set(data);
	}

	public static void set (String nomUsuari) {
		Data data = new Data(
				RequestThread.idioma(),
				RequestThread.ipAddress(),
				RequestThread.codiUsuari(),
				(nomUsuari == null) ? RequestThread.nomUsuari() : nomUsuari,
				RequestThread.email(),
				RequestThread.dateTimeLocal(),
				Map.of()
		);
		dataThread.set(data);
	}

	/** Retorna l'idioma del request o, si no se'n proporciona cap, l'idioma per defecte de l'aplicació */
	public static Locale idioma () {
		return (dataThread.get() == null)
			? Languages.getDefault()
			: dataThread.get().idioma;
	}

	public static String ipAddress () {
		return (dataThread.get() == null)
			? ""
			: dataThread.get().ipAddress;	
	}
	
	public static String nomUsuari () {
		return (dataThread.get() == null)
			? UNKNOWN
			: dataThread.get().nomUsuari;
	}

	public static String codiUsuari () {
		return (dataThread.get() == null)
				? UNKNOWN
				: dataThread.get().codiUsuari;
	}

	public static String email () {
		return (dataThread.get() == null)
				? UNKNOWN
				: dataThread.get().email();
	}

	public static LocalDateTime dateTimeLocal() {
		return (dataThread.get() == null)
			? LocalDateTime.now()
			: dataThread.get().dateTimeLocal;
	}

	public static LocalDate dateLocal() {
		return dateTimeLocal().toLocalDate();
	}
	
	public static String etag (Entity entity) {
		if (dataThread.get() == null)
			return "";
		var etagValue = dataThread.get().etags.get(entity); 
		return (etagValue == null)	? "" : etagValue;	
	}

	public static void clean () {
		dataThread.remove();
	}	
}
