package ar.edu.ubp.das.bean.ws;

import java.util.ArrayList;
import java.util.List;

import ar.edu.ubp.das.bean.AsistenciasFinalizadasBean;
public class ListaFinalizadosResponseBean {

	public ListaFinalizadosResponseBean() {
		this.listaAsistenciasFinalizadas = new ArrayList<AsistenciasFinalizadasBean>();
	}
	public ListaFinalizadosResponseBean(List<AsistenciasFinalizadasBean> listaFinalizadas) {
		this.listaAsistenciasFinalizadas = listaFinalizadas;
	}
	
	private String idServicio;
	private int estado;
	private String mensaje;
	private List<AsistenciasFinalizadasBean> listaAsistenciasFinalizadas;
	
	public String getIdServicio() {
		return idServicio;
	}
	public void setIdServicio(String idServicio) {
		this.idServicio = idServicio;
	}
	public int getEstado() {
		return estado;
	}
	public void setEstado(int estado) {
		this.estado = estado;
	}
	public String getMensaje() {
		return mensaje;
	}
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
	public List<AsistenciasFinalizadasBean> getListaAsistenciasFinalizadas() {
		return listaAsistenciasFinalizadas;
	}
	public void setListaAsistenciasFinalizadas(List<AsistenciasFinalizadasBean> listaAsistenciasFinalizadas) {
		this.listaAsistenciasFinalizadas = listaAsistenciasFinalizadas;
	}
}
