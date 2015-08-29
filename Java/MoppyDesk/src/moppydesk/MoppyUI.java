/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import moppydesk.inputs.MoppySequencer;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.awt.EventQueue;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.JOptionPane;
import moppydesk.inputs.MoppyMIDIInput;
import moppydesk.outputs.ReceiverMarshaller;
import moppydesk.ui.MoppyControlWindow;

/**
 *
 * @author Sam
 */
public class MoppyUI {

    MoppyControlWindow mainWindow;
    //Input objects
    public MoppySequencer ms;
    public MoppyMIDIInput midiIn;
    /**
     * The {@link ReceiverMarshaller} will be added as a receiver to whatver
     * input object is selected.
     */
    public ReceiverMarshaller rm = new ReceiverMarshaller();
    public Preferences prefs = Preferences.userNodeForPackage(MoppyUI.class);

    protected void startup() {
        //Initialize parts
        try {
            ms = new MoppySequencer(rm);
            midiIn = new MoppyMIDIInput();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.toString());
        }
        mainWindow = new MoppyControlWindow(this);
        mainWindow.setStatus("Initializing...");
        mainWindow.setVisible(true);
        mainWindow.setStatus("Initialized.");
        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() { shutdownTasks(); }
        }));
    }

    //Things to do/cleanup before we close the application
    private void shutdownTasks() {
        mainWindow.currentInputPanel.shuttingDown();
        rm.close();
    }
    
    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MoppyUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MoppyUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MoppyUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MoppyUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MoppyUI ui = new MoppyUI();
                ui.startup();
            }
        });
    }

    public void savePreferences() {
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void putPreferenceObject(String key, Object object) {
        try {
            prefs.putByteArray(key, serializePref(object));
        } catch (IOException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Object getPreferenceObject(String key) {
        try {
            return deserializePref(prefs.getByteArray(key, null));
        } catch (NullPointerException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.WARNING, "No preference set for " + key, ex);
        } catch (IOException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

//Preference to object
    private static Object deserializePref(byte[] b) throws NullPointerException, IOException, ClassNotFoundException {
        return new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
    }

//Object to preference
    private static byte[] serializePref(Object p) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p);
        oos.close();
        return baos.toByteArray();
    }
}
