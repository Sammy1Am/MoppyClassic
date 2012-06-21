package moppydesk.outputs;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sammy1Am
 */
public class MoppyCOMBridge {

    static int FIRST_PIN = 2;
    static int MAX_PIN = 17;
    int SERIAL_RATE = 9600;
    OutputStream os;
    SerialPort com;
    private boolean isOutputOpen = false;

    public MoppyCOMBridge(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        com = (SerialPort) cpi.open("MoppyDesk", 2000);
        com.setSerialPortParams(SERIAL_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        os = com.getOutputStream();
        isOutputOpen = true;
    }

    /**
     * Convenience method that splits the periodData int into two bytes for
     * sending over serial.
     *
     * @param pin Controller pin to handle ntoe
     * @param periodData length of period in microSeconds
     */
    public void sendEvent(byte pin, int periodData) {
        sendEvent(pin, (byte) ((periodData >> 8) & 0xFF), (byte) (periodData & 0xFF));
    }

    /**
     * Sends an event to the Arduino.
     *
     * @param pin Controller pin
     * @param b1
     * @param b2
     */
    public void sendEvent(byte pin, byte b1, byte b2) {
        sendArray(new byte[]{pin, b1, b2});
    }

    private void sendArray(byte[] message) {
        try {
            os.write(message);
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(MoppyCOMBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sends a '0' period to all drives to silence them.
     */
    private void silenceDrives() {
        // Stop notes
        for (int d = FIRST_PIN; d <= MAX_PIN; d += 2) {
            sendArray(new byte[]{(byte) d, (byte) 0, (byte) 0});
        }
    }

    /**
     * Sends a special code (first byte=100) to reset the drives
     */
    public void resetDrives() {
        if (isOutputOpen) {
            //Send reset code
            sendArray(new byte[]{(byte) 100, (byte) 0, (byte) 0});
            try {
                Thread.sleep(500); // Give the drives time to reset
            } catch (InterruptedException ex) {
                Logger.getLogger(MoppyCOMBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void close() {
        if (os != null) {
            if (isOutputOpen) { //Only attempt to silence if the output is still open.
                silenceDrives();
            }
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(MoppyCOMBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (com != null) {
            com.close();
        }
        isOutputOpen = false;
    }

    public static String[] getAvailableCOMPorts() {
        ArrayList<String> portIdentifierStrings = new ArrayList<String>();

        Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
        while (comPorts.hasMoreElements()) {
            portIdentifierStrings.add(comPorts.nextElement().getName());
        }

        return portIdentifierStrings.toArray(new String[portIdentifierStrings.size()]);
    }
}
