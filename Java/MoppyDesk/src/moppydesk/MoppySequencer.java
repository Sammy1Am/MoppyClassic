/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.*;
import moppydesk.arduino.MoppyBridge;
import moppydesk.arduino.MoppyPlayer;

/**
 *
 * @author Törcsi
 */
public abstract class MoppySequencer implements MetaEventListener{

    public abstract void loadFile(String filePath) throws InvalidMidiDataException, IOException, MidiUnavailableException;
    
    public abstract void startSequencer();
    
    public abstract void stopSequencer();
    
    public abstract void setTempo(float newTempo);
    
    public abstract void addListener(MoppyStatusConsumer newListener);
    
    public abstract void removeListener(MoppyStatusConsumer oldListener);
    
    public abstract void closeSequencer();

    public abstract void meta(MetaMessage meta);
}
