/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the server to the client as a response to <code>StartMesage</code>.
 * It indicates the collection is already running.
 * 
 * @author Miroslav LHOTAN
 */
public class StartFailMessage extends Message{

    public StartFailMessage() {
        code=4;
    }
    
}
