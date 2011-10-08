/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

/**
 *
 * @author Sam
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
            mb = new MoppyBridge("COM3");
            mp = new MoppyPlayer(mb);

            mb.resetDrives();

            



            Sequence sequence = MidiSystem.getSequence(new File("songs/ImperialMarch.mid"));

            final Sequencer sequencer = MidiSystem.getSequencer(false);

            new Thread(){
                @Override
            public void run(){
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                 while (true){
                        try {
                            if (br.readLine().equalsIgnoreCase("exit")) {
                                System.exit(0);
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                 }
            }
            }.start();

            sequencer.open();
            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(110);
            System.out.println(sequence.getTracks().length);
            sequencer.getTransmitter().setReceiver(mp);

            sequencer.start();
            while (sequencer.isRunning()){
                Thread.sleep(3000);
            }
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
