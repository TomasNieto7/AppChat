
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

public class MainFrame extends JFrame {

    final private Font mainFont = new Font("Arial", Font.BOLD, 18);
    JTextField textMessage;
    private String message;
    JPanel resPanel = new JPanel();

    private Socket socket;
    private DataInputStream input;
    private DataOutputStream output;
    private String userName;
    private List<JFrame> dmsFrame = new ArrayList<>();
    private List<JPanel> dmsPanel = new ArrayList<>();

    public MainFrame(Socket socket) throws IOException {
        this.socket = socket;
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    public void openDM(String name) throws IOException {
        dmFrame(name);
    }

    public void sendOpenDM(String destinatario, String emisor) throws IOException {
        output.writeUTF("open^@" + destinatario + "^" + emisor);
    }

    public JPanel createListPanel(String server, String nameUser, String msg) {
        // Crear un panel horizontal
        JPanel resPanelU = new JPanel();
        resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS));
        // Alineación a la izquierda del contenido
        resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Crear el label a la derecha
        JLabel serverLB = new JLabel(server);
        serverLB.setFont(mainFont);
        serverLB.setPreferredSize(new Dimension(100, 20));

        // Crear el botón a la izquierda
        JButton nameUserButton = new JButton(nameUser);
        nameUserButton.setFont(mainFont);
        // Elimina el diseño por defecto
        nameUserButton.setBorderPainted(false);   // Quitar el borde
        nameUserButton.setContentAreaFilled(false); // Quitar el fondo
        nameUserButton.setFocusPainted(false);    // Quitar el efecto de foco
        nameUserButton.setOpaque(false);          // Hacer el botón transparente

