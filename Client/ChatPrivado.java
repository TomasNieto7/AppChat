
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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

public class ChatPrivado {

    final private Font mainFont = new Font("Arial", Font.BOLD, 18); // Define la fuente principal que se usará en la interfaz (Arial, negrita, tamaño 18)
    private String message; // Variable para almacenar el mensaje a enviar
    private ClientServer clientServer; // Instancia que maneja la comunicación cliente-servidor
    private String userName; // Nombre del usuario actual
    private List<JFrame> dmsFrame; // Lista que contiene los JFrame de chats directos abiertos
    private List<JPanel> dmsPanel; // Lista que contiene los JPanel de los chats directos

    // Constructor que inicializa los atributos de la clase
    public ChatPrivado(ClientServer clientServer, String userName, List<JFrame> dmsFrame, List<JPanel> dmsPanel) {
        this.clientServer = clientServer; // Inicializa clientServer
        this.userName = userName; // Inicializa userName
        this.dmsFrame = dmsFrame; // Inicializa dmsFrame
        this.dmsPanel = dmsPanel; // Inicializa dmsPanel
    }

    // Método que crea un nuevo JFrame para chatear con un usuario específico
    public void dmFrame(String dmUser) throws IOException {
        // Crea un nuevo JFrame para el chat
        JFrame nuevoFrame = new JFrame("Nuevo Frame");

        // Crea una etiqueta que muestra con quién se está chateando
        JLabel lblFirstName = new JLabel("Chat privado con " + dmUser);
        lblFirstName.setFont(mainFont); // Aplica la fuente definida

        // Crea un campo de texto para ingresar mensajes
        JTextField textMessage = new JTextField();
        textMessage.setFont(mainFont); // Aplica la fuente definida
        textMessage.setPreferredSize(new Dimension(400, 40)); // Establece el tamaño preferido

        // Crea un panel para el título
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(4, 1, 5, 5)); // Establece el layout del panel
        titlePanel.add(lblFirstName); // Agrega la etiqueta al panel de título

