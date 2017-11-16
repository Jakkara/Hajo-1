import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class UDPClient {
    private InetAddress activeIPAddress;
    private int activePortNumber = 3126;
    private DatagramSocket socketUDP;
    //TCP
    private ServerSocket serverSocket;
    private Socket connectionTCP;
    private ObjectOutputStream output;
    private ObjectInputStream input;


    public UDPClient() {
    }

    public void createUDPSocket() {
        try {
            socketUDP = new DatagramSocket();
            activeIPAddress = InetAddress.getByName("localhost");
        } catch (SocketException sE) {
            sE.printStackTrace();
        } catch (UnknownHostException uhE) {
            uhE.printStackTrace();
        }
    }

    public void sendUdpPacket(String message) {
        try {
            byte[] dataArrayToSend = message.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(dataArrayToSend, dataArrayToSend.length, activeIPAddress, activePortNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createTcpSocket(){ //luo socketit TCP-keskustelua varten
        try{
            serverSocket = new ServerSocket(activePortNumber);
            while (true){
                connectionTCP = serverSocket.accept();
                System.out.println("Forming connection with server.");
                output = new ObjectOutputStream(connectionTCP.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connectionTCP.getInputStream());
                System.out.println("Stream setup complete.");
            }
        }catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }
}


