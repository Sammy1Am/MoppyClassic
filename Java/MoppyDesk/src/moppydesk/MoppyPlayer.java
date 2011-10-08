/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import gnu.io.SerialPort;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
/**
 *
 * @author Sam
 */
public class MoppyPlayer implements Receiver {

    public static int[] microPeriods = {
        0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,
        30578,28861,27242,25713,24270,20409,21622,20409,19263,18182,17161,16198, //C1 - B1
        15289,14436,13621,12856,12135,11454,10811,10205,9632,9091,8581,8099, //C2 - B2
        7645,7218,6811,6428,6068,5727,5406,5103,4816,4546,4291,4050, //C3 - B3
        3823,3609,3406,3214,3034,2864,2703,2552,2408,2273,2146,2025, //C4 - B4
        0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0
    };

    MoppyBridge mb;
    SerialPort com;

    public MoppyPlayer(MoppyBridge newMb) {
        mb = newMb;
    }

    public void close(){
        mb.close();
    }

    public void send(MidiMessage message, long timeStamp) {
        if (message.getStatus() > 127 && message.getStatus() < 144){ // Note OFF
            byte pin = (byte)(2*(message.getStatus() - 127));
            //System.out.println("Got note OFF on pin: " + (channel & 0xFF));
            mb.sendEvent(pin, 0);
        }
        else if (message.getStatus() > 143 && message.getStatus() < 160){ // Note ON
            byte pin = (byte)(2*(message.getStatus() - 143));
            int period = microPeriods[(message.getMessage()[1] & 0xff)];
            //System.out.println("Got note ON on pin: " + (pin & 0xFF) + " with period " + period);
            mb.sendEvent(pin, period);
        }
    }
}
