package ar.edu.ubp.das.containers;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.credenciales.CredencialesBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.interfaces.IMessageContainer;
import ar.edu.ubp.das.logger.Logger;

public class MessageContainer implements IMessageContainer {
	/*
	 * Este contenedor se usa para obtener y guardar los mensajes del chat
	 * entre los usuarios y los servicios de asistencia
	 */
	private CredencialesBean credenciales;
	
	public MessageContainer(CredencialesBean credenciales) {
		this.credenciales = credenciales;
	}
	
	public List<DetalleAsistenciaBean> ObtenerMensajes(){
		List<DetalleAsistenciaBean> listaMensajes = new ArrayList<DetalleAsistenciaBean>();
		
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			
			listaMensajes = dao.select(null);
		} catch (SQLException e) {
			Logger.getLogger(this.credenciales.getLogPath()).escribirLog("No se pudo recuperar los mensajes enviados",e);
			return null;
		}		
		return listaMensajes;
	}
	
	public int GuardarMensajes(List<DetalleAsistenciaBean> mensajesNuevos, String entidad) {
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			for (DetalleAsistenciaBean detalleAsistenciaBean : mensajesNuevos) {
				//Controlar si falla
				try {
					detalleAsistenciaBean.setIdServicio(entidad);
					dao.insert(detalleAsistenciaBean);
				} catch (Exception e) {
					Logger.getLogger(this.credenciales.getLogPath()).escribirLog("No se pudo guardar el mensaje " + Instant.now().toString());
					Logger.getLogger(this.credenciales.getLogPath() + "Mensajes").escribirLog(new Gson().toJson(detalleAsistenciaBean));
				}
			}
		} catch (SQLException e) {
			Logger.getLogger(this.credenciales.getLogPath()).escribirLog("Error al crear el dao en el guardado de mensajes ",e);
			Logger logger = Logger.getLogger(this.credenciales.getLogPath() + "Mensajes");
			Gson gson = new Gson();
			mensajesNuevos.stream().forEach(p -> logger.escribirLog(gson.toJson(p)));
			return -1;
		}		
		
		return 0;
	}

	public int MarcarEnviados(List<DetalleAsistenciaBean> mensajesEnviados) {
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			for (DetalleAsistenciaBean detalleAsistenciaBean : mensajesEnviados) {
				
				dao.update(detalleAsistenciaBean);
				
			}
		} catch (SQLException e) {
			Logger.getLogger(this.credenciales.getLogPath()).escribirLog("Error al crear el dao marcando los mensajes enviados",e);
			return -1;
		}	
		
		return 0;
	}

}
