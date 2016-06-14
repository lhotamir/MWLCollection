/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>DataReader</code> class serves for reading application data stored in the database.
 * 
 * @author Miroslav LHOTAN
 */
public class DataReader {

    private String from;
    private String to;
    private String file;
    private Connection con;
    private String fromNodeName;
    private String toNodeName;
/**
 * Creates new instance of <code>DataReader</code> for reading data from database, if reading request comes from remote client.
 * @param from Starting date of the desired time interval.
 * @param to Ending date of the desired time interval.
 * @param file Desired path to the file in which data will be exported. For stdout the name is "-".
 * @param fromNodeName Name of the start node of the desired link.
 * @param toNodeName  Name of the end node of the desired link.
 */
    public DataReader(String from, String to, String file, String fromNodeName, String toNodeName) {
        this.from = from;
        this.to = to;
        this.file = file;
        this.fromNodeName = fromNodeName;
        this.toNodeName = toNodeName;
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost/gsm", "gsmapplication", "gsmapp");
        } catch (SQLException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
            con = null;
        }
    }
/**
 * Creates new instance of <code>DataReader</code> for reading data from the database, when reading is requested via server command.
 */
    public DataReader() {
        this.from = null;
        this.to = null;
        this.file = null;
        this.fromNodeName = null;
        try {
            con = DriverManager.getConnection("jdbc:postgresql://localhost/gsm", "gsmapplication", "gsmapp");
        } catch (SQLException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
            con = null;
        }
    }
/**
 * Gets neccessary information for reading individual records from the user, when reading is requested via server command.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean readRecords() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter output file name (enter \"-\" for stdout): ");

        file = input.nextLine();
        System.out.println("Enter demanded start node name: ");
        fromNodeName = input.nextLine();
        System.out.println("Enter demanded end node name: ");
        toNodeName = input.nextLine();

        System.out.println("Enter first time interval bound in this format \"YEAR-MONTH-DAY HOUR:MINUTE\" : ");
        from = input.nextLine();
        //from="'"+from+"'";
        System.out.println("Enter second time interval bound in the same format: ");
        to = input.nextLine();
        //to="'"+to+"'";


        return getRecords(null);
    }
/**
 * Gets neccessary information for reading nodes information from the user, when reading is requested via server command.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean readNodes() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter output file name (enter \"-\" for stdout): ");
        file = input.nextLine();
        return getNodes(null);
    }
/**
 * Gets neccessary information for reading links information from the user, when reading is requested via server command.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean readLinks() {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter output file name (enter \"-\" for stdout): ");
        file = input.nextLine();
        return getLinks(null);
    }
/**
 * Composes database query for reading records and writes the results in the file, stdout or network stream.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean getRecords(ObjectOutputStream output) {
        if (con == null) {
            System.err.println("Could not establish database connection.");
            return false;
        }
        PrintStream out;
        if (file.equals("-")) {
            out = System.out;
        } else if (file.equals("*")) {
            out=new PrintStream(output, true);
        } else {
            try {
                out = new PrintStream(new File(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Cannot create file.");
                return false;
            }
        }
        try {

            PreparedStatement query = con.prepareStatement("select l.linkid as \"linkid\","
                    + "r.\"time\","
                    + "f.name as \"node name\","
                    + "t.name as \"far node name\","
                    + "r.txpower,"
                    + "r.rxpower "
                    + "from record as r inner join link as l on (r.linkid=l.linkid) "
                    + "inner join node as f on (l.fromnodeid=f.nodeid) "
                    + "inner join node as t on (l.tonodeid=t.nodeid) "
                    + "where r.\"time\" between to_timestamp(?,'YYYY-MM-DD HH24:MI') and to_timestamp(?,'YYYY-MM-DD HH24:MI') and f.name=? and t.name=? "
                    + "order by \"time\" asc;");

            query.setString(1, from);
            query.setString(2, to);
            query.setString(3, fromNodeName);
            query.setString(4, toNodeName);
            System.out.println("Executing query...");
            ResultSet rs = query.executeQuery();
            System.out.println("Writing...");
            while (rs.next()) {
                int linkid = rs.getInt(1);
                String time = rs.getString(2);
                String fname = rs.getString(3);
                String tname = rs.getString(4);
                int txpower = rs.getInt(5);
                double rxpower = rs.getDouble(6);
                out.println(linkid + ";" + time + ";" + fname + ";" + tname + ";" + txpower + ";" + rxpower);
            }
            if(file.equals("*")){
                out.println("end");
            }
            rs.close();
            query.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getLocalizedMessage());
            return false;
        }
        System.out.println("Query complete.");
        return true;
    }
/**
 * Composes database query for reading links information and writes the results in the file, stdout or network stream.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean getLinks(ObjectOutputStream output) {
        if (con == null) {
            System.err.println("Could not establish database connection.");
            return false;
        }
        PrintStream out;
        if (file.equals("-")) {
            out = System.out;
        } else if (file.equals("*")) {
            out=new PrintStream(output, true);
        } else {
            try {
                out = new PrintStream(new File(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Cannot create file.");
                return false;
            }
        }
        try {
            PreparedStatement query = con.prepareStatement("select l.linkid,f.name,t.name from link as l inner join node as f on(l.fromnodeid=f.nodeid) inner join node as t on (l.tonodeid=t.nodeid);");
            System.out.println("Executing query...");
            ResultSet rs = query.executeQuery();
            System.out.println("Writing...");
            while (rs.next()) {
                out.println(rs.getInt(1) + ";" + rs.getString(2) + ";" + rs.getString(3));
            }
            if(file.equals("*")){
                out.println("end");
            }
            rs.close();
            query.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getLocalizedMessage());
            return false;
        }
        System.out.println("Query complete.");
        return true;
    }
/**
 * Composes database query for reading nodes inforamtion and writes the results in the file, stdout or network stream.
 * 
 * @return True if reading was successful, false if not.
 */
    public boolean getNodes(ObjectOutputStream output) {
        if (con == null) {
            System.err.println("Could not establish database connection.");
            return false;
        }
        PrintStream out;
        if (file.equals("-")) {
            out = System.out;
        } else if (file.equals("*")) {
            out=new PrintStream(output, true);
        } else {
            try {
                out = new PrintStream(new File(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Cannot create file.");
                return false;
            }
        }
        try {
            PreparedStatement query = con.prepareStatement("select node.nodeid,node.name,node.ipaddress,node.lat,node.long from node;");
            System.out.println("Executing query...");
            ResultSet rs = query.executeQuery();
            System.out.println("Writing...");
            while (rs.next()) {
                out.println(rs.getInt(1) + ";" + rs.getString(2) + ";" + rs.getString(3) + ";" + rs.getDouble(4) + ";" + rs.getDouble(5));
            }
            if(file.equals("*")){
                out.println("end");
            }
            rs.close();
            query.close();
            con.close();

        } catch (SQLException ex) {
            Logger.getLogger(DataReader.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.getLocalizedMessage());
            return false;
        }
        System.out.println("Query complete.");
        return true;
    }
}
