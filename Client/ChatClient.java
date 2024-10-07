
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String answer;

    public ChatClient(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void listenForMessages() {
        Thread messageListener = new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = input.readUTF();
                    System.out.println("Servidor: " + messageFromServer);
                    answer = messageFromServer;
                }
            } catch (IOException e) {
                System.out.println("Error al recibir mensaje del servidor: " + e.getMessage());
            }
        });
        messageListener.start();
    }

    public void connectedClient(String host, int port, String name) {
        try {
            System.out.println("Conectado al servidor en: " + port);
            output.writeUTF("j^" + name);
            listenForMessages(); 
        } catch (IOException ioe) {
            System.out.println("Error al conectar con el servidor: " + ioe.getMessage());
        }
    }


    public void sendMessage(String msg) throws IOException {
        output.writeUTF("m^" + msg);
    }

    public String getRes() {
        return answer;
    }

    public void setRes(String msg) {
        answer = msg;
    }

    public void running(String[] args) {
        int port = Integer.parseInt(args[1]);
        connectedClient(args[0], port, args[2]);
    }
}
