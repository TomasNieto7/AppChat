
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private List<Socket> vector = new ArrayList<>(); // Lista para almacenar los sockets de las conexiones de los clientes
    private List<Thread> hilos = new ArrayList<>(); // Lista para almacenar los hilos que se crearán para manejar las conexiones de los clientes
    private List<HiloChatServer> hilosChats = new ArrayList<>(); // Lista para almacenar las instancias de HiloChatServer, que gestionan la comunicación con cada cliente
    private int port; // Puerto en el que el servidor escucha las conexiones

    // Constructor que inicializa el puerto en el que se abrirá el servidor
    public ChatServer(int port) {
        this.port = port;
    }

    // Método para obtener la lista de hilos que manejan los clientes
    public List<Thread> getHilos() {
        return hilos;
    }

    // Método para obtener la lista de objetos HiloChatServer que gestionan la comunicación
    public List<HiloChatServer> getHilosChats() {
        return hilosChats;
    }

    // Método para obtener la lista de sockets conectados
    public List<Socket> getVector() {
        return vector;
    }

    // Método para asignar una nueva lista de sockets conectados
    public void setVector(List<Socket> vector) {
        this.vector = vector;
    }

    // Método para asignar una nueva lista de hilos
    public void setHilos(List<Thread> hilos) {
        this.hilos = hilos;
    }

    // Método para asignar una nueva lista de hilos de chat (HiloChatServer)
    public void setHilosChats(List<HiloChatServer> hilosChats) {
        this.hilosChats = hilosChats;
    }

    // Método que intenta crear un ServerSocket en el puerto especificado
    private ServerSocket connect() {
        try {
            ServerSocket sSocket = new ServerSocket(port); // Crear un ServerSocket en el puerto
            return sSocket; // Retornar el ServerSocket creado
        } catch (IOException ioe) {
            System.out.println("No se pudo realizar la conexión"); // Imprimir error si no se puede crear el ServerSocket
        }
        return null; // Retornar null en caso de error
    }

    // Método principal que espera conexiones de clientes y crea un hilo por cada cliente
    public void principal() {
        ServerSocket sSocket = connect(); // Conectar el servidor al puerto especificado
        if (sSocket != null) { // Si el servidor se conectó correctamente
            try {
                while (true) {
                    System.out.println("ChatServer abierto y esperando conexiones en puerto " + port); // Imprimir que el servidor está esperando conexiones
                    Socket socket = sSocket.accept(); // Aceptar una conexión de cliente
                    vector.add(socket); // Añadir el socket del cliente a la lista
                    // Crear una instancia de HiloChatServer para manejar la comunicación con el cliente
                    HiloChatServer hiloChatServer = new HiloChatServer(socket, vector, this);
                    // Crear un hilo para ejecutar el HiloChatServer y gestionarlo
                    Thread hilo = new Thread(hiloChatServer);
                    hilos.add(hilo); // Añadir el hilo a la lista de hilos
                    hilosChats.add(hiloChatServer); // Añadir el objeto HiloChatServer a la lista de hilos de chat
                    hilo.start(); // Iniciar el hilo
                }
            } catch (IOException ioe) {
                ioe.printStackTrace(); // Imprimir el stack trace en caso de error
            }
        } else {
            System.err.println("No se pudo abrir el puerto"); // Imprimir error si no se pudo abrir el puerto
        }
    }

    // Método main que inicia el servidor con el puerto pasado como argumento
    public static void main(String[] args) {
        ChatServer chat = new ChatServer(Integer.parseInt(args[0])); // Crear una instancia de ChatServer con el puerto recibido por parámetro
        chat.principal(); // Ejecutar el método principal para empezar a escuchar conexiones
    }

}
