/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the server to the client as a response to <code>StartMesage</code>.
 * It indicates the collection has started successfuly and is running now.
 * 
 * @author Miroslav LHOTAN
 */
public class StartSuccessMessage extends Message{

    public StartSuccessMessage() {
        code=3;
    }
    
}
