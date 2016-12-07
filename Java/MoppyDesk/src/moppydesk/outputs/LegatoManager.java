/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package moppydesk.outputs;

public class LegatoManager {
    int currentNote = 0;
    
    void noteOn(int note) {
        currentNote = note;
        
//        System.out.println("Note turned on: " + note);
//        System.out.println("Current note: " + currentNote);
    }
    
    boolean noteOff(int note) {
        if(note == currentNote) {
            currentNote = 0;
            
//          System.out.println("Note turned off: " + note);
//          System.out.println("Current note: " + currentNote);
            
            return true;
        }
        return false;
    }
}
