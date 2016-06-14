/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

/**
 * Class <code>NodeRow</code> represents parsed row from config file for adding new nodes.
 * @author Miroslav LHOTAN
 */
public class NodeRow {
    private String ipAddress;
    private double lat;
    private double lon;
    private String name;
    private int nodeSet;

    
/**
 * 
 * @param ipAddress IP address of the added node as read from the config file.
 * @param lat Latitude as read from the config file.
 * @param lon Longitude as read from the config file.
 * @param name Node name as read from the config file.
 */
    public NodeRow(String ipAddress, double lat, double lon, String name,int nodeSet) {
        this.ipAddress = ipAddress;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.nodeSet=nodeSet;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getNodeSet() {
        return nodeSet;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
