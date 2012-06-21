/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import java.io.Serializable;

/**
 *
 * @author Sam
 */
public class OutputSetting implements Serializable{
    public enum OutputType {MOPPY, MIDI};
    
    public final int MIDIChannel;
    public boolean enabled = false;
    public OutputType type = OutputType.MOPPY;
    public String comPort;
    public String midiDeviceName;
    
    public OutputSetting(int MIDIChannel){
        this.MIDIChannel = MIDIChannel;
    }
}
