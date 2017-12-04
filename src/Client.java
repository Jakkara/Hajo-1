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

    public void createUDPSocket() {//luo lähettämistä varten UDP-soketti
        try {
            socketUDP = new DatagramSocket();
            activeIPAddress = InetAddress.getByName("localhost");
            System.out.println("UDP-soketti luotu.");
        } catch (SocketException sE) {
            sE.printStackTrace();
        } catch (UnknownHostException uhE) {
            uhE.printStackTrace();
        }
    }

    public void sendUdpPacket(String message) { //lähetä porttiosoitteen sisältävä paketti
        offeredPort = message;
        try {
            byte[] dataArrayToSend = message.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(dataArrayToSend, dataArrayToSend.length, activeIPAddress, activePortNumber);
            socketUDP.send(packetToSend);
            System.out.println("UDP-paketti lähetetty.");
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
            sendUdpPacket(offeredPort); //jos timeout, lähetä uudestaan
        }
    }
    private void communicationPhase() { //kun ollaan valmiita kuuntelemaan käskyjä
        System.out.println("Client kuuntelee viestiä.");
        int message;
        do {
            try {
                message = input.readInt();
                System.out.println("Server lähetti luvun : " + message);
                inputInterpreter(message);      //käsittelee viestin

            } catch (Exception e) {
                /*e.printStackTrace();
                try {
                    answerRequest(-1); //välitä serverille tieto että ei saatu t
                    inputInterpreter(0);        //sulje hallitusti
                } catch (IOException ioE) {
                }*/
            }
        }while (true);
        }
    private void runSummingThreads(int n){
        for (int i = 0; i < n; i++){         //luodaan portit 3127:(3127+n)
            try{
                activeCalculators.add(new Calculator(i + 3127)); //luo uusi laskija
                Thread.sleep(200);  //hetken tauko
                activeCalculators.get(i).start();      //käynnistetään olio
                answerRequest(i + 3127); //ilmoita olion olevan aktiivinen
            }catch (InterruptedException iE){
                iE.printStackTrace();}

            System.out.println("Summaussäie " + activeCalculators.get(i).getPort() + " käynnistetty.");
        }
    }

    private void inputInterpreter(int receivedInt) throws IOException {     //käsittele saatu luku
        if (!portsAreSetup) { //jos portteja ei vielä avattu, käynnistä
            runSummingThreads(receivedInt);
            portsAreSetup = true;
            System.out.println("*****");
            System.out.println("Portit auki");
            System.out.println("*****");
        }
        else{
            switch (receivedInt) {
                case 0:             //lopettaa summauspalvelijat ja sulkee yhteydet
                    for (Calculator calc : activeCalculators) {
                        calc.kill();
                    }
                    output.close();
                    input.close();
                    connectionTCP.close();
                    System.out.println("Palvelijat lopetettu ja yhteydet suljettu.");
                    System.exit(0);
                    break;

                case 1:             //tähän mennessä välitettyjen lukujen summa
                    int calcTotalValue = 0;
                    for (int i = 0; i < activeCalculators.size(); i++) {
                        calcTotalValue += activeCalculators.get(i).getSum();
                    }
                    System.out.println("Kokonaissumma on " + calcTotalValue + "\n");
                    answerRequest(calcTotalValue);
                    break;


                case 2:              //mille palvelijalle välitetty summa suurin
                    int greatestCalc = Integer.MIN_VALUE;
                    int threadIndex = 0;
                    for (int i = 0; i < activeCalculators.size(); i++) {
                        if (greatestCalc < activeCalculators.get(i).getSum()) {
                            greatestCalc = activeCalculators.get(i).getSum();
                            threadIndex = i + 1;
                        }
                    }
                    System.out.println("Palvelin, jolla suurin summa : " + threadIndex + "\n");
                    answerRequest(threadIndex);
                    break;

                case 3:               //kaikille palvelimille välitettyjen lukujen kokonaismäärä
                    int numbersReceived = 0;
                    for (int i = 0; i < activeCalculators.size(); i++) {
                        numbersReceived += activeCalculators.get(i).getNumbersReceivedAmount();
                    }
                    System.out.println("Välitettyjen lukujen kokonaismäärä on " + numbersReceived + "\n");
                    answerRequest(numbersReceived);
                    break;

                default:              //vastaa takaisin luvulla -1, jos ei mikään edellisistä tapauksista
                    answerRequest(-1);
            }
		}
    }

    private void answerRequest(int n){ //välitä viesti takaisin palvelimelle
        try{
            output.writeInt(n);
            output.flush();
        }catch (IOException ioE){/*ioE.printStackTrace();*/
        }
    }
}


