/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 * Sent from the client to the server as a command to start data collection.
 * 
 * @author Miroslav LHOTAN
 */
public class StartMessage extends Message{
    private int nodeSet;
    public StartMessage(int nodeSet){
        this.nodeSet=nodeSet;
        code=1;
    }

    public int getNodeSet() {
        return nodeSet;
    }
    
}
