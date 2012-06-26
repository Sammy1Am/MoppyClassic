package moppydesk.outputs;

import javax.sound.midi.Receiver;

/**
 * Adds a reset function to the MIDI Receiver class.
 * @author Sam
 */
public interface MoppyReceiver extends Receiver{
    
    /**
     * Returns the drives/xylophone/calliope/organ/drums to a reset-state.
     * This should not disconnect or dispose of any connection though.
     */
    public void reset();
}
