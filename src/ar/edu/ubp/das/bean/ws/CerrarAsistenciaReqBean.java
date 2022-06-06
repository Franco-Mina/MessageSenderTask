package ar.edu.ubp.das.bean.ws;

public class CerrarAsistenciaReqBean {

	private String token;
	private String motivo;
	private int idSolicitud;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getMotivo() {
		return motivo;
	}
	public void setMotivo(String motivo) {
		this.motivo = motivo;
	}
	public int getIdSolicitud() {
		return idSolicitud;
	}
	public void setIdSolicitud(int idSolicitud) {
		this.idSolicitud = idSolicitud;
	}
	
}
