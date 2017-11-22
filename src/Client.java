import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client {
    private InetAddress activeIPAddress;
    private int activePortNumber = 3126;
    private DatagramSocket socketUDP;
    //TCP
    private ServerSocket serverSocket;
    private Socket connectionTCP;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean portsAreSetup = false;
    private ArrayList<Calculator> activeCalculators;

    public Client() {}

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
            serverSocket.setSoTimeout(5000);
            while (true){
                connectionTCP = serverSocket.accept();
                System.out.println("Forming connection with server.");
                output = new ObjectOutputStream(connectionTCP.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connectionTCP.getInputStream());
                System.out.println("Stream setup complete.");
                communicationPhase();
            }
        }catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }
    private void communicationPhase(){ //kun ollaan valmiita kuuntelemaan käskyjä
        System.out.println("Client listening for input.");
        String message = "";
        try{
            message = (String) input.readObject();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!portsAreSetup ){ //jos portteja ei vielä avattu, käynnistä
            int portsAmount = Integer.parseInt(message);
            runSummingThreads(portsAmount);
            portsAreSetup = true;
            System.out.println("Portit auki");
        }else {//TODO ota vastaan kyselyt palvelimelta
        }
    }
    private void runSummingThreads(int n){
        for (int i = 3127; i <= 3127 + n; i++){ //luodaan portteja lähtien 3127
            activeCalculators.add(new Calculator(i));
        }
    }
}


