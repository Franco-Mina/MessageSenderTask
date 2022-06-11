package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.bean.ServicioBean;
import ar.edu.ubp.das.db.Dao;

public class MSServicioDao extends Dao<ServicioBean,ServicioBean> {

	public MSServicioDao(String provider, String connectionString) {
		super(provider,connectionString);
	}
	
	@Override
	public ServicioBean make(ResultSet result) throws SQLException {
		ServicioBean bean = new ServicioBean();
		
		bean.setId(result.getString("id"));
		bean.setNombre(result.getString("nombre"));
		bean.setHabilitado(result.getString("habilitado"));
		bean.setDireccion(result.getString("direccion"));
		bean.setFecha_creacion(result.getTimestamp("fecha_creacion"));
		bean.setHorarioAtencion(result.getString("horario_atencion"));
		bean.setTelefono(result.getLong("telefono"));
		
		return bean;
	}

	@Override
	public ServicioBean insert(ServicioBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServicioBean update(ServicioBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServicioBean delete(ServicioBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ServicioBean> select(ServicioBean bean) throws SQLException {
		this.connect();
		this.setProcedure("dbo.OBTENER_SERVICIOS()");
		
		return this.executeQuery();
	}

	@Override
	public boolean valid(ServicioBean bean) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
