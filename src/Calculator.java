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
    public void run() { //aktivoi kommunikointia varten TCP-streamin
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
        try{
            while(!stop) {
                int receivedInt = input.readInt();
                if (receivedInt == 0) kill();
                else{
                    amountOfReceivedInputs += 1;
                    sum += receivedInt;
                    System.out.println("Laskijaolio " + portNumber + " sai luvun " + receivedInt);

                }
            }
        }catch (IOException ioE){
        }
    }
    public int getSum(){ return sum; }

    public int getNumbersReceivedAmount(){
        return amountOfReceivedInputs;
    }

    public void kill(){
        try{
            connectionTCP.close();
            input.close();
            stop = true;
            System.out.println("Laskijaolio " + portNumber + " sai tappokäskyn ja lopettaa toimintansa.");
        }catch (IOException ioE){}
    }

    public int getPort() {
    	return portNumber;
    }
}

