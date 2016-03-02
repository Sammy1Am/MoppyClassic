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
    public void silence();
    
    //MrSolidSnake745: Adding events to allow for defining appropriate responses per microcontroller
    //These events will cause a specific set of system bytes to be sent on MoppyPlayerOutput, but can be useful for other things
    public void connecting();
    public void disconnecting();
    public void sequenceStarting();
    public void sequenceStopping();
}
