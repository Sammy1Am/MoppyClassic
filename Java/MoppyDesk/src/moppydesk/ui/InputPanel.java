package moppydesk.ui;

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
    
    //MrSolidSnake745: Below two optional methods define how preferences are saved/loaded for a given input panel    
    public void savePreferences() {}; //Called when connecting or switching panels on main window dropdown (MoppyControlWindow: , connect())
    public void loadPreferences() {}; //Called when main window class is instantiated (MoppyControlWindow: constructor)
}
