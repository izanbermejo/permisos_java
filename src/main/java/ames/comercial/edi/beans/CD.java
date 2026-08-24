package ames.comercial.edi.beans;

public class CD {
	
	public static final int i_inicio = 2;
	public static final int i_paisComprador = 5;
	public static final int i_personaContactoComprador = 40;
	public static final int i_telefonoComprador = 75;
	public static final int i_faxComprador = 110;
	public static final int i_emailComprador = 145;
	
	private String inicio;
	private String paisComprador;
	private String personaContactoComprador;
	private String telefonoComprador;
	private String faxComprador;
	private String emailComprador;
	
	@Override
	public String toString() {
		return "CD [inicio=" + inicio + ", paisComprador=" + paisComprador + ", personaContactoComprador="
				+ personaContactoComprador + ", telefonoComprador=" + telefonoComprador + ", faxComprador="
				+ faxComprador + ", emailComprador=" + emailComprador + "]";
	}

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getPaisComprador() {
		return paisComprador;
	}

	public void setPaisComprador(String paisComprador) {
		this.paisComprador = paisComprador;
	}

	public String getPersonaContactoComprador() {
		return personaContactoComprador;
	}

	public void setPersonaContactoComprador(String personaContactoComprador) {
		this.personaContactoComprador = personaContactoComprador;
	}

	public String getTelefonoComprador() {
		return telefonoComprador;
	}

	public void setTelefonoComprador(String telefonoComprador) {
		this.telefonoComprador = telefonoComprador;
	}

	public String getFaxComprador() {
		return faxComprador;
	}

	public void setFaxComprador(String faxComprador) {
		this.faxComprador = faxComprador;
	}

	public String getEmailComprador() {
		return emailComprador;
	}

	public void setEmailComprador(String emailComprador) {
		this.emailComprador = emailComprador;
	}

	public CD() {
	}

}
