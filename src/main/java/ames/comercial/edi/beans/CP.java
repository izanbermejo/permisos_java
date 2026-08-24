package ames.comercial.edi.beans;

public class CP {

	public static final int i_inicio = 2;
	public static final int i_nombreProveedor = 37;
	public static final int i_direccionProveedor = 72;
	public static final int i_localidadProveedor = 107;
	public static final int i_provinciaProveedor = 142;
	public static final int i_codigoPostalProveedor = 152;

	private String inicio;
	private String nombreProveedor;
	private String direccionProveedor;
	private String localidadProveedor;
	private String provinciaProveedor;
	private String codigoPostalProveedor;

	public String getNombreProveedor() {
		return nombreProveedor;
	}

	public void setNombreProveedor(String nombreProveedor) {
		this.nombreProveedor = nombreProveedor;
	}

	public String getDireccionProveedor() {
		return direccionProveedor;
	}

	public void setDireccionProveedor(String direccionProveedor) {
		this.direccionProveedor = direccionProveedor;
	}

	public String getLocalidadProveedor() {
		return localidadProveedor;
	}

	public void setLocalidadProveedor(String localidadProveedor) {
		this.localidadProveedor = localidadProveedor;
	}

	public String getProvinciaProveedor() {
		return provinciaProveedor;
	}

	public void setProvinciaProveedor(String provinciaProveedor) {
		this.provinciaProveedor = provinciaProveedor;
	}

	public String getCodigoPostalProveedor() {
		return codigoPostalProveedor;
	}

	public void setCodigoPostalProveedor(String codPosProveedor) {
		this.codigoPostalProveedor = codPosProveedor;
	}

	// Constructores, getters y setters

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	@Override
	public String toString() {
		return "CP [inicio=" + inicio + ", nombreProveedor=" + nombreProveedor + ", direccionProveedor="
				+ direccionProveedor + ", localidadProveedor=" + localidadProveedor + ", provinciaProveedor="
				+ provinciaProveedor + ", codPosProveedor=" + codigoPostalProveedor + "]";
	}

	public CP() {
	}

}
