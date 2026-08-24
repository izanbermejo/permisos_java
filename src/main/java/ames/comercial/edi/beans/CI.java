package ames.comercial.edi.beans;

public class CI {
	
	public static final int i_inicio = 2;
	public static final int i_idComprador = 37;
	public static final int i_idProveedor = 72;
	public static final int i_numCuentaInternaProveedor = 107;
	public static final int i_idExpedidor = 142;
	public static final int i_idFacturado = 177;
	
	private String inicio;
	private String idComprador;
	private String idProveedor;
	private String numCuentaInternaProveedor;
	private String idExpedidor;
	private String idFacturado;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getIdComprador() {
		return idComprador;
	}

	public void setIdComprador(String idComprador) {
		this.idComprador = idComprador;
	}

	public String getIdProveedor() {
		return idProveedor;
	}

	public void setIdProveedor(String idProveedor) {
		this.idProveedor = idProveedor;
	}

	public String getNumCuentaInternaProveedor() {
		return numCuentaInternaProveedor;
	}

	public void setNumCuentaInternaProveedor(String numCuentaInternaProveedor) {
		this.numCuentaInternaProveedor = numCuentaInternaProveedor;
	}

	public String getIdExpedidor() {
		return idExpedidor;
	}

	public void setIdExpedidor(String idExpedidor) {
		this.idExpedidor = idExpedidor;
	}

	public String getIdFacturado() {
		return idFacturado;
	}

	public void setIdFacturado(String idFacturado) {
		this.idFacturado = idFacturado;
	}

	@Override
	public String toString() {
		return "CI [inicio=" + inicio + ", idComprador=" + idComprador + ", idProveedor=" + idProveedor
				+ ", numCuentaInternaProveedor=" + numCuentaInternaProveedor + ", idExpedidor=" + idExpedidor
				+ ", idFacturado=" + idFacturado + "]";
	}

	public CI() {
	}
}
