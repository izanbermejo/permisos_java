package ames.comercial.edi.beans;

public class LS {
	public static final int i_inicio = 2;
	public static final int i_codigoSolicitante = 37;
	public static final int i_nombreSolicitante = 72;
	public static final int i_personaAprovisionamiento = 107;
	public static final int i_telefonoSolicitante = 142;

	private String inicio;
	private String codigoSolicitante;
	private String nombreSolicitante; // Grupo aprovisionamiento
	private String personaAprovisionamiento; // Grupo aprovisionamiento
	private String telefonoSolicitante;

	public String getInicio() {
		return inicio;
	}

	public void setInicio(String inicio) {
		this.inicio = inicio;
	}

	public String getCodigoSolicitante() {
		return codigoSolicitante;
	}

	public void setCodigoSolicitante(String codigoSolicitante) {
		this.codigoSolicitante = codigoSolicitante;
	}

	public String getNombreSolicitante() {
		return nombreSolicitante;
	}

	public void setNombreSolicitante(String nombreSolicitante) {
		this.nombreSolicitante = nombreSolicitante;
	}

	public String getPersonaAprovisionamiento() {
		return personaAprovisionamiento;
	}

	public void setPersonaAprovisionamiento(String personaAprovisionamiento) {
		this.personaAprovisionamiento = personaAprovisionamiento;
	}

	public String getTelefonoSolicitante() {
		return telefonoSolicitante;
	}

	public void setTelefonoSolicitante(String telefonoSolicitante) {
		this.telefonoSolicitante = telefonoSolicitante;
	}

	@Override
	public String toString() {
		return "LS{" +
				"inicio='" + inicio + '\'' +
				", codigoSolicitante='" + codigoSolicitante + '\'' +
				", grupoAprovisionamiento='" + nombreSolicitante + '\'' +
				", personaAprovisionamiento='" + personaAprovisionamiento + '\'' +
				", telefonoSolicitante='" + telefonoSolicitante + '\'' +
				'}';
	}

	public LS() {
		super();
	}
}
