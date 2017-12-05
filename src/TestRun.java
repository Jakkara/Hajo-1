public class TestRun {
    public static void main(String[] args){
        Client dave = new Client(); //client testikäyttöön
        dave.createUDPSocket();
        dave.sendUdpPacket("3200"); //asetetaan TCP-portiksi 3200
        dave.createTcpSocket();
    }
}

