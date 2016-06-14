/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import cz.cvut.lhotamir.mwlcollection.messages.Message;
import cz.cvut.lhotamir.mwlcollection.messages.StartFailMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StartMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StartSuccessMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StopFailMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StopSuccessMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class
 * <code>Control</code> represents a thread that receives commands from remote
 * client and executes them.
 *
 * @author Miroslav LHOTAN
 */
public class Control implements Runnable {

    private SurveillanceThread surveillanceThread;
    private boolean running;
    private Socket socket;

    public Control(SurveillanceThread t, Socket socket) {
        this.surveillanceThread = t;
        running = true;
        this.socket = socket;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        try {
            running = true;

            ObjectInputStream in;
            ObjectOutputStream out;
            Message mes;
            System.out.println("GSM Data Collection");




            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Client connected from: " + socket.getInetAddress());
            mes = (Message) in.readObject();
            while (mes.getCode() != -1) {
                int code = mes.getCode();
                switch (code) {
                    case 1:
                        StartMessage startMes=(StartMessage)mes;
                        if (surveillanceThread.startCollection(startMes.getNodeSet())) {
                            out.writeObject(new StartSuccessMessage());
                        } else {
                            System.out.println("Collection already running");
                            out.writeObject(new StartFailMessage());
                        }
                        break;
                    case 2:
                        if (!surveillanceThread.getSber().isEmpty()) {
                            surveillanceThread.stopCollection();
                            out.writeObject(new StopSuccessMessage());
                        } else {
                            System.out.println("Collection is not running right now");
                            out.writeObject(new StopFailMessage());
                        }
                        break;
                    case 8:
                        if (!surveillanceThread.getSber().isEmpty()) {
                            surveillanceThread.stopCollection();

                            System.out.println("Server quitting");
                            System.exit(0);
                        } else {
                            System.out.println("Server quitting");
                            System.exit(0);
                        }
                        break;
                    /*case 9:
                        StateMessage stateMes = new StateMessage();
                        if (surveillanceThread.getSber().isRunning()) {
                            stateMes.setRunning(true);
                            stateMes.setRunningSince(surveillanceThread.getSber().getRunningSince());
                        } else {
                            stateMes.setRunning(false);
                        }
                        out.writeObject(stateMes);
                        break;*/
                    case 10:
                        DataReader reader = new DataReader(null, null, "*", null, null);
                        reader.getNodes(out);
                        break;
                    /*case 11:
                        int nodesAdded = surveillanceThread.getSber().addNodes(in);
                        out.writeObject(new AddNodesMessage(nodesAdded));
                        break;
                    case 12:
                        int linksAdded = surveillanceThread.getSber().addLinks(in);
                        out.writeObject(new AddLinksMessage(linksAdded));
                        break;
                    case 13:
                        reader = new DataReader(null, null, "*", null, null);
                        reader.getLinks(out);
                        break;
                    case 14:
                        ReadRecordsMessage readRec=(ReadRecordsMessage)mes;
                        reader = new DataReader(readRec.getFrom(), readRec.getTo(), "*", readRec.getFromNodeName(), readRec.getToNodeName());
                        reader.getRecords(out);
                        break;*/
                }
                mes = (Message) in.readObject();
            }

            in.close();
            out.close();
            socket.close();
            System.out.println("Control client quitting");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Control.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
