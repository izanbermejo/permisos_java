package ames.comercial.comandes;

import ames.comercial.server.ETag;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;

@SuppressWarnings("serial")
public class ComandesException {

	public static class ComandaChanged extends AppException {
		public ComandaChanged(ETag etag) {
			super(I18N.getLiteral("comanda_changed", etag.usuari(), etag.data()));
		}		
	}
	
	public static class ComandaNoExisteix extends AppException {
		public ComandaNoExisteix () {
			super(I18N.getLiteral("comanda_no_existeix"));
		}		
	}

	public static class OperacioNoPermesaComandaStockSeguretat extends AppException {
		public OperacioNoPermesaComandaStockSeguretat () {
			super(I18N.getLiteral("operacio_no_permesa_stock_seguretat"));
		}
	}
	
	public static class LiniaComandaChanged extends AppException {
		public LiniaComandaChanged(ETag etag) {
			super(I18N.getLiteral("linia_comanda_changed", etag.usuari(), etag.data()));
		}		
	}

	public static class LiniaComandaNoExisteix extends AppException {
		public LiniaComandaNoExisteix () {
			super(I18N.getLiteral("linia_comanda_no_existeix"));
		}
	}

	public static class LiniaComandaJaExisteixAmbMateixaData extends AppException {
		public LiniaComandaJaExisteixAmbMateixaData () {
			super(I18N.getLiteral("linia_comanda_amb_data_ja_existent"));
		}
	}

	public static class NoPotCanviarNomLiniaServida extends AppException {
		public NoPotCanviarNomLiniaServida () { super(I18N.getLiteral("comandes.nopotcanviarnomliniaservida")); }
	}

	public static class ErrorEnviantCorreuComanda extends AppException {
		public ErrorEnviantCorreuComanda (Throwable cause) {
			super(I18N.getLiteral("error_enviant_correu_comanda"), cause);
		}
	}

	public static class JustificantEnviatNoTrobat extends AppException {
		public JustificantEnviatNoTrobat () {
			super(I18N.getLiteral("justificant_enviat_no_trobat"));
		}
	}

	public static class AdjuntNotFound extends AppException {
		public AdjuntNotFound () { super(I18N.getLiteral("adjunt_no_trobat")); }
	}

	public static class TarifaCoixinetsNoDefinida extends AppException {
		public TarifaCoixinetsNoDefinida () { super(I18N.getLiteral("tarifa_coixinets_no_definida")); }
	}

	public static class TarifaBarresNoDefinida extends AppException {
		public TarifaBarresNoDefinida () { super(I18N.getLiteral("tarifa_barres_no_definida")); }
	}

	public static class TarifaMedicalNoDefinida extends AppException {
		public TarifaMedicalNoDefinida () { super(I18N.getLiteral("tarifa_medical_no_definida")); }
	}

	public static class TarifaIbinsaNoDefinida extends AppException {
		public TarifaIbinsaNoDefinida () { super(I18N.getLiteral("tarifa_ibinsa_no_definida")); }
	}

	public static class TarifaFiltresNoDefinida extends AppException {
		public TarifaFiltresNoDefinida () { super(I18N.getLiteral("tarifa_filtres_no_definida")); }
	}

	public static class PesesEspecialsDiferentDivisa extends AppException {
		public PesesEspecialsDiferentDivisa () { super(I18N.getLiteral("peses_especials_diferent_tarifa")); }
	}

	public static class DivisaTarifesDiferent extends AppException {
		public DivisaTarifesDiferent () { super(I18N.getLiteral("divisa_tarifes_diferent")); }
	}

	public static class DivisaClientDiferentTarifes extends AppException {
		public DivisaClientDiferentTarifes(Divisa divisaClient, Divisa divisaTarifa) {
			super(I18N.getLiteral("divisa_client_diferent_divisa_tarifa", divisaClient.symbol(), divisaTarifa.symbol()));
		}
	}

	public static class PreuNoCalculat extends AppException {
		public PreuNoCalculat(long linia) {
			super(I18N.getLiteral("preu_linia_no_calculat", linia));
		}
	}

	public static class QuantitatNoServibleSegonsTarifa extends AppException {
		public QuantitatNoServibleSegonsTarifa(long quantitat, String referencia) {
			super(I18N.getLiteral("quantitat_no_servible_segons_tarifa", quantitat, referencia));
		}
	}

	public static class QuantitatNoValidaFila extends AppException {
		public QuantitatNoValidaFila(int fila) {
			super(I18N.getLiteral("quantitat_no_valida_fila", fila));
		}
	}

	public static class DataNoValidaFila extends AppException {
		public DataNoValidaFila(int fila) {
			super(I18N.getLiteral("data_no_valida_fila", fila));
		}
	}

	public static class ReferenciaNoExisteixFila extends AppException {
		public ReferenciaNoExisteixFila(String referencia, int fila) {
			super(I18N.getLiteral("referencia_no_existeix_fila", referencia, fila));
		}
	}

	public static class UnitatsEmbalatgeIncorrectaFila extends AppException {
		public UnitatsEmbalatgeIncorrectaFila(String referencia, int fila) {
			super(I18N.getLiteral("unitats_embalatge_incorrecta_fila", referencia, fila));
		}
	}

	public static class NovaQuantitatInferiorServida extends AppException {
		public NovaQuantitatInferiorServida(long novaQuanitat, long quantitatSerida) {
			super(I18N.getLiteral("nova_quantitat_inferior_servida", novaQuanitat, quantitatSerida));
		}
	}

	public static class JustificantEncaraNoEnviat extends AppException {
		public JustificantEncaraNoEnviat() {
			super(I18N.getLiteral("justificant_encara_no_enviat"));
		}
	}

	public static class CanviarPreuLiniaServida extends AppException {
		public CanviarPreuLiniaServida() {
			super(I18N.getLiteral("no_canviar_preu_linia_servida"));
		}
	}

	public static class ProgramaNoDisponibleNormalitzats extends AppException {
		public ProgramaNoDisponibleNormalitzats() {
			super(I18N.getLiteral("programa_no_disponible_normalitzats"));
		}
	}

	public static class QuantitatNoPotSerInferiorServida extends AppException {
		public QuantitatNoPotSerInferiorServida(long quantitatServida) {
			super(I18N.getLiteral("quantitat_no_pot_ser_inferior_servida", I18N.getNumberFormat().format(quantitatServida)));
		}
	}

	public static class QuantitatServirExcessiva extends AppException {
		public QuantitatServirExcessiva(String liniaFormat, String referencia, long quantitatServir, long maxServible) {
			super(I18N.getLiteral("quantitat_servir_excessiva",
					liniaFormat,
					referencia,
					I18N.getNumberFormat().format(quantitatServir),
					I18N.getNumberFormat().format(maxServible)));
		}
	}

}
