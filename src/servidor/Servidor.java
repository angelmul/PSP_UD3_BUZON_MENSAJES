package servidor;

import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import modelo.Usuario;

import java.io.*;

/**
* <h1>Clase Servidor</h1>
* <p>Esta clase main es la encargada de gestionar de recibir las peticiones y enviarlas a 
* un hilo contestador..</p>
*
* @author  Angel Mulero
* @version 1.0
*/

public class Servidor {
	
	private static ServerSocket server;
	private static Map<String, Usuario> usuarios = new HashMap<>();
	private static final String REGUSERS = "[Registro Usuarios] Usuario: ";
	private static final String CLAVEADMIN = "SoyAdmin!";
	private static boolean apagadoAdmin = false;
	
    /** 
     * Metodo main encargado recibir las peticiones de los usuarios y crear un hilo Contestador para seguir respondiendo peticiones
     * hasta que este se apague remotamente o se cierre.
     * @param args Argumentos con  los que se lanza.
     * @throws IOException Salta si el socket servidor se cae o tiene problemas de conexión.
     */
	public static void main(String[] args) {
		//Monitor para encargarse de sincronizar las accesos de los hilos a la clase.
		Object monitor = new Object();
		int nRespuestas = 0;
		try {
			System.out.println("[Creando servidor...]");
			server = new ServerSocket();		
				
			System.out.println("[Dirección asignada]");
			InetSocketAddress addr = new InetSocketAddress("0.0.0.0", 5555);
			server.bind(addr);
						
			System.out.println("[Aceptando conexiones]");
				
			while(!apagadoAdmin) {
				nRespuestas++;
				Socket newSocket = server.accept();
				System.out.println("[Conexion recibida abriendo socket en hilo "+nRespuestas+"]");
				new Contestador(newSocket, monitor);
				}
				
			System.out.println("[Apagando el servidor...]");
			server.close();
							
		} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
    /** 
     * Metodo con el que se verifica el uso de la clave del admin.
     * @param mensaje Clave con la que se intenta acceder.
     * @return resultado Devuelve si la operación se ha realizado correctamente.
     */
		public static boolean operacionAdmin(String mensaje) {
			boolean resultado = false;
			if (mensaje.contentEquals(CLAVEADMIN)) {
				resultado = true;
				Servidor.apagadoAdmin = true;
			}
			return resultado;
		}
		
	    /** 
	     * Metodo para realizar una parada del servidor y de sus hilos.
	     * @throws IOException Salta si el socket servidor se cae o tiene problemas de conexión.
	     */
		public static void apagadoRemoto() throws IOException {
			System.out.println("[Apagando el servidor por petición de Admin...]");
			server.close();
			System.exit(0);
		}
		
	    /** 
	     * Metodo para comprobar si un usuario esta registrado en la lista y conectarlo y en el caso de que no registrarlo o
		 * de rechazarlo porque el usuario ya este conectado actualmente.
	     * @throws IOException Salta si el socket servidor se cae o tiene problemas de conexión.
	     * @param usuario nombre del usuario
	     * @return resultado Devuelve si la operación se ha realizado correctamente.
	     */
		public static boolean identificarUsuario(String usuario) {
			boolean resultado = false;
			if(!usuarios.containsKey(usuario)) {
				usuarios.put(usuario,new Usuario(usuario,true));
				resultado = true;
				System.out.println(REGUSERS+usuario+" registrado y conectado");
			} else if(!usuarios.get(usuario).getConectado()){
				usuarios.get(usuario).setConectado(true);
				resultado = true;
				System.out.println(REGUSERS+usuario+" conectado");
			} else { 
				System.out.println(REGUSERS+usuario+" ya esta conectado, rechazando conexión");
			}
			return resultado;
		}
		
	    /** 
	     * Metodo para buscar un usuario.
	     * @throws IOException Salta si el socket servidor se cae o tiene problemas de conexión.
	     * @param usuario nombre del usuario
	     * @return resultado Devuelve si la operación se ha realizado correctamente.
	     */
		public static boolean buscarUsuario(String usuario) {
			boolean resultado = false;
			if(usuarios.containsKey(usuario)) {
				resultado = true;
			}
			return resultado;
		}
		
	    /** 
	     * Metodo para dejar registrado un mensaje de un usuario registrado en otro usuario registrado o en el mismo.
	     * @param usuario nombre del usuario al que se le quiere dejar el mensaje.
	     * @param mensaje mensaje que se le quiere dejar.
	     */
		public static void registrarMensaje(String usuario, String mensaje) {
				usuarios.get(usuario).addMensaje(mensaje);
		}
		
	    /** 
	     * Metodo para comprobar si hay mensajes en la lista de mensajes de un usario.
	     * @param usuario nombre del usuario
	     * @return boolean devuelve si el usuario tiene mensajes nuevos o no.
	     */
		public static boolean estadoBandeja (String usuario) {
			return usuarios.get(usuario).estadoMensajes();
		}
		
	    /** 
	     * Metodo para devolver al hilo contestador la lista de mensajes del usuario que lo solicita y que tenga almenos un mensaje.
	     * @param usuario nombre del usuario
	     * @return usuarios.get(usuario).getMensajes() devuelve la lista de mensajes del usuario
	     */
		public static List<String> obtenerMensajesUsuario(String usuario){
			return usuarios.get(usuario).getMensajes();
		}
		
	    /** 
	     * Metodo para resetear la lista de mensajes de un usuario despues de haber leido sus mensajes recibidos.
	     * @param usuario nombre del usuario	     
	     */
		public static void borrarMensajes (String usuario) {
			usuarios.get(usuario).setMensajes();
		}
		
	    /** 
	     * Metodo para desconectar a un usuario de la lista.
	     * @param usuario nombre del usuario
	     * @return resultado devuelve el resultado de la operacion (correcto o no)
	     */
		public static boolean desconectarUsuario(String usuario) {
			boolean resultado = false;
			if(usuarios.containsKey(usuario)) {
				usuarios.get(usuario).setConectado(false);
				resultado = true;
				System.out.println(REGUSERS+usuario+" desconectado");
			}
			return resultado;
		}

	}
