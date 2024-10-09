
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatGlobal extends JFrame {

    private List<String> userList = new ArrayList<>(); // Lista de usuarios conectados
    private List<JPanel> listPanels = new ArrayList<>(); // Lista de paneles de usuarios
    private JPanel titlePanel = new JPanel(); // Panel para el título
    final private Font mainFont = new Font("Arial", Font.BOLD, 18); // Fuente principal para la interfaz
    private JTextField textMessage; // Campo de texto para el mensaje
    private String message; // Mensaje que se va a enviar
    private JPanel resPanel = new JPanel(); // Panel para mostrar las respuestas
    private ClientServer clientServer; // Instancia del cliente/servidor para la comunicación
    private String userName; // Nombre del usuario
    private List<JFrame> dmsFrame; // Lista de ventanas de mensajes directos

    // Constructor de la clase
    public ChatGlobal(ClientServer clientServer, String userName, List<JFrame> dmsFrame) {
        this.clientServer = clientServer; // Inicializa la instancia de ClientServer
        this.userName = userName; // Inicializa el nombre del usuario
        this.dmsFrame = dmsFrame; // Inicializa la lista de ventanas de DM
    }

    // Método para inicializar la interfaz del chat
    public void initialize(String user) {
        // Crear etiqueta del título
        JLabel lblFirstName = new JLabel("Chat Grupal");
        lblFirstName.setFont(mainFont);
        // Campo de texto para mensajes
        textMessage = new JTextField();
        textMessage.setFont(mainFont);
        textMessage.setPreferredSize(new Dimension(400, 40));
        // Configurar panel del título
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
        titlePanel.setPreferredSize(new Dimension(400, 200));
        JScrollPane scrollPaneList = new JScrollPane(titlePanel); // Panel con scroll
        titlePanel.add(lblFirstName); // Agregar la etiqueta al panel del título
        // Botón para enviar documentos
        JButton btnDoc = new JButton("Enviar Documento");
        btnDoc.setFont(mainFont);
        btnDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText(); // Obtener el mensaje del campo de texto
                    clientServer.sendDoc(message); // Enviar el documento
                    textMessage.setText(""); // Limpiar el campo de texto
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });
        // Botón para enviar mensajes
        JButton btnOK = new JButton("Enviar");
        btnOK.setFont(mainFont);
        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText(); // Obtener el mensaje del campo de texto
                    if (message.length() != 0) { // Verificar que el mensaje no esté vacío
                        clientServer.sendMessage(message); // Enviar el mensaje
                    } else {
                        // Mostrar alerta si el mensaje está vacío
                        JOptionPane.showMessageDialog(null, "Ups, el mensaje está vacío", "Alerta", JOptionPane.WARNING_MESSAGE);
                    }
                    textMessage.setText(""); // Limpiar el campo de texto
                } catch (IOException ioe) {
                    System.out.println("Error al enviar mensaje: " + ioe.getMessage());
                }
            }
        });
        // Panel para los botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(textMessage, BorderLayout.CENTER); // Agregar campo de texto al panel de botones
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.add(btnDoc); // Agregar botón de documento
        buttonWrapper.add(btnOK); // Agregar botón de envío
        buttonsPanel.add(buttonWrapper, BorderLayout.SOUTH); // Agregar botones al panel de botones
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.PAGE_AXIS)); // Configurar panel de respuestas
        JScrollPane scrollPane = new JScrollPane(resPanel); // Panel de respuestas con scroll
        // Crear panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(scrollPaneList, BorderLayout.NORTH); // Agregar panel de título
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Agregar panel de respuestas
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH); // Agregar panel de botones
        add(mainPanel); // Agregar panel principal a la ventana
        // Configurar la ventana
        setTitle("Welcome " + user); // Título de la ventana
        setSize(500, 500); // Tamaño de la ventana
        setMinimumSize(new Dimension(300, 100)); // Tamaño mínimo de la ventana
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
        setVisible(true); // Hacer visible la ventana
    }

    // Método para crear un panel que muestra la lista de usuarios
    public JPanel createListPanel(String server, String nameUser, String msg) {
        JPanel resPanelU = new JPanel(); // Crear un panel horizontal
        resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS)); // Configurar el layout
        resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT); // Alineación a la izquierda
        // Crear el label para el servidor
        JLabel serverLB = new JLabel(server);
        serverLB.setFont(mainFont);
        serverLB.setPreferredSize(new Dimension(100, 20));
        // Crear el botón para el nombre de usuario
        JButton nameUserButton = new JButton(nameUser);
        nameUserButton.setFont(mainFont);
        nameUserButton.setBorderPainted(false); // Quitar el borde
        nameUserButton.setContentAreaFilled(false); // Quitar el fondo
        nameUserButton.setFocusPainted(false); // Quitar el efecto de foco
        nameUserButton.setOpaque(false); // Hacer el botón transparente
        // Agregar MouseListener para el botón de nombre de usuario
        nameUserButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Detectar si es un doble clic
                try {
                    String name = nameUserButton.getText(); // Obtener el texto del botón
                    String destinatario = name.replace(":", ""); // Quitar ":" del nombre
                    JFrame frameDM = clientServer.getDmFrameChat(dmsFrame, userName, destinatario + ":"); // Obtener ventana de DM
                    if (e.getClickCount() == 2 && !destinatario.equals(userName) && !destinatario.equals("server") && frameDM == null) {
                        clientServer.openDMFrame(name); // Abrir ventana de DM
                        clientServer.sendOpenDM(destinatario, userName); // Enviar mensaje para abrir DM
                    }
                } catch (IOException ioe) {
                    ioe.getStackTrace(); // Manejo de excepciones
                }
            }
        });
        // Crear el label para el mensaje
        JLabel msgLB = new JLabel(msg);
        msgLB.setFont(mainFont);
        // Establecer tamaño preferido y fondo del panel
        resPanelU.setPreferredSize(new Dimension(1000, 20));
        resPanelU.setBackground(Color.GRAY); // Establecer color de fondo
        // Añadir los componentes al panel (botón a la izquierda, label a la derecha)
        resPanelU.add(serverLB);
        resPanelU.add(nameUserButton);
        resPanelU.add(msgLB);
        return resPanelU; // Retornar el panel creado
    }

    // Método para crear un panel para respuestas de usuarios
    public JPanel createResPanel(String nameUser, String msg) {
        JPanel resPanelU = new JPanel(); // Crear un panel horizontal
        resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS)); // Configurar el layout
        resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT); // Alineación a la izquierda
        // Crear el botón para el nombre de usuario
        JButton nameUserButton = new JButton(nameUser);
        nameUserButton.setFont(mainFont);
        nameUserButton.setBorderPainted(false); // Quitar el borde
        nameUserButton.setContentAreaFilled(false); // Quitar el fondo
        nameUserButton.setFocusPainted(false); // Quitar el efecto de foco
        nameUserButton.setOpaque(false); // Hacer el botón transparente
        nameUserButton.setPreferredSize(new Dimension(100, 20)); // Establecer tamaño preferido

        // Agregar MouseListener para el botón de nombre de usuario
        nameUserButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Detectar si es un doble clic
                try {
                    String name = nameUserButton.getText(); // Obtener el texto del botón
                    String destinatario = name.replace(":", ""); // Quitar ":" del nombre
                    if (e.getClickCount() == 2 && !destinatario.equals(userName) && !destinatario.equals("server")) {
                        clientServer.openDMFrame(name); // Abrir ventana de DM
                        clientServer.sendOpenDM(destinatario, userName); // Enviar mensaje para abrir DM
                    }
                } catch (IOException ioe) {
                    ioe.getStackTrace(); // Manejo de excepciones
                }
            }
        });
        // Crear el label para el mensaje
        JLabel resUserLb = new JLabel(msg);
        resUserLb.setFont(mainFont);
        resUserLb.setPreferredSize(new Dimension(300, 20)); // Establecer tamaño preferido
        // Establecer tamaño preferido del panel
        resPanelU.setPreferredSize(new Dimension(400, 20));
        // Añadir los componentes al panel (botón a la izquierda, label a la derecha)
        resPanelU.add(nameUserButton);
        resPanelU.add(resUserLb);
        return resPanelU; // Retornar el panel creado
    }

    // Método para renderizar las respuestas en el panel
    public void renderRes(String answer) {
        String[] tokens = answer.split("\\^"); // Dividir la respuesta en tokens
        JPanel resPanelU;
        resPanelU = createResPanel(tokens[0], tokens[1]); // Crear panel de respuesta
        resPanel.add(resPanelU); // Añadir el panel de respuesta
        // Actualizar la interfaz gráfica
        SwingUtilities.updateComponentTreeUI(resPanel);
    }

    // Método para eliminar un usuario de la lista
    public void deleteUserList(String message) {
        String[] tokens = message.split("\\^"); // Dividir el mensaje en tokens
        String userDelete = null; // Usuario a eliminar
        for (String user : userList) {
            if (user.equals(tokens[2])) {
                userDelete = user; // Encontrar el usuario a eliminar
            }
        }
        if (userDelete != null) {
            userList.remove(userDelete); // Eliminar usuario de la lista
            deleteUserPanelList(userDelete); // Eliminar panel del usuario
        }
    }

    // Método para eliminar el panel de un usuario específico
    public void deleteUserPanelList(String userDelete) {
        JPanel deletePanel = null; // Panel a eliminar
        for (JPanel listPanel : listPanels) {
            // Obtén el segundo componente, que es nameUserButton
            Component secondComponent = listPanel.getComponent(1);
            // Verifica si es un JButton y obtén su texto
            if (secondComponent instanceof JButton) {
                JButton nameUserButton = (JButton) secondComponent;
                String userConnected = nameUserButton.getText(); // Obtener el texto del botón
                if (userConnected.equals(userDelete)) {
                    deletePanel = listPanel; // Encontrar el panel del usuario a eliminar
                }
            }
        }
        if (deletePanel != null) {
            listPanels.remove(deletePanel); // Eliminar el panel de la lista
            titlePanel.remove(deletePanel); // Eliminar el panel del título
            SwingUtilities.updateComponentTreeUI(titlePanel); // Actualizar interfaz gráfica
        }
    }

    // Método para añadir un usuario a la lista
    public void addUserList(String message) {
        String[] tokens = message.split("\\^"); // Dividir el mensaje en tokens
        for (String user : userList) {
            if (user.equals(tokens[2])) {
                return; // Si el usuario ya está en la lista, salir del método
            }
        }
        userList.add(tokens[2]); // Añadir usuario a la lista
        renderList(message); // Renderizar la lista actualizada
    }

    // Método para renderizar la lista de usuarios
    public void renderList(String message) {
        String[] tokens = message.split("\\^"); // Dividir el mensaje en tokens
        JPanel listPanel;
        listPanel = createListPanel("-", tokens[2], " is connected"); // Crear panel para el usuario
        titlePanel.add(listPanel); // Añadir panel al título
        listPanels.add(listPanel); // Añadir panel a la lista de paneles
        SwingUtilities.updateComponentTreeUI(titlePanel); // Actualizar interfaz gráfica
    }
}
