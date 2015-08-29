package moppydesk.outputs;

import java.util.ArrayList;
import java.util.Arrays;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 * Marshals data from the chosen input device to the (up to) 16 output receivers.
 * Receivers are defined in a size-16 array, one slot for each MIDI channel.  Each received
 * message is routed to the appropriate receiver based on its channel.  If a receiver
 * is not defined for the given channel's index in the array, the message is dropped.
 */
public class ReceiverMarshaller implements MoppyReceiver{

    /**
     * Array of {@link MoppyReceiver} references, one for each channel.  The same receiver
     * object can be assigned to multiple indexes if it is going to be handling multiple
     * channels of data.
     */
    private final MoppyReceiver[] outputReceivers = new MoppyReceiver[16];
    private final boolean[] receiverEnabled = new boolean[16];
    
    /**
     * Creates a new ReceiverMarshaller with an empty array of {@link Receiver}s.
     * @param receivers 
     */
    public ReceiverMarshaller(){
        //Nothing for now.
    }
    
    /**
     * Sets a channel's Receiver object.  If a receiver is already assigned to this 
     * channel, {@link Receiver#close()} will be called before it is removed and replaced
     * with the new {@link Receiver}.
     * @param MIDIChannel
     * @param channelReceiver 
     */
    public void setReceiver(int MIDIChannel, MoppyReceiver channelReceiver){
        if (MIDIChannel < 1 || MIDIChannel > 16){
            throw new IllegalArgumentException("Only channels 1-16 are supported by the ReceiverMarshaller!");
        }
        if (outputReceivers[MIDIChannel-1] != null){
            outputReceivers[MIDIChannel-1].close();
        }
        outputReceivers[MIDIChannel-1] = channelReceiver;
    }
    
    //MrSolidSnake745: Adding functions to enable/disable receivers (essentially channels) for progrommatic control
    //WARNING: These are not user facing! If you disable a channel and forget to enable it, the user will have no way of correcting it without restarting Moppy!
    //  To help mitigate this, I'm calling enableAll when connected
    public void enableReceiver(int ch) {
        if (outputReceivers[ch-1] != null) {receiverEnabled[ch-1] = true;}
    }
    
    public void enableAll() { Arrays.fill(receiverEnabled, true); }
    
    public void disableReceiver(int ch) {
        if (outputReceivers[ch-1] != null) {receiverEnabled[ch-1] = false;}
    }        
    
    public void disableAll() { Arrays.fill(receiverEnabled, false); 
    
    }
    /**
     * Closes all receivers, and removes them from the array (fills array with nulls).
     */
    public void clearReceivers()
    {
        close();
    }

    public void send(MidiMessage message, long timeStamp) {
       int ch = ((ShortMessage)message).getChannel();
       if (outputReceivers[ch]!= null && receiverEnabled[ch]){
           outputReceivers[ch].send(message, timeStamp);
       }
    }

    /**
     * Explicity closes all output receivers, and nulls the array.
     * This differs slightly from the use described in {@link MidiDevice} in that
     * the ReceiverMarshaller itself is not necessarily closed when this is called.
     * After being closed, new receivers can continue to be added to the ReceiverMarshaller.
     */
    public void close() {
        disconnecting();
        for (Receiver r: outputReceivers){
            if (r!= null){
                r.close();
            }
        }
        Arrays.fill(outputReceivers, null);
    }

    private ArrayList<MoppyReceiver> getUniqueReceivers() {    
        ArrayList<MoppyReceiver> uniqueReceivers = new ArrayList<>();
        for (MoppyReceiver r: outputReceivers){
            if (r!= null && !uniqueReceivers.contains(r)){ uniqueReceivers.add(r); }
        }
        return uniqueReceivers;
    }
    
    /**
     * Finds the unique set of receivers and calls the {@link MoppyReceiver#reset() } method.
     * We go through the trouble of finding unique receivers incase the reset is time-consuming.
     */    
    public void reset() { for (MoppyReceiver r : getUniqueReceivers()){ r.reset(); } }
    public void silence() { for (MoppyReceiver r : getUniqueReceivers()){ r.silence(); } }
    public void connecting() { enableAll(); for (MoppyReceiver r : getUniqueReceivers()){ r.connecting(); } }
    public void disconnecting() { for (MoppyReceiver r : getUniqueReceivers()){ r.disconnecting(); } }
    public void sequenceStarting() { for (MoppyReceiver r : getUniqueReceivers()){ r.sequenceStarting(); } }
    public void sequenceStopping() { for (MoppyReceiver r : getUniqueReceivers()){ r.sequenceStopping(); } }        
}
