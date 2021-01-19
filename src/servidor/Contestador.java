package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
* <h1>Clase Contestador usada por el Servidor</h1>
* <p>Esta clase es usada por el Servidor
* como hilo para responder peticiones de clientes del buzón de mensajes.</p>
*
* @author  Angel Mulero
* @version 1.0
*/

public class Contestador implements Runnable {

	private Thread t;
	private Socket newSocket;
	private Object monitor;
	private DataInputStream entrada;
	private DataOutputStream salida;
	private String usuario;
	private boolean desconectarCliente = false;

    /** 
     * Metodo constructor encargada de iniciar el hilo, con los atributos pasados por parametros.
     * @param servidor recibe el socket del servidor con la conexión del cliente que ha realizado la petición de conexión
     * @param monitorServer recibe el monitor para sincronizar los metodos que acceden a la clase servidor.
     */
	public Contestador(Socket servidor, Object monitorServer) {
		t = new Thread(this, "Contestador");
		t.start();
		newSocket = servidor;
		monitor = monitorServer;
	}

    /** 
     * Metodo que se inicia con la construcción del hilo, encargado de establecer el canal de comunicación.
     * y llamar a los metodos de interacción.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	@Override
	public void run() {
		try {
			entrada = new DataInputStream(newSocket.getInputStream());
			salida = new DataOutputStream(newSocket.getOutputStream());

			identificar();
			
			if (!desconectarCliente) {
				interactuar();
			}
			
			desconectar();
			newSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    /** 
     * Metodo para verificar el usuario del cliente y devolver si es valido.
     * Devuelve al cliente si el usuario es correcto o incorrecto.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	private void identificar() throws IOException {
		String usuarioRecibido = entrada.readUTF();
		synchronized (monitor) {
			if (!Servidor.identificarUsuario(usuarioRecibido)) {
				salida.writeBoolean(false);
				desconectarCliente = true;
			} else {
				usuario = usuarioRecibido;
				salida.writeBoolean(true);
				salida.writeUTF("Bienvenido " + usuario);
			}
		}
	}

    /** 
     * Metodo para interactuar con las opciones recogidas en el cliente.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	private void interactuar() throws IOException {
		 desconectarCliente = false;
		while (!desconectarCliente) {
			salida.writeUTF("-MuestraOpciones"); //Ordena al cliente imprimir todas las opciones del menu.
			switch (entrada.readInt()) { //Recoge la opcion devuelta por el cliente.
			case 1:
				leerMensajes();
				break;
			case 2:
				escribirMensaje();
				break;
			case 3:
				desconectarCliente = true;
				break;
			default:
				System.out.println("[OPCIÓN RECIBIDA NO RECONOCIDA]");
			}
		}
	}

    /** 
     * Metodo para interactuar con las opciones recogidas en el cliente.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	private void leerMensajes() throws IOException {
		ArrayList<String> mensajesNoLeidos;
		salida.writeUTF("-RevisarMensajes");
		synchronized (monitor) {
			if(Servidor.estadoBandeja(usuario)) {
				salida.writeUTF("No tienes ningun mensaje nuevo!");
				salida.writeBoolean(false);
			} else {
				mensajesNoLeidos = (ArrayList<String>) Servidor.obtenerMensajesUsuario(usuario);
				Servidor.borrarMensajes(usuario);
				salida.writeUTF("Tienes "+mensajesNoLeidos.size()+" mensajes nuevos");
				salida.writeBoolean(true);
				salida.writeInt(mensajesNoLeidos.size());
				
				for(int i=0; i < mensajesNoLeidos.size(); i++) {
					salida.writeUTF("-------------------------\n"+mensajesNoLeidos.get(i)+"\n-------------------------");
				}
			}
		}
	}

    /** 
     * Metodo para registrar en la lista del servidor
     * el mensaje que quiere dejar el cliente para un usuario registrado.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	private void escribirMensaje() throws IOException {
		String usuarioDestinatario = "";
		String mensaje = "";

		salida.writeUTF("-EnviarMensaje");
		salida.writeUTF(
				"Escribe el usuario del destinatario de este mensaje, que este registrado");
		usuarioDestinatario = entrada.readUTF();
		synchronized (monitor) {
			if(Servidor.operacionAdmin(usuarioDestinatario)) {
				desconectarCliente = true;
				salida.writeBoolean(false);
				salida.writeUTF("[Petición aceptada] Apagando el servidor remoto...");
			} else if (Servidor.buscarUsuario(usuarioDestinatario)) {
				salida.writeBoolean(true);
				salida.writeUTF("Escribe un mensaje");
				mensaje = entrada.readUTF();
				mensaje = "[Mensaje de "+usuario+"] "+"\nCuerpo del mensaje: "+mensaje;
				Servidor.registrarMensaje(usuarioDestinatario, mensaje);
				salida.writeUTF("-------------------------\nSe ha enviado tu mensaje... \n" + mensaje + " a \n[Enviado a " + usuarioDestinatario+"]\n-------------------------");
			} else {
				salida.writeBoolean(false);
				salida.writeUTF("El usuario " + usuarioDestinatario + " no existe, introduzca un usuario registrado");
			}
		}
		salida.writeBoolean(true);
		
		//En caso de que el cliente envie la clave para hacer peticiones de admin, apagara su hilo y el servidor entero.
		if(desconectarCliente) synchronized (monitor) {
			Servidor.apagadoRemoto();
		}
	}

    /** 
     * Metodo para desconectar el cliente de su hilo y terminar este.
     * @throws IOException En caso de error en la comicación con el cliente salta.
     */
	private void desconectar() throws IOException {
		synchronized (monitor) {
			if (!Servidor.desconectarUsuario(usuario)) {
				salida.writeBoolean(false);
			} else {
				System.out.println("[Conexion con " + usuario + " cerrada]");
				salida.writeBoolean(true);
			}
		}
	}

}
