/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Represents a message sent from the client when exiting. Server then ends the 
 * thread executing the commands from this particular client.
 * 
 * @author Miroslav LHOTAN
 */
public class ClientQuitMessage extends Message{

    public ClientQuitMessage() {
        code=-1;
    }
    
}
