package moppydesk;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.sound.midi.*;

/**
 *
 * @author Sammy1Am
 */
public class MoppySequencer implements MetaEventListener{

    MoppyBridge mb;
    MoppyPlayer mp;
    Sequencer sequencer;
    Transmitter midiIn;
    ArrayList<MoppyStatusConsumer> listeners = new ArrayList<MoppyStatusConsumer>(1);

    public MoppySequencer(String comPort, int midiPort) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, MidiUnavailableException {
        mb = new MoppyBridge(comPort); //Create MoppyBridge on the COM port with the Arduino
        mp = new MoppyPlayer(mb);

        mb.resetDrives();

        MidiDevice device;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        try {
            device = MidiSystem.getMidiDevice(infos[midiPort]);
            System.out.println ("MIDI port selected: "+ midiPort);
            device.open();
            midiIn = device.getTransmitter();
            midiIn.setReceiver(mp);
       } catch (MidiUnavailableException e) {
            System.out.println ("MIDI port error: "+ midiPort);
        }

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.getTransmitter().setReceiver(mp); // Set MoppyPlayer as a receiver.
        sequencer.addMetaEventListener(this);
    }

    public void loadFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException {

        sequencer.stop();
        Sequence sequence = MidiSystem.getSequence(new File(filePath));
        
        sequencer.setSequence(sequence);
        System.out.println("Loaded sequence with "+(sequence.getTracks().length-1)+" MIDI channels.");
    }
    
    public void startSequencer(){
        sequencer.start();
    }
    
    public void stopSequencer(){
        if (sequencer.isOpen()){
                sequencer.stop();
            }
        mb.resetDrives();
    }
    
    public void setTempo(float newTempo){
        sequencer.setTempoInBPM(newTempo);
    }
    
    public void addListener(MoppyStatusConsumer newListener){
        listeners.add(newListener);
    }
    
    public void removeListener(MoppyStatusConsumer oldListener){
        listeners.remove(oldListener);
    }
    
    public void closeSequencer(){
        stopSequencer();
        sequencer.close();
        mp.close();
    }

    public void meta(MetaMessage meta) {
        if (meta.getType() == 81){
            int uSecondsPerQN = 0;
            uSecondsPerQN |= meta.getData()[0] & 0xFF;
            uSecondsPerQN <<= 8;
            uSecondsPerQN |= meta.getData()[1] & 0xFF;
            uSecondsPerQN <<= 8;
            uSecondsPerQN |= meta.getData()[2] & 0xFF;
            
            int newTempo = 60000000/uSecondsPerQN;
            
            sequencer.setTempoInBPM(newTempo);
            for (MoppyStatusConsumer c : listeners){
                c.tempoChanged(newTempo);
            }
            
            System.out.println("Tempo changed to: " + newTempo);
        }
    }
}
