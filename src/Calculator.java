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
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(portNumber);
            while (!stop) {
                connectionTCP = serverSocket.accept();
                System.out.println(portNumber + " aktiivinen.");
                input = new ObjectInputStream(connectionTCP.getInputStream());
                summingPhase();
            }
        } catch (IOException ioE) {
            ioE.printStackTrace();
        }
    }
    private void summingPhase(){
        //TODO : laske saadut luvut
        int receivedInt = 0;
        try{
            receivedInt = input.readInt();
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
        stop = true;
    }
    public int getPort() {
    	return portNumber;
    }
}

