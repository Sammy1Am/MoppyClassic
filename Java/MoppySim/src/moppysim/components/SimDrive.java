/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moppysim.components;

import com.jsyn.ports.UnitOutputPort;
import com.jsyn.unitgen.SquareOscillator;
import moppysim.ui.DrivePanel;

/**
 *
 * @author Sam
 */
public class SimDrive {
    
    public SquareOscillator so = new SquareOscillator();
    public DrivePanel drivePanel = new DrivePanel();
    
    public SimDrive(){
        so.noteOff();
    }
    
    public void setNote(int periodData){
        
        if (periodData>0){
            so.noteOn(1000000/periodData, 0.05);
            drivePanel.playingNote(1000000/periodData);
        } else {
            so.noteOff();
            drivePanel.playingNote(0);
        }
    }
    
    public void resetDrive(){
        drivePanel.resetDrive();
    }
    
    public UnitOutputPort getOutput(){
        return so.getOutput();
    }
}
