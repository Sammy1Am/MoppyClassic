package moppydesk.midputs;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import moppydesk.Constants;

/**
 * This class will re-map notes from specified incoming channels to a pool of
 * output channels based on one of several strategies.
 */
public class DrivePooler implements Receiver, Transmitter {

    public enum PoolingStrategy {

        STRAIGHT_THROUGH, ROUND_ROBIN, STACK
    };
    Receiver receiver = null;
    private int startInput = 0;
    private int endInput = 0;
    private int startOutput = 0;
    private int endOutput = 7;
    private PoolingStrategy currentStrategy = PoolingStrategy.STRAIGHT_THROUGH;
    private int[] currentNotes = new int[Constants.NUM_MIDI_CHANNELS];
    private int rrNextNote = 0;

    public void setInputRange(int start, int end) {
        startInput = start - 1;
        endInput = end - 1;
    }

    public void setOutputRange(int start, int end) {
        startOutput = start - 1;
        endOutput = end - 1;
    }

    public void setStrategy(PoolingStrategy newStrat) {
        allOff();
        currentStrategy = newStrat;
    }

    public void send(MidiMessage message, long timeStamp) {
        if (receiver != null) {
            if (message instanceof ShortMessage
                    && (message.getMessage()[0] & 0xFF) > 127 && (message.getMessage()[1] & 0xFF) < 160) {
                switch (currentStrategy) {
                    case STRAIGHT_THROUGH:
                        receiver.send(message, timeStamp); //No modifications
                        break;
                    case ROUND_ROBIN:
                        receiver.send(roundRobinMap(message), timeStamp);
                        break;
                    case STACK:
                        receiver.send(stackMap(message), timeStamp);
                }
            }
        }
    }

    private MidiMessage roundRobinMap(MidiMessage message) {

        if (rrNextNote < startOutput || rrNextNote > endOutput) {
            rrNextNote = startOutput;
        }

        ShortMessage mappedMessage = (ShortMessage) message;
        try {
            if (mappedMessage.getChannel() >= startInput && mappedMessage.getChannel() <= endInput) {
                if (mappedMessage.getCommand() == ShortMessage.NOTE_OFF || (mappedMessage.getCommand() == ShortMessage.NOTE_ON && mappedMessage.getData2() == 0)) {
                    int noteNumber = mappedMessage.getData1();
                    for (int n = startOutput; n <= endOutput; n++) {
                        if (currentNotes[n] == noteNumber) {
                            currentNotes[n] = -1;
                            mappedMessage.setMessage(mappedMessage.getCommand(), n, mappedMessage.getData1(), mappedMessage.getData2());
                            break; // Only turn off one of the notes
                        }
                    }
                } else if (mappedMessage.getCommand() == ShortMessage.NOTE_ON) {
                    int targetNote = rrNextNote;

                    while (targetNote <= endOutput) {
                        if (currentNotes[targetNote] < 0) {
                            break; // If that one's free, we're good.
                        } else if (targetNote == endOutput) {
                            targetNote = startOutput;
                        } else {
                            targetNote++;
                        }

                        if (targetNote == rrNextNote) {
                            break; //We've gone around once, stop!
                        }
                    }

                    currentNotes[targetNote] = mappedMessage.getData1();
                    mappedMessage.setMessage(mappedMessage.getCommand(), targetNote, mappedMessage.getData1(), mappedMessage.getData2());

                    rrNextNote++;
                }
            } else {
                return message; //It's not in the mapping range, let it pass through...
            }
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(DrivePooler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mappedMessage;

    }

    private MidiMessage stackMap(MidiMessage message) {
        ShortMessage mappedMessage = (ShortMessage) message;
        try {
            //If it's in the chosen range:
            if (mappedMessage.getChannel() >= startInput && mappedMessage.getChannel() <= endInput) {
                if (mappedMessage.getCommand() == ShortMessage.NOTE_OFF || (mappedMessage.getCommand() == ShortMessage.NOTE_ON && mappedMessage.getData2() == 0)) {
                    int noteNumber = mappedMessage.getData1();
                    for (int n = startOutput; n <= endOutput; n++) {
                        if (currentNotes[n] == noteNumber) {
                            currentNotes[n] = -1;
                            mappedMessage.setMessage(mappedMessage.getCommand(), n, mappedMessage.getData1(), mappedMessage.getData2());
                        }
                    }
                } else if (mappedMessage.getCommand() == ShortMessage.NOTE_ON) {

                    int targetChannel = 0;

                    for (int n = startOutput; n <= endOutput; n++) {
                        if (currentNotes[n] < 0) {
                            targetChannel = n;
                            break;
                        }
                    }

                    currentNotes[targetChannel] = mappedMessage.getData1();
                    mappedMessage.setMessage(mappedMessage.getCommand(), targetChannel, mappedMessage.getData1(), mappedMessage.getData2());
                }
            } else {
                return message; //It's not in the mapping range, let it pass through...
            }
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(DrivePooler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mappedMessage;
    }

    private void allOff() {
        Arrays.fill(currentNotes, -1);
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
