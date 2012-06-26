/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk.outputs;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import javax.sound.midi.MidiDevice.Info;

/**
 *
 * @author Sam
 */
public class MoppyMIDIOutput implements MoppyReceiver{
    
    MidiDevice device;
    Receiver deviceReceiver;
    
    public MoppyMIDIOutput(String midiDeviceName) throws MidiUnavailableException{
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (Info i : infos){
            try {
                if (i.getName().equalsIgnoreCase(midiDeviceName)){
                    this.device = MidiSystem.getMidiDevice(i);
                }
            } catch (MidiUnavailableException ex) {
                Logger.getLogger(MoppyMIDIOutput.class.getName()).log(Level.SEVERE, null, ex);
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
                if (dev.getMaxReceivers() != 0){
                    outInfos.put(i.getName(), i);
                }
            } catch (MidiUnavailableException ex) {
                Logger.getLogger(MoppyMIDIOutput.class.getName()).log(Level.SEVERE, null, ex);
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

    public void reset() {
        //Nothing really to do here, I don't think.
        if (deviceReceiver != null){
            try {
                ShortMessage resetMessage = new ShortMessage();
                resetMessage.setMessage(ShortMessage.SYSTEM_RESET);
                deviceReceiver.send(resetMessage,(long)-1);
            } catch (InvalidMidiDataException ex) {
                Logger.getLogger(MoppyMIDIOutput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
