/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the server to the client as a response to <code>StartMesage</code>.
 * It indicates the collection has been successfuly stopped.
 * @author Miroslav LHOTAN
 */
public class StopSuccessMessage extends Message{

    public StopSuccessMessage() {
        code=5;
    }
    
}
