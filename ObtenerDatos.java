import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.*;


/**
 * La clase ObtenerDatos implementa cuatro métodos públicos que permiten obtener
 * determinados datos de los certificados de tarjetas DNIe, Izenpe y Ona.
 *
 * @author tbc
 */
public class ObtenerDatos {
    private static final String direccion = "Source/datos.txt";
    private static final byte[] dnie_v_1_0_Atr = {
        (byte) 0x3B, (byte) 0x7F, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6A, (byte) 0x44,
        (byte) 0x4E, (byte) 0x49, (byte) 0x65, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0x90, (byte) 0x00};
    private static final byte[] dnie_v_1_0_Mask = {
        (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFF};

    public ObtenerDatos() {
    }

    public Usuario LeerNIF() throws FileNotFoundException, IOException {

        Usuario user = null;
        byte[] datos=null;
        try {
            Card c = ConexionTarjeta();
            if (c == null) {
                throw new Exception("ACCESO DNIe: No se ha encontrado ninguna tarjeta");
            }
            byte[] atr = c.getATR().getBytes();
            CardChannel ch = c.getBasicChannel();

            if (esDNIe(atr)) {
                datos = leerCertificado(ch);
                if(datos!=null)
                    user = leerDatosUsuario(datos);
            }
            c.disconnect(false);

        } catch (Exception ex) {
            Logger.getLogger(ObtenerDatos.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        /* Creación de nuestro fichero*/
 /* Posteriormente almacenaremos nuestro información en un fichero txt llamado datos.txt*/ 
        FileWriter fichero = null;
        PrintWriter pw = null;
        

        try {
            fichero = new FileWriter(direccion);
                // Creamos nuestra solicitud para almacenar la información en el fichero.       
            pw = new PrintWriter(fichero);
            /*Copiamos los datos a nuestro fichero txt */
            pw.println("Informacion de nuestro dni:");
            byte[] b = new byte[1];
            for(int i=0;i<datos.length;i++)
            {
                b[0]=datos[i];
                pw.println(i+"\t"+String.format(" %2X", datos[i])+"\t"+datos[i]+"\t"+new String(b));
            }
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        } 
        /*Cerramo nuestro Buffer de escritura */ 
        finally {
            
            try {
                if (pw != null) {
                    pw.flush();
                    pw.close();
                    fichero.close();
                }       
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        // Nos devuelve el usuario.
        return user;
    }

    /**
     * @param ch
     * @return
     * @throws CardException
     * @throws CertificateException 
     */
    public byte[] leerCertificado(CardChannel ch) throws CardException, CertificateException, IOException {
        
        int offset = 0;
        String completName = null;

        //[1] PRÁCTICA 3. Punto 1.a*/
        
  //  Comando -> Select
  //  Función -> Selecciona un fichero directo dedicado (DF) por el nombre.
  //  Estructura que sigue -> 0x00 0xa4 0x04 0x00 0x0b 0x4D 0x61 0x73 0x74 .... 0x65
  //   1º 0x00 -> "CLA"
  //   2º 0x04 -> "INS"
  //   3º 0x04 -> "P1"
  //   4º 0x00 -> "P2"
  //   5º 0x0b -> "LC" o longitud de los datos.
  //   Demas octetos -> "Datos de acuerdo a P1-P2"
         
        byte[] command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
        ResponseAPDU r = ch.transmit(new CommandAPDU(command));
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        }

        //[2] PRÁCTICA 3. Punto 1.a
        
   //  Comando -> Select
   //  Función -> Selecciona un fichero directo dedicado (DF) por el nombre.
   //  Estructura -> 0x00 0xA4 0x00 0x00 0x02 0x50 0x15
   //   - 1º 0x00 -> "CLA"
   //   - 2º 0x04 -> "INS"
   //  - 3º  0x00 -> "P1"
   //   - 4º 0x00 -> "P2"
   //   - 5º 0x02 -> "LC" o longitud de los datos.
   //   - Resto de cotectos: "Datos de aceurdo a P1-P2"
   //  @see Apartado 4.8. Comando Select del Manual de Comandos para Desarrolladores 102
   //  @return Null
         
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
        r = ch.transmit(new CommandAPDU(command));

        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        }

        //[3] PRÁCTICA 3. Punto 1.a
    //  Comando -> Select
   //  Función -> Selecciona un fichero directo dedicado (DF) por el nombre.
   //  Estructura -> 0x00 0xA4 0x00 0x00 0x02 0x50 0x15
   //   - 1º 0x00 -> "CLA"
   //   - 2º 0x04 -> "INS"
   //  - 3º  0x00 -> "P1"
   //   - 4º 0x00 -> "P2"
   //   - 5º 0x02 -> "LC" o longitud de los datos.
   //   - Resto de cotectos: "Datos de aceurdo a P1-P2"
   //  @see Apartado 4.8. Comando Select del Manual de Comandos para Desarrolladores 102
   //  @return Null
         
        command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
        r = ch.transmit(new CommandAPDU(command));

        byte[] responseData = null;
        if ((byte) r.getSW() != (byte) 0x9000) {
            System.out.println("ACCESO DNIe: SW incorrecto");
            return null;
        } else {
            responseData = r.getData();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] r2 = null;
        int bloque = 0;

        do {
  //[4] PRÁCTICA 3. Punto 1.b
            
  // Comando -> Read Binary
  //Función: Obtener el contenido (o parte) de un fichero elemental transparente en el mensaje de respuesta al este comando.
  // Estructura: 
  //   1º          octecto: 0x0X "CLA"
  //   2º          octecto: 0xB0 "INS"
  //   3º y 4º     octecto: 0xXX "P1-P2"
  //   5º          octecto: 0xXX "LC" -> Vacío.
  //   6º hasta Nº octecto     -> Vacío.
  //   (N+1)º      octecto: 0xXX "LE"
  // @see Apartado 4.9. Comando Read  Binary del Manual de Comandos para Desarrolladores 102
  // @return Null           
                        
            final byte CLA = (byte) 0x00;//El valor aceptado puede ser desde 0x00 hasta 0x0F
            final byte INS = (byte) 0xB0;//El valor para especificar el comando es 0xB0
            final byte LE = (byte) 0xFF;// Número de bytes a leer, si este esta a 0, el nº de bytes a leer es 256.

            //[4] PRÁCTICA 3. Punto 1.b
            // Vamos a construi con el comando " READ BINARY", el 1º byte a leear del principio del fichero.
             //P1 y P2, nos indica el byte del fichero, por el cual empezamos a leer.
            command = new byte[]{CLA, INS, (byte) bloque/*P1*/, (byte) 0x00/*P2*/, LE};
           
            r = ch.transmit(new CommandAPDU(command));
            //System.out.println("ACCESO DNIe: Response SW1=" + String.format("%X", r.getSW1()) + " SW2=" + String.format("%X", r.getSW2()));
            
            if ((byte) r.getSW() == (byte) 0x9000) {
                r2 = r.getData();
                
                baos.write(r2, 0, r2.length);

                for (int i = 0; i < r2.length; i++) {
                    byte[] t = new byte[1];
                    t[0] = r2[i];
                    String eco = i + (0xff * bloque) + String.format(" %2X", r2[i]) + " " + String.format(" %d", r2[i])+" "+new String(t);
                    System.out.println(eco);
                }
               bloque++;
            } else {
                return null;
            }

        } while (r2.length >= 0xfe);
        
        return baos.toByteArray();
    }

    
    
    // Método que nos indica la conexión con la tarjeta.
    /**
     * Este método establece la conexión con la tarjeta. La función busca el
     * Terminal que contenga una tarjeta, independientemente del tipo de tarjeta
     * que sea.
     *
     * @return objeto Card con conexión establecida
     * @throws Exception
     */
    private Card ConexionTarjeta() throws Exception {

        Card card = null;
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        //System.out.println("Terminals: " + terminals);

        for (int i = 0; i < terminals.size(); i++) {

            // get terminal
            CardTerminal terminal = terminals.get(i);

            try {
                if (terminal.isCardPresent()) {
                    card = terminal.connect("*"); //T=0, T=1 or T=CL(not needed)
                }
            } catch (Exception e) {
                
                System.out.println("Exception catched: " + e.getMessage());
                card = null;
            }
        }
        return card;
    }

    /**
     * Este método nos permite saber el tipo de tarjeta que estamos leyendo del
     * Terminal, a partir del ATR de ésta.
     *
     * @param atrCard ATR de la tarjeta que estamos leyendo
     * @return tipo de la tarjeta. 1 si es DNIe, 2 si es Starcos y 0 para los
     * demás tipos
     */
    private boolean esDNIe(byte[] atrCard) {
        int j = 0;
        boolean found = false;

        //Es una tarjeta DNIe?
        if (atrCard.length == dnie_v_1_0_Atr.length) {
            found = true;
            while (j < dnie_v_1_0_Atr.length && found) {
                if ((atrCard[j] & dnie_v_1_0_Mask[j]) != (dnie_v_1_0_Atr[j] & dnie_v_1_0_Mask[j])) {
                    found = false; //No es una tarjeta DNIe
                }
                j++;
            }
        }
        if (found == true) {
            return true;
        } else {
            return false;
        }

    }

    private Usuario leerDatosUsuario(byte[] informacion) {
 //  Vamos analizar los datos del dni para obtner nuestro -> Nombre, Apellidos y NIF. 
 //  Variables que vamos a usar:
 
 
//   usu -> Nos almacena el Nombre, Apellido y el DNI leido.
       Usuario usu = new Usuario(); 
     
       String dni=""; 
       //Array que nos devolvera nombre y apellidos.
       String[] user = new String[3]; 
       user[0]=" ";
       user[1]=" ";
       user[2]=" ";
         // Indica el inicio del set, el número en hexadeciaml es 31.
       int set=49; 
       // dni en decimal
       int [] oiddni={85,4,5};
       // Los apellidos y el nombre en decimal.
       int [] oidapn={85,4,3};
        int i=0,j=0; 
       // Variable n almacena bytes de tipo array y nos lo pasa a tipo entero
       byte[] n = new byte[1]; 
       int posicion=0;
      
       
       while(j<informacion.length){
            if(informacion[j]==set){
                // Buscamos OID del DNI.
                
                if(informacion[j+6]==oiddni[0] && informacion[j+7]==oiddni[1] && informacion[j+8]==oiddni[2]){
                    posicion=(j+11);
                    
                    // Localizamos DNI
                    
                     for(j=posicion ; j<posicion+9;j++){
                         // Extramos los bytes
                        n[0] = informacion[j]; 
                        
                       //Lo pasamos a string y realizamos la concatenació 
                        dni = dni + new String(n);
                    }
                    j=informacion.length; 
                }
            }
            j++;
}
                    
                    
                    // Ahora vamos a buscas y extraer los apellidos y el nombre de nuestro dni 
                   
       j=0;
       i=0;
       while(j<informacion.length){
           // Indica (32) un espacio 
          if(informacion[j]==set){
               
           if(informacion[j+6]==oidapn[0] && informacion[j+7]==oidapn[1] && informacion[j+8]==oidapn[2]){
        //Encontramos el OId referente a nuestro usuario del dni (Nombre, Apellidos, DNI...)
        // Iniciamos la busqueda del primer apellido.            
        j=(j+11);
                    while(informacion[j]!=40)
  //Buscaremos dentro hasta que localizamos el caracter '('
                        
             {
      if(informacion[j]!=32 && informacion[j]!=44){
       //Extracion de bytes.
            n[0] = informacion[j]; 
            
            //Convertimos la cadena a string y lo concatenamos
             user[i] = user[i]+ new String(n);
                   }
    if(informacion[j]==32 && i==0){
            //Hemos encontrado el primero Apellido .
                            i++;
                                
                        }
                        if(informacion[j]==44 && i!=0){
                            //Hemos encontrado el segundo Apellido
                            i++;
                        }
                        j++;
                        
                    }
                    j=informacion.length;
                }
            }
            j++;
        }
            
       //Almacenamos los datos del usuario
       usu.setApellido1(user[0]);
       usu.setApellido2(user[1]);
       usu.setNombre(user[2]);
       usu.setNif(dni);
       
       // mostramos los datos por pantalla del Usuario del DNI
       return usu;
    }
}