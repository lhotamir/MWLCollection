/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

/**
 *
 * @author Miroslav
 */
public class EBandNode extends Node {

    private static final String inputPowerOID = ".1.3.6.1.4.1.193.223.2.7.1.1.1";
    private static final String outputPowerOID = ".1.3.6.1.4.1.193.223.2.7.1.1.2";
    private int linkId;
    private EBandNode oppositeNode;

    public EBandNode(String ipAddress, int nodeID, String name, Snmp snmp) {
        super(ipAddress, nodeID, name, snmp);
        target.setSecurityName(new OctetString("admin"));
    }

    public void setOppositeNode(EBandNode opNode) {
        this.oppositeNode = opNode;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    private int getSNMPValue(String oid) throws IOException, NullPointerException {
        PDU pdu = new ScopedPDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        int value = 0;

        ResponseEvent response = snmp.send(pdu, target);
        PDU responsePDU = response.getResponse();
        value = responsePDU.getVariable(new OID(oid)).toInt();

        return value;
    }

    public double getInputPowerValue() throws IOException, NullPointerException {
        double value;

        value = getSNMPValue(inputPowerOID);
        return value / 10;
    }

    public int getOutputPowerValue() throws IOException, NullPointerException {
        int value;

        value = getSNMPValue(outputPowerOID);
        return value;
    }

    @Override
    public void getValues() {
        try {
            //To change body of generated methods, choose Tools | Templates.
            int outputPowerValue = getOutputPowerValue();
            double inputPowerValue = oppositeNode.getInputPowerValue();
            insert.executeUpdate("insert into record(linkid,\"time\",rxpower,txpower) values(" + this.linkId + ",'now'," + inputPowerValue + "," + outputPowerValue + ")");
        } catch (IOException | SQLException ex) {
            Logger.getLogger(EBandNode.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            System.out.println("EBAND TIMEOUT: " + this.toString());
            ex.printStackTrace();
        }

    }

}
