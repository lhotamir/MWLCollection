/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the server to the client as a response to <code>StartMesage</code>.
 * It indicates the collection has not been stopped, because it is not runnng at
 * the moment.
 * @author Miroslav LHOTAN
 */
public class StopFailMessage extends Message{

    public StopFailMessage() {
        code=6;
    }
    
}
