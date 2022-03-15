package ar.edu.ubp.das.bean;

import java.sql.Timestamp;

public class DetalleAsistenciaBean {

	private int id;
	private String idAsistencia;
	private String idServicio;
	private String tipoDato;
	private String Dato;
	private Timestamp fechaCreacion;
	private Timestamp fechaEnvio;
	private Boolean finalizado;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdAsistencia() {
		return idAsistencia;
	}
	public void setIdAsistencia(String idAsistencia) {
		this.idAsistencia = idAsistencia;
	}
	public String getTipoDato() {
		return tipoDato;
	}
	public void setTipoDato(String tipoDato) {
		this.tipoDato = tipoDato;
	}
	public Timestamp getFechaCreacion() {
		return fechaCreacion;
	}
	public void setFechaCreacion(Timestamp fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}
	public Timestamp getFechaEnvio() {
		return fechaEnvio;
	}
	public void setFechaEnvio(Timestamp fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}
	public String getDato() {
		return Dato;
	}
	public void setDato(String dato) {
		Dato = dato;
	}
	public String getIdServicio() {
		return idServicio;
	}
	public void setIdServicio(String idServicio) {
		this.idServicio = idServicio;
	}
	public Boolean getFinalizado() {
		return finalizado;
	}
	public void setFinalizado(Boolean finalizado) {
		this.finalizado = finalizado;
	}
	
}
