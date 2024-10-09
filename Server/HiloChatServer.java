
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

public class HiloChatServer implements Runnable {

    private Socket socket;
    private List<Socket> vector;
    private DataInputStream netIn;
    private DataOutputStream netOut;
    private String name;
    private ChatServer chatServer;

    public HiloChatServer(Socket socket, List<Socket> vector, ChatServer chatServer) {
        this.socket = socket;
        this.vector = vector;
        this.chatServer = chatServer;
    }

    private void initStreams() throws IOException {
        netIn = new DataInputStream(socket.getInputStream());
    }

    private void sendMsg(String msg) throws IOException {
        for (Socket soc : vector) {
            netOut = new DataOutputStream(soc.getOutputStream());
            netOut.writeUTF(msg);
        }
    }

    private void sendMsgDM(String[] tokens) throws IOException {
        // Verificamos si el mensaje es un mensaje privado
        if (tokens[1].startsWith("@")) {
            String destinatario = tokens[1].substring(1); // quitamos el '@'
            String mensaje = "dm^" + name + "-" + destinatario + ":^" + tokens[2] + "^" + tokens[3];
            // Buscamos el socket del destinatario
            for (Socket soc : vector) {
                HiloChatServer client = getClientByName(destinatario);
                if (client != null && client.getSocket().equals(soc)) {
                    // Enviamos el mensaje solo al destinatario
                    netOut = new DataOutputStream(soc.getOutputStream());
                    netOut.writeUTF(mensaje);
                    netOut = new DataOutputStream(socket.getOutputStream());
                    netOut.writeUTF(mensaje);
                    return;
                }
            }
        }
    }

    private void sendMsgDocDM(String[] tokens) throws IOException {
        // Verificamos si el mensaje es un mensaje privado
        if (tokens[1].startsWith("@")) {
            String destinatario = tokens[1].substring(1); // quitamos el '@'
            String mensaje = "ddm^" + name + "-" + destinatario + ":^" + tokens[2];
            // Buscamos el socket del destinatario
            for (Socket soc : vector) {
                HiloChatServer client = getClientByName(destinatario);
                if (client != null && client.getSocket().equals(soc)) {
                    // Enviamos el mensaje solo al destinatario
                    netOut = new DataOutputStream(soc.getOutputStream());
                    netOut.writeUTF(mensaje);
                    netOut = new DataOutputStream(socket.getOutputStream());
                    netOut.writeUTF("dm^" + name + "-" + destinatario + ":^" + tokens[2]);
                    return;
                }
            }
        }
    }

    private void sendFlagDM(String[] tokens) throws IOException {
        // Verificamos si el mensaje es un mensaje privado
        if (tokens[1].startsWith("@")) {
            String destinatario = tokens[1].substring(1); // quitamos el '@'
            String mensaje = "openDM^" + name;
            // Buscamos el socket del destinatario
            for (Socket soc : vector) {
                HiloChatServer client = getClientByName(destinatario);
                if (client != null && client.getSocket().equals(soc)) {
                    // Enviamos el mensaje solo al destinatario
                    netOut = new DataOutputStream(soc.getOutputStream());
                    netOut.writeUTF(mensaje);
                    return;
                }
            }
        }
    }

    private void sendFlagCloseDM(String[] tokens) throws IOException {
        // Verificamos si el mensaje es un mensaje privado
        if (tokens[1].startsWith("@")) {
            String destinatario = tokens[1].substring(1); // quitamos el '@'
            String mensaje = "closeDM^" + destinatario + "^" + tokens[2];
            // Buscamos el socket del destinatario
            for (Socket soc : vector) {
                HiloChatServer client = getClientByName(destinatario);
                if (client != null && client.getSocket().equals(soc)) {
                    // Enviamos el mensaje solo al destinatario
                    netOut = new DataOutputStream(soc.getOutputStream());
                    netOut.writeUTF(mensaje);
                    return;
                }
            }
        }
    }

    public void updateSocket(Socket newSocket) throws IOException {
        this.socket = newSocket;
        initStreams(); // Reinicializar los streams
    }

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    private HiloChatServer getClientByName(String name) {
        for (HiloChatServer client : chatServer.getHilosChats()) {
            if (client.getName().equals(name)) {
                return client;
            }
        }
        return null;
    }

    public void removeUser(Socket socket) {
        List<Socket> vector = chatServer.getVector();
        List<Thread> hilos = chatServer.getHilos();
        List<HiloChatServer> hilosChats = chatServer.getHilosChats();
        int indexDelete = vector.indexOf(socket);
        if (indexDelete >= 0) {
            vector.remove(indexDelete);
            hilos.remove(indexDelete);
            hilosChats.remove(indexDelete);
            chatServer.setVector(vector);
            chatServer.setHilos(hilos);
            chatServer.setHilosChats(hilosChats);
        }
    }

