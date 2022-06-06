package ar.edu.ubp.das.manager;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import ar.edu.ubp.das.bean.AsistenciasFinalizadasBean;
import ar.edu.ubp.das.bean.ServicioBean;
import ar.edu.ubp.das.bean.ws.CerrarAsistenciaReqBean;
import ar.edu.ubp.das.bean.ws.CerrarAsistenciaRespBean;
import ar.edu.ubp.das.bean.ws.ListaFinalizadosRequestBean;
import ar.edu.ubp.das.bean.ws.ListaFinalizadosResponseBean;
import ar.edu.ubp.das.conections.ConnectionManager;
import ar.edu.ubp.das.conections.util.Conexion;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logger.Logger;
import ar.edu.ubp.das.token.db.ConsoleTokenManger;

public class ChatManager {
	private final String cadenaConexion = "jdbc:sqlserver://172.10.3.106;databaseName=gobierno_provincial;user=sa;password=Francomina1;";
	private final String usuario        = "sa";
	private final String password       = "Francomina1";
	private final String logPath        = "c:/Logger/MessageSender/Chats/";
	private final String pathConexiones = "src/ar/edu/ubp/das/manager/conexiones.xml";
	
	
	public int CerrarChats() {
		Gson gson = new Gson();
		try {
			List<AsistenciasFinalizadasBean> listaAsistencias = ObtenerMensajes();
	
			List<String> listaServicios = ObtenerServicios();

			Map<String, List<AsistenciasFinalizadasBean>> asistenciaPorSevicio = 
					listaAsistencias.stream().collect(Collectors.groupingBy(AsistenciasFinalizadasBean::getIdServicio));
			
			ConnectionManager connectionManager = new ConnectionManager(pathConexiones, 
					new  ConsoleTokenManger(this.cadenaConexion,this.usuario,this.password),logPath);
			
			for (String servicio : listaServicios) {
				Conexion conexion = connectionManager.getConexiones().stream().
						filter(x ->x.getDescripcion() != null && Objects.equals(x.getDescripcion(), "ChatCerrados"+servicio))
						.findFirst().orElse(null);
				
				if(conexion == null) {
					Logger.getLogger(this.logPath).escribirLog("No se encontro una conexion para pedir los chats finalizados a la entidad "
							+ servicio);
					// Removemos los chats de la entidad para despues no marcarlos como enviados.
					asistenciaPorSevicio.remove(servicio);
					//Continuamos con la proxima entidad.
					continue;
				}
				
				List<AsistenciasFinalizadasBean> asistenciasServicio = asistenciaPorSevicio.get(servicio);
				ListaFinalizadosRequestBean request = new ListaFinalizadosRequestBean();
				List<AsistenciasFinalizadasBean> marcarEnDb = new ArrayList<AsistenciasFinalizadasBean>();
				
				request.setFecha(Timestamp.from(Instant.now()));
				
				String jsonRespuesta = connectionManager.callApi(conexion.getNroConexion(), request);
				
				ListaFinalizadosResponseBean finalizadosRespuesta = gson.fromJson(jsonRespuesta,ListaFinalizadosResponseBean.class);
				
				for (AsistenciasFinalizadasBean asistenciaFinalizada : finalizadosRespuesta.getListaAsistenciasFinalizadas()) {
					if(asistenciasServicio != null && !asistenciasServicio.isEmpty()) {
						AsistenciasFinalizadasBean asistenciaLocal = asistenciasServicio.stream()
								.filter(x->x.getIdAsistencia() == asistenciaFinalizada.getIdAsistencia()).findFirst().orElse(null);
								
								if(asistenciaLocal != null) {
									//validar cancelacion
									if(asistenciaLocal.getEstado() != null && asistenciaLocal.getEstado().toLowerCase().contentEquals("cancelado")
											&& asistenciaFinalizada.getMotivoCancelacion() != null && !asistenciaFinalizada.getMotivoCancelacion().isEmpty()) {
										//TODO: Corregir si el usuario esta deshabilitado
									}
								}
					}					
					 
					marcarEnDb.add(asistenciaFinalizada);
				}
				
				MarcarChatsCerrados(marcarEnDb);
				
				if(asistenciasServicio != null) {
					List<Integer> listaIdSolicitudRecibidas = finalizadosRespuesta.getListaAsistenciasFinalizadas().stream()
							.map(AsistenciasFinalizadasBean::getIdAsistencia).collect(Collectors.toList());
					//Obtenemos la listas con las asistencias que nosotros tenemos como finalizadas pero el servicio no
	 				List<AsistenciasFinalizadasBean> cancelacionesANotificar =  asistenciasServicio.stream()							
							.filter(x->!listaIdSolicitudRecibidas.contains(x.getIdAsistencia())).collect(Collectors.toList());
	 				
	 				if(!cancelacionesANotificar.isEmpty()) {
	 					//Notificamos las que tenemos canceladas pero el servicio no
	 					NotificarCancelaciones(cancelacionesANotificar,connectionManager,servicio);
	 				}
				}
				
			}
		}
		catch(Exception e) {
			Logger.getLogger(logPath).escribirLog(e);
		}
		
		return 0;
	}
	
