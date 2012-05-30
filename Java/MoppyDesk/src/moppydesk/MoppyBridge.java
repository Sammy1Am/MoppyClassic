package moppydesk;

import gnu.io.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sammy1Am
 */
public class MoppyBridge {

    static int MAX_DRIVES = 8;
    
    int SERIAL_RATE = 9600;
    OutputStream os;
    SerialPort com;

    public MoppyBridge(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        com = (SerialPort) cpi.open("MoppyDesk", 2000);
        com.setSerialPortParams(SERIAL_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        os = com.getOutputStream();
    }

    /**
     * Convenience method that splits the periodData int
     * into two bytes for sending over serial.
     * @param pin Controller pin to handle ntoe
     * @param periodData length of period in microSeconds
     */
    public void sendEvent(byte pin, int periodData){
        sendEvent(pin, (byte)((periodData >> 8) & 0xFF), (byte)(periodData & 0xFF));
    }

    /**
     * Sends an event to the Arduino.
     * @param pin Controller pin
     * @param b1
     * @param b2 
     */
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

    /**
     * Sends a '0' period to all drives to silence them.
     */
    public void silenceDrives(){
        // Stop notes
        for (int d=0;d <= MAX_DRIVES; d++){
            int pin = 2+d*2;
            sendArray(new byte[] {(byte)pin,(byte)0,(byte)0});
        }
    }

    /**
     * Sends a special code (first byte=100) to reset the drives
     */
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
        if (os != null){
            silenceDrives();
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
