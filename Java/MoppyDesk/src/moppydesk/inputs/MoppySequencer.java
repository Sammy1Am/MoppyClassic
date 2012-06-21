package moppydesk;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

/**
 *
 * @author Sammy1Am
 */
public class MoppySequencer implements MetaEventListener{

    Sequencer sequencer;
    Sequence currentSequence = null;
    ArrayList<MoppyStatusConsumer> listeners = new ArrayList<MoppyStatusConsumer>(1);

    public MoppySequencer(Receiver newReceiver) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, MidiUnavailableException {

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.getTransmitter().setReceiver(newReceiver); // Set MoppyPlayer as a receiver.
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
}
