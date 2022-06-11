package ar.edu.ubp.das.interfaces;

import java.util.List;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;

public interface IMessageContainer {
	List<DetalleAsistenciaBean> ObtenerMensajes();
	int GuardarMensajes(List<DetalleAsistenciaBean> mensajesNuevos, String entidad);
	int MarcarEnviados(List<DetalleAsistenciaBean> mensajesEnviados);
}
