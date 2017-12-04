import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Calculator extends Thread {
    private int portNumber;
    private Socket connectionTCP;
    private ServerSocket serverSocket;
    private ObjectInputStream input;
    private int amountOfReceivedInputs;
    private int sum;
    private volatile boolean stop = false;

    Calculator(int port){
        portNumber = port;
    } //sidotaan porttinumeroon konstruktorissa

    @Override
    public void run() { //aktivoi kommunikointia varten TCP-soketin ja streamin
        try {
            serverSocket = new ServerSocket(portNumber);
            while (!stop) {
                connectionTCP = serverSocket.accept();
                System.out.println("Laskija portissa " + portNumber + " aktiivinen.");
                input = new ObjectInputStream(connectionTCP.getInputStream());
                System.out.println("*****");
                summingPhase();
            }
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }
    private void summingPhase(){ //käsittelee sille lähetetyt luvut
        int receivedInt = 0;
        try{
            receivedInt = input.readInt();
            System.out.println("Laskijaolio " + portNumber + " sai luvun  " + receivedInt);

            if (receivedInt == 0)kill();
            sum += receivedInt;
            amountOfReceivedInputs += 1;
        }catch (IOException ioE){ioE.printStackTrace();
        }
    }
    public int getSum(){
        return sum;
    }

    public int getNumbersReceivedAmount(){
        return amountOfReceivedInputs;
    }

    public void kill(){
        try{
            connectionTCP.close();
            input.close();
            System.out.println("Laskijaolio " + portNumber + " sai tappokäskyn ja lopettaa toimintansa.");
        }catch (IOException ioE){}
        stop = true;
    }

    public int getPort() {
    	return portNumber;
    }
}

