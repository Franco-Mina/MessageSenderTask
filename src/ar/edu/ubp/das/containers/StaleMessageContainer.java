package ar.edu.ubp.das.containers;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.interfaces.IMessageContainer;
import ar.edu.ubp.das.logger.Logger;

public class StaleMessageContainer implements IMessageContainer {
	/*
	 * Este contenedor se usa para marcar los chats que hay que fueron terminados desde el servicio de asistencia
	 * y notificar a los servicios de asistencia los chats terminados que no se notifico el fin del chat
	 */
	
	private final String cadenaConexion = "jdbc:sqlserver://172.10.3.106;databaseName=gobierno_provincial";
	private final String logPath        = "c:/Logger/MessageSender/";
	
	public List<DetalleAsistenciaBean> ObtenerMensajes(){
		List<DetalleAsistenciaBean> listaMensajes = new ArrayList<DetalleAsistenciaBean>();
		
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.das.daos",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");
			
			listaMensajes = dao.select(null);
		} catch (SQLException e) {
			Logger.getLogger(logPath).escribirLog("No se pudo recuperar los mensajes enviados",e);
			return null;
		}		
		return listaMensajes;
	}
	
	public int GuardarMensajes(List<DetalleAsistenciaBean> mensajesNuevos) {
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.das.daos",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");
			for (DetalleAsistenciaBean detalleAsistenciaBean : mensajesNuevos) {
				//Controlar si falla
				try {
					dao.insert(detalleAsistenciaBean);
				} catch (Exception e) {
					Logger.getLogger(logPath).escribirLog("No se pudo guardar el mensaje " + Instant.now().toString());
					Logger.getLogger(logPath + "Mensajes").escribirLog(new Gson().toJson(detalleAsistenciaBean));
				}
			}
		} catch (SQLException e) {
			Logger.getLogger(logPath).escribirLog("Error al crear el dao en el guardado de mensajes ",e);
			Logger logger = Logger.getLogger(logPath + "Mensajes");
			Gson gson = new Gson();
			mensajesNuevos.stream().forEach(p -> logger.escribirLog(gson.toJson(p)));
			return -1;
		}		
		
		return 0;
	}

	public int MarcarEnviados(List<DetalleAsistenciaBean> mensajesEnviados) {
		try {
			Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> dao = DaoFactory.getDao("DetalleAsistencia", "ar.edu.das.daos",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");
			for (DetalleAsistenciaBean detalleAsistenciaBean : mensajesEnviados) {
				//Controlar si falla
				dao.update(detalleAsistenciaBean);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}	
		
		return 0;
	}
}
