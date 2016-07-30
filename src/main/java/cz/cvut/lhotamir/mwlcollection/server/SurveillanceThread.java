/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents thread that is relaying command to the collection thread
 * from either console or from remote clients. It is also accepting connections
 * from these clients. Default listening port is 3999.
 * 
 * @author Miroslav LHOTAN
 */
public class SurveillanceThread implements Runnable{

    private int port;
    private boolean running;
    private List<MWLCollection> sber;
    private List<Thread> GSMsberThread;
/**
 * Creates new instance of <code>SurveillanceThread</code>. Also creates new instance
 * of <code>GSMsber</code> to be ready for starting collection.
 * 
 * @param port Port number on which the thread is set to listnening on.
 */
    public SurveillanceThread(int port) {
        this.port = port;
        this.running = true;
        sber = new ArrayList<MWLCollection>();
        GSMsberThread = new ArrayList<Thread>();
    }

    public List<MWLCollection> getSber() {
        return sber;
    }

    public void addSber(MWLCollection sber) {
        this.sber.add(sber);
    }
/**
 * Starts the data collection as a new thread.
 */
    public boolean startCollection(int nodeSet) {
        for(int i=0;i<sber.size();i++){
            if(nodeSet==sber.get(i).getNodeSet()){
                return false;
            }
        }
        
        MWLCollection newCollection=new MWLCollection();
        newCollection.setNodeSet(nodeSet);
        sber.add(newCollection);
        newCollection.setRunning(true);
        GSMsberThread.add(new Thread(newCollection));
        GSMsberThread.get(GSMsberThread.size()-1).start();
        return true;
    }
/**
 * Stops data collection and waits for the collection thread to end. Also creates
 * new instance of <code>GSMsber</code> to be ready for starting new collection.
 */
    public void stopCollection() {
        for(int i=0;i<sber.size();i++){
            sber.get(i).setRunning(false);
            try {
                GSMsberThread.get(i).join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SurveillanceThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sber.clear();
        GSMsberThread.clear();
    }

    @Override
    public void run() {
        try {
            ServerSocket sSocket = new ServerSocket(port);

            while (running) {
                Socket socket = sSocket.accept();
                System.out.println("Client connecting from: "+socket.getInetAddress());
                new Thread(new Control(this,socket)).start();
        
            }
        } catch (IOException ex) {
            Logger.getLogger(SurveillanceThread.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    int getDelay() {
        if(!sber.isEmpty()){
            return sber.get(0).getDelay();
        }
        return -1;
    }

    void setDelay(int delay) {
        for(int i=0;i<sber.size();i++){
            sber.get(i).setDelay(delay);
        }
    }
    
    boolean setThreadDelay(int thread, int delay){
        try{
            sber.get(thread).setDelay(delay);
            return true;
        }catch(ArrayIndexOutOfBoundsException e){
            System.err.println("Thread number: "+thread+" is not running");
            return false;
        }
    }
}
