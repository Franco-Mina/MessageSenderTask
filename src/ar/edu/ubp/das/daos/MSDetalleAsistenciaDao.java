package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.db.Dao;

public class MSDetalleAsistenciaDao extends Dao<DetalleAsistenciaBean, DetalleAsistenciaBean> {
	
	public MSDetalleAsistenciaDao(String provider, String connectionString) {
		super(provider,connectionString);
	}
	@Override
	public DetalleAsistenciaBean delete(DetalleAsistenciaBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DetalleAsistenciaBean insert(DetalleAsistenciaBean bean) throws SQLException {
		this.connect();
		this.setProcedure("dbo.INSERTAR_MENSAJE(?,?,?,?,?,?,?)");
		this.setParameter(1, bean.getIdAsistencia());
		this.setParameter(2, bean.getTipoDato());
		this.setParameter(3, bean.getDato());
		this.setParameter(4, bean.getFechaCreacion());	
		this.setParameter(5, bean.getIdServicio());
		this.setParameter(6, bean.getCreadoPor());
		boolean finalizado = bean.isAsistenciaFinalizada() != null && bean.isAsistenciaFinalizada().booleanValue();
		this.setParameter(7, finalizado);
		
		this.executeUpdate();
		
		return bean;
		
	}

	@Override
	public DetalleAsistenciaBean make(ResultSet arg0) throws SQLException {
		DetalleAsistenciaBean bean = new DetalleAsistenciaBean();
		
		bean.setId(arg0.getInt("Id"));
		bean.setIdAsistencia(arg0.getString("Id_Solicitud"));
		bean.setTipoDato(arg0.getString("Tipo_Dato"));
		bean.setFechaCreacion(arg0.getTimestamp("Fecha_Creacion"));
		bean.setFechaEnvio(arg0.getTimestamp("Fecha_Envio"));
		bean.setDato(arg0.getString("Dato"));
		bean.setIdServicio(arg0.getString("Id_Servicio"));
		
		return bean;
	}

	@Override
	public List<DetalleAsistenciaBean> select(DetalleAsistenciaBean arg0) throws SQLException {
		this.connect();
		this.setProcedure("dbo.GetDetalleAsistencia()");
		
		return this.executeQuery();
	}

	@Override
	public DetalleAsistenciaBean update(DetalleAsistenciaBean arg0) throws SQLException {
		this.connect();
		this.setProcedure("dbo.MARCAR_MENSAJE_ENVIADO(?)");
		this.setParameter(1, arg0.getId());
		this.executeUpdate();
		return arg0;
	}

	@Override
	public boolean valid(DetalleAsistenciaBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
