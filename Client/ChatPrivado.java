
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

public class ChatPrivado {

    final private Font mainFont = new Font("Arial", Font.BOLD, 18);
    private String message;
    ClientServer clientServer;
    private String userName;
    private List<JFrame> dmsFrame;
    private List<JPanel> dmsPanel;

    public ChatPrivado(ClientServer clientServer, String userName, List<JFrame> dmsFrame, List<JPanel> dmsPanel) {
        this.clientServer = clientServer;
        this.userName = userName;
        this.dmsFrame = dmsFrame;
        this.dmsPanel = dmsPanel;
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
                    clientServer.sendDocDM("@" + destinatario + "^" + message);
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
                    clientServer.sendMessageDM("@" + destinatario + "^", message);
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
        // Configurar la operación de cierre personalizada
        nuevoFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Acción personalizada antes de cerrar el frame
                int respuesta = JOptionPane.showConfirmDialog(null,
                        "¿Estás seguro de que deseas salir?", "Confirmar salida",
                        JOptionPane.YES_NO_OPTION);

                if (respuesta == JOptionPane.YES_OPTION) {
                    // Si el usuario confirma, cerrar la aplicación
                    String title = nuevoFrame.getTitle();
                    String[] tokens = title.split("-");
                    JFrame frameDM = getDmFrame(dmsFrame, tokens[0], tokens[1]);
                    dmsFrame.remove(frameDM);
                    clientServer.sendCloseDM(tokens[1], tokens[0]);
                    nuevoFrame.dispose(); // Cierra el JFrame
                }
                // Si no confirma, no hace nada y el frame permanece abierto
            }
        });
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

        try {
            String[] tokens = answer.split("\\^");
            String msg = "";
            if (tokens[0].equals("ddm")) {
                msg = tokens[2];
            }else{
                msg = CifradoAES.desencriptar(tokens[2], CifradoAES.toSecretKey(tokens[3]));
            }
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
                    JLabel resUserLb = new JLabel(msg);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
