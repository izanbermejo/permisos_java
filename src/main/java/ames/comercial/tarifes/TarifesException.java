package ames.comercial.tarifes;

import ames.comercial.server.ETag;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;

@SuppressWarnings("serial")
public class TarifesException {
	public static class TarifaNoValida extends AppException {
		public TarifaNoValida (String msg) {
			super(msg);
		}		
	}

	public static class NoExisteixTarifa extends AppException {
		public NoExisteixTarifa () {
			super(I18N.getLiteral("no_te_tarifa"));
		}
	}
}
