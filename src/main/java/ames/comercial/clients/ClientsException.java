package ames.comercial.clients;

import ames.comercial.server.ETag;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;

@SuppressWarnings("serial")
public class ClientsException {
	public static class AdjuntNotFound extends AppException {
		public AdjuntNotFound () { super(I18N.getLiteral("adjunt_no_trobat")); }
	}
}
