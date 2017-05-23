package AATTAA.practica03.servidor.conectBBDD;

public class UsuarioDatos {
	//Atributos
	 private String nombre;
	 private String dni;
	 
	 //Constructor por defecto
	 public UsuarioDatos (){
		 this.nombre = "test";
		 this.dni = "test";
	 }
	 
	 //Constructor por parámetro
	 public UsuarioDatos (String nombre, String dni){
		 this.nombre = nombre;
		 this.dni = dni;		 
	 }
 
	 //Metodos de acceso
		public String getNombre() {
			return nombre;
		}

		public void setNombre(String nombre) {
			this.nombre = nombre;
		}

		public String getDni() {
			return dni;
		}

		public void setDni(String dni) {
			this.dni = dni;
		}	 
	    
}
