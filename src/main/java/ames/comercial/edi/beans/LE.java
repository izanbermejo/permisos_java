package ames.comercial.edi.beans;

public class LE {

	public static final int i_inicio=2;
	public static final int i_tipoBulto=37;
	public static final int i_referenciaEmalaje=72;
	public static final int i_piezasPorEmbalaje=87;
	public static final int i_tipoContenedor=122;
	public static final int i_numeroEmbalajes=137;
	public static final int i_nivelEmpaquetamiento=140;

	private String inicio;
	private String tipoBulto;
	private String referenciaEmbalaje;
	private String piezasPorEmbalaje;
	private String tipoContenedor; // Pallet
	private String numeroEmbalajes;
	private String nivelEmpaquetamiento;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getTipoBulto() {
		return tipoBulto;
	}

	public void setTipoBulto(String tipoBulto) {
		this.tipoBulto = tipoBulto;
	}

	public String getReferenciaEmbalaje() {
		return referenciaEmbalaje;
	}

	public void setReferenciaEmbalaje(String referenciaEmbalaje) {
		this.referenciaEmbalaje = referenciaEmbalaje;
	}

	public String getPiezasPorEmbalaje() {
		return piezasPorEmbalaje;
	}

	public void setPiezasPorEmbalaje(String piezasPorEmbalaje) {
		this.piezasPorEmbalaje = piezasPorEmbalaje;
	}

	public String getTipoContenedor() {
		return tipoContenedor;
	}

	public void setTipoContenedor(String tipoContenedor) {
		this.tipoContenedor = tipoContenedor;
	}

	public String getNumeroEmbalajes() {
		return numeroEmbalajes;
	}

	public void setNumeroEmbalajes(String numeroEmbalajes) {
		this.numeroEmbalajes = numeroEmbalajes;
	}

	public String getNivelEmpaquetamiento() {
		return nivelEmpaquetamiento;
	}

	public void setNivelEmpaquetamiento(String nivelEmpaquetamiento) {
		this.nivelEmpaquetamiento = nivelEmpaquetamiento;
	}

	@Override
	public String toString() {
		return "LE{" +
				"inicio='" + inicio + '\'' +
				", tipoBulto='" + tipoBulto + '\'' +
				", referenciaEmalaje='" + referenciaEmbalaje + '\'' +
				", piezasPorEmbalaje='" + piezasPorEmbalaje + '\'' +
				", tipoContenedor='" + tipoContenedor + '\'' +
				", numeroEmbalajes='" + numeroEmbalajes + '\'' +
				", nivelEmpaquetamiento='" + nivelEmpaquetamiento + '\'' +
				'}';
	}

	public LE() {
		super();
	}
}
