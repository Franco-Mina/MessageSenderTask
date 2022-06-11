package ar.edu.ubp.das.Servicios;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ar.edu.ubp.das.bean.ServicioBean;
import ar.edu.ubp.das.credenciales.CredencialesBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logger.Logger;

public class Servicios {
	private CredencialesBean credenciales;
	
	public Servicios(CredencialesBean credenciales) {
		this.credenciales = credenciales;
	}
	
	public List<String> ObtenerServicios(){
		List<ServicioBean> listaServicios = new ArrayList<ServicioBean>();
		
		try {
			Dao<ServicioBean,ServicioBean> dao = DaoFactory.getDao("Servicio", "ar.edu.ubp.das",
					"com.microsoft.sqlserver.jdbc.SQLServerDriver", credenciales.getCadenaConexion(), "MS");

			listaServicios = dao.select(null);
			
			if(listaServicios == null)
				listaServicios = new ArrayList<ServicioBean>();
			
		} catch (SQLException e) {
			Logger.getLogger(credenciales.getLogPath()).escribirLog("No se pudo recuperar los servicios",e);
			return null;
		}		
		return listaServicios.stream().filter(x -> x.getHabilitado().toLowerCase().contentEquals("s")).map(x->x.getId()).collect(Collectors.toList());
	}		
	
}
