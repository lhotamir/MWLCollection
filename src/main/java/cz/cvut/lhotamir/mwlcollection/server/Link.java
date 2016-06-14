/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

/**
 * The <code>Link</code> class represents one direction of a real link in the real 
 * network. 
 * @author Miroslav LHOTAN
 */
public class Link {
    private String txOID;
    private String rxOID;
    private String ifName;
    private int linkID;
    /**
     * Link class constructor.
     * @param linkID ID of the Link as saved in the database.
     * @param txOID OID of the object storing transmitting power on the near end of the link.
     * @param rxOID OID of the object storing received signal power on the far end of the link.
     * @param ifName Name of the near end interface of the link.
     */
    public Link(int linkID,String txOID,String rxOID,String ifName){
        this.ifName=ifName;
        this.txOID=txOID;
        this.rxOID=rxOID;
        this.linkID=linkID;
    }
    @Override
    public String toString(){
        return "LinkID: "+linkID+" ifName: "+ifName+" RxOID: "+rxOID+" TxOID: "+txOID;
    }

    public String getTxOID() {
        return txOID;
    }

    public String getRxOID() {
        return rxOID;
    }

    public int getLinkID() {
        return linkID;
    }
    
}
