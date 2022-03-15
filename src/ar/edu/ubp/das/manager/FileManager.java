package ar.edu.ubp.das.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import ar.edu.ubp.das.bean.DetalleAsistenciaBean;
import ar.edu.ubp.das.logger.Logger;

public class FileManager {

	private String _logPath = "";
	
	public FileManager(String logPath) {
		_logPath =logPath;
	}
	
	public List<DetalleAsistenciaBean> RecuperarMensajesNoGuardados(){
		Gson gson = new Gson();
		String path = _logPath + "Mensajes";
		File directorio = new File(path);
		File archivos[] = directorio.listFiles();
		List<DetalleAsistenciaBean> listaMensajes = new ArrayList<DetalleAsistenciaBean>();		
		
		for (File file : archivos) {
			try {
				//Deserializamos los mensajes
				Files.lines(file.toPath()).
					forEachOrdered(f -> listaMensajes.add(gson.fromJson(f, DetalleAsistenciaBean.class)));
			} catch (IOException e) {
				Logger.getLogger(_logPath).escribirLog("Error recuperando mensajes", e);
				continue;
			}
		}
		return listaMensajes;
	}
}
