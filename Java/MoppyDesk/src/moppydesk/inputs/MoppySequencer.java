package moppydesk.inputs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import moppydesk.outputs.ReceiverMarshaller;

/**
 *
 * @author Sammy1Am
 */
public class MoppySequencer implements MetaEventListener, Transmitter{

    Sequencer sequencer;
    Sequence currentSequence = null;
    Transmitter sequenceTransmitter; // Because we'll be piping all the messages to a common receiver, we'll only need one of these
    ArrayList<MoppyStatusConsumer> listeners = new ArrayList<MoppyStatusConsumer>(1);
    ReceiverMarshaller rm;

    public MoppySequencer(ReceiverMarshaller rmIn) throws MidiUnavailableException {

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.addMetaEventListener(this);
        sequenceTransmitter = sequencer.getTransmitter(); // This method creates a new transmitter each time it's called!
        rm = rmIn;
    }

    public void loadFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException {
        sequencer.stop();
        Sequence sequence = MidiSystem.getSequence(new File(filePath));
        
        sequencer.setSequence(sequence);
        System.out.println("Loaded sequence with "+(sequence.getTracks().length-1)+" MIDI tracks.");
        currentSequence = sequence;
    }
    
    public void startSequencer(){
        rm.sequenceStarting();
        sequencer.start();
    }
    
    public boolean isRunning(){
        return sequencer.isRunning();
    }
    
    public void stopSequencer(){
        if (sequencer.isOpen()){
            rm.sequenceStopping();
            sequencer.stop();
        }
    }
    
    public void resetSequencer(){
        if (sequencer.isOpen()){
            rm.sequenceStopping();    
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
        else if (meta.getType() == 47) {
            //MrSolidSnake745: Exposing sequence stopping event to receivers
            //The end of a sequence is the same as it stopping
            rm.sequenceStopping();
            //MrSolidSnake745: Exposing end of sequence event to status consumers
            for (MoppyStatusConsumer c : listeners) { c.sequenceEnded(); }            
            System.out.println("End of current sequence");
        }
    }

    public void setReceiver(Receiver receiver) {
            sequenceTransmitter.setReceiver(receiver);
    }

    public Receiver getReceiver() {
        return sequenceTransmitter.getReceiver();
    }

    public void close() {
        closeSequencer();
    }
}
