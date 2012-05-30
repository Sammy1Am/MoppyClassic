package moppydesk;

import javax.sound.midi.*;

/**
 * @author miguelduarte42
 */
public class Keyboard implements Receiver{

    private MidiDevice inputDevice;
    private Transmitter transmitter;
    private MoppyPlayer mp;
    
    private int numberOfDrives = 5;
    
    private int currentMode = 0;
    private int currentNoteIndex = 0;
    private int[] notes = new int[numberOfDrives];
    
    private int noteCount = 0;
    
    public static String[] MODES = {"Single Channel", "Round Robin"}; 
    
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
        
        //Keep track of the notes that are being played. This is necessary in cases
        //where you press two keys, and then release one of them
        if(info.noteOn)
            noteCount++;
        else
            noteCount--;
        
        if(info.event == 0) {
            if(noteCount == 0) {
                for(int i = 1 ; i <= MoppyBridge.MAX_DRIVES ; i++) {
                    info.position = i;
                    mp.send(mm, l, info);
                }
            }
        }else{
            for(int i = 1 ; i <= MoppyBridge.MAX_DRIVES ; i++) {
                    info.position = i;
                    mp.send(mm, l, info);
                }
        }
    }
    
    /*
     * This selection mode plays every not on a different drive. If you are playing one
     * particular note and then play another one at the same time, the second note will
     * be played on the next drive. If you then stop playing the first note, this method
     * will find which drive it belonged to and will turn it off.
     */
    private void sendRoundRobin(MidiMessage mm, long l,  MoppyPlayer.MidiInfo info) {
        
        if(info.noteOn) {
            
            boolean freeSpot = false;
            
            //Try to find a drive that is currently silenced
            for(int i = 0 ; i < notes.length ; i++) {
                if(notes[i] == 0) {
                    freeSpot = true;
                    currentNoteIndex = i;
                    break;
                }
            }
            
            //If every drive is busy, just select the next one
            if(!freeSpot)
                currentNoteIndex = (currentNoteIndex+1) % notes.length;
            
            //Save the position of the current note so we can silence the drive later
            notes[currentNoteIndex] = info.message[1];
            
            //Pins start at 1, not 0
            info.position = currentNoteIndex+1;
            
        } else { //if note OFF
            for(int i = 0 ; i < notes.length; i++) { 
                
                if(notes[i] == info.message[1]) {
                    //Found the note to turn off, and its corresponding drive (drives start at 1, not 0)
                    info.position = i+1;
                    
                    //Reset the stored value for this note, since the drive will be silenced
                    notes[i] = 0;
                    break;
                }
            }
        }
        
        mp.send(mm, l, info);
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