/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

/**
 * <code>Node</code> represents entity Node as it is stored in the database.
 * @author Miroslav LHOTAN
 */
public class Node {
    /** This is an OID for a MIB sub-tree containing radio input power values from the whole node.*/
    private static final String inputPowerOID = "1.3.6.1.4.1.193.81.3.4.3.1.3.1.10.";
    
    /** This is an OID for a MIB sub-tree containing radio output power values from the whole node.*/
    private static final String outputPowerOID = "1.3.6.1.4.1.193.81.3.4.3.1.3.1.1.";
    
    /** This is an OID for a MIB sub-tree containing radio frequency values from the whole node.*/
    private static final String baseTXFrequencyOID = "1.3.6.1.4.1.193.81.3.4.3.1.2.1.1.";
    
    /** This is an OID for a MIB sub-tree containing interface names from the whole node.*/
    private static final String ifNameOID = "1.3.6.1.2.1.31.1.1.1.1.";
    protected String ipAddress;
    protected String name;
    protected Statement insert;
    protected int nodeID;
    protected List<Link> links;
    protected Snmp snmp;
    protected UserTarget target;
/**
 * In the constructor the values read from the database are stored in new instance of Node 
 * and for each instance new SNMP Target is created too.
 * 
 * @param ipAddress Address of the Node in the network
 * @param nodeID ID of the node in the database
 * @param name Name of the node in the database
 * @param snmp SNMP instance
 */
    public Node(String ipAddress, int nodeID, String name, Snmp snmp) {
        this.ipAddress = ipAddress;
        links = new ArrayList<Link>();
        this.nodeID = nodeID;
        this.snmp = snmp;
        this.name = name;

        target = new UserTarget();
        target.setAddress(GenericAddress.parse("udp:" + ipAddress + "/161"));
        target.setRetries(1);
        target.setTimeout(2000);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(SecurityLevel.AUTH_NOPRIV);
        target.setSecurityName(new OctetString("control_user"));
    }
/**
 * Sets the insert statement.
 * 
 * @param insert SQL statement for inserting data into database.
 */
    public void setInsert(Statement insert) {
        this.insert = insert;
    }

    public String getIpAddress() {
        return ipAddress;

    }

    public String getName() {
        return name;
    }

    public int getNodeID() {
        return nodeID;
    }

    @Override
    public String toString() {
        String ret = Integer.toString(nodeID) + " " + ipAddress + "\n";
        for (Link link : links) {
            ret += (link.toString() + "\n");
        }

        return ret;
    }
/**
 * Adds new link starting from this Node to into it. This happens when loading data 
 * from database.
 * 
 * @param link Link to be added 
 */
    public void addLink(Link link) {
        links.add(link);
    }
/**
 * Lists all interfaces from the node. It reads all the interface's names one-by-one and 
 * then return list of <code>VariableBindings</code> returned from the device.
 * 
 * @return List of interface names and their OIDs
 */
    public List<VariableBinding> getInterfaces() {
        PDU pdu = new ScopedPDU();
        PDU responsePDU;
        pdu.add(new VariableBinding(new OID(ifNameOID)));
        List<VariableBinding> names = new ArrayList<VariableBinding>();
        //pdu.setMaxRepetitions(200);
        pdu.setType(PDU.GETNEXT);
        try {
            do {
                ResponseEvent response = snmp.send(pdu, target);
                responsePDU = response.getResponse();
                names.add(responsePDU.get(0));
                pdu.clear();
                pdu.add(responsePDU.get(0));
                //System.out.println(responsePDU.get(0));
            }while(responsePDU.get(0).getOid().startsWith(new OID(ifNameOID)));
        } catch (IOException ex) {
            Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
            names = null;
        }
        return names;
    }

    /**
     * Reads values from all the links starting at this Node and writes them
     * into the database. This is where the SNMP GET request is formed and sent 
     * to the device through the target object. If no response arrives in time, no
     * data are stored and current link is simply skipped.
     * 
     * @throws IOException
     * @throws SQLException
     */
    public void getValues() throws IOException, SQLException {

        for (Link link : links) {
            PDU pdu = new ScopedPDU();
            pdu.add(new VariableBinding(new OID(outputPowerOID + link.getTxOID())));
            pdu.add(new VariableBinding(new OID(inputPowerOID + link.getRxOID())));
            pdu.add(new VariableBinding(new OID(baseTXFrequencyOID + link.getTxOID())));
            pdu.setType(PDU.GET);



            try {
                ResponseEvent response = snmp.send(pdu, target);
                PDU responsePDU = response.getResponse();
                int txPower = responsePDU.getVariable(new OID(outputPowerOID + link.getTxOID())).toInt();

                double rxPower = responsePDU.getVariable(new OID(inputPowerOID + link.getRxOID())).toInt();
                rxPower /= 10;
                int frequency = responsePDU.getVariable(new OID(baseTXFrequencyOID + link.getTxOID())).toInt();

                insert.executeUpdate("insert into record(linkid,\"time\",frequency,rxpower,txpower) values(" + link.getLinkID() + ",'now'," + frequency + "," + rxPower + "," + txPower + ")");
            } catch (NullPointerException e) {
                System.out.println("TIMEOUT: " + link.toString());
                continue;
            }//System.out.println("Tx: "+txPower+" Rx: "+rxPower+" f: "+frequency);


        }

    }
}
