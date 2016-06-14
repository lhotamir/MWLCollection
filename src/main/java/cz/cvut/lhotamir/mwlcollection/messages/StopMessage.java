/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the client to the server as a command to stop data collection.
 * @author Miroslav LHOTAN
 */
public class StopMessage extends Message{
    public StopMessage(){
        code=2;
    }
}
