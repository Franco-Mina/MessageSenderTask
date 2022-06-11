package ar.edu.ubp.das.bean.ws;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;

public class ChatRequestBean {

	public ChatRequestBean() {}
	public ChatRequestBean(List<DetalleAsistenciaBean> mensajes) {
		this.listaMensajes = mensajes != null ? mensajes : new ArrayList<DetalleAsistenciaBean>();
		this.fecha = Date.from(Instant.now());
	}
	
	private List<DetalleAsistenciaBean> listaMensajes;
	private Date fecha;
	private String token;
	
	public List<DetalleAsistenciaBean> getListaMensajes() {
		return listaMensajes;
	}
	public void setListaMensajes(List<DetalleAsistenciaBean> listaMensajes) {
		this.listaMensajes = listaMensajes;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
