
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChatServer {

    private List<Socket> vector = new ArrayList<>();
    private List<Thread> hilos = new ArrayList<>();
    private List<HiloChatServer> hilosChats = new ArrayList<>();
    private DataOutputStream netOut;

    public ChatServer(int port) {
        this.port = port;
    }

    public List<Thread> getHilos() {
        return hilos;
    }

    public List<HiloChatServer> getHilosChats() {
        return hilosChats;
    }

    public List<Socket> getVector() {
        return vector;
    }

    public void setVector(List<Socket> vector) {
        this.vector = vector;
    }

    public void setHilos(List<Thread> hilos) {
        this.hilos = hilos;
    }

    public void setHilosChats(List<HiloChatServer> hilosChats) {
        this.hilosChats = hilosChats;
    }

    private int port;

    private ServerSocket connect() {
        try {
            ServerSocket sSocket = new ServerSocket(port);
            return sSocket;
        } catch (IOException ioe) {
            System.out.println("No se pudo realizar la conexión");
        }
        return null;
    }

    public void principal() {
        ServerSocket sSocket = connect();
        if (sSocket != null) {
            try {
                // startSocketMonitor();
                // startSendingActivity();
                while (true) {
                    System.out.println("ChatServer abierto y esperando conexiones en puerto " + port);
                    Socket socket = sSocket.accept();
                    vector.add(socket);
                    System.out.println(vector.size());
                    HiloChatServer hiloChatServer = new HiloChatServer(socket, vector, this);
                    Thread hilo = new Thread(hiloChatServer);
                    hilos.add(hilo);
                    hilosChats.add(hiloChatServer);
                    hilo.start();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.err.println("No se pudo abrir el puerto");
        }
    }

    private void sendUserActive() throws IOException {
        String name;
        for (int i = 0; i < hilosChats.size(); i++) {
            HiloChatServer chat = hilosChats.get(i);
            boolean flag = isAlive(hilos.get(i));
            name = chat.getName();
            if (name != null && flag) {
                for (Socket soc : vector) {
                    netOut = new DataOutputStream(soc.getOutputStream());
                    netOut.writeUTF("server:^" + chat.getName() + " is connected");
                }
            }
        }
    }

    public void startSendingActivity() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    sendUserActive();
                } catch (IOException ioe) {
                    System.out.println("nadie esta conectado");
                    System.out.println(ioe);
                }
            }
        };

        // Ejecutar la tarea cada 30 segundos (30 * 1000 ms)
        timer.scheduleAtFixedRate(task, 0, 30 * 1000);
    }

    public void startSocketMonitor() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    socketMonitor();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };

        // Ejecutar la tarea cada 30 segundos (30 * 1000 ms)
        timer.scheduleAtFixedRate(task, 0, 100);
    }

    public void socketMonitor() throws IOException {
        List<Integer> indicesParaEliminar = new ArrayList<>();
        for (int i = 0; i < vector.size(); i++) {
            Socket socket = vector.get(i);
            if (socket.isClosed()) {
                indicesParaEliminar.add(port);
            }
        }
        removeUser(indicesParaEliminar);
    }

    public boolean isAlive(Thread hilo) {
        return hilo.isAlive();
    }

    public synchronized void removeUser(List<Integer> indicesParaEliminar) {
        // Eliminar los elementos marcados
        for (int i = indicesParaEliminar.size() - 1; i >= 0; i--) {
            int indice = indicesParaEliminar.get(i);
            if (indice >= 0 && indice < hilos.size() && indice < hilosChats.size() && indice < vector.size()) {
                hilos.remove(indice);
                hilosChats.remove(indice);
                vector.remove(indice);
            }
        }
    }

    public void removeUser2(Socket deleteSocket) {
        int indice = vector.indexOf(deleteSocket);
        if (indice >= 0 && indice < hilos.size() && indice < hilosChats.size() && indice < vector.size()) {
            hilos.remove(indice);
            hilosChats.remove(indice);
            vector.remove(indice);
        }
    }

    public static void main(String[] args) {
        ChatServer chat = new ChatServer(Integer.parseInt(args[0]));
        chat.principal();
    }

}
