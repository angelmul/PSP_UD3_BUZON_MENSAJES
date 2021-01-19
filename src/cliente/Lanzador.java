package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
* <h1>Clase Cliente del buzón de mensajes</h1>
* <p>Esta clase main es la encargada de gestionar las peticiones
* del cliente y comunicarse con el servidor para recibir
* las respuestas o mensajes del servidor.</p>
*
* @author  Angel Mulero
* @version 1.0
*/
//Clase main del cliente
public class Lanzador {

	private static Scanner teclado = new Scanner(System.in);
	private static DataInputStream entrada;
	private static DataOutputStream salida;
	private static boolean conexion = false;
	
    /** 
     * Metodo main encargado realizar la conexión y la interacción con el servidor.
     * @param args Argumentos con  los que se lanza.
     * @throws Exception Lanza una excepcion en caso de perdia de conexión o cierre forzada con el servidor.
     */
	public static void main(String[] args) throws Exception {		
		conexion = true;
		int opc = 0;
		try {
			//Establece una conexion con el servidor.
			System.out.println("[Iniciando cliente...]");
			Socket clienteSocket = new Socket();
			InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
			clienteSocket.connect(addr);
			
			//Establece un canal de comunicacion.
			entrada = new DataInputStream(clienteSocket.getInputStream());
			salida = new DataOutputStream(clienteSocket.getOutputStream());
			
			//Identificacion del usuario.
			identificarse();
			
			//Interactua mientras la conexion este activa.
			while(conexion) {
				opc = 0;
				switch(entrada.readUTF()) {
				case "-MuestraOpciones":
					while(opc != 1 && opc != 2 && opc != 3) {
						System.out.println("---OPCIONES---");
			    		System.out.println("1. Revisar mensajes nuevos");
						System.out.println("2. Enviar mensaje");
						System.out.println("3. Desconectarse");
						System.out.println("Selecione [1/3]:");
						
						opc = seleccionarOpc();
					}
					salida.writeInt(opc);
					if (opc == 3) {
						conexion = false;
					}
					break;
				
				case "-RevisarMensajes":
					leerMensajes();
					break;
				
				case "-EnviarMensaje":
					enviarMensaje();
					break;
					
				default:
					System.out.println("[OPCIÓN NO RECONOCIDA]");
				}
			}
			
			clienteSocket.close();
		} catch (IOException e) { //Excepcion en caso de que se pierda la conexion con el servidor o de que nunca la haya.
			System.out.println("[ERROR DE CONEXIÓN CON EL SERVIDOR] Vuelva a intentar conectarse en unos instantes o cuando sepa que el servidor esta fucionando.");
		}
		teclado.close();
	}
	
    /** 
     * Metodo para devolver la opcion, asegurando que se introduzca un numero (int).
     * @return Devuelve un numero int.
     */
	private static int seleccionarOpc() {
		int opc = 0;
		try {
			opc = teclado.nextInt();
		} catch (InputMismatchException  e) {
			opc = 0;
			teclado = new Scanner(System.in);
		}		
		return opc;
	}

    /** 
     * Metodo para identificar el usuario buscandolo en la lista del Servidor
	 * en caso de que el usuario ya se encuentre conectado rechazara la conexion.
     * @throws Exception Excepcion por identificarse a un usuario ya conectado y registrado.
     * @throws Exception Excepcion por superar el numero de intentos para escribir un usuario adecuado.
     */
	private static void identificarse() throws Exception {
		String usuario = "";
		int intentos = 0;
		
		while(usuario.contentEquals("") && intentos < 4) {
			System.out.println("[Inicie sesión]");
			System.out.println("Usuario: ");
			usuario = teclado.next();
			intentos++;
		}
		
		if (intentos < 4) {
			salida.writeUTF(usuario);
			if (entrada.readBoolean() == true) {
				System.out.println(entrada.readUTF());
			} else { //Excepcion el cliente identificarse con un usuario ya conectado al sistema.
				throw new Exception("[ERROR DE IDENTIFICACIÓN] El usuario con el que intentas identificarte ya se encuentra conectado, porfavor inice la aplicación con otro usuario");
			}
		} else { //Excepcion el cliente a superado el limite de intentos fallidos al intentar establecerse un usuario.
			throw new Exception("[ERROR DE IDENTIFICACIÓN] Has superado el limite de intentos para identifcarte como usuario");
		}
	}
	
    /** 
     * Metodo que recoge la lista con todos los mensajes nuevos del cliente identificado con un usuario.
     * @throws IOException En caso de error en la comicación con el servidor salta.
     */
	private static void leerMensajes() throws IOException {
		int nMensajes = 0;
		
		System.out.println(entrada.readUTF());
		
		if(entrada.readBoolean() == true) {
			nMensajes = entrada.readInt();
			
			for(int i=0; i < nMensajes; i++) {
				System.out.println(entrada.readUTF());
			}
		}

	}
	
    /** 
     * Metodo para enviar un mensaje a un usuario registrado, no tiene que estar conectado obligatoriamente.
     * @throws IOException En caso de error en la comicación con el servidor salta.
     */
	private static void enviarMensaje() throws IOException {
		boolean terminadeProcesar = false;
		String mensaje = "";

		System.out.println(entrada.readUTF());
		mensaje = "";

		while(mensaje.contentEquals("")) {
			System.out.println("Usuario:");
			mensaje = teclado.next();
		}
		salida.writeUTF(mensaje);
		
		if(entrada.readBoolean() == true) {
			System.out.println(entrada.readUTF());
			mensaje = "";
			while(mensaje.contentEquals("")) {
				System.out.println("Mensaje:");
				mensaje = teclado.nextLine();
			}
			salida.writeUTF(mensaje);
			System.out.println(entrada.readUTF());
		} else {
			mensaje = entrada.readUTF();
			System.out.println(mensaje);
			
			if(mensaje.contains("[Petición aceptada]")) {
				conexion = false;
			}
		}
		
		//Espera a que el hilo del servidor termine de procesar la peticion del mensaje, para sincronizar el envio de mensajes.
		while(!terminadeProcesar) {
			terminadeProcesar = entrada.readBoolean();
		}
	}
}
