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
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author Sam
 */
public class MoppyPlayer1 implements Receiver {

    OutputStream os;
    SerialPort com;

    public MoppyPlayer1(String portName) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
        CommPortIdentifier cpi = CommPortIdentifier.getPortIdentifier(portName);
        com = (SerialPort) cpi.open("MoppyDesk", 2000);
        com.setSerialPortParams(31250, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        os = com.getOutputStream();
    }

    public void close(){
        if (os!=null){
            try {
                os.close();
            } catch (IOException ex) {
                Logger.getLogger(MoppyPlayer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (com!=null){
            com.close();
        }

    }

    public void send(MidiMessage message, long timeStamp) {
        if (message.getStatus() > 127 && message.getStatus() < 160){
            try {
                ShortMessage sm = (ShortMessage) message;
                //System.out.println(sm.getChannel());
                //TODO Channels start at 0??
                sm.setMessage(sm.getStatus()+1, sm.getData1(), sm.getData2()); //Try shifting channel
                os.write(sm.getMessage());
            } catch (InvalidMidiDataException ex) {
                Logger.getLogger(MoppyPlayer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MoppyPlayer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