	public int NotificarCancelaciones(List<AsistenciasFinalizadasBean> cancelacionesANotificar,ConnectionManager connectionManager,String servicio) {
		try {
			Gson gson = new Gson();
			Conexion conexion = connectionManager.getConexiones().stream().
					filter(x ->x.getDescripcion() != null && Objects.equals(x.getDescripcion(), "CancelarChat"+servicio))
					.findFirst().orElse(null);
			
			List<AsistenciasFinalizadasBean> solicitudesMarcarNotificadas =  new ArrayList<AsistenciasFinalizadasBean>();
			
			if(conexion == null) {
				Logger.getLogger(this.logPath).escribirLog("No se encontro una conexion para enviar los chats finalizados a la entidad "
						+ servicio);
				return -1;
			}
			for (AsistenciasFinalizadasBean asistenciasFinalizadasBean : cancelacionesANotificar) {
				try {
					CerrarAsistenciaReqBean request = new CerrarAsistenciaReqBean();
					request.setIdSolicitud(asistenciasFinalizadasBean.getIdSolicitud());
					request.setMotivo(asistenciasFinalizadasBean.getMotivoCancelacion());
					
					String jsonRespuesta = connectionManager.callApi(conexion.getNroConexion(), request);
					
					CerrarAsistenciaRespBean respuesta = gson.fromJson(jsonRespuesta, CerrarAsistenciaRespBean.class);

					if(respuesta == null || respuesta.getEstado() != 1) {
						StringBuilder error = new StringBuilder("Error al enviar la cancelacion del chat " + asistenciasFinalizadasBean.getIdAsistencia());
						if(respuesta !=null && respuesta.getMensaje() != null) {
							error.append(" con error:" + respuesta.getMensaje()); 
						}
						Logger.getLogger(logPath).escribirLog(error.toString());
					}
					
					solicitudesMarcarNotificadas.add(asistenciasFinalizadasBean);
				}catch (Exception e) {
					Logger.getLogger(logPath).escribirLog("Error al enviar la cancelacion del chat " + 
							asistenciasFinalizadasBean.getIdAsistencia() + " con error:"+ e.getMessage());
				}				
			}	
			if(!solicitudesMarcarNotificadas.isEmpty()) {
				MarcarChatsCerrados(cancelacionesANotificar);
			}
			
				
		} catch (Exception e) {
			Logger.getLogger(logPath).escribirLog(e);
		}
		
		return 0;
	}
	
	
	public List<AsistenciasFinalizadasBean> ObtenerMensajes(){
		List<AsistenciasFinalizadasBean> listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		
		try {
			Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean> dao = DaoFactory.getDao("Asistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");
			
			listaAsistencias = dao.select(null);
			
			if(listaAsistencias == null)
				listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		} catch (SQLException e) {
			Logger.getLogger(logPath).escribirLog("No se pudo recuperar los chats",e);
			return null;
		}		
		return listaAsistencias;
	}
	
	public List<String> ObtenerServicios(){
		List<ServicioBean> listaServicios = new ArrayList<ServicioBean>();
		
		try {
			Dao<ServicioBean,ServicioBean> dao = DaoFactory.getDao("Servicio", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");

			listaServicios = dao.select(null);
			
			if(listaServicios == null)
				listaServicios = new ArrayList<ServicioBean>();
			
		} catch (SQLException e) {
			Logger.getLogger(logPath).escribirLog("No se pudo recuperar los servicios",e);
			return null;
		}		
		return listaServicios.stream().filter(x -> x.getHabilitado().toLowerCase().contentEquals("s")).
				map(x->x.getId()).collect(Collectors.toList());
	}
	
	
	public List<AsistenciasFinalizadasBean> MarcarChatsCerrados(List<AsistenciasFinalizadasBean> finalizadosRespuesta){
		List<AsistenciasFinalizadasBean> listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		
		try {
			Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean> dao = DaoFactory.getDao("Asistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.cadenaConexion, "MS");
			for (AsistenciasFinalizadasBean asistenciasFinalizadasBean : finalizadosRespuesta) {
				dao.insert(asistenciasFinalizadasBean);
			}
			 
		} catch (SQLException e) {
			Logger.getLogger(logPath).escribirLog("No se pudo recuperar los chats",e);
			return null;
		}		
		return listaAsistencias;
	}
}
