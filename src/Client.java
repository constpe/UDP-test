import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

public class Client {
    static long packetsAmount;
    static int packetSize;
    static DatagramSocket client;

    static void receiveInfo(DatagramPacket receivedPacket, JTextArea resultArea) {
        byte[] info = receivedPacket.getData();

        long receivedPacketsAmount = 0;

        for (int i = 0, byteOrder = 1; i < 4; i++, byteOrder *= 256) {
            receivedPacketsAmount += (info[i] & 0xff) * byteOrder;
        }

        String lostPercent = String.format("%.4f", (double)(packetsAmount - receivedPacketsAmount) / packetsAmount);

        String speed = "";
        int deliveryTime = (info[4] & 255);
        long size = (receivedPacketsAmount * packetSize * 8);
        if (deliveryTime != 0) {
            speed = String.format("%.4f", (double)(size / deliveryTime) / 1000000);
        }

        resultArea.setText("Packets delivered: " + receivedPacketsAmount + " of " + packetsAmount +
                " (" + lostPercent + "% lost)");
        if (!speed.equals(""))
            resultArea.setText(resultArea.getText() + "\nSpeed: " + speed + " Mbits/sec");
    }

    static class Receiver implements Runnable {
        Receiver(JFrame mainFrame, JTextArea resultArea) {
            Thread t = new Thread(this);
            this.mainFrame = mainFrame;
            this.resultArea = resultArea;
            t.start();
        }

        private JFrame mainFrame;
        private JTextArea resultArea;

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[5];
                    DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                    client.receive(receivedPacket);

                    receiveInfo(receivedPacket, resultArea);
                }
            }
            catch (IOException e) {
                JOptionPane.showMessageDialog(mainFrame, "Server connection error", "Connection error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static void initBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte)(i % 255);
        }
    }

    Client() {
        String lookandfeel = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(lookandfeel);
        }
        catch (Exception e) {}
        
        JFrame mainFrame = new JFrame("Lab 2 - UDP test");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(330, 240);
        mainFrame.setResizable(false);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        mainFrame.getContentPane().add(panel);

        JLabel amountLabel = new JLabel("Enter amount of packets");
        amountLabel.setBounds(5, 5, 150, 20);
        JLabel sizeLabel = new JLabel("Enter size of packets");
        sizeLabel.setBounds(5, 30, 150, 20);
        JLabel ipLabel = new JLabel("Enter server ip");
        ipLabel.setBounds(5, 55, 150, 20);
        JLabel portLabel = new JLabel("Enter server port");
        portLabel.setBounds(5, 80, 150, 20);
        JLabel resultLabel = new JLabel("Result");
        resultLabel.setBounds(140, 140, 40, 20);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField amountField = new JTextField(16);
        amountField.setBounds(160, 5, 160, 20);
        JTextField sizeField = new JTextField(16);
        sizeField.setBounds(160, 30, 160, 20);
        JTextField ipField = new JTextField(16);
        ipField.setBounds(160, 55, 160, 20);
        JTextField portField = new JTextField(5);
        portField.setBounds(160, 80, 160, 20);
        JButton sendButton = new JButton("Send");
        sendButton.setBounds(5, 110, 100, 20);

        JTextArea resultArea = new JTextArea();
        resultArea.setBounds(5, 165, 315, 40);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        resultArea.setEditable(false);

        panel.add(amountLabel);
        panel.add(sizeLabel);
        panel.add(ipLabel);
        panel.add(portLabel);
        panel.add(amountField);
        panel.add(sizeField);
        panel.add(ipField);
        panel.add(portField);
        panel.add(sendButton);
        panel.add(resultLabel);
        panel.add(resultArea);

        amountField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if ((keyEvent.getKeyChar() > '9' || keyEvent.getKeyChar() < '0') && keyEvent.getKeyChar() != 8)
                    keyEvent.consume();
                if (amountField.getText().length() > 6)
                    keyEvent.consume();
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        sizeField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if ((keyEvent.getKeyChar() > '9' || keyEvent.getKeyChar() < '0') && keyEvent.getKeyChar() != 8)
                    keyEvent.consume();
                if (sizeField.getText().length() > 4)
                    keyEvent.consume();
            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        portField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {

            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
                if ((keyEvent.getKeyChar() > '9' || keyEvent.getKeyChar() < '0') && keyEvent.getKeyChar() != 8)
                    keyEvent.consume();
                if (portField.getText().length() > 4)
                    keyEvent.consume();
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {

            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (amountField.getText().equals("") || sizeField.getText().equals("") || portField.getText().equals("") || ipField.getText().equals("")) {
                    JOptionPane.showMessageDialog(mainFrame, "Fields can't be empty", "Empty fields", JOptionPane.ERROR_MESSAGE);
                }
                else if (Integer.parseInt(sizeField.getText()) > 65308 ) {
                    JOptionPane.showMessageDialog(mainFrame, "Size of packet can't be more then 65308", "Incorrect value", JOptionPane.ERROR_MESSAGE);
                }
                else if (Integer.parseInt(portField.getText()) > 65535) {
                    JOptionPane.showMessageDialog(mainFrame, "Size of packet can't be more then 65535", "Incorrect value", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    try {
                        int port = Integer.parseInt(portField.getText());
                        packetsAmount = Integer.parseInt(amountField.getText());
                        packetSize = Integer.parseInt(sizeField.getText());
                        client = new DatagramSocket();

                        new Receiver(mainFrame, resultArea);

                        for (int i = 0; i < packetsAmount; i++) {
                            byte buffer[] = new byte[packetSize];
                            initBuffer(buffer);
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipField.getText()), port);
                            client.send(packet);

                        }
                    }
                    catch (IOException e) {
                        JOptionPane.showMessageDialog(mainFrame, "Server connection error", "Connection error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        mainFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });
    }
}
