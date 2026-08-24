package ames.comercial.edi.beans;

public class AA {
	public static final int i_inicio = 2;
	public static final int i_referenciaAlbaranEntrada = 37;
	public static final int i_fechaAlbaran = 47;
	public static final int i_horaAlbaran = 52;
	public static final int i_cantidadEnviadaAlbaran = 68;
	public static final int i_cantidadRecibidaAlbaran = 84;
	public static final int i_fechaRecepcion = 94;

	private String inicio;
	private String referenciaAlbaranEntrada;
	private String fechaAlbaran;
	private String horaAlbaran;
	private String cantidadEnviadaAlbaran;
	private String cantidadRecibidaAlbaran;
	private String fechaRecepcion;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getReferenciaAlbaranEntrada() {
		return referenciaAlbaranEntrada;
	}

	public void setReferenciaAlbaranEntrada(String referenciaAlbaranEntrada) {
		this.referenciaAlbaranEntrada = referenciaAlbaranEntrada;
	}

	public String getFechaAlbaran() {
		return fechaAlbaran;
	}

	public void setFechaAlbaran(String fechaAlbaran) {
		this.fechaAlbaran = fechaAlbaran;
	}

	public String getHoraAlbaran() {
		return horaAlbaran;
	}

	public void setHoraAlbaran(String horaAlbaran) {
		this.horaAlbaran = horaAlbaran;
	}

	public String getCantidadEnviadaAlbaran() {
		return cantidadEnviadaAlbaran;
	}

	public void setCantidadEnviadaAlbaran(String cantidadEnviadaAlbaran) {
		this.cantidadEnviadaAlbaran = cantidadEnviadaAlbaran;
	}

	public String getCantidadRecibidaAlbaran() {
		return cantidadRecibidaAlbaran;
	}

	public void setCantidadRecibidaAlbaran(String cantidadRecibidaAlbaran) {
		this.cantidadRecibidaAlbaran = cantidadRecibidaAlbaran;
	}

	public String getFechaRecepcion() {
		return fechaRecepcion;
	}

	public void setFechaRecepcion(String fechaRecepcion) {
		this.fechaRecepcion = fechaRecepcion;
	}

	@Override
	public String toString() {
		return "AA{" +
				"inicio='" + inicio + '\'' +
				", referenciaAlbaranEntrada='" + referenciaAlbaranEntrada + '\'' +
				", fechaAlbaran='" + fechaAlbaran + '\'' +
				", horaAlbaran='" + horaAlbaran + '\'' +
				", cantidadEnviadaAlbaran='" + cantidadEnviadaAlbaran + '\'' +
				", cantidadRecibidaAlbaran='" + cantidadRecibidaAlbaran + '\'' +
				", fechaRecepcion='" + fechaRecepcion + '\'' +
				'}';
	}

	public AA() {
	}
}
