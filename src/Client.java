import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Client{
    //UDP
    private InetAddress activeIPAddress;
    private int activePortNumber = 3126;
    private DatagramSocket socketUDP;
    private String offeredPort = "";
    //TCP
    private ServerSocket serverSocket;
    private Socket connectionTCP;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    //Globals
    private boolean portsAreSetup = false;
    private ArrayList<Calculator> activeCalculators = new ArrayList<>(3140);
    private int timeOut = 5000;
    private long elapsedTime = 0L;
    private long startTime = 0L;


    public Client() {}

    public void createUDPSocket() {     //luo l�hett�mist� varten UDP-soketti
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

    public void sendUdpPacket(String message) { //l�het� porttiosoitteen sis�lt�v� paketti
        offeredPort = message; //talletetaan silt� varalta ett� ensimm�inen paketti ei mene perille
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
                int tryCounter = 0;
                while(tryCounter <= 5){
                    try{
                        connectionTCP = serverSocket.accept();
                    }catch(SocketTimeoutException stoE){
                        System.out.print("Ei yhteytt� TCP-porttiin. Jatketaan kuuntelua. ");
                        if (tryCounter <= 5){ //yritet��n 5 kertaa
                            tryCounter++;
                            sendUdpPacket(offeredPort); //jos timeout, l�het� uudestaan
                        }if(tryCounter == 6){System.out.println("Ei yhteytt�. Suljetaan ohjelma.");System.exit(0);}
                    }
                }

                System.out.println("Muodostetaan TCP-yhteys serveriin.");
                output = new ObjectOutputStream(connectionTCP.getOutputStream());
                output.flush();
                input = new ObjectInputStream(connectionTCP.getInputStream());
                System.out.println("Stream serveriin aktiivinen.");
                communicationPhase();
            }
        }catch (IOException ioE){ioE.printStackTrace();}
    }
    private void communicationPhase() { //kun ollaan valmiita kuuntelemaan k�skyj�
        System.out.println("Client kuuntelee viesti�.");
        int message;
        startTime = System.currentTimeMillis(); //nykyhetki
        do {
            try {
                while(elapsedTime < timeOut){ //toimii timeouttina, verrataan alussa 5s ja my�hemmin 60s
                    message = input.readInt();
                    System.out.println("Server l�hetti luvun : " + message);
                    inputInterpreter(message);      //k�sittelee viestin
                    elapsedTime = (new Date()).getTime() - startTime;       //pit�� kirjaa kuluneesta ajasta
                }
                throw new TimeoutException();       //jos aikaraja ylittyy, heit� poikkeus
            } catch (TimeoutException e) {
                try {
                    answerRequest(-1); //v�lit� serverille tieto ett� ei saatu t
                    inputInterpreter(0);        //sulje hallitusti
                } catch (IOException ioE) {}
            }catch (IOException ioE){
                //ioE.printStackTrace();
            }
        }while (true);
    }

    private void runSummingThreads(int n){
        for (int i = 0; i < n; i++){         //luodaan portit 3127:(3127+n)
            try{
                activeCalculators.add(new Calculator(i + 3127)); //luo uusi laskija
                activeCalculators.get(i).start();      //k�ynnistet��n olio
                Thread.sleep(200);  //hetken tauko
                answerRequest(i + 3127); //ilmoita olion olevan aktiivinen
            }catch (InterruptedException iE){
                iE.printStackTrace();
            }
            System.out.println("Summauss�ie " + activeCalculators.get(i).getPort() + " k�ynnistetty.");
        }
    }

    private void inputInterpreter(int receivedInt) throws IOException {     //k�sittele saatu luku
        elapsedTime = 0L; //luku saatu, nollaa ajastin
        startTime = System.currentTimeMillis(); //nykyhetki p�ivittyy

        if (!portsAreSetup) { //jos portteja ei viel� avattu, k�ynnist� s�ikeet
            runSummingThreads(receivedInt);
            portsAreSetup = true;
            System.out.println("*****");
            System.out.println("Portit auki");
            System.out.println("*****");
            timeOut = 60000; //kun ensimm�inen numero on saatu, muutetaan timeout minuutiksi
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

                case 1:             //t�h�n menness� v�litettyjen lukujen summa
                    int calcTotalValue = 0;
                    for (int i = 0; i < activeCalculators.size(); i++) {
                        calcTotalValue += activeCalculators.get(i).getSum();
                    }
                    System.out.println("Kokonaissumma on " + calcTotalValue + "\n");
                    answerRequest(calcTotalValue);
                    break;


                case 2:              //mille palvelijalle v�litetty summa suurin
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

                case 3:               //kaikille palvelimille v�litettyjen lukujen kokonaism��r�
                    int numbersReceived = 0;
                    for (int i = 0; i < activeCalculators.size(); i++) {
                        numbersReceived += activeCalculators.get(i).getNumbersReceivedAmount();
                    }
                    System.out.println("V�litettyjen lukujen kokonaism��r� on " + numbersReceived + "\n");
                    answerRequest(numbersReceived);
                    break;

                default:              //vastaa takaisin luvulla -1, jos ei mik��n edellisist� tapauksista
                    answerRequest(-1);
            }
		}
    }

    private void answerRequest(int n){ //v�lit� viesti takaisin palvelimelle
        try{
            output.writeInt(n);
            output.flush();
        }catch (IOException ioE){/*ioE.printStackTrace();*/
        }
    }
}


