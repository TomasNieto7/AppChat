
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.SecretKey;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ClientServer {

    private Socket socket; // Socket para la conexión con el servidor
    private DataInputStream input; // Stream para recibir datos del servidor
    private DataOutputStream output; // Stream para enviar datos al servidor
    private String userName; // Nombre de usuario del cliente
    private ChatGlobal chatGlobal; // Objeto para manejar el chat global
    private ChatPrivado chatPrivado; // Objeto para manejar los chats privados
    private List<JFrame> dmsFrame = new ArrayList<>(); // Lista para almacenar las ventanas de chats privados
    private List<JPanel> dmsPanel = new ArrayList<>(); // Lista para almacenar los paneles de chats privados
    private boolean flag = false; // Lista para almacenar los paneles de chats privados

    // Constructor que inicializa el socket y los streams de entrada y salida
    public ClientServer(Socket socket, String username) throws IOException {
        this.socket = socket; // Inicializa el socket
        this.userName = username; // Establece el nombre de usuario
        input = new DataInputStream(socket.getInputStream()); // Inicializa el stream de entrada
        output = new DataOutputStream(socket.getOutputStream()); // Inicializa el stream de salida
        chatGlobal = new ChatGlobal(this, username, dmsFrame); // Inicializa el chat global
        chatPrivado = new ChatPrivado(this, username, dmsFrame, dmsPanel); // Inicializa el chat privado
    }

    // Método main que establece la conexión con el servidor y ejecuta el cliente
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(args[0], Integer.parseInt(args[1])); // Conecta al servidor usando la dirección y
                                                                        // puerto proporcionados
        ClientServer clientServer = new ClientServer(socket, args[2].toLowerCase()); // Crea una instancia del cliente
        clientServer.running(args); // Ejecuta el método para mantener al cliente en funcionamiento
    }

    // Método que mantiene al cliente conectado y escucha la desconexión
    public void running(String[] args) {
        int port = Integer.parseInt(args[1]); // Extrae el puerto
        String user = args[2].toLowerCase(); // Extrae el nombre de usuario
        String host = args[0]; // Extrae la dirección del host
        connectedClient(host, port, user); // Conecta al cliente al servidor

        // Agregar un shutdown hook para manejar la desconexión del cliente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                for (int i = 0; i < dmsFrame.size(); i++) {
                    JFrame frame = dmsFrame.get(i); // Obtiene cada ventana de chat privado
                    String titleFrame = frame.getTitle(); // Obtiene el título de la ventana
                    String[] tokens = titleFrame.split("-"); // Separa el título para obtener el destinatario y emisor
                    sendCloseDM(tokens[1], tokens[0]); // Envía un mensaje para cerrar el DM
                    dmsFrame.remove(i); // Elimina la ventana de la lista
                }
                sendDisconnection(); // Envía un mensaje de desconexión al servidor
                System.out.println("¡Programa cerrado con Ctrl + C!"); // Mensaje de cierre
            } catch (IOException ioe) {
                ioe.getStackTrace(); // Manejo de excepciones
            }
        }));

        // Mantiene al cliente en ejecución
        try {
            Thread.sleep(Long.MAX_VALUE); // Suspende el hilo indefinidamente
        } catch (InterruptedException e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }

    // Método que maneja la conexión del cliente al servidor
    public void connectedClient(String host, int port, String name) {
        try {
            System.out.println("Conectado al servidor en: " + port); // Mensaje de conexión
            output.writeUTF("j^" + name); // Envía el nombre de usuario al servidor
            userName = name; // Actualiza el nombre de usuario
            chatGlobal.initialize(userName); // Inicializa el chat global con el nombre de usuario
            listenForMessages(); // Comienza a escuchar mensajes del servidor
        } catch (IOException ioe) {
            System.out.println("Error al conectar con el servidor: " + ioe.getMessage()); // Mensaje de error
        }
    }

    // Método que escucha los mensajes del servidor en un hilo separado
    public void listenForMessages() {
        Thread messageListener = new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = input.readUTF(); // Lee un mensaje del servidor
                    String[] tokens = messageFromServer.split("\\^"); // Divide el mensaje en tokens
                    // Maneja el tipo de mensaje recibido
                    switch (tokens[0]) {
                        case "dm":
                            chatPrivado.renderResDM(messageFromServer); // Renderiza el mensaje del chat privado
                            break;
                        case "openDM":
                            openDMFrame(tokens[1]); // Abre una ventana de chat privado
                            break;
                        case "closeDM":
                            closeDM(tokens[2], tokens[1]); // Cierra la ventana de chat privado
                            break;
                        case "d":
                            receiverDoc(tokens); // Recibe un documento
                            break;
                        case "ddm":
                            receiverDocDM(tokens); // Recibe un documento en el chat privado
                            break;
                        case "l":
                            chatGlobal.addUserList(messageFromServer); // Agrega un usuario a la lista
                            break;
                        case "j":
                            chatGlobal.addUserList(messageFromServer); // Agrega un usuario que se unió
                            flag = true;
                            break;
                        case "p":
                            chatGlobal.deleteUserList(messageFromServer); // Elimina un usuario de la lista
                            break;
                        case "m":
                            chatGlobal.renderRes(tokens[1] + "^" + tokens[2]); // Renderiza un mensaje del chat global
                            break;
                        case "u":
                            if (flag == false) {
                                System.out.println("Usuario ya registrado");
                                System.exit(0);
                            }
                            break;
                        default:
                            System.out.println("default"); // Manejo de caso por defecto
                            break;
                    }
                }
            } catch (IOException ioe) {
                ioe.getStackTrace(); // Manejo de excepciones
            }
        });
        messageListener.start(); // Inicia el hilo de escucha de mensajes
    }

    // Método para enviar un mensaje al servidor
    public void sendMessage(String msg) throws IOException {
        output.writeUTF("m^" + msg); // Envía el mensaje prefijado
    }

    // Método para enviar un mensaje de desconexión al servidor
    public void sendDisconnection() throws IOException {
        output.writeUTF("p^"); // Envía el prefijo de desconexión
    }

    // Método para enviar un mensaje directo a un destinatario
    public void sendMessageDM(String destinatario, String msg) throws IOException {
        try {
            SecretKey secretKey = CifradoAES.keyGenerator(); // Genera una clave secreta
            String textoEncriptado = CifradoAES.encriptar(msg, secretKey); // Encripta el mensaje
            // Envía el mensaje encriptado junto con la clave secreta
            output.writeUTF("dm^" + destinatario + textoEncriptado + "^" + CifradoAES.toString(secretKey));
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }

    // Método para enviar un documento al servidor
    public void sendDoc(String doc) throws IOException {
        try {
            File file = new File(doc); // Crea un objeto File con la ruta proporcionada
            // Verifica si el archivo existe
            if (file.exists()) {
                long fileSize = file.length(); // Obtener el tamaño del archivo
                if (fileSize <= 50000000) { // Verifica si el tamaño del archivo es menor o igual a 50MB
                    output.writeUTF("d^" + doc); // Envía el prefijo del documento
                    // Envía el tamaño del archivo
                    DataOutputStream netOutDoc = new DataOutputStream(socket.getOutputStream());
                    netOutDoc.writeLong(fileSize); // Envía el tamaño del archivo
                    // Envía el archivo en bloques de 4096 bytes
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        netOutDoc.write(buffer, 0, bytesRead); // Escribe los bytes leídos al stream de salida
                    }
                    fileInputStream.close(); // Cierra el flujo de entrada del archivo
                } else {
                    // Muestra un mensaje de alerta si el archivo es demasiado grande
                    JOptionPane.showMessageDialog(null, "Ups, este archivo supera el tamaño máximo (50MB)", "Alerta",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // Muestra un mensaje de alerta si el archivo no se encuentra
                JOptionPane.showMessageDialog(null, "Archivo no encontrado", "Alerta", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }

    // Método para recibir un documento del servidor
    public void receiverDoc(String[] tokens) throws IOException {
        // Recibir archivo
        InputStream inputStream = socket.getInputStream(); // Obtiene el flujo de entrada del socket
        DataInputStream dataInputStream = new DataInputStream(inputStream); // Crea un DataInputStream para leer el
                                                                            // archivo

        // Leer el tamaño del archivo
        long fileSize = dataInputStream.readLong(); // Lee el tamaño del archivo

        FileOutputStream fileOutputStream = new FileOutputStream(tokens[2]); // Crea un flujo de salida para guardar el
                                                                             // archivo
        byte[] buffer = new byte[4096]; // Buffer para leer el archivo
        int bytesRead;
        long totalBytesRead = 0; // Contador de bytes leídos

        // Leer el archivo hasta que se hayan recibido todos los bytes
        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead); // Escribe los bytes leídos en el archivo
            totalBytesRead += bytesRead; // Actualiza el total de bytes leídos
        }
        chatGlobal.renderRes(tokens[1] + "^" + tokens[2]); // Renderiza la respuesta en el chat global
        fileOutputStream.close(); // Cierra el flujo de salida del archivo
    }

    // Método para enviar un documento a un destinatario en el chat privado
    public void sendDocDM(String doc) throws IOException {
        try {
            String[] tokens = doc.split("\\^"); // Divide la cadena de documento en tokens
            File file = new File(tokens[1]); // Crea un objeto File con la ruta del documento
            // Verifica si el archivo existe
            if (file.exists()) {

                long fileSize = file.length(); // Obtener el tamaño del archivo
                if (fileSize <= 50000000) { // Verifica si el tamaño del archivo es menor o igual a 50MB
                    output.writeUTF("ddm^" + doc); // Envía el prefijo del documento directo
                    // Enviar el tamaño del archivo
                    DataOutputStream netOutDoc = new DataOutputStream(socket.getOutputStream());
                    netOutDoc.writeLong(fileSize); // Envía el tamaño del archivo

                    // Envía el archivo en bloques de 4096 bytes
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        netOutDoc.write(buffer, 0, bytesRead); // Escribe los bytes leídos al stream de salida
                    }
                    fileInputStream.close(); // Cierra el flujo de entrada del archivo
                } else {
                    // Muestra un mensaje de alerta si el archivo es demasiado grande
                    JOptionPane.showMessageDialog(null, "Ups, este archivo supera el tamaño máximo (50MB)", "Alerta",
                            JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // Muestra un mensaje de alerta si el archivo no se encuentra
                JOptionPane.showMessageDialog(null, "Archivo no encontrado", "Alerta", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }

    // Método para recibir un documento en el chat privado
    public void receiverDocDM(String[] tokens) throws IOException {
        // Recibir archivo
        InputStream inputStream = socket.getInputStream(); // Obtiene el flujo de entrada del socket
        DataInputStream dataInputStream = new DataInputStream(inputStream); // Crea un DataInputStream para leer el
                                                                            // archivo

        // Leer el tamaño del archivo
        long fileSize = dataInputStream.readLong(); // Lee el tamaño del archivo

        FileOutputStream fileOutputStream = new FileOutputStream(tokens[2]); // Crea un flujo de salida para guardar el
                                                                             // archivo
        byte[] buffer = new byte[4096]; // Buffer para leer el archivo
        int bytesRead;
        long totalBytesRead = 0; // Contador de bytes leídos

        // Leer el archivo hasta que se hayan recibido todos los bytes
        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead); // Escribe los bytes leídos en el archivo
            totalBytesRead += bytesRead; // Actualiza el total de bytes leídos
        }

        chatPrivado.renderResDM(tokens[0] + "^" + tokens[1] + "^" + tokens[2]); // Renderiza la respuesta en el chat
                                                                                // privado
        fileOutputStream.close(); // Cierra el flujo de salida del archivo
    }

    // Método para enviar un mensaje que abre un DM
    public void sendOpenDM(String destinatario, String emisor) throws IOException {
        output.writeUTF("open^@" + destinatario + "^" + emisor); // Envía un mensaje para abrir un DM
    }

    // Método para abrir una ventana de chat privado
    public void openDMFrame(String name) {
        try {
            chatPrivado.dmFrame(name); // Llama al método para abrir la ventana de DM
        } catch (IOException ioe) {
            ioe.printStackTrace(); // Manejo de excepciones
        }
    }

    // Método para cerrar una ventana de chat privado
    public void closeDM(String emisor, String destinatario) throws IOException {
        JFrame frameDM = getDmFrameChat(dmsFrame, emisor, destinatario + ":"); // Obtiene la ventana de DM
        if (frameDM != null) {
            dmsFrame.remove(frameDM); // Elimina la ventana de la lista
            frameDM.dispose(); // Cierra el JFrame
        }
    }

    // Método para obtener la ventana de chat privado
    public JFrame getDmFrameChat(List<JFrame> frames, String emisor, String destinatario) {
        return chatPrivado.getDmFrame(frames, emisor, destinatario); // Llama al método para obtener la ventana de DM
    }

    // Método para enviar un mensaje para cerrar un DM
    public void sendCloseDM(String destinatario, String emisor) {
        try {
            output.writeUTF("closeDM^@" + destinatario + "^" + emisor); // Envía un mensaje para cerrar el DM
        } catch (IOException ioe) {
            ioe.printStackTrace(); // Manejo de excepciones
        }
    }
}
