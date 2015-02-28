/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package moppydesk;

/**
 *
 * @author Sam
 */
public interface MoppyStatusConsumer {
    public void tempoChanged(int newTempo);
    public void sequenceEnded();
}
