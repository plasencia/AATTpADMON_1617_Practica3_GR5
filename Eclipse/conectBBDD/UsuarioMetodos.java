package AATTAA.practica03.servidor.conectBBDD;

import java.util.List;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Clase que contiene los métodos necesarios para establecer coneixón con la base de datos,
 * definios mediante una interfaz.
 * 
 * @author plasencia
 *
 */
public class UsuarioMetodos implements UsuarioMetodosInterface {
	private JdbcTemplate jdbcTemplate;//Permite utizar JDB
	private DataSource dataSource;

	/**
	 * Inyección de dependneicas mediante el método setter
	 * 
	 * @param dataSource
	 */
	public void setDataSource(DataSource dataSource) {
	   this.dataSource = dataSource;
	   this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/**
	 * Metodo existe, comprueba si hay un usuario con el dni
	 * 
	 * @param dni de tipo String
	 * @return usuario de tipo UusarioDatos
	 */
	public UsuarioDatos existe (String dni){
		/* Variables */
		String sql; //Contiene la setencia SQL
		MapearUsuario mapper; //Mapeo para el acceso a la base de datos
		List<UsuarioDatos> usuario=null; //Quey devuelve una lista o null
		Object[] parametros = {dni}; //Necesaria para query
		
		/* Inicialización de las variables. */
		sql= "select * from usuario where contraseña = ? ";
		mapper = new MapearUsuario();
		
		/* Establecemos la conexion con la base de datos. */
		usuario = this.jdbcTemplate.query(sql, parametros, mapper);
		
		/* Comprobramos si existe o no */
		if (usuario.isEmpty()){//No existe
			return null;//Devolvemos un null.
		}else{//Existe
			return usuario.get(0);//Devolvemos los datos del usuario.
		}
		
	}

}
