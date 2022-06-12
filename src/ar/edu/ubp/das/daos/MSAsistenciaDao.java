package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.bean.AsistenciasFinalizadasBean;
import ar.edu.ubp.das.db.Dao;

public class MSAsistenciaDao extends Dao<AsistenciasFinalizadasBean, AsistenciasFinalizadasBean>{
	
	public MSAsistenciaDao(String provider, String connectionString) {
		super(provider,connectionString);
	}
	
	@Override
	public AsistenciasFinalizadasBean delete(AsistenciasFinalizadasBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsistenciasFinalizadasBean insert(AsistenciasFinalizadasBean arg0) throws SQLException {
		this.connect();
		this.setProcedure("dbo.MARCAR_CHAT_CERRADO(?,?,?,?)");
		this.setParameter(1, arg0.getIdAsistencia());
		this.setParameter(2, arg0.getIdServicio());
		this.setParameter(3, arg0.getFechaFinalizada());
		this.setParameter(4, arg0.getMotivoCancelacion());
		
		this.executeUpdate();
		
		return arg0;
	}

	@Override
	public AsistenciasFinalizadasBean make(ResultSet arg0) throws SQLException {
		AsistenciasFinalizadasBean bean = new AsistenciasFinalizadasBean();
		
		bean.setIdAsistencia(arg0.getInt("Id"));
		bean.setIdServicio(arg0.getString("Id_Servicio"));
		bean.setIdSolicitud(arg0.getInt("id_solicitud"));
		bean.setFechaFinalizada(arg0.getTimestamp("Fecha_Cierre"));
		bean.setFechaCancelacion(arg0.getTimestamp("Fecha_Cancelacion"));
		bean.setMotivoCancelacion(arg0.getString("Motivo_Cancelacion"));	
		bean.setEstado(arg0.getString("Estado"));
		
		return bean;
	}

	@Override
	public List<AsistenciasFinalizadasBean> select(AsistenciasFinalizadasBean arg0) throws SQLException {
		this.connect();
		this.setProcedure("dbo.OBTENER_CHATS_FINALIZADOS");
		
		return this.executeQuery();
	}

	@Override
	public AsistenciasFinalizadasBean update(AsistenciasFinalizadasBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean valid(AsistenciasFinalizadasBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
