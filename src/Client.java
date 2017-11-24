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

    public void createUDPSocket() {//luo lähettämistä varten UDP-soketti
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

    public void sendUdpPacket(String message) { //lähetä porttiosoitteen sisältävä paketti
        try {
            byte[] dataArrayToSend = message.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(dataArrayToSend, dataArrayToSend.length, activeIPAddress, activePortNumber);
            socketUDP.send(packetToSend);
            System.out.println("UDP packet sent.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void createTcpSocket(){ //luo socket TCP-keskustelua varten
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
    private void communicationPhase() { //kun ollaan valmiita kuuntelemaan käskyjä
        System.out.println("Client listening for input.");
        int message;
        do {
            try {
                message = input.readInt();
                inputInterpreter(message);      //käsittelee viestin

            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (true);
    }
    private void runSummingThreads(int n){
    for (int i = 3127; i <= 3127 + n; i++){         //luodaan portit 3127:(3127+n)
        activeCalculators.add(new Calculator(i));   //luodaan ja...
        activeCalculators.get(i).run();             //...käynnistetään oliot
        System.out.println("Summing threads alive.");
    }
    }
    private void inputInterpreter(int message) {     //käsittele saatu luku
        //TODO viestin käsittely
        if (!portsAreSetup) { //jos portteja ei vielä avattu, käynnistä
            runSummingThreads(receivedInt);
            portsAreSetup = true;
            System.out.println("Portit auki");
        }
        switch (receivedInt) {
		case 0:						//lopettaa summauspalvelijat
			for (Calculator sum : activeCalculators) {
				try {
					sum.kill();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			close();
			break;
			
		case 1:					//t?h?n menness? v?litettyjen lukujen summa
			int calcTotalValue = 0;
			for (int i=0; i<activeCalculators.size(); i++) {
				calcTotalValue += activeCalculators.get(i).getSum();
			}
			System.out.println("Kokonaissumma on " + sumServersTotalValue);
			return calcTotalValue;	
			break;
		

		case 2:					//mille palvelijalle välitetty summa suurin
			int greatestCalc = 0;
			for (int i = 0; i < activeCalculators.size(); i++) {
				if (greatestCalc < activeCalculators.get(i).getSum())
					greatestCalc = activeCalculators.get(i).getSum();
			}
			System.out.println("Palvelin, jolla suurin summa=" + greatestCalc); 
			break;
		
		case 3:					//kaikille palvelimille välitettyjen lukujen kokonaismäärä
            
			break;
			default:

			
		}

    }private void answerRequest(int n){ //välitä viesti takaisin palvelimelle
        try{
            output.writeInt(n);
            output.flush();
        }catch (IOException ioE){ioE.printStackTrace();
        }
    }
}


