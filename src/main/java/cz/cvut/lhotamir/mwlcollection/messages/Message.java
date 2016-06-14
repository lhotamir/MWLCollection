/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

import java.io.Serializable;

/**
 * Represents a message sent between client and server.
 * 
 * @author Miroslav LHOTAN
 */
public abstract class Message implements Serializable{
    protected int code;
    
    public Message() {
        code = 0;
    }

    public int getCode() {
        return code;
    }
}
