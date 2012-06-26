package moppydesk.inputs;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Transmitter;
import moppydesk.MoppyStatusConsumer;

/**
 *
 * @author Sammy1Am
 */
public class MoppySequencer implements MetaEventListener, Transmitter{

    Sequencer sequencer;
    Sequence currentSequence = null;
    ArrayList<MoppyStatusConsumer> listeners = new ArrayList<MoppyStatusConsumer>(1);

    public MoppySequencer() throws MidiUnavailableException {

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.addMetaEventListener(this);
    }

    public void loadFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException {
        sequencer.stop();
        Sequence sequence = MidiSystem.getSequence(new File(filePath));
        
        sequencer.setSequence(sequence);
        System.out.println("Loaded sequence with "+(sequence.getTracks().length-1)+" MIDI channels.");
        currentSequence = sequence;
    }
    
    public void startSequencer(){
        sequencer.start();
    }
    
    public boolean isRunning(){
        return sequencer.isRunning();
    }
    
    public void stopSequencer(){
        if (sequencer.isOpen()){
                sequencer.stop();
            }
    }
    
    public void resetSequencer(){
        if (sequencer.isOpen()){
                sequencer.stop();
                sequencer.setTickPosition(0);
            }
    }
    
    public void setTempo(float newTempo){
        sequencer.setTempoInBPM(newTempo);
    }
    
    public long getSecondsLength(){
        return sequencer.getMicrosecondLength()/1000000;
    }
    
    public long getSecondsPosition(){
        return sequencer.getMicrosecondPosition()/1000000;
    }
    
    public void setSecondsPosition(long seconds){
        sequencer.setMicrosecondPosition(seconds*1000000);
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

    public void setReceiver(Receiver receiver) {
        try {
            sequencer.getTransmitter().setReceiver(receiver);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MoppySequencer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Receiver getReceiver() {
        try {
            return sequencer.getReceiver();
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MoppySequencer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void close() {
        closeSequencer();
    }
}
