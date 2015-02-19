package moppydesk.midputs;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;

/**
 * This class will perform various operations on notes as they pass through.  Mostly this
 * is for keeping notes within the specified range.
 */
public class NoteFilter implements Receiver, Transmitter {

    Receiver receiver = null;
    private boolean autoContstrain = false;
    private boolean ignoreTen = false;
    
    private static final int LOWEST_NOTE = 24; //C1
    private static final int HIGHEST_NOTE = 71; //B4
    
    public void setAutoConstrain(boolean newValue) {
        autoContstrain = newValue;
    }
    
    public void setIgnoreTen(boolean newValue) {
        ignoreTen = newValue;
    }

    public void send(MidiMessage message, long timeStamp) {
        if (receiver != null) {
            if (message instanceof ShortMessage
                    && (message.getMessage()[0] & 0xFF) >= 128 && (message.getMessage()[1] & 0xFF) <= 159) {
                ShortMessage filteredMessage =  (ShortMessage) message;
                
                if (autoContstrain){
                    constrainNote(filteredMessage);
                }
                if (ignoreTen && filteredMessage.getChannel() == 9){
                    return; // If we're ignoring 10 and that's the channel, just return immediately
                }
                
                receiver.send(filteredMessage, timeStamp);
            } else {
                receiver.send(message, timeStamp); // If it's not a note event, pass it on through.
            }
        }
    }

    /**
     * Adjusts the note data within this message to fit between {@link #LOWEST_NOTE} and {@link #HIGHEST_NOTE}, inclusive.
     * This will affect both on and off messages so that any altered notes turned on will also be turned off.
     * @param message 
     */
    private void constrainNote(ShortMessage message){
        int newNote = message.getData1();
        
        while (newNote<LOWEST_NOTE){
            newNote += 12; // Up an octave
        }
        while (newNote>HIGHEST_NOTE){
            newNote -= 12; // Down an octave
        }
        
        try {
            message.setMessage(message.getStatus(), newNote, message.getData2());
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(NoteFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //
    //// Transmitter-methods
    //
    public void close() {
        if (receiver != null) {
            receiver.close();
            receiver = null;
        }
    }

    public void setReceiver(Receiver newReceiver) {
        if (this.receiver != null) {
            this.receiver.close();
        }
        this.receiver = newReceiver;
    }

    public Receiver getReceiver() {
        return receiver;
    }


}
