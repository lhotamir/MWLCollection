/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.messages;

/**
 *
 * @author Tharadalf
 */
public class ReadRecordsMessage extends Message{

    private String to;
    private String from;
    private String fromNodeName;
    private String toNodeName;

    public ReadRecordsMessage(String to, String from, String fromNodeName, String toNodeName) {
        code=14;
        this.to = to;
        this.from = from;
        this.fromNodeName = fromNodeName;
        this.toNodeName = toNodeName;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public String getFromNodeName() {
        return fromNodeName;
    }

    public String getToNodeName() {
        return toNodeName;
    }
    
}
