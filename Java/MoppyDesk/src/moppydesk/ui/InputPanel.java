package moppydesk.ui;

import javax.sound.midi.Transmitter;
import javax.swing.JPanel;

/**
 *
 * @author Sam
 */
public abstract class InputPanel extends JPanel{
    /** Returns the {@link Trasmitter} being controled by this panel*/
    abstract Transmitter getTransmitter();
    /** Called when the outputs are connected to the input device*/
    abstract void connected();
    /** Called when the outputs are disconnected from the input device*/
    abstract void disconnected();
}
