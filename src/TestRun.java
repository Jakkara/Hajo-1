public class TestRun {
    public static void main(String[] args){
        WorkDistributor hal = new WorkDistributor();
        Client dave = new Client();
        dave.createUDPSocket();
        dave.sendUdpPacket("3200");
        dave.createTcpSocket();
    }
}

