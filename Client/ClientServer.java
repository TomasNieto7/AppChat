
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

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String userName;
    private ChatGlobal chatGlobal;
    private ChatPrivado chatPrivado;
    private List<JFrame> dmsFrame = new ArrayList<>();
    private List<JPanel> dmsPanel = new ArrayList<>();

    public ClientServer(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.userName = username;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        chatGlobal = new ChatGlobal(this, username, dmsFrame, dmsPanel);
        chatPrivado = new ChatPrivado(this, username, dmsFrame, dmsPanel);
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        ClientServer clientServer = new ClientServer(socket, args[2].toLowerCase());
        clientServer.running(args);
    }

    public void running(String[] args) {
        int port = Integer.parseInt(args[1]);
        String user = args[2].toLowerCase();
        String host = args[0];
        connectedClient(host, port, user);
        // Agregar un shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                sendDisconnection();
                System.out.println("¡Programa cerrado con Ctrl + C!");
            } catch (IOException ioe) {
                ioe.getStackTrace();
            }
        }));
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void connectedClient(String host, int port, String name) {
        try {
            System.out.println("Conectado al servidor en: " + port);
            output.writeUTF("j^" + name);
            userName = name;
            chatGlobal.initialize(userName);
            listenForMessages();
        } catch (IOException ioe) {
            System.out.println("Error al conectar con el servidor: " + ioe.getMessage());
        }
    }

    public void listenForMessages() {
        Thread messageListener = new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = input.readUTF();
                    System.out.println(messageFromServer);
                    String[] tokens = messageFromServer.split("\\^");
                    System.out.println(tokens[0]);
                    switch (tokens[0]) {
                        case "dm":
                            chatPrivado.renderResDM(messageFromServer);
                            break;
                        case "openDM":
                            openDMFrame(tokens[1]);
                            break;
                        case "closeDM":
                            closeDM(tokens[2], tokens[1]);
                            break;
                        case "d":
                            receiverDoc(tokens);
                            break;
                        case "ddm":
                            receiverDocDM(tokens);
                            break;
                        case "l":
                            chatGlobal.addUserList(messageFromServer);
                            break;
                        case "j":
                            chatGlobal.addUserList(messageFromServer);
                            break;
                        case "p":
                            chatGlobal.deleteUserList(messageFromServer);
                            break;
                        case "m":
                            chatGlobal.renderRes(tokens[1] + "^" + tokens[2]);
                            break;
                        default:
                            System.out.println("default");

                            break;
                    }
                }
            } catch (IOException ioe) {
                ioe.getStackTrace();
            }
        });
        messageListener.start();
    }

    public void sendMessage(String msg) throws IOException {
        output.writeUTF("m^" + msg);
    }

    public void sendDisconnection() throws IOException {
        output.writeUTF("p^");
    }

    public void sendMessageDM(String destinatario, String msg) throws IOException {
        try {
            SecretKey secretKey = CifradoAES.keyGenerator();
            String textoEncriptado = CifradoAES.encriptar(msg, secretKey);
            System.out.println(secretKey);
            output.writeUTF("dm^" + destinatario + textoEncriptado + "^" + CifradoAES.toString(secretKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDoc(String doc) throws IOException {

        try {
            File file = new File(doc);
            // Verificar si el archivo existe
            if (file.exists()) {
                output.writeUTF("d^" + doc);

                long fileSize = file.length();  // Obtener el tamaño del archivo
                if (fileSize <= 50000000) {
                    System.out.println(fileSize);

                    // Enviar el tamaño del archivo
                    DataOutputStream netOutDoc = new DataOutputStream(socket.getOutputStream());
                    netOutDoc.writeLong(fileSize);

                    // Enviar el archivo
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        netOutDoc.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Se envio");
                    fileInputStream.close();
                } else {
                    JOptionPane.showMessageDialog(null, "Ups, este archivo supera el tamaño maximo (50MB)", "Alerta", JOptionPane.WARNING_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(null, "Archivo no encontrado", "Alerta", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiverDoc(String[] tokens) throws IOException {
        // Recibir archivo
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Leer el tamaño del archivo
        long fileSize = dataInputStream.readLong();

        FileOutputStream fileOutputStream = new FileOutputStream(tokens[2]);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;

        // Leer el archivo hasta que se hayan recibido todos los bytes
        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        System.out.println("se guardo");
        chatGlobal.renderRes(tokens[1] + "^" + tokens[2]);
        fileOutputStream.close();
    }

    public void sendDocDM(String doc) throws IOException {
        try {
            String[] tokens = doc.split("\\^");
            File file = new File(tokens[1]);
            // Verificar si el archivo existe
            if (file.exists()) {
                output.writeUTF("ddm^" + doc);

                long fileSize = file.length();  // Obtener el tamaño del archivo
                if (fileSize <= 50000000) {
                    // Enviar el tamaño del archivo
                    DataOutputStream netOutDoc = new DataOutputStream(socket.getOutputStream());
                    netOutDoc.writeLong(fileSize);

                    // Enviar el archivo
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        netOutDoc.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Se envio");
                    fileInputStream.close();
                } else {
                    JOptionPane.showMessageDialog(null, "Ups, este archivo supera el tamaño maximo (50MB)", "Alerta", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Archivo no encontrado", "Alerta", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiverDocDM(String[] tokens) throws IOException {
        // Recibir archivo
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Leer el tamaño del archivo
        long fileSize = dataInputStream.readLong();

        FileOutputStream fileOutputStream = new FileOutputStream(tokens[2]);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;

        // Leer el archivo hasta que se hayan recibido todos los bytes
        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }

        // renderResDM(tokens[0] + "^" + tokens[1] + "^" + tokens[2]);
        fileOutputStream.close();
    }

    public void sendOpenDM(String destinatario, String emisor) throws IOException {
        output.writeUTF("open^@" + destinatario + "^" + emisor);
    }

    public void openDMFrame(String name) {
        try {
            chatPrivado.dmFrame(name);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void closeDM(String emisor, String destinatario) throws IOException {
        JFrame frameDM = getDmFrameChat(dmsFrame, emisor, destinatario + ":");
        if (frameDM != null) {
            dmsFrame.remove(frameDM);
            frameDM.dispose(); // Cierra el JFrame
        }
    }

    public JFrame getDmFrameChat(List<JFrame> frames, String emisor, String destinatario) {
        return chatPrivado.getDmFrame(frames, emisor, destinatario);
    }

    public void sendCloseDM(String destinatario, String emisor) {
        try {
            output.writeUTF("closeDM^@" + destinatario + "^" + emisor);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
