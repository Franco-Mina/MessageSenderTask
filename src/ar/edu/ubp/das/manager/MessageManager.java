package ar.edu.ubp.das.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.bean.ws.ChatRequestBean;
import ar.edu.ubp.das.bean.ws.ChatResponseBean;
import ar.edu.ubp.das.conections.ConnectionManager;
import ar.edu.ubp.das.conections.util.Conexion;
import ar.edu.ubp.das.interfaces.IMessageContainer;
import ar.edu.ubp.das.logger.Logger;
import ar.edu.ubp.das.token.db.ConsoleTokenManger;

public class MessageManager {
	private final String cadenaConexion = "jdbc:sqlserver://172.10.3.106;databaseName=gobierno_provincial";
	private final String usuario        = "sa";
	private final String password       = "Francomina1";
	private final String logPath        = "c:/Logger/MessageSender/";
	private IMessageContainer contenedorDeMensajes;
	
	public MessageManager(IMessageContainer contenedor) {
		this.contenedorDeMensajes = contenedor;
	}	
	
	public int EnviarMensajes() {
		try {
			
			List<DetalleAsistenciaBean> listaMensajes = contenedorDeMensajes.ObtenerMensajes();
			//Separar por entidad 
			Map<String, List<DetalleAsistenciaBean>> mensajesPorEntidad = 
					listaMensajes.stream().collect(
							Collectors.groupingBy(DetalleAsistenciaBean::getIdServicio));
			//Llamar por entidad
			
			ConnectionManager connectionManager = new ConnectionManager("src/ar/edu/ubp/das/manager/conexiones.xml", 
					new  ConsoleTokenManger(this.cadenaConexion,this.usuario,this.password),logPath);
			
			Gson gson = new Gson();
			
			for (String entidad : mensajesPorEntidad.keySet()) {
			
				Conexion conexion = connectionManager.getConexiones().stream().
						filter(x -> x.getDescripcion().contentEquals("Mensaje_"+entidad)).findFirst().orElse(null);
				
				if(conexion == null) {
					Logger.getLogger(this.logPath).escribirLog("No se encontro una conexion para enviar mensajes a la entidad "
							+ entidad + " los mensajes quedaran registrados para intentar nuevamente.");
					//Removemos los mensajes de la entidad para despues no marcarlos como enviados.
					mensajesPorEntidad.remove(entidad);
					//Continuamos con la proxima entidad.
					continue;
				}
				
				ChatRequestBean request = new ChatRequestBean(mensajesPorEntidad.get(entidad));
				String respuesta = 
						connectionManager.callApi(conexion.getNroConexion(), request);
			
				ChatResponseBean mensajesNuevos = gson.fromJson(respuesta, ChatResponseBean.class);
							
				if(!mensajesNuevos.getListaMensajes().isEmpty()) {
					contenedorDeMensajes.GuardarMensajes(mensajesNuevos.getListaMensajes());	
				}	
				
				//Marcar Enviados
				contenedorDeMensajes.MarcarEnviados(mensajesPorEntidad.get(entidad));
			}
			
			FileManager fileManager = new FileManager(logPath);
			//Recuperamos algun mensaje que no se pudo guardar antes
			List<DetalleAsistenciaBean> mensajesNoGuardadosAsistenciaBeans = fileManager.RecuperarMensajesNoGuardados();
	
			contenedorDeMensajes.GuardarMensajes(mensajesNoGuardadosAsistenciaBeans);
			
			return 0;
		}
		catch (Exception e) {
			Logger.getLogger(logPath).escribirLog("Error en el envio de mensajes " , e);
			return -1;
		}
	}
	
	
}
