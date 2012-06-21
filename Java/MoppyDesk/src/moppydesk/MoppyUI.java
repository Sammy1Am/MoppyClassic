/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import moppydesk.outputs.ReceiverMarshaller;
import moppydesk.ui.MoppyControlWindow;
import org.jdesktop.application.SingleFrameApplication;

/**
 *
 * @author Sam
 */
public class MoppyUI extends SingleFrameApplication{

    public MoppySequencer ms = null;
    public Receiver[] outputReceivers = new Receiver[16];
    public ReceiverMarshaller rm = new ReceiverMarshaller(outputReceivers);
    
    public Preferences prefs = Preferences.userNodeForPackage(MoppyUI.class);
    
    @Override
    protected void startup() {
        //MoppyMainWindow mainWindow = new MoppyMainWindow(this);
        MoppyControlWindow mainWindow = new MoppyControlWindow(this);
        show(mainWindow);
    }
    
     /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(MoppyUI.class, args);
    }
    
    public MoppySequencer initializeSequencer() {
        try {
            if (ms!= null){
                ms.closeSequencer();
                ms = null;
            }

            ms = new MoppySequencer(rm);
            
        } catch (NoSuchPortException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PortInUseException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedCommOperationException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ms;
    }
    
    public void shutdownSequencer(){
        if (ms != null){
            ms.closeSequencer();
            ms = null;
        }
    }

    public void savePreferences(){
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void putPreferenceObject(String key, Object object){
        try {
            prefs.putByteArray(key, serializePref(object));
        } catch (IOException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Object getPreferenceObject(String key){
        try {
            return deserializePref(prefs.getByteArray(key, null));
        } catch (NullPointerException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.WARNING, "No preference set for "+key, ex);
        } catch (IOException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MoppyUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
//Preference to object
  private static Object deserializePref(byte[] b) throws NullPointerException, IOException, ClassNotFoundException
  {
    return new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
  }
  
//Object to preference
  private static byte[] serializePref(Object p) throws IOException
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(p);
    oos.close();
    return baos.toByteArray();
  }
    
}
