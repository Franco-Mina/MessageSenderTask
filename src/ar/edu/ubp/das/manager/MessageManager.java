package ar.edu.ubp.das.manager;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import ar.edu.ubp.das.Servicios.Servicios;
import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.bean.ws.ChatRequestBean;
import ar.edu.ubp.das.bean.ws.ChatResponseBean;
import ar.edu.ubp.das.conections.ConnectionManager;
import ar.edu.ubp.das.conections.util.Conexion;
import ar.edu.ubp.das.credenciales.CredencialesBean;
import ar.edu.ubp.das.interfaces.IMessageContainer;
import ar.edu.ubp.das.logger.Logger;
import ar.edu.ubp.das.token.db.ConsoleTokenManger;

public class MessageManager {
	private CredencialesBean credenciales;
	private IMessageContainer contenedorDeMensajes;
	
	public MessageManager(IMessageContainer contenedor,CredencialesBean credenciales) {
		this.contenedorDeMensajes = contenedor;
		this.credenciales = credenciales;
	}	
	
	public int EnviarMensajes() {
		try {
			List<String> listaServicios = new Servicios(credenciales).ObtenerServicios();
			
			List<DetalleAsistenciaBean> listaMensajes = contenedorDeMensajes.ObtenerMensajes();
			//Separar por entidad 
			Map<String, List<DetalleAsistenciaBean>> mensajesPorEntidad = 
					listaMensajes.stream().collect(
							Collectors.groupingBy(DetalleAsistenciaBean::getIdServicio));
			
			//Llamar por entidad			
			ConnectionManager connectionManager = new ConnectionManager(credenciales.getPathConexiones(), 
					new ConsoleTokenManger(credenciales.getCadenaConexion(),credenciales.getUsuario(),credenciales.getPassword()),credenciales.getLogPath());
			
			Gson gson = new Gson();
			
			for (String entidad : listaServicios) {
				Conexion conexion = connectionManager.getConexiones().stream().
						filter(x ->x.getDescripcion() != null && x.getDescripcion().contentEquals("Mensaje"+entidad))
						.findFirst().orElse(null);
				
				if(conexion == null) {
					Logger.getLogger(this.credenciales.getLogPath()).escribirLog("No se encontro una conexion para enviar mensajes a la entidad "
							+ entidad + " los mensajes quedaran registrados para intentar nuevamente.");
					//Removemos los mensajes de la entidad para despues no marcarlos como enviados.
					mensajesPorEntidad.remove(entidad);
					//Continuamos con la proxima entidad.
					continue;
				}
				
				List<DetalleAsistenciaBean> mensajesEntidad = mensajesPorEntidad.get(entidad);
				
				ChatRequestBean request = new ChatRequestBean(mensajesEntidad);
				String respuesta = 
						connectionManager.callApi(conexion.getNroConexion(), request);
			
				ChatResponseBean mensajesNuevos = gson.fromJson(respuesta, ChatResponseBean.class);
			
				if(mensajesNuevos.getEstado() != 1) {
					//Si la respues no es positiva continuamos con el proximo servicio
					continue;
				}
				
				if(!mensajesNuevos.getListaMensajes().isEmpty()) {
					contenedorDeMensajes.GuardarMensajes(mensajesNuevos.getListaMensajes(),entidad);	
				}	
				
				if(mensajesEntidad != null && !mensajesEntidad.isEmpty()) {
					//Marcar Enviados
					contenedorDeMensajes.MarcarEnviados(mensajesEntidad);
				}				
			}			
	
			System.out.println("Fin envio de mensajes");
			return 0;
		}
		catch (Exception e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog("Error en el envio de mensajes ", e);
			return -1;
		}
	}
	
	
}
