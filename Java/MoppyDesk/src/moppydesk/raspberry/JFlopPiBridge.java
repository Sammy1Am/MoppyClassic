package moppydesk.raspberry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 *
 * @author Törcsi
 */
public class JFlopPiBridge implements Receiver {

    DatagramSocket clientSocket;
    int port;
    InetAddress IPAddress;
    int PiRes;
    List<MidiMessage> msgline = Collections.synchronizedList(new ArrayList<MidiMessage>());
    static int FIRST_PIN = 2;
    static int MAX_PIN = 17;
    private int[] currentPeriod = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; //its a bit larger then needed but we have ram... :)

    public JFlopPiBridge(String ip, int port, int res) {
        try {
            clientSocket = new DatagramSocket();
            IPAddress = InetAddress.getByName(ip);
            this.port = port;
            PiRes = res;
        } catch (SocketException ex) {
            Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JFlopPiBridge(String ip, int port) {
        this(ip, port, 40);
    }
    /*
     * public void run() { if (msgline.size() > 0) { ArrayList<MidiMessage>
     * deletelist = new ArrayList<MidiMessage>(); for (int i = 0; i <
     * msgline.size(); i++) { deletelist.add(msgline.get(i)); int ch =
     * (msgline.get(i).getStatus() & 0x0f); if (ch < 9) {
     * sendToPi(msgline.get(i), 0); } } msgline.removeAll(deletelist); } }
     */

    @Override
    public void send(MidiMessage message, long timeStamp) {
        //the msgline has historical reason it coud be optimalized out...
        msgline.add(message);
        ArrayList<MidiMessage> deletelist = new ArrayList<MidiMessage>();
        for (int i = 0; i < msgline.size(); i++) {
            deletelist.add(msgline.get(i));
            int ch = (msgline.get(i).getStatus() & 0x0f);
            if (ch < 9) {
                sendToPi(msgline.get(i), 0);
            }
        }
        msgline.removeAll(deletelist);
    }

    @Override
    public void close() {
        silenceDrives();
        clientSocket.close();
    }
    //for more info see MoppyPlayer comments

    public void sendToPi(MidiMessage message, long timeStamp) {
        byte[] sendData = new byte[3];
        if (message.getStatus() > 127 && message.getStatus() < 144) { // Note OFF
            byte pin = (byte) (2 * (message.getStatus() - 127));;
            sendData[0] = pin;
            sendData[1] = 0;
            sendData[2] = 0;
            currentPeriod[pin] = 0;
        } else if (message.getStatus() > 143 && message.getStatus() < 160) { // Note ON
            byte pin = (byte) (2 * (message.getStatus() - 143));
            int d = (message.getMessage()[1] & 0xff);
            int freq = (int) (Math.pow(2, (d - (float) 69) / (float) 12) * 440);
            long freqDelay = (long) ((float) 1 / (float) freq * (float) 1000 * (float) 1000);
            int period = (int) (freqDelay / (PiRes * 2));
            if (message.getMessage()[2] == 0) {
                sendData[0] = pin;
                sendData[1] = 0;
                sendData[2] = 0;
                currentPeriod[pin] = 0;
            } else {
                sendData[0] = pin;
                sendData[1] = (byte) ((period >> 8) & 0xFF);
                sendData[2] = (byte) (period & 0xFF);
                currentPeriod[pin] = period;
            }
        } else if (message.getStatus() > 223 && message.getStatus() < 240) { //Pitch bends 
            byte pin = (byte) (2 * (message.getStatus() - 223));

            if (currentPeriod[pin] != 0) {
                double pitchBend = ((message.getMessage()[2] & 0xff) << 8) + (message.getMessage()[1] & 0xff);
                int period = (int) (currentPeriod[pin] / Math.pow(2.0, (pitchBend - 8192) / 8192));
                sendData[0] = pin;
                sendData[1] = (byte) ((period >> 8) & 0xFF);
                sendData[2] = (byte) (period & 0xFF);
            }
        }

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void silenceDrives() {
        // Stop notes
        for (int d = FIRST_PIN; d <= MAX_PIN; d += 2) {
            byte[] sendData = new byte[3];
            sendData[0] = (byte) d;
            sendData[1] = 0;
            sendData[2] = 0;
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            try {
                clientSocket.send(sendPacket);
            } catch (IOException ex) {
                Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void resetDrives() {
        silenceDrives();
        //Send reset code
        byte[] sendData = new byte[3];
        sendData[0] = 100;
        sendData[1] = 0;
        sendData[2] = 0;
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        try {
            clientSocket.send(sendPacket);
            Thread.sleep(1500); // Give the drives time to reset
        } catch (IOException ex) {
            Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(JFlopPiBridge.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
