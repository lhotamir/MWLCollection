/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

/**
 * Class <code>LinkRow</code> represents parsed row from config file for adding new links.
 * @author Miroslav LHOTAN
 */
public class LinkRow {
    private String name;
    private String farEndName;
    private String ifName;
    private String farEndIfName;

    public LinkRow(String name, String farEndName, String ifName, String farEndIfName) {
        this.name = name;
        this.farEndName = farEndName;
        this.ifName = ifName;
        this.farEndIfName = farEndIfName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFarEndName() {
        return farEndName;
    }

    public void setFarEndName(String farEndName) {
        this.farEndName = farEndName;
    }

    public String getIfName() {
        return ifName;
    }

    public void setIfName(String ifName) {
        this.ifName = ifName;
    }

    public String getFarEndIfName() {
        return farEndIfName;
    }

    public void setFarEndIfName(String farEndIfName) {
        this.farEndIfName = farEndIfName;
    }

    @Override
    public String toString() {
        return "LinkRow{" + "name=" + name + ", farEndName=" + farEndName + ", ifName=" + ifName + ", farEndIfName=" + farEndIfName + '}';
    }
    
    
}
