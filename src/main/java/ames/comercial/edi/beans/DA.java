package ames.comercial.edi.beans;

public class DA {

	public static final int i_inicio = 2;
	public static final int i_tipo = 3;
	public static final int i_cantidad = 20;
	public static final int i_unidadMedida = 23;
	public static final int i_fechaInicial = 33;
	public static final int i_horaInicial = 38;
	public static final int i_fechaFinal = 48;
	public static final int i_horaFinal = 53;
	public static final int i_razonInstruccion = 54;
	public static final int i_numeroRAN = 89;
	public static final int i_fechaRAN = 99;
	public static final int i_frecuenciaEnvio = 102;
	public static final int i_numeroTarjetaKanban = 137;
	public static final int i_ultimoNumeroKanban = 150;
//	public static final int i_filler = 180;

	private String inicio;
	private String tipo;
	private String cantidad;
	private String unidadMedida;
	private String fechaInicial;
	private String horaInicial;
	private String fechaFinal;
	private String horaFinal;
	private String razonInstruccion;
	private String numeroRAN;
	private String fechaRAN;
	private String frecuenciaEnvio;
	private String numeroTarjetaKanban;
	private String ultimoNumeroRAN;
//	private String filler;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCantidad() {
		return cantidad;
	}

	public void setCantidad(String cantidad) {
		this.cantidad = cantidad;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public String getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(String fechaInicial) {
		this.fechaInicial = fechaInicial;
	}

	public String getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(String horaInicial) {
		this.horaInicial = horaInicial;
	}

	public String getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(String fechaFinal) {
		this.fechaFinal = fechaFinal;
	}

	public String getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(String horaFinal) {
		this.horaFinal = horaFinal;
	}

	public String getRazonInstruccion() {
		return razonInstruccion;
	}

	public void setRazonInstruccion(String razonInstruccion) {
		this.razonInstruccion = razonInstruccion;
	}

	public String getNumeroRAN() {
		return numeroRAN;
	}

	public void setNumeroRAN(String numeroRAN) {
		this.numeroRAN = numeroRAN;
	}

	public String getFechaRAN() {
		return fechaRAN;
	}

	public void setFechaRAN(String fechaRAN) {
		this.fechaRAN = fechaRAN;
	}

	public String getFrecuenciaEnvio() {
		return frecuenciaEnvio;
	}

	public void setFrecuenciaEnvio(String frecuenciaEnvio) {
		this.frecuenciaEnvio = frecuenciaEnvio;
	}

	public String getNumeroTarjetaKanban() {
		return numeroTarjetaKanban;
	}

	public void setNumeroTarjetaKanban(String numeroTarjetaKanban) {
		this.numeroTarjetaKanban = numeroTarjetaKanban;
	}

	public String getUltimoNumeroRAN() {
		return ultimoNumeroRAN;
	}

	public void setUltimoNumeroRAN(String ultimoNumeroRAN) {
		this.ultimoNumeroRAN = ultimoNumeroRAN;
	}

//	public String getFiller() {
//		return filler;
//	}
//
//	public void setFiller(String filler) {
//		this.filler = filler;
//	}

	@Override
	public String toString() {
		return "DA [inicio=" + inicio + ", tipo=" + tipo + ", cantidad=" + cantidad + ", unidadMedida=" + unidadMedida
				+ ", fechaInicial=" + fechaInicial + ", horaInicial=" + horaInicial + ", fechaFinal=" + fechaFinal
				+ ", horaFinal=" + horaFinal + ", razonInstruccion=" + razonInstruccion + ", numeroRAN=" + numeroRAN
				+ ", fechaRAN=" + fechaRAN + ", frecuenciaEnvio=" + frecuenciaEnvio + ", numeroTarjetaKanban="
//				+ numeroTarjetaKanban + ", ultimoNumeroRAN=" + ultimoNumeroRAN + ", filler=" + filler + "]";
				+ numeroTarjetaKanban + ", ultimoNumeroRAN=" + ultimoNumeroRAN + "]";
	}

	}
