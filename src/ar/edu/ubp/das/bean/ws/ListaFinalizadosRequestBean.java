package ar.edu.ubp.das.bean.ws;

import java.sql.Timestamp;

public class ListaFinalizadosRequestBean {

	private String token;
	private Timestamp fecha;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Timestamp getFecha() {
		return fecha;
	}
	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}	
}
