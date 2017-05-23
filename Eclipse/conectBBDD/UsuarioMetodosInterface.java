package AATTAA.practica03.servidor.conectBBDD;

/**
 * Interfaz.
 * @author plasencia
 *
 */
public interface UsuarioMetodosInterface {
	/**
	 * Metodo existe, comprueba si hay un usuario con el dni
	 * 
	 * @param dni de tipo String
	 * @return usu de tipo UusarioDatos
	 */
	public UsuarioDatos existe (String dni);

}
