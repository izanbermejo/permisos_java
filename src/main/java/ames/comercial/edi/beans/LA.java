package ames.comercial.edi.beans;

public class LA {

	public static final int i_inicio = 2;
	public static final int i_idArticuloComprador = 37;
	public static final int i_estadoArticulo = 42;
	public static final int i_codigoAccion = 43;
	public static final int i_unidadMedida = 46;
	public static final int i_lugarEntrega = 63;
	public static final int i_lugarDestinoFinal = 80;
	public static final int i_codigoAlmacen = 97;
	public static final int i_fechaCubierta = 107;
	public static final int i_paisOrigenCodificado = 110;
	public static final int i_fechaLimiteEntrega = 120;
	public static final int i_fechaCalculoActual = 130;
	public static final int i_fechaInicioCalculo = 140;
	public static final int i_codigoFrecuenciaEntrega = 141;
	public static final int i_indicadorRequerimiento = 142;
	public static final int i_codigoCaracteristicaItem = 144;
	public static final int i_horaLimiteEntrega = 149;
	
	private String inicio;
	private String idArticuloComprador;
	private String estadoArticulo;
	private String codigoAccion;
	private String unidadMedida;
	private String lugarEntrega;
	private String lugarDestinoFinal;
	private String codigoAlmacen;
	private String fechaCubierta;
	private String paisOrigenCodificado;
	private String fechaLimiteEntrega;
	private String fechaCalculoActual;
	private String fechaInicioCalculo;
	private String codigoFrecuenciaEntrega;
	private String indicadorRequerimiento;
	private String codigoCaracteristicaItem;
	private String horaLimiteEntrega;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getIdArticuloComprador() {
		return idArticuloComprador;
	}

	public void setIdArticuloComprador(String idArticuloComprador) {
		this.idArticuloComprador = idArticuloComprador;
	}

	public String getEstadoArticulo() {
		return estadoArticulo;
	}

	public void setEstadoArticulo(String estadoArticulo) {
		this.estadoArticulo = estadoArticulo;
	}

	public String getCodigoAccion() {
		return codigoAccion;
	}

	public void setCodigoAccion(String codigoAccion) {
		this.codigoAccion = codigoAccion;
	}

	public String getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(String unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public String getLugarEntrega() {
		return lugarEntrega;
	}

	public void setLugarEntrega(String lugarEntrega) {
		this.lugarEntrega = lugarEntrega;
	}

	public String getLugarDestinoFinal() {
		return lugarDestinoFinal;
	}

	public void setLugarDestinoFinal(String lugarDestinoFinal) {
		this.lugarDestinoFinal = lugarDestinoFinal;
	}

	public String getCodigoAlmacen() {
		return codigoAlmacen;
	}

	public void setCodigoAlmacen(String codigoAlmacen) {
		this.codigoAlmacen = codigoAlmacen;
	}

	public String getFechaCubierta() {
		return fechaCubierta;
	}

	public void setFechaCubierta(String fechaCubierta) {
		this.fechaCubierta = fechaCubierta;
	}

	public String getPaisOrigenCodificado() {
		return paisOrigenCodificado;
	}

	public void setPaisOrigenCodificado(String paisOrigenCodificado) {
		this.paisOrigenCodificado = paisOrigenCodificado;
	}

	public String getFechaLimiteEntrega() {
		return fechaLimiteEntrega;
	}

	public void setFechaLimiteEntrega(String fechaLimiteEntrega) {
		this.fechaLimiteEntrega = fechaLimiteEntrega;
	}

	public String getFechaCalculoActual() {
		return fechaCalculoActual;
	}

	public void setFechaCalculoActual(String fechaCalculoActual) {
		this.fechaCalculoActual = fechaCalculoActual;
	}

	public String getFechaInicioCalculo() {
		return fechaInicioCalculo;
	}

	public void setFechaInicioCalculo(String fechaInicioCalculo) {
		this.fechaInicioCalculo = fechaInicioCalculo;
	}

	public String getCodigoFrecuenciaEntrega() {
		return codigoFrecuenciaEntrega;
	}

	public void setCodigoFrecuenciaEntrega(String codigoFrecuenciaEntrega) {
		this.codigoFrecuenciaEntrega = codigoFrecuenciaEntrega;
	}

	public String getIndicadorRequerimiento() {
		return indicadorRequerimiento;
	}

	public void setIndicadorRequerimiento(String indicadorRequerimiento) {
		this.indicadorRequerimiento = indicadorRequerimiento;
	}

	public String getCodigoCaracteristicaItem() {
		return codigoCaracteristicaItem;
	}

	public void setCodigoCaracteristicaItem(String codigoCaracteristicaItem) {
		this.codigoCaracteristicaItem = codigoCaracteristicaItem;
	}

	public String getHoraLimiteEntrega() {
		return horaLimiteEntrega;
	}

	public void setHoraLimiteEntrega(String horaLimiteEntrega) {
		this.horaLimiteEntrega = horaLimiteEntrega;
	}

	@Override
	public String toString() {
		return "LA [inicio=" + inicio + ", idArticuloComprador=" + idArticuloComprador + ", estadoArticulo="
				+ estadoArticulo + ", codigoAccion=" + codigoAccion + ", unidadMedida=" + unidadMedida
				+ ", lugarEntrega=" + lugarEntrega + ", lugarDestinoFinal=" + lugarDestinoFinal + ", codigoAlmacen="
				+ codigoAlmacen + ", fechaCubierta=" + fechaCubierta + ", paisOrigenCodificado=" + paisOrigenCodificado
				+ ", fechaLimiteEntrega=" + fechaLimiteEntrega + ", fechaCalculoActual=" + fechaCalculoActual
				+ ", fechaInicioCalculo=" + fechaInicioCalculo + ", codigoFrecuenciaEntrega=" + codigoFrecuenciaEntrega
				+ ", indicadorRequerimiento=" + indicadorRequerimiento + ", codigoCaractetisticaItem="
				+ codigoCaracteristicaItem + ", horaLimite=" + horaLimiteEntrega + "]";
	}

	public LA() {
		super();
	}
}