        // Agregar MouseListener para capturar eventos del ratón
        nameUserButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Detecta si es doble clic
                try {
                    String name = nameUserButton.getText();
                    String destinatario = name.replace(":", "");
                    if (e.getClickCount() == 2 && !destinatario.equals(userName) && !destinatario.equals("server")) {
                        dmFrame(name);
                        sendOpenDM(destinatario, userName);
                    }
                } catch (IOException ioe) {
                    ioe.getStackTrace();
                }

            }
        });

        // Crear el label a la derecha
        JLabel msgLB = new JLabel(msg);
        msgLB.setFont(mainFont);

        // Establecer tamaño preferido y fondo del panel
        resPanelU.setPreferredSize(new Dimension(1000, 20));
        resPanelU.setBackground(Color.GRAY);
        // resPanelU.setBackground(new Color(135, 206, 250));

        // Añadir los componentes al panel (botón a la izquierda, label a la derecha)
        resPanelU.add(serverLB);
        resPanelU.add(nameUserButton);
        resPanelU.add(msgLB);
        return resPanelU;
    }

    public JPanel createResPanel(String nameUser, String msg) {
        // Crear un panel horizontal
        JPanel resPanelU = new JPanel();
        resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS));
        // Alineación a la izquierda del contenido
        resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Crear el botón a la izquierda
        JButton nameUserButton = new JButton(nameUser);
        nameUserButton.setFont(mainFont);
        // Elimina el diseño por defecto
        nameUserButton.setBorderPainted(false);   // Quitar el borde
        nameUserButton.setContentAreaFilled(false); // Quitar el fondo
        nameUserButton.setFocusPainted(false);    // Quitar el efecto de foco
        nameUserButton.setOpaque(false);          // Hacer el botón transparente
        nameUserButton.setPreferredSize(new Dimension(100, 20));

        // Agregar MouseListener para capturar eventos del ratón
        nameUserButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Detecta si es doble clic
                try {
                    String name = nameUserButton.getText();
                    String destinatario = name.replace(":", "");
                    if (e.getClickCount() == 2 && !destinatario.equals(userName) && !destinatario.equals("server")) {
                        dmFrame(name);
                        sendOpenDM(destinatario, userName);
                    }
                } catch (IOException ioe) {
                    ioe.getStackTrace();
                }

            }
        });

        // Crear el label a la derecha
        JLabel resUserLb = new JLabel(msg);
        resUserLb.setFont(mainFont);
        resUserLb.setPreferredSize(new Dimension(300, 20));

        // Establecer tamaño preferido y fondo del panel
        resPanelU.setPreferredSize(new Dimension(400, 20));
        // resPanelU.setBackground(new Color(135, 206, 250));

        // Añadir los componentes al panel (botón a la izquierda, label a la derecha)
        resPanelU.add(nameUserButton);
        resPanelU.add(resUserLb);
        return resPanelU;
    }

    public void renderRes(String answer) {

        String[] tokens = answer.split("\\^");

        JPanel resPanelU;

        if (tokens[0].equals("l")) {
            resPanelU = createListPanel(tokens[1], tokens[2], tokens[3]);
            resPanel.add(resPanelU);
        } else {
            resPanelU = createResPanel(tokens[0], tokens[1]);
            resPanel.add(resPanelU);
        }

        // Añadir el panel al contenedor principal
        // Actualizar la interfaz gráfica
        SwingUtilities.updateComponentTreeUI(resPanel);
    }

    public void dmFrame(String dmUser) throws IOException {
        // Crea un nuevo JFrame
        JFrame nuevoFrame = new JFrame("Nuevo Frame");

        JLabel lblFirstName = new JLabel("Chat privado con " + dmUser);
        lblFirstName.setFont(mainFont);

        JTextField textMessage = new JTextField();
        textMessage.setFont(mainFont);
        textMessage.setPreferredSize(new Dimension(400, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(4, 1, 5, 5));
        titlePanel.add(lblFirstName);

        JButton btnDoc = new JButton("Enviar Documento");
        btnDoc.setFont(mainFont);
        btnDoc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText();
                    String destinatario = dmUser.replace(":", "");
                    sendDocDM("@" + destinatario + "^" + message);
                    textMessage.setText("");
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });

        JButton btnOK = new JButton("Enviar");
        btnOK.setFont(mainFont);
        btnOK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText();
                    String destinatario = dmUser.replace(":", "");
                    sendMessageDM("@" + destinatario + "^" + message);
                    textMessage.setText("");
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(textMessage, BorderLayout.CENTER);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(btnDoc);
        buttonWrapper.add(btnOK);
        buttonsPanel.add(buttonWrapper, BorderLayout.SOUTH);

        JPanel resPanelDM = new JPanel();
        resPanelDM.setLayout(new BoxLayout(resPanelDM, BoxLayout.PAGE_AXIS));
        // resPanel.setBackground(Color.black);
        JScrollPane scrollPane = new JScrollPane(resPanelDM);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        nuevoFrame.add(mainPanel);

        nuevoFrame.setTitle(userName + "-" + dmUser);
        nuevoFrame.setSize(500, 500);
        nuevoFrame.setMinimumSize(new Dimension(300, 100));
        nuevoFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo el nuevo frame
        // Muestra el nuevo frame
        nuevoFrame.setVisible(true);
        dmsFrame.add(nuevoFrame);
        dmsPanel.add(resPanelDM);
    }

    public JFrame getDmFrame(List<JFrame> frames, String emisor, String destinatario) {
        destinatario = destinatario.replace(":", "");
        for (JFrame jFrame : frames) {
            String title = jFrame.getTitle();
            if ((destinatario + "-" + emisor).equals(title) || (emisor + "-" + destinatario).equals(title)) {
                return jFrame;
            }
        }
        return null;
    }

    public void renderResDM(String answer) {

        String[] tokens = answer.split("\\^");
        String[] tokensEmisorDestinatario = tokens[1].split("-");
        JFrame frameDM = getDmFrame(dmsFrame, tokensEmisorDestinatario[0], tokensEmisorDestinatario[1]);
        if (frameDM != null) {

            Component contentPane = frameDM.getContentPane().getComponent(0);

            if (contentPane instanceof Container) {
                Container container = (Container) contentPane;
                Component[] components = container.getComponents();

                JScrollPane scrollPane = (JScrollPane) components[1]; // Obtienes el scrollPane
                JPanel resPanelDM = (JPanel) scrollPane.getViewport().getView(); // Obtienes el resPanelDM desde el scrollPane
                // Crear un panel horizontal
                JPanel resPanelU = new JPanel();
                resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS));
                // Alineación a la izquierda del contenido
                resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT);

                // Crear el botón a la izquierda
                JButton nameUserButton = new JButton(tokensEmisorDestinatario[0] + ":");
                nameUserButton.setFont(mainFont);
                // Elimina el diseño por defecto
                nameUserButton.setBorderPainted(false);   // Quitar el borde
                nameUserButton.setContentAreaFilled(false); // Quitar el fondo
                nameUserButton.setFocusPainted(false);    // Quitar el efecto de foco
                nameUserButton.setOpaque(false);          // Hacer el botón transparente
                nameUserButton.setPreferredSize(new Dimension(100, 20));

                // Crear el label a la derecha
                JLabel resUserLb = new JLabel(tokens[2]);
                resUserLb.setFont(mainFont);
                resUserLb.setPreferredSize(new Dimension(300, 20));

                // Establecer tamaño preferido y fondo del panel
                resPanelU.setPreferredSize(new Dimension(400, 20));
                // resPanelU.setBackground(new Color(135, 206, 250));

                // Añadir los componentes al panel (botón a la izquierda, label a la derecha)
                resPanelU.add(nameUserButton);
                resPanelU.add(resUserLb);

                // Añadir el panel al contenedor principal
                resPanelDM.add(resPanelU);

                // Actualizar la interfaz gráfica
                SwingUtilities.updateComponentTreeUI(resPanelDM);
            } else {
                System.out.println("El componente no es un contenedor.");
            }

        }

    }

    public void initialize(String user) {
        JLabel lblFirstName = new JLabel("Chat Grupal");
        lblFirstName.setFont(mainFont);

        JTextField textMessage = new JTextField();
        textMessage.setFont(mainFont);
        textMessage.setPreferredSize(new Dimension(400, 40));

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(4, 1, 5, 5));
        titlePanel.add(lblFirstName);

        JButton btnDoc = new JButton("Enviar Documento");
        btnDoc.setFont(mainFont);
        btnDoc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText();
                    sendDoc(message);
                    textMessage.setText("");
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });

        JButton btnOK = new JButton("Enviar");
        btnOK.setFont(mainFont);
        btnOK.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText();
                    sendMessage(message);
                    textMessage.setText("");
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(textMessage, BorderLayout.CENTER);

        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(btnDoc);
        buttonWrapper.add(btnOK);
        buttonsPanel.add(buttonWrapper, BorderLayout.SOUTH);

        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.PAGE_AXIS));
        // resPanel.setBackground(Color.black);
        JScrollPane scrollPane = new JScrollPane(resPanel);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setTitle("Welcome " + user);
        setSize(500, 500);
        setMinimumSize(new Dimension(300, 100));
        // Configurar la operación de cierre personalizada
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Acción personalizada antes de cerrar el frame
                int respuesta = JOptionPane.showConfirmDialog(null,
                        "¿Estás seguro de que deseas salir?", "Confirmar salida",
                        JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario confirma, cerrar la aplicación
                    dispose(); // Cierra el JFrame
                    System.exit(0); // Termina el programa
                }
                // Si no confirma, no hace nada y el frame permanece abierto
            }
        });
        setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
        MainFrame myFrame = new MainFrame(socket);
        myFrame.running(args);
    }

    //------------------------------------------------------------
    public void listenForMessages() {
        Thread messageListener = new Thread(() -> {
            try {
                while (true) {
                    String messageFromServer = input.readUTF();
                    System.out.println(messageFromServer);
                    String[] tokens = messageFromServer.split("\\^");
                    if (messageFromServer.startsWith("dm")) {
                        renderResDM(messageFromServer);
                    } else if (messageFromServer.startsWith("openDM")) {
                        openDM(tokens[1]);
                    } else if (tokens[0].equals("d")) {
                        receiverDoc(tokens);
                    } else if (tokens[0].equals("ddm")) {
                        receiverDocDM(tokens);
                    } else {
                        renderRes(messageFromServer);
                    }

                }
            } catch (IOException ioe) {
                ioe.getStackTrace();
            }
        });
        messageListener.start();
    }

    public void connectedClient(String host, int port, String name) {
        try {
            System.out.println("Conectado al servidor en: " + port);
            output.writeUTF("j^" + name);
            userName = name;
            initialize(userName);
            listenForMessages();
        } catch (IOException ioe) {
            System.out.println("Error al conectar con el servidor: " + ioe.getMessage());
        }
    }

    public void sendMessage(String msg) throws IOException {
        output.writeUTF("m^" + msg);
    }

    public void sendDisconnection() throws IOException {
        output.writeUTF("p^");
    }

    public void sendMessageDM(String msg) throws IOException {
        output.writeUTF("dm^" + msg);
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
        renderRes(tokens[1] + "^" + tokens[2]);
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

        renderResDM(tokens[0] + "^" + tokens[1] + "^" + tokens[2]);
        fileOutputStream.close();
    }

    public void running(String[] args) {
        int port = Integer.parseInt(args[1]);
        String user = args[2];
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

}
