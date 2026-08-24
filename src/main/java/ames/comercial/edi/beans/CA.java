package ames.comercial.edi.beans;

public class CA {

	public static final int i_inicio = 2;
	public static final int i_tipo = 12;
	public static final int i_numeroEnvio = 26;
	public static final int i_buzonOrigen = 61;
	public static final int i_buzonDestino = 96;
	public static final int i_numeroDocumento = 131;
	public static final int i_codigoDocumento = 141;
	public static final int i_documento = 151;
	public static final int i_logistica = 161;
	public static final int i_funcion = 164;
	public static final int i_referencia = 178;
//	public static final int i_filler = 180;

	private String inicio;
	private String tipo;
	private String numeroEnvio;
	private String buzonOrigen;
	private String buzonDestino;
	private String numeroDocumento;
	private String codigoDocumento;
	private String documento;
	private String logistica;
	private String funcion;
	private String referencia;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getLogistica() {
		return logistica;
	}

	public void setLogistica(String logistica) {
		this.logistica = logistica;
	}

	public String getFuncion() {
		return funcion;
	}

	public void setFuncion(String funcion) {
		this.funcion = funcion;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNumeroEnvio() {
		return numeroEnvio;
	}

	public void setNumeroEnvio(String numeroEnvio) {
		this.numeroEnvio = numeroEnvio;
	}

	public String getBuzonOrigen() {
		return buzonOrigen;
	}

	public void setBuzonOrigen(String buzonOrigen) {
		this.buzonOrigen = buzonOrigen;
	}

	public String getBuzonDestino() {
		return buzonDestino;
	}

	public void setBuzonDestino(String buzonDestino) {
		this.buzonDestino = buzonDestino;
	}

	public String getNumeroDocumento() {
		return numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getCodigoDocumento() {
		return codigoDocumento;
	}

	public void setCodigoDocumento(String codigoDocumento) {
		this.codigoDocumento = codigoDocumento;
	}

	@Override
	public String toString() {
		return "CA{" +
				"inicio='" + inicio + '\'' +
				", tipo='" + tipo + '\'' +
				", numeroEnvio='" + numeroEnvio + '\'' +
				", buzonOrigen='" + buzonOrigen + '\'' +
				", buzonDestino='" + buzonDestino + '\'' +
				", numeroDocumento='" + numeroDocumento + '\'' +
				", codigoDocumento='" + codigoDocumento + '\'' +
				", documento='" + documento + '\'' +
				", logistica='" + logistica + '\'' +
				", funcion='" + funcion + '\'' +
				", referencia='" + referencia + '\'' +
				'}';
	}

	public CA() {
	}

}
