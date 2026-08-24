package ames.comercial.edi.beans;

public class LG {

	public static final int i_inicio=2;
	public static final int i_descripcionArticulo=37;
	public static final int i_numeroContrato=72;
	public static final int i_fechaContrato=82;
	public static final int i_numeroDocumentoAnterior=117;
	public static final int i_fechaDocumentoAnterior=127;
	public static final int i_numeroLineaContrato=133;
	public static final int i_numeroDeplano=168;

	private String inicio;
	private String descripcionArticulo;
	private String numeroContrato;
	private String fechaContrato;
	private String numeroDocumentoAnterior;
	private String fechaDocumentoAnterior;
	private String numeroLineaContrato;
	private String numeroDePlano;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getDescripcionArticulo() {
		return descripcionArticulo;
	}

	public void setDescripcionArticulo(String descripcionArticulo) {
		this.descripcionArticulo = descripcionArticulo;
	}

	public String getNumeroContrato() {
		return numeroContrato;
	}

	public void setNumeroContrato(String numeroContrato) {
		this.numeroContrato = numeroContrato;
	}

	public String getFechaContrato() {
		return fechaContrato;
	}

	public void setFechaContrato(String fechaContrato) {
		this.fechaContrato = fechaContrato;
	}

	public String getNumeroDocumentoAnterior() {
		return numeroDocumentoAnterior;
	}

	public void setNumeroDocumentoAnterior(String numeroDocumentoAnterior) {
		this.numeroDocumentoAnterior = numeroDocumentoAnterior;
	}

	public String getFechaDocumentoAnterior() {
		return fechaDocumentoAnterior;
	}

	public void setFechaDocumentoAnterior(String fechaDocumentoAnterior) {
		this.fechaDocumentoAnterior = fechaDocumentoAnterior;
	}

	public String getNumeroLineaContrato() {
		return numeroLineaContrato;
	}

	public void setNumeroLineaContrato(String numeroLineaContrato) {
		this.numeroLineaContrato = numeroLineaContrato;
	}

	public String getNumeroDePlano() {
		return numeroDePlano;
	}

	public void setNumeroDePlano(String numeroDePlano) {
		this.numeroDePlano = numeroDePlano;
	}

	@Override
	public String toString() {
		return "LG{" +
				"inicio='" + inicio + '\'' +
				", descripcionArticulo='" + descripcionArticulo + '\'' +
				", numeroContrato='" + numeroContrato + '\'' +
				", fechaContrato='" + fechaContrato + '\'' +
				", numeroDocumentoAnterior='" + numeroDocumentoAnterior + '\'' +
				", fechaDocumentoAnterior='" + fechaDocumentoAnterior + '\'' +
				", numeroLineaContrato='" + numeroLineaContrato + '\'' +
				", numeroDePlano='" + numeroDePlano + '\'' +
				'}';
	}

	public LG() {
		super();
	}
}
