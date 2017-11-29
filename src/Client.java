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
    private String offeredPort = "";
    //TCP
    private ServerSocket serverSocket;
    private Socket connectionTCP;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean portsAreSetup = false;
    private ArrayList<Calculator> activeCalculators = new ArrayList<>(3140);

    public Client() {}

    public void createUDPSocket() {//luo l�hett�mist� varten UDP-soketti
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

    public void sendUdpPacket(String message) { //l�het� porttiosoitteen sis�lt�v� paketti
        offeredPort = message;
        try {
            byte[] dataArrayToSend = message.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(dataArrayToSend, dataArrayToSend.length, activeIPAddress, activePortNumber);
            socketUDP.send(packetToSend);
            System.out.println("UDP-paketti l�hetetty.");
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
                System.out.println("Muodostetaan TCP-yhteys serveriin.");
                output = new ObjectOutputStream(connectionTCP.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connectionTCP.getInputStream());
                System.out.println("Stream serveriin aktiivinen.");
                communicationPhase();
            }
        }catch (IOException ioE) {
            ioE.printStackTrace();
            sendUdpPacket(offeredPort); //jos timeout, l�het� uudestaan
        }
    }
    private void communicationPhase() { //kun ollaan valmiita kuuntelemaan k�skyj�
        System.out.println("Client kuuntelee viesti�.");
        int message;
        do {
            try {
                message = input.readInt();
                System.out.println("Server l�hetti luvun : " + message);
                inputInterpreter(message);      //k�sittelee viestin

            } catch (Exception e) {
                e.printStackTrace();
                try{
                    answerRequest(-1); //v�lit� serverille tieto ett� ei saatu t
                    inputInterpreter(0);        //sulje hallitusti
                }catch (IOException ioE){}
                }
        }while (true);
    }
    private void runSummingThreads(int n){
        System.out.println("Pekka. " + n);
        for (int i = 0; i <= n; i++){         //luodaan portit 3127:(3127+n)
            activeCalculators.add(new Calculator(i + 3127));   //luodaan ja...
            activeCalculators.get(i).run();             //...k�ynnistet��n oliot
            System.out.println("P�tk�.");
            answerRequest(activeCalculators.get(i).getPort());
            System.out.println("Summauss�ikeet k�ynnistetty.");
        }
    }
    private void inputInterpreter(int receivedInt) throws IOException {     //k�sittele saatu luku
        //TODO viestin k�sittely
        if (!portsAreSetup) { //jos portteja ei viel� avattu, k�ynnist�
            runSummingThreads(receivedInt);
            portsAreSetup = true;
            System.out.println("Portit auki");
        }
        switch (receivedInt) {
		case 0:						//lopettaa summauspalvelijat ja sulkee yhteydet
			for (Calculator calc : activeCalculators) {
					calc.kill(); 
			}
			output.close();
			input.close();
			connectionTCP.close();
			System.out.println("Palvelijat lopetettu ja yhteydet suljettu.");
			break;
			
		case 1:					//t?h?n menness? v?litettyjen lukujen summa
			int calcTotalValue = 0;
			for (int i=0; i<activeCalculators.size(); i++) {
				calcTotalValue += activeCalculators.get(i).getSum();
			}
			System.out.println("Kokonaissumma on " + calcTotalValue);
			answerRequest(calcTotalValue);	
			break;
		

		case 2:					//mille palvelijalle v�litetty summa suurin
			int greatestCalc = 0;
			for (int i = 0; i < activeCalculators.size(); i++) {
				if (greatestCalc < activeCalculators.get(i).getSum())
					greatestCalc = activeCalculators.get(i).getPort();
			}
			System.out.println("Palvelin, jolla suurin summa=" + greatestCalc);
			answerRequest(greatestCalc);
			break;
		
		case 3:					//kaikille palvelimille v�litettyjen lukujen kokonaism��r�
			int numbersReceived=0;
			for(int i=0; i < activeCalculators.size(); i++) {
				numbersReceived += activeCalculators.get(i).getNumbersReceivedAmount();
			}
			System.out.println("V�litettyjen lukujen kokonaism��r� on " + numbersReceived);
			answerRequest(numbersReceived);
			break;
			
		default:				//vastaa takaisin luvulla -1, jos ei mik��n edellisist� tapauksista
			answerRequest(-1);
		}

    }private void answerRequest(int n){ //v�lit� viesti takaisin palvelimelle
        try{
            output.writeInt(n);
            output.flush();
        }catch (IOException ioE){ioE.printStackTrace();
        }
    }
}