        // Crea el botón para enviar documentos
        JButton btnDoc = new JButton("Enviar Documento");
        btnDoc.setFont(mainFont); // Aplica la fuente definida
        btnDoc.addActionListener(new ActionListener() { // Agrega un ActionListener para el botón
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText(); // Obtiene el mensaje del campo de texto
                    String destinatario = dmUser.replace(":", ""); // Limpia el destinatario
                    clientServer.sendDocDM("@" + destinatario + "^" + message); // Envía el documento
                    textMessage.setText(""); // Limpia el campo de texto
                } catch (IOException ioe) {
                    ioe.printStackTrace(); // Manejo de excepciones
                }
            }
        });

        // Crea el botón para enviar mensajes
        JButton btnOK = new JButton("Enviar");
        btnOK.setFont(mainFont); // Aplica la fuente definida
        btnOK.addActionListener(new ActionListener() { // Agrega un ActionListener para el botón
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText(); // Obtiene el mensaje del campo de texto
                    if (message.length() != 0) { // Verifica si el mensaje no está vacío
                        String destinatario = dmUser.replace(":", ""); // Limpia el destinatario
                        clientServer.sendMessageDM("@" + destinatario + "^", message); // Envía el mensaje
                    } else {
                        // Muestra un mensaje de alerta si el campo de texto está vacío
                        JOptionPane.showMessageDialog(null, "Ups, el mensaje esta vacio", "Alerta", JOptionPane.WARNING_MESSAGE);
                    }
                    textMessage.setText(""); // Limpia el campo de texto
                } catch (IOException ioe) {
                    ioe.printStackTrace(); // Manejo de excepciones
                }
            }
        });

        // Crea un panel para los botones
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BorderLayout()); // Establece el layout del panel
        buttonsPanel.add(textMessage, BorderLayout.CENTER); // Agrega el campo de texto al centro del panel

        // Crea un panel para envolver los botones
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Establece el layout
        buttonWrapper.add(btnDoc); // Agrega el botón de enviar documento
        buttonWrapper.add(btnOK); // Agrega el botón de enviar mensaje
        buttonsPanel.add(buttonWrapper, BorderLayout.SOUTH); // Agrega el panel de botones al sur

        // Crea un panel para las respuestas del chat
        JPanel resPanelDM = new JPanel();
        resPanelDM.setLayout(new BoxLayout(resPanelDM, BoxLayout.PAGE_AXIS)); // Establece el layout
        JScrollPane scrollPane = new JScrollPane(resPanelDM); // Agrega un JScrollPane para el panel de respuestas

        // Crea el panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout()); // Establece el layout del panel principal
        mainPanel.add(titlePanel, BorderLayout.NORTH); // Agrega el panel de título al norte
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Agrega el JScrollPane al centro
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH); // Agrega el panel de botones al sur

        nuevoFrame.add(mainPanel); // Agrega el panel principal al nuevo JFrame

        nuevoFrame.setTitle(userName + "-" + dmUser); // Establece el título del JFrame
        nuevoFrame.setSize(500, 500); // Establece el tamaño del JFrame
        nuevoFrame.setMinimumSize(new Dimension(300, 100)); // Establece el tamaño mínimo del JFrame
        // Configurar la operación de cierre personalizada
        nuevoFrame.addWindowListener(new WindowAdapter() { // Escucha el evento de cerrar la ventana
            @Override
            public void windowClosing(WindowEvent e) {
                // Acción personalizada antes de cerrar el frame
                int respuesta = JOptionPane.showConfirmDialog(null,
                        "¿Estás seguro de que deseas salir?", "Confirmar salida",
                        JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) { // Si el usuario confirma
                    // Si el usuario confirma, cerrar la aplicación
                    String title = nuevoFrame.getTitle(); // Obtiene el título del JFrame
                    String[] tokens = title.split("-"); // Divide el título para obtener emisor y destinatario
                    JFrame frameDM = getDmFrame(dmsFrame, tokens[0], tokens[1]); // Obtiene el JFrame del chat existente
                    dmsFrame.remove(frameDM); // Elimina el JFrame de la lista
                    clientServer.sendCloseDM(tokens[1], tokens[0]); // Informa al servidor que se cierra el chat
                    nuevoFrame.dispose(); // Cierra el JFrame
                }
                // Si no confirma, no hace nada y el frame permanece abierto
            }
        });
        // Muestra el nuevo frame
        nuevoFrame.setVisible(true); // Establece la visibilidad del JFrame
        dmsFrame.add(nuevoFrame); // Agrega el nuevo JFrame a la lista de frames
        dmsPanel.add(resPanelDM); // Agrega el JPanel de respuestas a la lista de panels
    }

    // Método que busca un JFrame de chat existente entre dos usuarios
    public JFrame getDmFrame(List<JFrame> frames, String emisor, String destinatario) {
        destinatario = destinatario.replace(":", ""); // Quita los : del destinatario
        for (JFrame jFrame : frames) { // Itera sobre la lista de JFrames
            String title = jFrame.getTitle(); // Obtiene el título del JFrame
            // Verifica si el título coincide con el emisor y destinatario
            if ((destinatario + "-" + emisor).equals(title) || (emisor + "-" + destinatario).equals(title)) {
                return jFrame; // Devuelve el JFrame si se encuentra
            }
        }
        return null; // Devuelve null si no se encuentra
    }

    // Método que renderiza la respuesta de un mensaje recibido
    public void renderResDM(String answer) {
        try {
            String[] tokens = answer.split("\\^"); // Divide la respuesta en tokens
            String msg = ""; // Inicializa el mensaje
            // Si el tipo de mensaje es ddm o contiene un punto, se asume que no está cifrado
            if (tokens[0].equals("ddm") || tokens[2].contains(".")) {
                msg = tokens[2]; // Se asigna el mensaje directamente
            } else {
                // Desencripta el mensaje si está cifrado
                msg = CifradoAES.desencriptar(tokens[2], CifradoAES.toSecretKey(tokens[3]));
            }
            String[] tokensEmisorDestinatario = tokens[1].split("-"); // Divide el segundo token para obtener emisor y destinatario
            JFrame frameDM = getDmFrame(dmsFrame, tokensEmisorDestinatario[0], tokensEmisorDestinatario[1]); // Obtiene el JFrame del chat

            if (frameDM != null) { // Verifica si se encontró el JFrame
                Component contentPane = frameDM.getContentPane().getComponent(0); // Obtiene el contenido del JFrame

                if (contentPane instanceof Container) { // Verifica si el contenido es un contenedor
                    Container container = (Container) contentPane; // Convierte a contenedor
                    Component[] components = container.getComponents(); // Obtiene los componentes del contenedor

                    JScrollPane scrollPane = (JScrollPane) components[1]; // Obtiene el JScrollPane
                    JPanel resPanelDM = (JPanel) scrollPane.getViewport().getView(); // Obtiene el JPanel de respuestas

                    // Crear un panel horizontal para mostrar la respuesta
                    JPanel resPanelU = new JPanel();
                    resPanelU.setLayout(new BoxLayout(resPanelU, BoxLayout.X_AXIS)); // Establece el layout
                    resPanelU.setAlignmentX(Component.LEFT_ALIGNMENT); // Alinea a la izquierda

                    // Crear el botón que muestra el nombre del emisor
                    JButton nameUserButton = new JButton(tokensEmisorDestinatario[0] + ":"); // Crea el botón con el nombre
                    nameUserButton.setFont(mainFont); // Aplica la fuente
                    // Elimina el diseño por defecto
                    nameUserButton.setBorderPainted(false);   // Quitar el borde
                    nameUserButton.setContentAreaFilled(false); // Quitar el fondo
                    nameUserButton.setFocusPainted(false);    // Quitar el efecto de foco
                    nameUserButton.setOpaque(false);          // Hacer el botón transparente
                    nameUserButton.setPreferredSize(new Dimension(100, 20)); // Establece el tamaño preferido

                    // Crear la etiqueta para mostrar el mensaje
                    JLabel resUserLb = new JLabel(msg); // Crea la etiqueta con el mensaje
                    resUserLb.setFont(mainFont); // Aplica la fuente
                    resUserLb.setPreferredSize(new Dimension(300, 20)); // Establece el tamaño preferido

                    // Establecer tamaño preferido del panel
                    resPanelU.setPreferredSize(new Dimension(400, 20)); // Establece el tamaño preferido

                    // Añadir el botón y la etiqueta al panel de respuesta
                    resPanelU.add(nameUserButton); // Agrega el botón con el nombre
                    resPanelU.add(resUserLb); // Agrega la etiqueta con el mensaje

                    // Añadir el panel de respuesta al contenedor principal
                    resPanelDM.add(resPanelU);

                    // Actualizar la interfaz gráfica
                    SwingUtilities.updateComponentTreeUI(resPanelDM); // Refresca el panel de respuestas
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Manejo de excepciones
        }
    }
}
