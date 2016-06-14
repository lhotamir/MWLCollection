/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 *
 * @author Tharadalf
 */
public class AddNodesMessage extends Message{
    private int added;
    public AddNodesMessage() {
        code=11;
    }

    public AddNodesMessage(int added) {
        this.added = added;
    }

    public int getAdded() {
        return added;
    }
    
}
