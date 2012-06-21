/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

/**
 *
 * @author Sam
 */
public class OutputSettings {
    public enum OutputType {MOPPY, MIDI};
    
    public final int MIDIChannel;
    public OutputType type = OutputType.MOPPY;
    public String comPort;
    public String midiDeviceName;
    
    public OutputSettings(int MIDIChannel){
        this.MIDIChannel = MIDIChannel;
    }
}
