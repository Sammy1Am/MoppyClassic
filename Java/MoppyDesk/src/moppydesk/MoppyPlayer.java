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
    
    public void send(MidiMessage message, long timeStamp) {
        send(message,timeStamp,null);
    }

    //Is called by Java MIDI libraries for each MIDI message encountered.
    public void send(MidiMessage message, long timeStamp, MidiInfo info) {
        
        if(info == null)
            info = new MidiInfo(message);
        
        if (info.status > 127 && info.status < 144) { // Note OFF
            //Convert the MIDI channel being used to the controller pin on the
            //Arduino by multipying by 2.

            byte pin = (byte) (2 * (info.position));

            //System.out.println("Got note OFF on pin: " + (pin & 0xFF));
            mb.sendEvent(pin, info.event);
            currentPeriod[info.status - 128] = 0;
        } else if (info.status > 143 && info.status < 160) { // Note ON
            //Convert the MIDI channel being used to the controller pin on the
            //Arduino by multipying by 2.
            byte pin = (byte) (2 * (info.position));

            //Zero velocity events turn off the pin.
            if (info.message[2] == 0) {
                mb.sendEvent(pin, info.event);
                currentPeriod[info.status - 144] = 0;
            } else {
                mb.sendEvent(pin, info.period);
                currentPeriod[info.status - 144] = info.period;
            }
        } else if (info.status > 223 && info.status < 240) { //Pitch bends
            //Only proceed if the note is on (otherwise, no pitch bending)
            if (currentPeriod[info.status - 224] != 0) {
                //Convert the MIDI channel being used to the controller pin on the
                //Arduino by multipying by 2.
                byte pin = (byte) (2 * (info.status - 223));
                //System.out.println(currentPeriod[message.getStatus() - 224] + "-" + period);
                mb.sendEvent(pin, info.period);
            }
        }

    }
    public class MidiInfo {
        
        //Corresponds to the note being played
        public int period;
        
        //Information on the amount of pitchbend
        public double pitchBend;
        
        //Position of the floppy drive. Starts at 1 and ends at MAXIMUM_NUMBER_OF_DRIVES
        public int position;
        
        //Description of the MIDI event
        public int event = 1;
        
        public int status;
        public byte message[];
        
        public boolean noteOn = false;

        public MidiInfo(MidiMessage midiMessage) {
            status = midiMessage.getStatus();
            message = midiMessage.getMessage();

            if (status > 127 && status < 144) { // Note OFF
                position = status - 127;
                event = 0;
            } else if (status > 143 && status < 160) { // Note ON
                position = status - 143;
                noteOn = true;

                //Get note number from MIDI message, and look up the period.
                //NOTE: Java bytes range from -128 to 127, but we need to make them
                //0-255 to use for lookups.  & 0xFF does the trick.

                // After looking up the period, devide by (the Arduino resolution * 2).
                // The Arduino's timer will only tick once per X microseconds based on the
                // resolution.  And each tick will only turn the pin on or off.  So a full
                // on-off cycle (one step on the floppy) is two periods.
                period = microPeriods[(message[1] & 0xff)] / (ARDUINO_RESOLUTION * 2);

                //Zero velocity events turn off the pin.
                if (message[2] == 0) {
                    event = 0;
                    noteOn = false;
                }

            } else if (status > 223 && status < 240) { //Pitch bends
                //Only proceed if the note is on (otherwise, no pitch bending)
                if (currentPeriod[status - 224] != 0) {
                    noteOn = true;
                    pitchBend = ((message[2] & 0xff) << 8) + (message[1] & 0xff);
                    period = (int) (currentPeriod[status - 224] / Math.pow(2.0, (pitchBend - 8192) / 8192));
                }
            }
        }
    }
}
