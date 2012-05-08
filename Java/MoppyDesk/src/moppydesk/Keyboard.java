package moppydesk;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiMessage;

/**
 *
 * @author miguelduarte42
 */
public class Keyboard implements Receiver{

    private MidiDevice inputDevice;
    private Transmitter transmitter;
    private MoppyPlayer mp;
    
    private int numberOfDrives = 5;
    
    private int currentMode;    
    private int currentNoteIndex = 0;
    private int[] notes = new int[numberOfDrives];
    
    private int currentNote;
    
    public static String[] MODES = {"Single Channel", "Balanced", "Round Robin"}; 
    
    public Keyboard(String deviceName, MoppyPlayer mp) {
        try {
            MidiDevice.Info info = getMidiDeviceInfo(deviceName);
            
            inputDevice = MidiSystem.getMidiDevice(info);
            inputDevice.open();
            
            transmitter = inputDevice.getTransmitter();
            transmitter.setReceiver(this);
            
            this.mp = mp;
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private MidiDevice.Info getMidiDeviceInfo(String deviceName) {
        
        MidiDevice.Info correctInfo = null;
        
        for(MidiDevice.Info info : MidiSystem.getMidiDeviceInfo())
            if(info.getName().equals(deviceName)) {
                correctInfo = info;
                break;
            }
        
        return correctInfo;
    }

    public void send(MidiMessage mm, long l) {
        
        MoppyPlayer.MidiInfo info = mp.new MidiInfo(mm);
        
        switch(currentMode) {
            case 0:
                sendSingleChannel(mm,l,info);
                break;
            case 1:
                sendBalanced(mm,l,info);
                break;
            case 2:
                sendRoundRobin(mm,l,info);
                break;
            default:
                mp.send(mm,l,info);
        }
    }
    
    /*
     * This selection mode will play a single note on all the drives
     */
    private void sendSingleChannel(MidiMessage mm, long l,  MoppyPlayer.MidiInfo info) {
        
        if(info.event > 0) { //Note ON
            
            //We need to silence the previous note, in case it is still being played
            
            //Save the correct value so we can send it later
            //and send the STOP message to all the drives
            int previousEvent = info.event;
            info.event = 0;
            for(int i = 1 ; i <= numberOfDrives ; i++) {
                info.position = i;
                mp.send(mm, l, info);
            }
            
            //Restore the correct value and
            //send the ON message to all the drives
            info.event = previousEvent;
            for(int i = 1 ; i <= numberOfDrives ; i++) {
                info.position = i;
                mp.send(mm, l, info);
            }
            
        }
    }
    
    /*
     * This selection mode will try to maximize the number of drives playing at the same time,
     * increasing the total volume. If you have 6 drives and are playing just 1 note, all the
     * drives will play that note. If you then play another note at the same time, this mode
     * will allocate 3 of the 6 drives to play each of the 2 notes.
     */
    private void sendBalanced(MidiMessage mm, long l,  MoppyPlayer.MidiInfo info) {
        //TODO
    }
    
    /*
     * This selection mode plays every not on a different drive. If you are playing one
     * particular note and then play another one at the same time, the second note will
     * be played on the next drive. If you then stop playing the first note, this method
     * will find which drive it belonged to and will turn it off.
     */
    private void sendRoundRobin(MidiMessage mm, long l,  MoppyPlayer.MidiInfo info) {
        
        if(info.event > 0) { //if note ON
            
            notes[currentNoteIndex] = info.period;
            
            //pins start at 1
            info.position = currentNoteIndex+1;
            
            //prepare the index for the next note (loops around the notes array)
            currentNoteIndex = (currentNoteIndex+1) % notes.length;
            
        } else { //if note OFF
            
            for(int i = 0 ; i < notes.length; i++) { 
                
                //found the note to turn off, and its corresponding drive
                if(notes[i] == info.period) {
                    
                    //send this particular message to the correct drive
                    info.position = i+1;
                    
                    //reset the stored value for this note
                    notes[i+1] = 0;
                }
            }
            
        }
    }

    public void close() {
        transmitter.close();
        inputDevice.close();
    }
    
    public void setMode(String mode) {
        for(int i = 0 ; i < MODES.length ; i++) {
            if(MODES[i].equals(mode)) {
                currentMode = i;
                break;
            }
        }
    }
}