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
public class SetThreadDelayMessage extends Message{
    private int threadNum;
    private int delay;

    public SetThreadDelayMessage(int threadNum, int delay) {
        this.threadNum = threadNum;
        this.delay = delay;
        code = 20;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public int getDelay() {
        return delay;
    }
    
}
