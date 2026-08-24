package ames.comercial.albarans;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.server.ETag;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;

@SuppressWarnings("serial")
public class AlbaransException {

	public static class AlbaraChanged extends AppException {
		public AlbaraChanged(ETag etag) {
			super(I18N.getLiteral("albara_changed", etag.usuari(), etag.data()));
		}
	}

	public static class LiniaAlbaraChanged extends AppException {
		public LiniaAlbaraChanged(ETag etag) {
			super(I18N.getLiteral("linia_albara_changed", etag.usuari(), etag.data()));
		}
	}

	public static class AlbaraNoExisteix extends AppException {
		public AlbaraNoExisteix(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_no_existeix", idAlbara.codi(), idAlbara.empresa()));
		}
	}

	public static class LiniaAlbaraNoExisteix extends AppException {
		public LiniaAlbaraNoExisteix(KeyLiniaAlbara idLiniaAlbara) {
			super(I18N.getLiteral("linia_albara_no_existeix", idLiniaAlbara.clauLiniaFormat(), idLiniaAlbara.idAlbara().empresa()));
		}
	}

	public static class AlbaraTancat extends AppException {
		public AlbaraTancat(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_tancat_no_eliminar_linia", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol canviar l'adreça (enviament, broker o proforma) d'un albarà que ja està tancat.
	 */
	public static class AlbaraTancatNoEditar extends AppException {
		public AlbaraTancatNoEditar(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_tancat_no_editar", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol canviar la data d'un albarà que ja està tancat. La data es propaga als
	 * moviments d'inventari i a la traçabilitat, per això només es pot canviar mentre està obert.
	 */
	public static class AlbaraTancatNoCanviarData extends AppException {
		public AlbaraTancatNoCanviarData(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_tancat_no_canviar_data", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol reobrir o modificar les línies d'un albarà que ja està facturat.
	 */
	public static class AlbaraFacturat extends AppException {
		public AlbaraFacturat(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_facturat_no_modificar", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol canviar el flag d'autofacturable d'un albarà que ja s'ha començat a
	 * facturar (està marcat com a facturat o té alguna línia amb quantitat facturada).
	 */
	public static class AlbaraFacturatNoCanviarAutofacturable extends AppException {
		public AlbaraFacturatNoCanviarAutofacturable(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_facturat_no_canviar_autofacturable", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol canviar el flag d'autofacturable d'un albarà que no és de client. Els
	 * albarans de traspàs i els de consum de plataforma no es facturen automàticament.
	 */
	public static class AlbaraNoAutofacturable extends AppException {
		public AlbaraNoAutofacturable(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_no_autofacturable", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol rectificar manualment la quantitat pendent de facturar d'una línia amb un
	 * valor negatiu o superior a la quantitat de la pròpia línia.
	 */
	public static class QuantitatPendentFacturarInvalida extends AppException {
		public QuantitatPendentFacturarInvalida(KeyLiniaAlbara idLiniaAlbara, long quantitatPendentFacturar, long quantitat) {
			super(I18N.getLiteral("quantitat_pendent_facturar_invalida", idLiniaAlbara.clauLiniaFormat(),
					quantitatPendentFacturar, quantitat));
		}
	}

	/**
	 * Es llença quan es vol modificar o eliminar un albarà que ja s'ha presentat al SII de la hisenda
	 * pública. El procés que genera els fitxers del SII marca els albarans que ha declarat
	 * ({@code is_enviat_hisenda}, l'{@code envhis} de l'Advantage) i, a partir d'aquell moment, el que
	 * s'ha declarat ja no es pot alterar: si cal rectificar-lo, s'ha de fer per la via de la declaració.
	 */
	public static class AlbaraPresentatHisenda extends AppException {
		public AlbaraPresentatHisenda(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_presentat_hisenda", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol reobrir o modificar les línies d'un albarà que té algun moviment de
	 * magatzem actiu (en preparació, en servei, servit o entregat).
	 */
	public static class AlbaraAmbMovimentMagatzem extends AppException {
		public AlbaraAmbMovimentMagatzem(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_amb_moviment_magatzem_no_modificar", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan la proposta de creació d'albarans ha canviat entre que es va previsualitzar i es va
	 * confirmar (la signatura de l'estat no coincideix). No es crea res i el frontend mostra el missatge
	 * perquè l'usuari torni a revisar la proposta.
	 */
	public static class PropostaModificada extends AppException {
		public PropostaModificada() {
			super(I18N.getLiteral("proposta_albara_modificada"));
		}
	}

	/**
	 * Es llença quan en crear un albarà de consum la quantitat sol·licitada d'una peça supera el
	 * pendent de consumir disponible (suma FIFO de les línies dels albarans de traspàs a plataforma).
	 */
	public static class PendentConsumirInsuficient extends AppException {
		public PendentConsumirInsuficient(String article, long demanat, long disponible) {
			super(I18N.getLiteral("pendent_consumir_insuficient", article, demanat, disponible));
		}
	}

	/**
	 * Es llença quan en crear un albarà de consum la quantitat consumida d'una peça supera el pendent
	 * de servir de les línies de comanda de l'article-client (suma FIFO per data sol·licitada).
	 */
	public static class PendentServirInsuficient extends AppException {
		public PendentServirInsuficient(String article, long consumit, long disponible) {
			super(I18N.getLiteral("pendent_servir_insuficient", article, consumit, disponible));
		}
	}

	/**
	 * Es llença quan es demana la traçabilitat de consums d'un magatzem que no està marcat per declarar
	 * al SII. En aquests magatzems les línies de traspàs no tenen pendent de consumir i els consums no
	 * deixen apunt a {@code albarans.sortides_plataforma}, de manera que no hi ha res a consultar: sense
	 * aquest error el frontend només veuria una llista buida i no en sabria el motiu.
	 */
	public static class MagatzemNoSII extends AppException {
		public MagatzemNoSII(String magatzem) {
			super(I18N.getLiteral("magatzem_no_sii", magatzem));
		}
	}

	/**
	 * Es llença quan es vol crear un albarà de consum amb peces de més d'un client. Un albarà de
	 * consum ha de ser sempre d'un únic client.
	 */
	public static class ConsumMultiplesClients extends AppException {
		public ConsumMultiplesClients() {
			super(I18N.getLiteral("consum_multiples_clients"));
		}
	}

	/**
	 * Es llença quan es vol crear un albarà de traspàs manual on l'origen i el destí coincideixen
	 * tant en empresa com en magatzem: no hi hauria cap moviment de mercaderia.
	 */
	public static class TraspasSenseMoviment extends AppException {
		public TraspasSenseMoviment() {
			super(I18N.getLiteral("traspas_sense_moviment"));
		}
	}

	/**
	 * Es llença quan es vol afegir manualment una línia a un albarà que no és de traspàs.
	 */
	public static class AlbaraNoTraspas extends AppException {
		public AlbaraNoTraspas(KeyAlbara idAlbara) {
			super(I18N.getLiteral("albara_no_traspas", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol afegir a un albarà una peça que no és de l'empresa que l'emet. Un
	 * article-client té dues empreses de facturació (la pròpia i la d'entrega) i l'stock pot viure a
	 * qualsevol de les dues, perquè els traspassos entre empreses el mouen d'una a l'altra; l'albarà
	 * ha de ser d'alguna d'elles.
	 */
	public static class ArticleAltraEmpresa extends AppException {
		public ArticleAltraEmpresa(String article, String empresaArticle, String empresaEntregaArticle, String empresaAlbara) {
			super(I18N.getLiteral("article_altra_empresa", article, empresaArticle, empresaEntregaArticle, empresaAlbara));
		}
	}

	/**
	 * Es llença quan es vol afegir manualment una línia amb una quantitat que no és positiva.
	 */
	public static class QuantitatLiniaInvalida extends AppException {
		public QuantitatLiniaInvalida(long quantitat) {
			super(I18N.getLiteral("quantitat_linia_invalida", quantitat));
		}
	}

	/**
	 * Es llença quan es vol marcar com a abonable un traspàs que no canvia d'empresa. Un traspàs entre
	 * magatzems de la mateixa empresa no es factura, i per tant tampoc no s'abona.
	 */
	public static class TraspasNoAbonable extends AppException {
		public TraspasNoAbonable(KeyAlbara idAlbara) {
			super(I18N.getLiteral("traspas_no_abonable", idAlbara.codiFormat()));
		}

		/** Variant per a la creació, quan l'albarà encara no existeix i per tant no té número */
		public TraspasNoAbonable() {
			super(I18N.getLiteral("traspas_nou_no_abonable"));
		}
	}

	/**
	 * Es llença quan es vol canviar el flag d'abonable d'un traspàs que ja s'ha començat a facturar o
	 * abonar. El canvi mouria el pendent de facturar de les línies i els registres de pendent d'abonar.
	 */
	public static class TraspasAbonableFacturacioIniciada extends AppException {
		public TraspasAbonableFacturacioIniciada(KeyAlbara idAlbara) {
			super(I18N.getLiteral("traspas_abonable_facturacio_iniciada", idAlbara.codiFormat()));
		}
	}

	/**
	 * Es llença quan es vol afegir manualment a un albarà de traspàs una peça que ja hi té línia. El
	 * pendent d'abonar de l'Advantage ({@code penabo}) no té número de línia, de manera que dues línies
	 * de la mateixa peça al mateix albarà hi generarien registres indistingibles.
	 */
	/**
	 * Es llença quan es vol rectificar el pendent de facturar d'una línia d'un traspàs abonable. Les
	 * seves línies no es facturen mai: la contrapartida econòmica és el pendent d'abonar.
	 */
	public static class TraspasAbonableSensePendentFacturar extends AppException {
		public TraspasAbonableSensePendentFacturar(KeyAlbara idAlbara) {
			super(I18N.getLiteral("traspas_abonable_sense_pendent_facturar", idAlbara.codiFormat()));
		}
	}

	public static class PecaJaAlAlbara extends AppException {
		public PecaJaAlAlbara(String article, String liniaFormat) {
			super(I18N.getLiteral("peca_ja_al_albara", article, liniaFormat));
		}
	}

	public static class AlbaraUrgent extends AppException {
		public AlbaraUrgent(KeyAlbara id) {
			super(I18N.getLiteral("albara_urgent"));
		}
	}
}
