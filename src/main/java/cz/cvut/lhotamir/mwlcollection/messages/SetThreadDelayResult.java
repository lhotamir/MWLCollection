/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 *
 * @author thara
 */
public class SetThreadDelayResult extends Message{
    private boolean success;

    public SetThreadDelayResult(boolean success) {
        this.success = success;
        code = 21;
    }

    public boolean isSuccess() {
        return success;
    }
    
}
