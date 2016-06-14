/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the client as a query on the collection state. Server sends it back
 * provided with information about the collection state.
 * 
 * @author Miroslav LHOTAN
 */
public class StateMessage extends Message{
    private boolean running;
    private long runningSince;
    public StateMessage() {
        code=9;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public long getRunningSince() {
        return runningSince;
    }

    public void setRunningSince(long runningSince) {
        this.runningSince = runningSince;
    }
    
    
}
