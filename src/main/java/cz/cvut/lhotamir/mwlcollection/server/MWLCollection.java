/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.MPv3;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * This is the main class of the whole application. It's main goal is to collect
 * data from the devices in the network. Collection is running as a separate
 * thread.
 *
 * @author Miroslav LHOTAN
 */
public class MWLCollection implements Runnable {

    private Snmp snmp;
    private USM usm;
    private TransportMapping transport;
    private boolean Running;
    private long runningSince;
    private int delay;
    private int nodeSet;

    public MWLCollection() {
        this.Running = false;
        delay = 200;
        nodeSet = 0;
    }

    public int getDelay() {
        return delay;
    }

    public void setNodeSet(int nodeSet) {
        this.nodeSet = nodeSet;
    }

    public int getNodeSet() {
        return nodeSet;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public long getRunningSince() {
        return runningSince;
    }

    public boolean isRunning() {
        return Running;
    }

    public void setRunning(boolean Running) {
        this.Running = Running;
    }

    /**
     * This method adds new Nodes to the database. It reads the file
     * line-by-line and stores the values into the database.
     *
     * @param file Path to file with configuration data.
     */
    public int addNodes(InputStream file) {
        if (Running) {
            System.out.println("Collection running. Cannot add any nodes.");
            return 0;
        }
        String ipAddress;
        String sep = System.getProperty("line.separator");
        ArrayList<NodeRow> nodes = new ArrayList<NodeRow>();

        Scanner scan = new Scanner(file);
        while (scan.hasNext()) {
            scan.useDelimiter(sep + "|;");
            String address = scan.next();
            if (address.equals("end")) {
                break;
            }
            double lat = Double.parseDouble(scan.next());
            double lon = Double.parseDouble(scan.next());
            String name = scan.next();
            int newNodeSet = Integer.parseInt(scan.next());
            nodes.add(new NodeRow(address, lat, lon, name, newNodeSet));
            System.out.println(nodes.get(nodes.size() - 1));
        }

        Connection con = null;
        PreparedStatement insertNode = null;
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost/gsm", "gsmapplication", "gsmapp");
            insertNode = con.prepareStatement("insert into node(ipaddress,lat,\"long\",name,nodeset) values(?,?,?,?,?)");
        } catch (SQLException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        int nodesAdded = 0;
        for (NodeRow nodeRow : nodes) {
            try {
                insertNode.setString(1, nodeRow.getIpAddress());
                insertNode.setDouble(2, nodeRow.getLat());
                insertNode.setDouble(3, nodeRow.getLon());
                insertNode.setString(4, nodeRow.getName());
                insertNode.setInt(5, nodeRow.getNodeSet());
                insertNode.executeUpdate();
            } catch (SQLException ex) {
                System.err.println(ex.getLocalizedMessage());
                //Logger.getLogger(GSMsber.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            nodesAdded++;
        }
        try {
            insertNode.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(nodesAdded + " nodes added.");
        return nodesAdded;
    }

    /**
     *
     * This method add new Links to the database similarly as the
     * <code>addNodes</code> method. In addition to the previous method it
     * matches provided interface names with the OIDs for near end and far end
     * interfaces.
     *
     * @param file Path to file with configuration data.
     */
    public int addLinks(InputStream file) {
        if (Running) {
            System.out.println("Collection running. Cannot add any links.");
            return 0;
        }
        Connection con = null;
        PreparedStatement nodeQuery = null, insertLink = null;
        ResultSet rs = null;
        ArrayList<LinkRow> links = new ArrayList<LinkRow>();
        ArrayList<Node> nodes = new ArrayList<Node>();
        String sep = System.getProperty("line.separator");

        Scanner scan = new Scanner(file);

        while (scan.hasNext()) {
            scan.useDelimiter(sep + "|;");
            String from = scan.next();
            if (from.equals("end")) {
                break;
            }
            String to = scan.next();
            String ifName = scan.next();
            String farIfName = scan.next();
            links.add(new LinkRow(from, to, ifName, farIfName));
            System.out.println(links.get(links.size() - 1));
        }
        //scan.close();

        try {
            setUpSnmp();
        } catch (IOException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost/gsm", "gsmapplication", "gsmapp");
            nodeQuery = con.prepareStatement("select * from node where name=?");
            insertLink = con.prepareStatement("insert into link(ifname,rxoid,txoid,fromnodeid,tonodeid,frequency) values(?,?,?,?,?)");
        } catch (SQLException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }

        int linksAdded = 0;
        for (LinkRow linkRow : links) {
            Node node;
            Node farNode;
            try {
                nodeQuery.setString(1, linkRow.getName());
                rs = nodeQuery.executeQuery();
                rs.next();
                node = new Node(rs.getString("ipaddress"), rs.getInt("nodeid"), rs.getString("name"), snmp);
            } catch (SQLException ex) {
                System.err.println(ex.getLocalizedMessage());
                //Logger.getLogger(GSMsber.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            try {
                nodeQuery.setString(1, linkRow.getFarEndName());
                rs = nodeQuery.executeQuery();
                rs.next();
                farNode = new Node(rs.getString("ipaddress"), rs.getInt("nodeid"), rs.getString("name"), snmp);
            } catch (SQLException ex) {
                System.err.println(ex.getLocalizedMessage());
                //Logger.getLogger(GSMsber.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }

            List<VariableBinding> interfaces = node.getInterfaces();
            try {
                insertLink.setString(1, linkRow.getIfName());
                insertLink.setInt(2, Integer.parseInt(getRxOID(interfaces, linkRow.getFarEndIfName())));
                insertLink.setInt(3, Integer.parseInt(getTxOID(interfaces, linkRow.getIfName())));
                insertLink.setInt(4, node.getNodeID());
                insertLink.setInt(5, farNode.getNodeID());
                insertLink.executeUpdate();
            } catch (SQLException ex) {
                System.err.println(ex.getLocalizedMessage());
                //Logger.getLogger(GSMsber.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            linksAdded++;
        }
        try {
            rs.close();
            nodeQuery.close();
            insertLink.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println(linksAdded + " links added.");
        return linksAdded;
    }

    /**
     * This method sets up SNMP for operation and adds the user used for
     * querying to its system.
     *
     * @throws IOException
     */
    public void setUpSnmp() throws IOException {
        transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        usm = new USM(SecurityProtocols.getInstance(),
                new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        transport.listen();

        snmp.getUSM().addUser(new OctetString("control_user"),
                new UsmUser(new OctetString("control_user"),
                        AuthMD5.ID,
                        new OctetString("ericsson"),
                        null, null));
        snmp.getUSM().addUser(new OctetString("admin"),
                new UsmUser(new OctetString("admin"),
                        AuthMD5.ID,
                        new OctetString("Ericsson2016"),
                        null, null));
    }

    /**
     * This is the method that is ran when new thread is started. First it reads
     * all the nodes from the database. Then it reads all the links and assigns
     * them to the nodes from where they start. Then it goes through the nodes
     * and links one-by-one, reads the values from the devices and writes them
     * to the database.
     */
    @Override
    public void run() {
        Connection con = null;
        PreparedStatement nodesQuery = null, linksQuery = null, ebandQuery = null;
        Statement insertData;
        ResultSet rs = null;
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<EBandNode> ebandNodes = new ArrayList<>();
        runningSince = (System.currentTimeMillis());
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost/gsm", "gsmapplication", "gsmapp");
            if (nodeSet != 0) {
                nodesQuery = con.prepareStatement("select * from node where nodeset=? and nodeid < 10000;");
                nodesQuery.setInt(1, nodeSet);
            } else {
                nodesQuery = con.prepareStatement("select * from node where nodeid < 10000;");
            }
            linksQuery = con.prepareStatement("select * from link where fromnodeid=?;");
            ebandQuery = con.prepareStatement("select * from node where nodeid > 10000 and nodeset=?;");
            insertData = con.createStatement();

            rs = nodesQuery.executeQuery();
            try {
                setUpSnmp();
            } catch (IOException ex) {
                Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (rs.next()) {
                nodes.add(new Node(rs.getString("ipaddress"), rs.getInt("nodeid"), rs.getString("name"), snmp));
            }
            for (Node node : nodes) {
                linksQuery.setInt(1, node.getNodeID());
                rs = linksQuery.executeQuery();
                while (rs.next()) {
                    node.addLink(new Link(rs.getInt("linkid"), Integer.toString(rs.getInt("txoid")), Integer.toString(rs.getInt("rxoid")), rs.getString("ifName")));
                }
                node.setInsert(insertData);

            }

            // add E-Band nodes initialization here
            ebandQuery.setInt(1, nodeSet);
            rs = ebandQuery.executeQuery();
            while (rs.next()) {
                ebandNodes.add(new EBandNode(rs.getString("ipaddress"), rs.getInt("nodeid"), rs.getString("name"), snmp));
            }
            for (EBandNode ebandNode : ebandNodes) {
                linksQuery.setInt(1, ebandNode.getNodeID());
                rs = linksQuery.executeQuery();
                while (rs.next()) {
                    ebandNode.setLinkId(rs.getInt("linkid"));
                    int opNodeID = rs.getInt("tonodeid");
                    EBandNode opNode = null;
                    for (EBandNode op : ebandNodes) {
                        if (op.getNodeID() == opNodeID) {
                            opNode = op;
                        }
                    }
                    ebandNode.setOppositeNode(opNode);
                    ebandNode.setInsert(insertData);
                }
            }

            System.out.println("Starting data collection.");
            while (Running) {
                for (Node node : nodes) {
                    try {
                        node.getValues();
                    } catch (IOException ex) {
                        Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                for (EBandNode eBandNode : ebandNodes) {
                    eBandNode.getValues();
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            rs.close();
            nodesQuery.close();
            ebandQuery.close();
            con.close();
            insertData.close();
            try {
                snmp.close();
            } catch (IOException ex) {
                Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(MWLCollection.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Stopping data collection.");

    }

    private String getTxOID(List<VariableBinding> interfaces, String ifName) {
        int index = 0;

        OID oid;
        for (int i = 0; i < interfaces.size(); i++) {
            if (interfaces.get(i).getVariable().toString().equals(ifName)) {
                index = i;
                break;
            }
        }
        oid = interfaces.get(index).getOid();

        return Integer.toString(oid.last());
    }

    private String getRxOID(List<VariableBinding> interfaces, String farEndIfName) {
        int index = 0;

        OID oid;
        for (int i = 0; i < interfaces.size(); i++) {
            if (interfaces.get(i).getVariable().toString().equals(farEndIfName)) {
                index = i;
                break;
            }
        }

        oid = interfaces.get(index).getOid();

        return Integer.toString(oid.last());
    }
}