    private void sendUserActive() throws IOException {
        List<Socket> sockets = chatServer.getVector();
        List<Thread> hilos = chatServer.getHilos();
        List<HiloChatServer> hilosChats = chatServer.getHilosChats();
        String nameA;
        for (int i = 0; i < hilosChats.size(); i++) {
            HiloChatServer chat = hilosChats.get(i);
            boolean flag = isAlive(hilos.get(i));
            nameA = chat.getName();

            // Verificar si el socket no está cerrado antes de intentar enviar mensajes
            if (nameA != null && flag && !nameA.equals(name) && !socket.isClosed()) {
                netOut = new DataOutputStream(socket.getOutputStream());
                netOut.writeUTF("l^server:^" + nameA + "^ is connected");
            }
        }
    }

    public boolean isAlive(Thread hilo) {
        return hilo.isAlive();
    }

    public void receiverDoc(String[] tokens) throws IOException {
        InputStream inputStream = socket.getInputStream();
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Leer el tamaño del archivo
        long fileSize = dataInputStream.readLong();

        FileOutputStream fileOutputStream = new FileOutputStream(tokens[1]);
        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalBytesRead = 0;

        // Leer el archivo hasta que se hayan recibido todos los bytes
        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }

        System.out.println("Archivo recibido y guardado.");
        fileOutputStream.close();
        sendDoc(tokens);
    }

    public void sendDoc(String[] tokens) throws IOException {
        // Seleccionar archivo para enviar
        sendMsg(tokens[0] + "^" + name + ":^" + tokens[1]);
        try {
            File file = new File(tokens[1]);
            long fileSize = file.length();  // Obtener el tamaño del archivo

            for (Socket soc : vector) {
                // Enviar el tamaño del archivo
                DataOutputStream netOutDoc = new DataOutputStream(soc.getOutputStream());
                netOutDoc.writeLong(fileSize);
                // Enviar el archivo
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    netOutDoc.write(buffer, 0, bytesRead);
                }
                System.out.println("Archivo enviado.");
                fileInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiverDocDM(String[] tokens) throws IOException {
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

        System.out.println("Archivo recibido y guardado.");
        fileOutputStream.close();
        sendDocDM(tokens);
    }

    public void sendDocDM(String[] tokens) throws IOException {
        // Seleccionar archivo para enviar
        sendMsgDocDM(tokens);
        try {
            // Verificamos si el mensaje es un mensaje privado
            if (tokens[1].startsWith("@")) {
                File file = new File(tokens[2]);
                long fileSize = file.length();  // Obtener el tamaño del archivo

                // Buscamos el socket del destinatario
                for (Socket soc : vector) {
                    String destinatario = tokens[1].substring(1); // quitamos el '@'
                    HiloChatServer client = getClientByName(destinatario);
                    if (client != null && client.getSocket().equals(soc)) {
                        // Enviar el tamaño del archivo
                        DataOutputStream netOutDoc = new DataOutputStream(soc.getOutputStream());
                        netOutDoc.writeLong(fileSize);
                        // Enviar el archivo
                        FileInputStream fileInputStream = new FileInputStream(file);
                        byte[] buffer = new byte[4096];
                        int bytesRead;

                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            netOutDoc.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Archivo enviado.");
                        fileInputStream.close();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String options(String res, String[] tokens) throws IOException {
        switch (tokens[0]) {
            case "p":
                res = "p^server:^";
                res += name + "^";
                removeUser(socket);
                socket.close();
                res += "left";
                break;
            case "j":
                name = tokens[1];
                res = "j^server:^";
                res += name + "^";
                res += "joined";
                break;
            case "m":
                res = "m^";
                res += name + ":^";
                res += tokens[1];
                break;
            case "dm":
                sendMsgDM(tokens);
                res = "-1";
                break;
            case "open":
                sendFlagDM(tokens);
                res = "-1";
                break;
            case "closeDM":
                sendFlagCloseDM(tokens);
                res = "-1";
                break;
            case "d":
                receiverDoc(tokens);
                res = "-1";
                break;
            case "ddm":
                receiverDocDM(tokens);
                res = "-1";
                break;
            default:
                throw new AssertionError();
        }
        return res;
    }

    public void run() {
        try {
            initStreams();
            sendUserActive();
            while (true) {
                String msg = netIn.readUTF();
                String[] tokens = msg.split("\\^");
                String res = "server:^";
                System.out.println(msg);
                res = options(res, tokens);
                if (!"-1".equals(res)) {
                    sendMsg(res);
                }
            }
        } catch (IOException ioe) {
            ioe.getStackTrace();
        }
    }

}
