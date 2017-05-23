package AATTAA.practica03.servidor;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import AATTAA.practica03.servidor.conectBBDD.UsuarioDatos;
import AATTAA.practica03.servidor.conectBBDD.UsuarioMetodosInterface;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Autowired
	//Busca en servlet-cont, un bean que implemente un DaoUsuario_Interface,
	//para poder acceder a la base de datos.
	UsuarioMetodosInterface dao; //Dependenica para establecer una conexion con la BBDD me
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Peticion Get para obtener el formulario de autenticacion, mediante HTTP sin uso de DNI
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {

		return "home";
	}
	
	@RequestMapping(value = "/loggin", method = RequestMethod.POST)
	public String autentica(HttpServletRequest req,Model model){
		
		
		// Variables
		String dni; //Contraseña
		String nombre; //Nombre
		UsuarioDatos usuario;
		String respuesta = null; //Nombre del jsp
		
		// Extremos los datos de la petición
		nombre = req.getParameter("usu");
		dni =req.getParameter("pwd");
		
		// Procesmos los datos
		if(nombre==null || dni==null){
			//Parametros de la petición vacios
			model.addAttribute("resultado","300 no ok"); //Enviamos la respuesta al jsp.
			respuesta= "exito";
		}else{
			//Conectamos con la base de datos.
			usuario = dao.existe(dni);
			
			//Trabajamos las respuestas
			if(usuario==null){
				//No existe el usuario
				model.addAttribute("resultado","400 no ok"); //Enviamos la respuesta al jsp.
				respuesta= "exito";
			}else{
				//Comprobamos el nombre
				if(usuario.getNombre().equals(nombre)){
					//Exito
					model.addAttribute("resultado","200 ok"); //Enviamos la respuesta al jsp.
					respuesta= "exito";
				}else{
					//Error en el nombre
					model.addAttribute("resultado","400 no ok"); //Enviamos la respuesta al jsp.
					respuesta= "exito";
				}
			}
		}

		return respuesta;
	}
	
	
}
