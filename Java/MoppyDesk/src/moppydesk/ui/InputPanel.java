package moppydesk.ui;

import java.awt.event.KeyEvent;
import javax.sound.midi.Transmitter;
import javax.swing.JPanel;

/**
 *
 * @author Sam
 */
public abstract class InputPanel extends JPanel{
    /** Returns the {@link Trasmitter} being controlled by this panel*/
    abstract Transmitter getTransmitter();
    /** Called when the outputs are connected to the input device*/
    abstract void connected();    
    /** Called when the outputs are disconnected from the input device*/
    abstract void disconnected();
    
    //Optional methods that define how preferences are saved/loaded for a given input panel    
    public void savePreferences() {};
    public void loadPreferences() {};
    
    //Another optional method to define how a panel handles the application shutting down
    //  If you're going to override this method, either include savePreferenes(); or call the base method
    public void shuttingDown() { savePreferences(); };
    
    //Optional Methods that define how an input panel reacts to these key events
    public boolean enterKeyAction(KeyEvent e) { return false; };
    public boolean tabKeyAction(KeyEvent e) { return false; };
    public boolean upKeyAction(KeyEvent e) { return false; };
    public boolean downKeyAction(KeyEvent e) { return false; };
}
