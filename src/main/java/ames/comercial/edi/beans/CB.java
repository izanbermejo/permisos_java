package ames.comercial.edi.beans;

public class CB {

	public static final int i_inicio = 2;
	public static final int i_fechaMensaje = 12;
	public static final int i_horaMensaje = 17;
	public static final int i_fechaInicioHorizonte = 33;
	public static final int i_fechaFinalHorizonte = 49;
	public static final int i_tipoFecha = 50;
	public static final int i_idTransportista = 85;
	public static final int i_tipoTransporte = 120;

	private String inicio;
	private String fechaMensaje;
	private String horaMensaje;
	private String fechaInicioHorizonte;
	private String fechaFinalHorizonte;
	private String tipoFecha;
	private String idTransportista;
	private String tipoTransporte;

	public CB() {
	}

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getFechaMensaje() {
		return fechaMensaje;
	}

	public void setFechaMensaje(String fechaMensaje) {
		this.fechaMensaje = fechaMensaje;
	}

	public String getHoraMensaje() {
		return horaMensaje;
	}

	public void setHoraMensaje(String horaMensaje) {
		this.horaMensaje = horaMensaje;
	}

	public String getFechaInicioHorizonte() {
		return fechaInicioHorizonte;
	}

	public void setFechaInicioHorizonte(String fechaInicioHorizonte) {
		this.fechaInicioHorizonte = fechaInicioHorizonte;
	}

	public String getFechaFinalHorizonte() {
		return fechaFinalHorizonte;
	}

	public void setFechaFinalHorizonte(String fechaFinalHorizonte) {
		this.fechaFinalHorizonte = fechaFinalHorizonte;
	}

	public String getTipoFecha() {
		return tipoFecha;
	}

	public void setTipoFecha(String tipoFecha) {
		this.tipoFecha = tipoFecha;
	}

	public String getIdTransportista() {
		return idTransportista;
	}

	public void setIdTransportista(String idTransportista) {
		this.idTransportista = idTransportista;
	}

	public String getTipoTransporte() {
		return tipoTransporte;
	}

	public void setTipoTransporte(String tipoTransporte) {
		this.tipoTransporte = tipoTransporte;
	}

	@Override
	public String toString() {
		return "CB [inicio=" + inicio + ", referenciaAlbaranProveedor=" + fechaMensaje
				+ ", referenciaAlbaranCliente=" + horaMensaje + ", idConsignatario=" + fechaInicioHorizonte
				+ ", nombreConsignatario=" + fechaFinalHorizonte + ", paisConsignatario=" + tipoFecha
//				+ ", fechaExpedicion=" + fechaExpedicion + ", horaExpedicion=" + horaExpedicion + ", filler=" + filler
				+ ", fechaExpedicion=" + idTransportista + ", horaExpedicion=" + tipoTransporte
				+ "]";
	}

}
