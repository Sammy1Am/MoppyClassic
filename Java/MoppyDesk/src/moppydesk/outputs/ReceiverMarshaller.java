/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk.outputs;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

/**
 *
 * @author Sam
 */
public class ReceiverMarshaller implements Receiver{

    Receiver[] outputReceivers = new Receiver[16];
    
    public ReceiverMarshaller(Receiver[] receivers){
        outputReceivers = receivers;
    }
    
    public void send(MidiMessage message, long timeStamp) {
       int ch = ((ShortMessage)message).getChannel();
       if (outputReceivers[ch]!= null){
           outputReceivers[ch].send(message, timeStamp);
       }
    }

    public void close() {
        for (Receiver r: outputReceivers){
            if (r!= null){
                r.close();
            }
        }
    }
}
