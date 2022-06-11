package ar.edu.ubp.das.bean;

import java.sql.Timestamp;

public class AsistenciasFinalizadasBean {

	private int idAsistencia;
	private String idServicio;
	private Timestamp fechaFinalizada;
	private Timestamp fechaCancelacion;
	private String motivoCancelacion;
	private String estado;
	public int getIdSolicitud() {
		return idSolicitud;
	}
	public void setIdSolicitud(int idSolicitud) {
		this.idSolicitud = idSolicitud;
	}
	private int idSolicitud;
	
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public void setIdAsistencia(int idAsistencia) {
		this.idAsistencia = idAsistencia;
	}
	public Timestamp getFechaFinalizada() {
		return fechaFinalizada;
	}
	public void setFechaFinalizada(Timestamp fechaFinalizada) {
		this.fechaFinalizada = fechaFinalizada;
	}
	public Timestamp getFechaCancelacion() {
		return fechaCancelacion;
	}
	public void setFechaCancelacion(Timestamp fechaCancelacion) {
		this.fechaCancelacion = fechaCancelacion;
	}
	public int getIdAsistencia() {
		return idAsistencia;
	}
	public String getIdServicio() {
		return idServicio;
	}
	public void setIdServicio(String idServicio) {
		this.idServicio = idServicio;
	}
	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}
	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}
}
