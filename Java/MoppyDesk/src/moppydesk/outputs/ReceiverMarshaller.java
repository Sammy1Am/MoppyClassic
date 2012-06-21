/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk.outputs;

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
public class ReceiverMarshaller implements Receiver{

    /**
     * Array of {@link Receiver} references, one for each channel.  The same receiver
     * object can be assigned to multiple indexes if it is going to be handling multiple
     * channels of data.
     */
    private final Receiver[] outputReceivers = new Receiver[16];
    
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
    public void setReceiver(int MIDIChannel, Receiver channelReceiver){
        if (MIDIChannel < 1 || MIDIChannel > 16){
            throw new IllegalArgumentException("Only channels 1-16 are supported by the ReceiverMarshaller!");
        }
        if (outputReceivers[MIDIChannel-1] != null){
            outputReceivers[MIDIChannel-1].close();
        }
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
       if (outputReceivers[ch]!= null){
           outputReceivers[ch].send(message, timeStamp);
       }
    }

    /**
     * Explicity closes all output receivers, and nulls the array.
     * This differs slightly from the use described in {@link MidiDevice} in that
     * the ReceiverMarshaller itself is not neccesarily closed when this is called.
     * After being closed, new receivers can continue to be added to the ReceiverMarshaller.
     */
    public void close() {
        for (Receiver r: outputReceivers){
            if (r!= null){
                r.close();
            }
        }
        Arrays.fill(outputReceivers, null);
    }
}
