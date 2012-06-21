/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

/**
 *
 * @author Sam
 */
public class MoppyMIDIBridge implements Receiver{
    
    MidiDevice device;
    Receiver deviceReceiver;
    
    public MoppyMIDIBridge(String midiDeviceName) throws MidiUnavailableException{
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (Info i : infos){
            try {
                if (i.getName().equalsIgnoreCase(midiDeviceName)){
                    this.device = MidiSystem.getMidiDevice(i);
                }
            } catch (MidiUnavailableException ex) {
                Logger.getLogger(MoppyMIDIBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        device.open();
        deviceReceiver = device.getReceiver();
    }
    
    public static HashMap<String,Info> getMIDIOutInfos(){
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        HashMap<String,Info> outInfos = new HashMap<String,Info>();
        
        for (Info i : infos){
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(i);
                if (dev.getMaxTransmitters() != 0){
                    outInfos.put(i.getName(), i);
                }
            } catch (MidiUnavailableException ex) {
                Logger.getLogger(MoppyMIDIBridge.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return outInfos;
    }

    public void send(MidiMessage message, long timeStamp) {
        deviceReceiver.send(message, timeStamp);
    }

    public void close() {
        deviceReceiver.close();
        device.close();
    }
}
