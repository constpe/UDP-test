import java.net.*;
import java.util.Scanner;
import java.util.Date;

public class Server {
    static void sendData(int lostPacketsAmount, long deliveryTime, DatagramSocket server, InetAddress senderIP, int senderPort) throws java.io.IOException{
        byte[] data = new byte[5];
        data[0] = (byte)(lostPacketsAmount & 255);
        data[1] = (byte)((lostPacketsAmount >> 8) & 255);
        data[2] = (byte)((lostPacketsAmount >> 16) & 255);
        data[3] = (byte)((lostPacketsAmount >> 24) & 255);
        data[4] = (byte)(deliveryTime / 1000);

        DatagramPacket sendingPacket = new DatagramPacket(data, data.length, senderIP, senderPort);
        server.send(sendingPacket);
    }

    public static void main(String[] args) {
        DatagramSocket server;

        try {
            Scanner reader = new Scanner(System.in);
            System.out.println("Enter server port: ");
            int port = reader.nextInt();
            System.out.println("Enter packet size: ");
            int packetSize = reader.nextInt();
            System.out.println("Server is working...");

            DatagramPacket receivedPacket = null;
            InetAddress senderIP = null;
            Date date;
            int senderPort = -1;
            server = new DatagramSocket(port);
            server.setSoTimeout(5000);

            while (true) {
                int receivedPacketAmount = 0;
                date = new Date();
                long startTime = date.getTime();
                long finalTime = date.getTime();

                try {
                    while (true) {
                        byte[] buffer = new byte[packetSize];
                        receivedPacket = new DatagramPacket(buffer, buffer.length);
                        server.receive(receivedPacket);

                        if (receivedPacket.getAddress() != null) {
                            senderIP = receivedPacket.getAddress();
                            senderPort = receivedPacket.getPort();
                        }

                        receivedPacketAmount += 1;
                        if (receivedPacketAmount == 1) {
                            date = new Date();
                            startTime = date.getTime();
                        }
                        date = new Date();
                        finalTime = date.getTime();
                    }
                } catch (SocketTimeoutException e) {}

                if (senderPort != -1) {
                    long deliveryTime = finalTime - startTime;
                    sendData(receivedPacketAmount, deliveryTime, server, senderIP, senderPort);
                    senderPort = -1;
                }
            }
        }
        catch (Exception e) {
            System.out.println("An error has occurred during server work\nServer stopped");
            e.printStackTrace();
        }
    }
}


