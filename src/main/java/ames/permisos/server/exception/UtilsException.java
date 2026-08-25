package ames.permisos.server.exception;

import ames.permisos.server.I18N;

/** Conjunt d'Exceptions utilitzades per les classes del package 'utils' */

@SuppressWarnings("serial")
public class UtilsException {

	public static class ParsedJson extends AppException {
		public ParsedJson (Exception cause) {
			super(I18N.getLiteral("json_parse_error"), cause);
		}
	}	
}
