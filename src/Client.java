import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Client implements Serializable{
    private InetAddress activeIPAddress;
    private int activePortNumber = 3126;
    private DatagramSocket socketUDP;
    //TCP
    private ServerSocket serverSocket;
    private Socket connectionTCP;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean portsAreSetup = false;
    private ArrayList<Calculator> activeCalculators = new ArrayList<>();

    public Client() {}

    public void createUDPSocket() {
        try {
            socketUDP = new DatagramSocket();
            activeIPAddress = InetAddress.getByName("localhost");
            System.out.println("Client UDP socket created.");
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
            socketUDP.send(packetToSend);
            System.out.println("UDP packet sent.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createTcpSocket(){ //luo socketin TCP-keskustelua varten
        try{
            serverSocket = new ServerSocket(3200);
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
        do{
            try{
                message = (String) input.readObject();
                inputInterpreter(message);

            }catch (Exception e){
                e.printStackTrace();
            }
        }while (!message.equals(""))
    private void runSummingThreads(int n){
        for (int i = 3127; i <= 3127 + n; i++){ //luodaan portit 3127:(3127+n)
            activeCalculators.add(new Calculator(i));
            System.out.println("Summing threads alive.");
        }
    }
    private void inputInterpreter(String message) {
        //TODO viestin käsittely
        if (!portsAreSetup) { //jos portteja ei vielä avattu, käynnistä
            int portsAmount = Integer.parseInt(message);
            runSummingThreads(portsAmount);
            portsAreSetup = true;
            System.out.println("Portit auki");
        }
    }
}


