/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

import moppydesk.arduino.MoppyComSequencer;
import org.jdesktop.application.SingleFrameApplication;

/**
 *
 * @author Sam
 */
public class MoppyUI extends SingleFrameApplication {

    MoppySequencer ms = null;

    @Override
    protected void startup() {
        MoppyMainWindow mainWindow = new MoppyMainWindow(this);
        show(mainWindow);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            launch(MoppyUI.class, args);
        }else{
            //there come the commandline interface
        }
    }
}
