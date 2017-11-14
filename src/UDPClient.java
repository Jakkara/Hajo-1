import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class UDPClient {
    private InetAddress activeIPAddress;
    private int activePortNumber = 3126;
    private DatagramSocket socket;


    public UDPClient() {
    }

    public void createSocket() {
        try {
            socket = new DatagramSocket();
            activeIPAddress = InetAddress.getByName("localhost");
        } catch (SocketException sE) {
            sE.printStackTrace();
        } catch (UnknownHostException uhE) {
            uhE.printStackTrace();
        }
    }

    public void sendPacket(String message) {
        try {
            byte[] dataArrayToSend = message.getBytes();
            DatagramPacket packetToSend = new DatagramPacket(dataArrayToSend, dataArrayToSend.length, activeIPAddress, activePortNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


