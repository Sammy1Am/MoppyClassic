package moppydesk.raspberry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import moppydesk.MoppySequencer;
import moppydesk.MoppyStatusConsumer;

/**
 *
 * @author Törcsi
 */
public class JFlopPiSequencer extends MoppySequencer implements MetaEventListener {

    Sequencer sequencer;
    JFlopPiBridge mmr;
    ArrayList<MoppyStatusConsumer> listeners = new ArrayList<MoppyStatusConsumer>(1);

    public JFlopPiSequencer(String ip,int port) throws MidiUnavailableException {
        mmr = new JFlopPiBridge(ip,port);

        sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.getTransmitter().setReceiver(mmr); // Set MoppyPlayer as a receiver.
        sequencer.addMetaEventListener(this);
    }

    public void loadFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException {

        sequencer.stop();
        Sequence sequence = MidiSystem.getSequence(new File(filePath));

        sequencer.setSequence(sequence);
        System.out.println("Loaded sequence with " + (sequence.getTracks().length - 1) + " MIDI channels.");
    }

    public void startSequencer() {
        sequencer.start();
    }

    public void stopSequencer() {
        if (sequencer.isOpen()) {
            sequencer.stop();
        }
        mmr.close();
    }

    public void setTempo(float newTempo) {
        sequencer.setTempoInBPM(newTempo);
    }

    public void closeSequencer() {
        stopSequencer();
        sequencer.close();
        mmr.close();
    }

    public void addListener(MoppyStatusConsumer newListener) {
        listeners.add(newListener);
    }

    public void removeListener(MoppyStatusConsumer oldListener) {
        listeners.remove(oldListener);
    }

    @Override
    public void meta(MetaMessage meta) {
        if (meta.getType() == 81) {
            int uSecondsPerQN = 0;
            uSecondsPerQN |= meta.getData()[0] & 0xFF;
            uSecondsPerQN <<= 8;
            uSecondsPerQN |= meta.getData()[1] & 0xFF;
            uSecondsPerQN <<= 8;
            uSecondsPerQN |= meta.getData()[2] & 0xFF;
            int newTempo = 60000000 / uSecondsPerQN;
            sequencer.setTempoInBPM(newTempo);
        }
    }
}
