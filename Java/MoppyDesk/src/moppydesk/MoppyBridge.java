/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package moppydesk;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sam
 */
public class MoppyBridge {

    int SERIAL_RATE = 9600;
    OutputStream os;
    SerialPort com;

    public MoppyBridge(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        com = (SerialPort) cpi.open("MoppyDesk", 2000);
        com.setSerialPortParams(SERIAL_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        os = com.getOutputStream();
    }

    public void sendEvent(byte pin, int periodData){
        sendEvent(pin, (byte)((periodData >> 8) & 0xFF), (byte)(periodData & 0xFF));
    }

    public void sendEvent(byte pin, byte b1, byte b2){
        sendArray(new byte[] {pin, b1, b2});
    }

    private void sendArray(byte[] message){
        try {
            os.write(message);
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(MoppyBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void silenceDrives(){
        // Stop notes
        for (int d=2;d<=6;d+=2){
            sendArray(new byte[] {(byte)d,(byte)0,(byte)0});
        }
    }

    public void resetDrives(){
        silenceDrives();
        //Send reset code
        sendArray(new byte[] {(byte)100,(byte)0,(byte)0});
        try {
            Thread.sleep(1500); // Give the drives time to reset
        } catch (InterruptedException ex) {
            Logger.getLogger(MoppyBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close(){
        silenceDrives();
        if (os != null){
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(MoppyBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (com != null){
            com.close();
        }
    }

}
