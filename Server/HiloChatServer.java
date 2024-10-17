
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

    private Socket socket;  // Socket para la conexión del cliente
    private List<Socket> vector;  // Lista de sockets de todos los clientes conectados
    private DataInputStream netIn;  // Flujo de entrada de datos desde el socket
    private DataOutputStream netOut;  // Flujo de salida de datos hacia el socket
    private String name;  // Nombre del cliente
    private ChatServer chatServer;  // Referencia al servidor de chat

    public HiloChatServer(Socket socket, List<Socket> vector, ChatServer chatServer) {
        this.socket = socket;  // Inicializar el socket
        this.vector = vector;  // Inicializar la lista de sockets
        this.chatServer = chatServer;  // Inicializar el servidor de chat
    }

    private void initStreams() throws IOException {
        netIn = new DataInputStream(socket.getInputStream());  // Inicializar el flujo de entrada desde el socket
    }

    private void sendMsg(String msg) throws IOException {
        for (Socket soc : vector) {  // Enviar el mensaje a todos los clientes conectados
            netOut = new DataOutputStream(soc.getOutputStream());  // Inicializar el flujo de salida
            netOut.writeUTF(msg);  // Enviar el mensaje
        }
    }

    private void sendMsgDM(String[] tokens) throws IOException {
        if (tokens[1].startsWith("@")) {  // Comprobar si el mensaje es un mensaje privado
            String destinatario = tokens[1].substring(1);  // Obtener el nombre del destinatario
            String mensaje = "dm^" + name + "-" + destinatario + ":^" + tokens[2] + "^" + tokens[3];  // Crear el mensaje
            for (Socket soc : vector) {  // Buscar el socket del destinatario
                HiloChatServer client = getClientByName(destinatario);  // Obtener el cliente por nombre
                if (client != null && client.getSocket().equals(soc)) {
                    netOut = new DataOutputStream(soc.getOutputStream());  // Enviar el mensaje al destinatario
                    netOut.writeUTF(mensaje);  // Enviar el mensaje
                    netOut = new DataOutputStream(socket.getOutputStream());  // También enviar una copia al emisor
                    netOut.writeUTF(mensaje);  // Enviar el mensaje
                    return;  // Terminar la ejecución
                }
            }
        }
    }

    private void sendMsgDocDM(String[] tokens) throws IOException {
        if (tokens[1].startsWith("@")) {  // Comprobar si el mensaje es un mensaje privado con documento
            String destinatario = tokens[1].substring(1);  // Obtener el nombre del destinatario
            String mensaje = "ddm^" + name + "-" + destinatario + ":^" + tokens[2];  // Crear el mensaje
            for (Socket soc : vector) {  // Buscar el socket del destinatario
                HiloChatServer client = getClientByName(destinatario);  // Obtener el cliente por nombre
                if (client != null && client.getSocket().equals(soc)) {
                    netOut = new DataOutputStream(soc.getOutputStream());  // Enviar el mensaje al destinatario
                    netOut.writeUTF(mensaje);  // Enviar el mensaje
                    netOut = new DataOutputStream(socket.getOutputStream());  // También enviar una copia al emisor
                    netOut.writeUTF("dm^" + name + "-" + destinatario + ":^" + tokens[2]);  // Enviar
                    return;  // Terminar la ejecución
                }
            }
        }
    }

    private void sendFlagDM(String[] tokens) throws IOException {
        if (tokens[1].startsWith("@")) {  // Comprobar si es una señal para abrir un chat privado
            String destinatario = tokens[1].substring(1);  // Obtener el nombre del destinatario
            String mensaje = "openDM^" + name;  // Crear el mensaje
            for (Socket soc : vector) {  // Buscar el socket del destinatario
                HiloChatServer client = getClientByName(destinatario);  // Obtener el cliente por nombre
                if (client != null && client.getSocket().equals(soc)) {
                    netOut = new DataOutputStream(soc.getOutputStream());  // Enviar el mensaje al destinatario
                    netOut.writeUTF(mensaje);  // Enviar el mensaje
                    return;  // Terminar la ejecución
                }
            }
        }
    }

    private void sendFlagCloseDM(String[] tokens) throws IOException {
        if (tokens[1].startsWith("@")) {  // Comprobar si es una señal para cerrar un chat privado
            String destinatario = tokens[1].substring(1);  // Obtener el nombre del destinatario
            String mensaje = "closeDM^" + destinatario + "^" + tokens[2];  // Crear el mensaje
            for (Socket soc : vector) {  // Buscar el socket del destinatario
                HiloChatServer client = getClientByName(destinatario);  // Obtener el cliente por nombre
                if (client != null && client.getSocket().equals(soc)) {
                    netOut = new DataOutputStream(soc.getOutputStream());  // Enviar el mensaje al destinatario
                    netOut.writeUTF(mensaje);  // Enviar el mensaje
                    return;  // Terminar la ejecución
                }
            }
        }
    }

    public void updateSocket(Socket newSocket) throws IOException {
        this.socket = newSocket;  // Actualizar el socket
        initStreams();  // Reinicializar los flujos
    }

    public String getName() {
        return name;  // Obtener el nombre del cliente
    }

    public Socket getSocket() {
        return socket;  // Obtener el socket del cliente
    }

    private HiloChatServer getClientByName(String name) {
        for (HiloChatServer client : chatServer.getHilosChats()) {  // Buscar un cliente por su nombre en la lista de hilos de chat
            String clientName = client.getName();
            if (clientName!=null && clientName.equals(name)) {
                return client;  // Retornar el cliente si coincide el nombre
            }
        }
        return null;  // Retornar null si no se encuentra el cliente
    }

    public void removeUser(Socket socket) {
        List<Socket> vector = chatServer.getVector();  // Obtener la lista de sockets
        List<Thread> hilos = chatServer.getHilos();  // Obtener la lista de hilos
        List<HiloChatServer> hilosChats = chatServer.getHilosChats();  // Obtener la lista de hilos de chat
        int indexDelete = vector.indexOf(socket);  // Obtener el índice del socket a eliminar
        if (indexDelete >= 0) {  // Verificar si el índice es válido
            vector.remove(indexDelete);  // Eliminar el socket de la lista
            hilos.remove(indexDelete);  // Eliminar el hilo correspondiente de la lista
            hilosChats.remove(indexDelete);  // Eliminar el HiloChatServer correspondiente
            chatServer.setVector(vector);  // Actualizar la lista de sockets en el servidor
            chatServer.setHilos(hilos);  // Actualizar la lista de hilos en el servidor
            chatServer.setHilosChats(hilosChats);  // Actualizar la lista de hilos de chat en el servidor
        }
    }

    private void sendUserActive() throws IOException {
        List<Socket> sockets = chatServer.getVector();  // Obtener la lista de sockets conectados
        List<Thread> hilos = chatServer.getHilos();  // Obtener la lista de hilos
        List<HiloChatServer> hilosChats = chatServer.getHilosChats();  // Obtener la lista de hilos de chat
        String nameA;  // Variable para almacenar el nombre del usuario
        for (int i = 0; i < hilosChats.size(); i++) {  // Recorrer la lista de hilos de chat
            HiloChatServer chat = hilosChats.get(i);  // Obtener el hilo de chat
            boolean flag = isAlive(hilos.get(i));  // Comprobar si el hilo está vivo
            nameA = chat.getName();  // Obtener el nombre del cliente

            if (nameA != null && flag && !nameA.equals(name) && !socket.isClosed()) {  // Verificar si el socket está cerrado
                netOut = new DataOutputStream(socket.getOutputStream());  // Inicializar el flujo de salida
                netOut.writeUTF("l^server:^" + nameA + "^ is connected");  // Enviar el mensaje de usuario activo
            }
        }
    }

    public boolean isAlive(Thread hilo) {
        return hilo.isAlive();  // Verificar si el hilo está vivo
    }

    public void receiverDoc(String[] tokens) throws IOException {
        InputStream inputStream = socket.getInputStream();  // Obtener el flujo de entrada del socket
        DataInputStream dataInputStream = new DataInputStream(inputStream);  // Crear un DataInputStream para leer los datos

        long fileSize = dataInputStream.readLong();  // Leer el tamaño del archivo
        FileOutputStream fileOutputStream = new FileOutputStream(tokens[1]);  // Crear un FileOutputStream para escribir el archivo

        byte[] buffer = new byte[4096];  // Crear un búfer para almacenar los bytes del archivo
        int bytesRead;  // Variable para almacenar la cantidad de bytes leídos
        long totalBytesRead = 0;  // Variable para almacenar el total de bytes leídos

        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {  // Leer hasta que se reciba todo el archivo
            fileOutputStream.write(buffer, 0, bytesRead);  // Escribir los bytes en el archivo
            totalBytesRead += bytesRead;  // Actualizar la cantidad de bytes leídos
        }

        System.out.println("Archivo recibido y guardado.");  // Imprimir mensaje de confirmación
        fileOutputStream.close();  // Cerrar el archivo
        sendDoc(tokens);  // Enviar el documento a otros clientes
    }

    public void sendDoc(String[] tokens) throws IOException {
        sendMsg(tokens[0] + "^" + name + ":^" + tokens[1]);  // Enviar un mensaje notificando sobre el archivo
        try {
            File file = new File(tokens[1]);  // Crear un archivo con el nombre especificado
            long fileSize = file.length();  // Obtener el tamaño del archivo

            for (Socket soc : vector) {  // Enviar el archivo a todos los clientes
                DataOutputStream netOutDoc = new DataOutputStream(soc.getOutputStream());  // Crear un flujo de salida
                netOutDoc.writeLong(fileSize);  // Enviar el tamaño del archivo

                FileInputStream fileInputStream = new FileInputStream(file);  // Crear un FileInputStream para leer el archivo
                byte[] buffer = new byte[4096];  // Crear un búfer para almacenar los bytes del archivo
                int bytesRead;  // Variable para almacenar la cantidad de bytes leídos

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {  // Leer y enviar el archivo
                    netOutDoc.write(buffer, 0, bytesRead);  // Enviar los bytes leídos
                }
                System.out.println("Archivo enviado.");  // Imprimir mensaje de confirmación
                fileInputStream.close();  // Cerrar el archivo
            }
        } catch (IOException e) {
            e.printStackTrace();  // Manejar excepciones
        }
    }

    public void receiverDocDM(String[] tokens) throws IOException {
        InputStream inputStream = socket.getInputStream();  // Obtener el flujo de entrada del socket
        DataInputStream dataInputStream = new DataInputStream(inputStream);  // Crear un DataInputStream para leer los datos

        long fileSize = dataInputStream.readLong();  // Leer el tamaño del archivo
        FileOutputStream fileOutputStream = new FileOutputStream(tokens[2]);  // Crear un FileOutputStream para escribir el archivo

        byte[] buffer = new byte[4096];  // Crear un búfer para almacenar los bytes del archivo
        int bytesRead;  // Variable para almacenar la cantidad de bytes leídos
        long totalBytesRead = 0;  // Variable para almacenar el total de bytes leídos

        while (totalBytesRead < fileSize && (bytesRead = inputStream.read(buffer)) != -1) {  // Leer hasta que se reciba todo el archivo
            fileOutputStream.write(buffer, 0, bytesRead);  // Escribir los bytes en el archivo
            totalBytesRead += bytesRead;  // Actualizar la cantidad de bytes leídos
        }

        System.out.println("Archivo recibido y guardado.");  // Imprimir mensaje de confirmación
        fileOutputStream.close();  // Cerrar el archivo
        sendDocDM(tokens);  // Enviar el documento al destinatario privado
    }

    public void sendDocDM(String[] tokens) throws IOException {
        sendMsgDocDM(tokens);  // Enviar un mensaje notificando sobre el archivo privado
        try {
            if (tokens[1].startsWith("@")) {  // Comprobar si es un mensaje privado
                File file = new File(tokens[2]);  // Crear un archivo con el nombre especificado
                long fileSize = file.length();  // Obtener el tamaño del archivo

                for (Socket soc : vector) {  // Buscar el socket del destinatario
                    String destinatario = tokens[1].substring(1);  // Obtener el nombre del destinatario
                    HiloChatServer client = getClientByName(destinatario);  // Obtener el cliente por nombre
                    if (client != null && client.getSocket().equals(soc)) {
                        DataOutputStream netOutDoc = new DataOutputStream(soc.getOutputStream());  // Crear un flujo de salida
                        netOutDoc.writeLong(fileSize);  // Enviar el tamaño del archivo

                        FileInputStream fileInputStream = new FileInputStream(file);  // Crear un FileInputStream para leer el archivo
                        byte[] buffer = new byte[4096];  // Crear un búfer para almacenar los bytes del archivo
                        int bytesRead;  // Variable para almacenar la cantidad de bytes leídos

                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {  // Leer y enviar el archivo
                            netOutDoc.write(buffer, 0, bytesRead);  // Enviar los bytes leídos
                        }
                        System.out.println("Archivo enviado.");  // Imprimir mensaje de confirmación
                        fileInputStream.close();  // Cerrar el archivo
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();  // Manejar excepciones
        }
    }

    private boolean validateName(String name) {
        HiloChatServer client = getClientByName(name);
        return client != null;
    }

    private String options(String res, String[] tokens) throws IOException {
        switch (tokens[0]) {
            case "p":  // Caso para desconectar a un cliente
                res = "p^server:^";
                res += name + "^";
                removeUser(socket);  // Eliminar al cliente de las listas del servidor
                socket.close();  // Cerrar la conexión del cliente
                res += "left";  // Notificar que el cliente se ha desconectado
                break;
            case "j":  // Caso para unirse al servidor
                if (validateName(tokens[1])) {
                    res = "u^server:^";
                    res += name + "^";
                    res += "esta ocupado";  // Notificar que el cliente se ha unido
                    netOut = new DataOutputStream(socket.getOutputStream());
                    netOut.writeUTF(res);
                    res += "-1";
                } else {
                    name = tokens[1];  // Asignar el nombre del cliente
                    res = "j^server:^";
                    res += name + "^";
                    res += "joined";  // Notificar que el cliente se ha unido
                }
                break;
            case "m":  // Caso para un mensaje público
                res = "m^";
                res += name + ":^";
                res += tokens[1];  // Añadir el mensaje público
                break;
            case "dm":  // Caso para un mensaje privado
                sendMsgDM(tokens);  // Enviar el mensaje privado
                res = "-1";  // Retornar -1 para no enviar un mensaje adicional
                break;
            case "open":  // Caso para abrir un chat privado
                sendFlagDM(tokens);  // Enviar la señal para abrir el chat privado
                res = "-1";  // Retornar -1
                break;
            case "closeDM":  // Caso para cerrar un chat privado
                sendFlagCloseDM(tokens);  // Enviar la señal para cerrar el chat privado
                res = "-1";  // Retornar -1
                break;
            case "d":  // Caso para recibir un archivo público
                receiverDoc(tokens);  // Recibir el archivo
                res = "-1";  // Retornar -1
                break;
            case "ddm":  // Caso para recibir un archivo privado
                receiverDocDM(tokens);  // Recibir el archivo privado
                res = "-1";  // Retornar -1
                break;
            default:
                throw new AssertionError();  // Lanzar una excepción si el comando no es reconocido
        }
        return res;  // Retornar la respuesta
    }

    public void run() {
        try {
            initStreams();  // Inicializar los flujos de entrada/salida
            sendUserActive();  // Notificar los usuarios activos
            while (true) {
                String msg = netIn.readUTF();  // Leer el mensaje del cliente
                String[] tokens = msg.split("\\^");  // Dividir el mensaje en tokens
                String res = "server:^";  // Inicializar la respuesta
                res = options(res, tokens);  // Manejar las opciones según el mensaje
                if (!"-1".equals(res)) {  // Si la respuesta no es -1, enviar el mensaje a todos
                    sendMsg(res);  // Enviar el mensaje a todos los clientes
                }
            }
        } catch (IOException ioe) {
            ioe.getStackTrace();  // Manejar excepciones
        }
    }
}
