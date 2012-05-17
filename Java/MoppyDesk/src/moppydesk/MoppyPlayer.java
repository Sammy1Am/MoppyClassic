package moppydesk;

import gnu.io.SerialPort;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

/**
 *
 * @author Sammy1Am
 */
public class MoppyPlayer implements Receiver {

    /**
     * The periods for each MIDI note in an array.  The floppy drives
     * don't really do well outside of the defined range, so skip those notes.
     * Periods are in microseconds because that's what the Arduino uses for its
     * clock-cycles in the micro() function, and because milliseconds aren't
     * precise enough for musical notes.
     */
    public static int[] microPeriods = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        30578, 28861, 27242, 25713, 24270, 22909, 21622, 20409, 19263, 18182, 17161, 16198, //C1 - B1
        15289, 14436, 13621, 12856, 12135, 11454, 10811, 10205, 9632, 9091, 8581, 8099, //C2 - B2
        7645, 7218, 6811, 6428, 6068, 5727, 5406, 5103, 4816, 4546, 4291, 4050, //C3 - B3
        3823, 3609, 3406, 3214, 3034, 2864, 2703, 2552, 2408, 2273, 2146, 2025, //C4 - B4
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };
    
    /**
     * Resolution of the Arduino code in microSeconds.
     */
    public static int ARDUINO_RESOLUTION = 40;
    
    /**
     * Current period of each MIDI channel (zero is off) as set 
     * by the NOTE ON message; for pitch-bending.
     */
    private int[] currentPeriod = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    
    MoppyBridge mb;
    SerialPort com;

    public MoppyPlayer(MoppyBridge newMb) {
        mb = newMb;
    }

    public void close() {
        mb.close();
    }

    //Is called by Java MIDI libraries for each MIDI message encountered.
    public void send(MidiMessage message, long timeStamp) {
        if (message.getStatus() > 127 && message.getStatus() < 144) { // Note OFF
            //Convert the MIDI channel being used to the controller pin on the
            //Arduino by multipying by 2.
            byte pin = (byte) (2 * (message.getStatus() - 127));
    
            //System.out.println("Got note OFF on pin: " + (pin & 0xFF));
            mb.sendEvent(pin, 0);
            currentPeriod[message.getStatus() - 128] = 0;    
        } else if (message.getStatus() > 143 && message.getStatus() < 160) { // Note ON
            //Convert the MIDI channel being used to the controller pin on the
            //Arduino by multipying by 2.
            byte pin = (byte) (2 * (message.getStatus() - 143));
    
            //Get note number from MIDI message, and look up the period.
            //NOTE: Java bytes range from -128 to 127, but we need to make them
            //0-255 to use for lookups.  & 0xFF does the trick.

            // After looking up the period, devide by (the Arduino resolution * 2).
            // The Arduino's timer will only tick once per X microseconds based on the
            // resolution.  And each tick will only turn the pin on or off.  So a full
            // on-off cycle (one step on the floppy) is two periods.
            int period = microPeriods[(message.getMessage()[1] & 0xff)] / (ARDUINO_RESOLUTION * 2);

            //System.out.println("Got note ON on pin: " + (pin & 0xFF) + " with period " + period);
            //System.out.println(message.getLength() + " " + message.getMessage()[message.getLength()-1]);

            //Zero velocity events turn off the pin.
            if (message.getMessage()[2] == 0) {
                mb.sendEvent(pin, 0);
                currentPeriod[message.getStatus() - 144] = 0;
            } else {
                mb.sendEvent(pin, period);
                currentPeriod[message.getStatus() - 144] = period;
            }
        } else if (message.getStatus() > 223 && message.getStatus() < 240) { //Pitch bends
            //Only proceed if the note is on (otherwise, no pitch bending)
            if (currentPeriod[message.getStatus() - 224] != 0) {
                //Convert the MIDI channel being used to the controller pin on the
                //Arduino by multipying by 2.
                byte pin = (byte) (2 * (message.getStatus() - 223));

                double pitchBend = ((message.getMessage()[2] & 0xff) << 8) + (message.getMessage()[1] & 0xff);

                int period = (int) (currentPeriod[message.getStatus() - 224] / Math.pow(2.0, (pitchBend - 8192) / 8192) * 2);
                //System.out.println(currentPeriod[message.getStatus() - 224] + "-" + period);
                mb.sendEvent(pin, period);
            }
        }

    }
}
