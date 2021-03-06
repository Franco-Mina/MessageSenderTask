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

import ar.edu.ubp.das.Servicios.Servicios;
import ar.edu.ubp.das.bean.AsistenciasFinalizadasBean;
import ar.edu.ubp.das.bean.ws.CerrarAsistenciaReqBean;
import ar.edu.ubp.das.bean.ws.CerrarAsistenciaRespBean;
import ar.edu.ubp.das.bean.ws.ListaFinalizadosRequestBean;
import ar.edu.ubp.das.bean.ws.ListaFinalizadosResponseBean;
import ar.edu.ubp.das.conections.ConnectionManager;
import ar.edu.ubp.das.conections.util.Conexion;
import ar.edu.ubp.das.credenciales.CredencialesBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logger.Logger;
import ar.edu.ubp.das.token.db.ConsoleTokenManger;

public class ChatManager {
	private CredencialesBean credenciales;
	
	public ChatManager(CredencialesBean credenciales) {
		this.credenciales= credenciales;
	}	
	
	public int CerrarChats() {
		Gson gson = new Gson();
		try {
			List<AsistenciasFinalizadasBean> listaAsistencias = ObtenerChatsCerrados();
	
			List<String> listaServicios = new Servicios(credenciales).ObtenerServicios();

			Map<String, List<AsistenciasFinalizadasBean>> asistenciaPorSevicio = 
					listaAsistencias.stream().collect(Collectors.groupingBy(AsistenciasFinalizadasBean::getIdServicio));
			
			ConnectionManager connectionManager = new ConnectionManager(credenciales.getPathConexiones(), 
					new ConsoleTokenManger(credenciales.getCadenaConexion(),credenciales.getUsuario(),credenciales.getPassword()),credenciales.getLogPath());
			
			for (String servicio : listaServicios) {
				Conexion conexion = connectionManager.getConexiones().stream().
						filter(x ->x.getDescripcion() != null && Objects.equals(x.getDescripcion(), "ChatCerrados"+servicio))
						.findFirst().orElse(null);
				
				if(conexion == null) {
					Logger.getLogger(this.credenciales.getLogPath()).escribirLog("No se encontro una conexion para pedir los chats finalizados a la entidad "
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
				
				String jsonRespuesta = null;
				
				try {
					jsonRespuesta = connectionManager.callApi(conexion.getNroConexion(), request);
				} catch (Exception e) {
					Logger.getLogger(credenciales.getLogPath()).escribirLog("Error al buscar los chats cerrados para " + servicio,e);
				}
				
				if(jsonRespuesta == null || jsonRespuesta.trim() == "") continue;
				
				ListaFinalizadosResponseBean finalizadosRespuesta = gson.fromJson(jsonRespuesta,ListaFinalizadosResponseBean.class);
				
				for (AsistenciasFinalizadasBean asistenciaFinalizada : finalizadosRespuesta.getListaAsistenciasFinalizadas()) {
					if(asistenciasServicio != null && !asistenciasServicio.isEmpty()) {
						AsistenciasFinalizadasBean asistenciaLocal = asistenciasServicio.stream()
								.filter(x->x.getIdAsistencia() == asistenciaFinalizada.getIdAsistencia()).findFirst().orElse(null);
								
								if(asistenciaLocal != null) {
									//validar cancelacion
									if(asistenciaLocal.getFechaCancelacion() != null && ((asistenciaFinalizada.getMotivoCancelacion() != null 
											&& !asistenciaFinalizada.getMotivoCancelacion().isEmpty() || asistenciaFinalizada.getFechaCancelacion() != null))) {
										//Si el servicio tiene marcada la cancelacion entonces se corrige 
										//CorregirCancelacion(asistenciaLocal);
									}
								}
					}					
					 
					marcarEnDb.add(asistenciaFinalizada);
				}
				
				if(!marcarEnDb.isEmpty())
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
			Logger.getLogger(credenciales.getLogPath()).escribirLog(e);
			return -1;
		}
		System.out.println("Fin cierre de chats");
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
				Logger.getLogger(this.credenciales.getLogPath()).escribirLog("No se encontro una conexion para enviar los chats finalizados a la entidad "
						+ servicio);
				return -1;
			}
			for (AsistenciasFinalizadasBean asistenciasFinalizadasBean : cancelacionesANotificar) {
				try {
					CerrarAsistenciaReqBean request = new CerrarAsistenciaReqBean();
					request.setIdSolicitud(asistenciasFinalizadasBean.getIdSolicitud());
					request.setMotivo(asistenciasFinalizadasBean.getMotivoCancelacion());
					
					String jsonRespuesta = null;
					
					try {
						jsonRespuesta = connectionManager.callApi(conexion.getNroConexion(), request);
					} catch (Exception e) {
						StringBuilder error = new StringBuilder("Error al enviar la cancelacion del chat " + asistenciasFinalizadasBean.getIdAsistencia());
						Logger.getLogger(credenciales.getLogPath()).escribirLog(error.toString());
					}
					
					CerrarAsistenciaRespBean respuesta = gson.fromJson(jsonRespuesta, CerrarAsistenciaRespBean.class);

					if(respuesta == null || respuesta.getEstado() != 1) {
						StringBuilder error = new StringBuilder("Error al enviar la cancelacion del chat " + asistenciasFinalizadasBean.getIdAsistencia());
						if(respuesta !=null && respuesta.getMensaje() != null) {
							error.append(" con error:" + respuesta.getMensaje()); 
						}
						Logger.getLogger(credenciales.getLogPath()).escribirLog(error.toString());
					}
					
					solicitudesMarcarNotificadas.add(asistenciasFinalizadasBean);
				}catch (Exception e) {
					Logger.getLogger(credenciales.getLogPath()).escribirLog("Error al enviar la cancelacion del chat " + 
							asistenciasFinalizadasBean.getIdAsistencia() + " con error:"+ e.getMessage());
				}				
			}	
			if(!solicitudesMarcarNotificadas.isEmpty()) {
				MarcarChatsCerrados(cancelacionesANotificar);
			}
			
				
		} catch (Exception e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog(e);
		}
		
		return 0;
	}
	
	
	public List<AsistenciasFinalizadasBean> ObtenerChatsCerrados(){
		List<AsistenciasFinalizadasBean> listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		
		try {
			Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean> dao = DaoFactory.getDao("Asistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			
			listaAsistencias = dao.select(null);
			
			if(listaAsistencias == null)
				listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		} catch (SQLException e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog("No se pudo recuperar los chats",e);
			return null;
		}		
		return listaAsistencias;
	}
	
	public List<AsistenciasFinalizadasBean> MarcarChatsCerrados(List<AsistenciasFinalizadasBean> finalizadosRespuesta){
		List<AsistenciasFinalizadasBean> listaAsistencias = new ArrayList<AsistenciasFinalizadasBean>();
		
		try {
			Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean> dao = DaoFactory.getDao("Asistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			for (AsistenciasFinalizadasBean asistenciasFinalizadasBean : finalizadosRespuesta) {
				dao.insert(asistenciasFinalizadasBean);
			}
			 
		} catch (SQLException e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog("No se pudo recuperar los chats",e);
			return null;
		}		
		return listaAsistencias;
	}
	
	public int CorregirCancelacion(AsistenciasFinalizadasBean asistenciaACorregir){
				
		try {
			Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean> dao = DaoFactory.getDao("Asistencia", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", this.credenciales.getCadenaConexion(), "MS");
			dao.update(asistenciaACorregir);	
			return 0;			 
		} catch (SQLException e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog("No se pudo recuperar los chats",e);
			return -1;
		}	
	}
}
