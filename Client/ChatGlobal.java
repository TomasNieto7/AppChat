
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

    private List<String> userList = new ArrayList<>();
    private List<JPanel> listPanels = new ArrayList<>();
    private JPanel titlePanel = new JPanel();
    final private Font mainFont = new Font("Arial", Font.BOLD, 18);
    private JTextField textMessage;
    private String message;
    private JPanel resPanel = new JPanel();
    private ClientServer clientServer;
    private String userName;
    private List<JFrame> dmsFrame;

    public ChatGlobal(ClientServer clientServer, String userName, List<JFrame> dmsFrame) {
        this.clientServer = clientServer;
        this.userName = userName;
        this.dmsFrame = dmsFrame;
    }

    public void initialize(String user) {
        JLabel lblFirstName = new JLabel("Chat Grupal");
        lblFirstName.setFont(mainFont);

        JTextField textMessage = new JTextField();
        textMessage.setFont(mainFont);
        textMessage.setPreferredSize(new Dimension(400, 40));

        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.PAGE_AXIS));
        titlePanel.setPreferredSize(new Dimension(400, 200));
        // resPanel.setBackground(Color.black);
        JScrollPane scrollPaneList = new JScrollPane(titlePanel);
        titlePanel.add(lblFirstName);

        JButton btnDoc = new JButton("Enviar Documento");
        btnDoc.setFont(mainFont);
        btnDoc.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    message = textMessage.getText();
                    clientServer.sendDoc(message);
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
                    if (message.length() != 0) {
                        clientServer.sendMessage(message);
                    } else JOptionPane.showMessageDialog(null, "Ups, el mensaje esta vacio", "Alerta", JOptionPane.WARNING_MESSAGE);
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
        mainPanel.add(scrollPaneList, BorderLayout.NORTH);
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
                    JFrame frameDM = clientServer.getDmFrameChat(dmsFrame, userName, destinatario + ":");
                    if (e.getClickCount() == 2 && !destinatario.equals(userName) && !destinatario.equals("server") && frameDM == null) {
                        clientServer.openDMFrame(name);
                        clientServer.sendOpenDM(destinatario, userName);
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
                        clientServer.openDMFrame(name);
                        clientServer.sendOpenDM(destinatario, userName);
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

        resPanelU = createResPanel(tokens[0], tokens[1]);
        resPanel.add(resPanelU);

        // Añadir el panel al contenedor principal
        // Actualizar la interfaz gráfica
        SwingUtilities.updateComponentTreeUI(resPanel);
    }

    public void deleteUserList(String message) {
        String[] tokens = message.split("\\^");
        String userDelete = null;
        for (String user : userList) {
            if (user.equals(tokens[2])) {
                userDelete = user;
            }
        }
        if (!userDelete.equals(null)) {
            userList.remove(userDelete);
            deleteUserPanelList(userDelete);
        }
    }

    public void deleteUserPanelList(String userDelete) {
        JPanel deletePanel = null;
        for (JPanel listPanel : listPanels) {
            // Obtén el segundo componente, que es nameUserButton
            Component secondComponent = listPanel.getComponent(1);

            // Verifica si es un JButton y obtén su texto
            if (secondComponent instanceof JButton) {
                JButton nameUserButton = (JButton) secondComponent;
                String userConnected = nameUserButton.getText();
                if (userConnected.equals(userDelete)) {
                    System.out.println("llego aqui");
                    deletePanel = listPanel;
                }
            }
        }
        if (deletePanel != null) {
            listPanels.remove(deletePanel);
            titlePanel.remove(deletePanel);
            SwingUtilities.updateComponentTreeUI(titlePanel);
        }

    }

    public void addUserList(String message) {
        String[] tokens = message.split("\\^");
        for (String user : userList) {
            if (user.equals(tokens[2])) {
                return;
            }
        }
        userList.add(tokens[2]);
        renderList(message);

    }

    public void renderList(String message) {
        String[] tokens = message.split("\\^");

        JPanel listPanel;

        listPanel = createListPanel("-", tokens[2], " is connected");
        titlePanel.add(listPanel);
        listPanels.add(listPanel);
        System.out.println("user added");

        SwingUtilities.updateComponentTreeUI(titlePanel);

    }

}
