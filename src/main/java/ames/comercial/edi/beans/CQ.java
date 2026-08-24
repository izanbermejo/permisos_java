package ames.comercial.edi.beans;

public class CQ {
	public static final int i_inicio = 2;
	public static final int i_paisProveedor = 5;
	public static final int i_personaContactoProveedor = 40;
	public static final int i_telefonoProveedor = 75;
	public static final int i_faxProveedor = 110;

	private String inicio;
	private String paisProveedor;
	private String personaContactoProveedor;
	private String telefonoProveedor;
	private String faxProveedor;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getPaisProveedor() {
		return paisProveedor;
	}

	public void setPaisProveedor(String paisProveedor) {
		this.paisProveedor = paisProveedor;
	}

	public String getPersonaContactoProveedor() {
		return personaContactoProveedor;
	}

	public void setPersonaContactoProveedor(String personaContactoProveedor) {
		this.personaContactoProveedor = personaContactoProveedor;
	}

	public String getTelefonoProveedor() {
		return telefonoProveedor;
	}

	public void setTelefonoProveedor(String telefonoProveedor) {
		this.telefonoProveedor = telefonoProveedor;
	}

	public String getFaxProveedor() {
		return faxProveedor;
	}

	public void setFaxProveedor(String faxProveedor) {
		this.faxProveedor = faxProveedor;
	}

	@Override
	public String toString() {
		return "CQ [inicio=" + inicio + ", paisProveedor=" + paisProveedor + ", personaContactoProveedor="
				+ personaContactoProveedor + ", telefonoProveedor=" + telefonoProveedor + ", faxProveedor="
				+ faxProveedor + "]";
	}

	public CQ() {
	}

}
