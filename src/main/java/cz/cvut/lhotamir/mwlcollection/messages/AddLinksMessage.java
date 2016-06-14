/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 *
 * @author Tharadalf
 */
public class AddLinksMessage extends Message{
    private int added;
    public AddLinksMessage() {
        code=12;
        added=0;
    }

    public int getAdded() {
        return added;
    }

    public AddLinksMessage(int added) {
        this.added = added;
    }

    
    
    
}
