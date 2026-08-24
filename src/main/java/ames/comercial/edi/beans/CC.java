package ames.comercial.edi.beans;

public class CC {

	public static final int i_inicio = 2;
	public static final int i_nombreComprador = 37;
	public static final int i_direccionComprador = 72;
	public static final int i_localidadComprador = 107;
	public static final int i_provinciaComprador = 132;
	public static final int i_codigoPostalComprador = 142;

	private String inicio;
	private String nombreComprador;
	private String direccionComprador;
	private String localidadComprador;
	private String provinciaComprador;
	private String codigoPostalComprador;

	@Override
	public String toString() {
		return "CC [inicio=" + inicio + ", nombreComprador=" + nombreComprador + ", direccionComprador="
				+ direccionComprador + ", localidadComprador=" + localidadComprador + ", provinciaComprador="
//				+ provinciaComprador + ", codigoPostalComprador=" + codigoPostalComprador + ", filler=" + filler + "]";
				+ provinciaComprador + ", codigoPostalComprador=" + codigoPostalComprador + "]";
	}

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getNombreComprador() {
		return nombreComprador;
	}

	public void setNombreComprador(String nombreComprador) {
		this.nombreComprador = nombreComprador;
	}

	public String getDireccionComprador() {
		return direccionComprador;
	}

	public void setDireccionComprador(String direccionComprador) {
		this.direccionComprador = direccionComprador;
	}

	public String getLocalidadComprador() {
		return localidadComprador;
	}

	public void setLocalidadComprador(String localidadComprador) {
		this.localidadComprador = localidadComprador;
	}

	public String getProvinciaComprador() {
		return provinciaComprador;
	}

	public void setProvinciaComprador(String provinciaComprador) {
		this.provinciaComprador = provinciaComprador;
	}

	public String getCodigoPostalComprador() {
		return codigoPostalComprador;
	}

	public void setCodigoPostalComprador(String codigoPostalComprador) {
		this.codigoPostalComprador = codigoPostalComprador;
	}

	public CC() {
	}

}
