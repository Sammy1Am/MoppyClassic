package moppydesk;

/**
 *
 * @author Sammy1Am
 */
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        MoppyBridge mb = null;
        MoppyPlayer mp = null;

        try {
            mb = new MoppyBridge("COM6"); //Create MoppyBridge on the COM port with the Arduino
            mp = new MoppyPlayer(mb);

            mb.resetDrives();

            //Load a MIDI file (TODO: Make this an argument, or a GUI window)
            Sequence sequence = MidiSystem.getSequence(new File("samplesongs/KirbysTheme.mid"));

            final Sequencer sequencer = MidiSystem.getSequencer(false);

            //Start a new thread to listen on the command-line to exit the program early
            new Thread(){
                @Override
            public void run(){
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                 while (true){
                        try {
                            if (br.readLine().equalsIgnoreCase("exit")) {
                                sequencer.stop();
                                sequencer.close();
                                System.exit(0);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                 }
            }
            }.start();

            
            //Start the sequencer, and set the tempo (not being read from file!)
            sequencer.open();
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(160);
            System.out.println(sequence.getTracks().length);
            sequencer.getTransmitter().setReceiver(mp); // Set MoppyPlayer as a receiver.

            sequencer.start(); //GO!
            
            //Wait til the sequence is done...
            while (sequencer.isRunning()){
                Thread.sleep(3000);
            }
            
            //Close the sequencer
            sequencer.close();

        } catch (MidiUnavailableException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidMidiDataException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPortException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PortInUseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedCommOperationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            //Reset everything and close down cleanly (hopefully)
            if (mb != null){
                mb.resetDrives();
                mb.close();
            }
            if (mp != null){
                mp.close();
            }
        }

        System.exit(0);
    }
}
