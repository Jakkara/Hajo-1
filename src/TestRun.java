public class TestRun {
    public static void main(String[] args){
        Client dave = new Client();
        dave.createUDPSocket();
        dave.sendUdpPacket("3200");
        dave.createTcpSocket();
    }
}

