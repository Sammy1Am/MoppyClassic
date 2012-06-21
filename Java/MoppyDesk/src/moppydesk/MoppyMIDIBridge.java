/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 *
 * @author Sam
 */
public class MoppyMIDIBridge {
    
    
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
}
