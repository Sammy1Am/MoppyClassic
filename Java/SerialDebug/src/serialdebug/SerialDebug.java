/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialdebug;

import gnu.io.NRSerialPort;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adam Allport
 */
public class SerialDebug {

    NRSerialPort serial;
    static int SERIAL_RATE = 9600;

    /**
     * @param args the command line arguments
     */
    public static String[] getComPorts() {
        Set<String> ports = NRSerialPort.getAvailableSerialPorts();
        return ports.toArray(new String[ports.size()]);
    }

    public void connect(String comPort) {
        serial = new NRSerialPort(comPort, SERIAL_RATE);
        serial.connect();
    }

    public static void main(String[] args) {
        // TODO code application logic here
        SerialDebug d = new SerialDebug();
        d.connect("COM2");
        DataInputStream in = new DataInputStream(d.serial.getInputStream());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                if (in.available() > 2) {
                    System.out.println(in.readUnsignedByte() + " " + in.readUnsignedByte() + " " + in.readUnsignedByte());
                } else {
                    Thread.sleep(1);
                }
            } catch (IOException ex) {
                Logger.getLogger(SerialDebug.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialDebug.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
