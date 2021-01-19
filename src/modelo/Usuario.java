package modelo;

import java.util.ArrayList;
import java.util.List;

/**
* <h1>Clase Usuario</h1>
* <p>Esta clase es un modelo usado por el Servidor
* almacena un usuario de un cliente y sus mensajes del buzón de mensajes.</p>
*
* @author  Angel Mulero
* @version 1.0
*/

public class Usuario {
	String nombre;
	boolean conectado;
	ArrayList<String> mensajes = new ArrayList<>();
	
    /** 
     * Metodo constructor encargado de iniciar la clase.
     * @param nombre El nombre del usuario
     * @param conectado El estado del usuario (Si se encuentra conectado o no)
     */
	public Usuario(String nombre, boolean conectado) {
		this.nombre = nombre;
		this.conectado = conectado;
	}
	
    /** 
     * Metodo get que devuelve el nombre del usuario.
     * @return nombre El nombre del usuario.
     */
	public String getNombre() {
		return nombre;
	}
	
    /** 
     * Metodo get que devuelve el estado del usuario.
     * @return conectado Si el usuario esta conectado o no.
     */
	public boolean getConectado() {
		return conectado;
	}
	
    /** 
     * Metodo set que establece el nombre de un usuarios.
     * @param nombre El nombre del usuario que se quiera almecenar.
     */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
    /** 
     * Metodo set que establece el estado del usuario.
     * @param conectado El estado que se le quiera dar al usuario (conectado o no conectado).
     */
	public void setConectado(boolean conectado) {
		this.conectado = conectado;
	}
	
    /** 
     * Metodo para agregar un mensaje a el buzon de mensajes de un usuario.
     * @param mensaje El mensaje de un usuario registrado.
     */
	public void addMensaje(String mensaje) {
		mensajes.add(mensaje);
	}
	
    /** 
     * Metodo para devolver la lista de mensajes.
     * @return mensajes Devuelve la lista de mensajes del usuario.
     */
	public List<String> getMensajes() {
		return mensajes;
	}
	
    /** 
     * Metodo para resetear la lista de mensajes.
     */
	public void setMensajes() {
		ArrayList<String> nuevaBandeja = new ArrayList<>();
		mensajes = nuevaBandeja;
	}
	
    /** 
     * Metodo para devolver si hay mensajes nuevos.
     * @return mensajes.isEmpty() Devuelve si el usuario tiene mensajes nuevos o no.
     */
	public boolean estadoMensajes() {
		return mensajes.isEmpty();
	}
}
