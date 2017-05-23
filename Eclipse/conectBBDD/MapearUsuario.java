package AATTAA.practica03.servidor.conectBBDD;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class MapearUsuario implements RowMapper<UsuarioDatos>{
	/**
	 * Metodo para mappear la base de datos.
	 * 
	 * @param rs de tipo ResultSet
	 * @param rowNum de tipo integger
	 * @exception SQLException, fallo en la conexión con la BBDD
	 * @return usuario de tipo UsuarioDatos.
	 */
	public UsuarioDatos mapRow(ResultSet rs, int rowNum){
		//Variables necesarios para el mapeo
		UsuarioDatos usuario;
		
		//Iniciamos la variable
		usuario= new UsuarioDatos();
		
		//Mapeo con la BBDD
		try {
			usuario.setNombre(rs.getString("nombre"));
			usuario.setDni(rs.getString("contraseña"));
		} catch (SQLException e) {
			
			e.printStackTrace();
			usuario=null;
		}

		//Devovlemos el resultado
		return usuario;
}
}
