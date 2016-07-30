/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is ran when the application is started. It starts a
 * SurveillanceThread and then passes console commands onto it.
 *
 * @author Miroslav LHOTAN
 */
public class Console {

    public static void main(String[] args) throws InterruptedException {

        String input;
        Scanner scan = new Scanner(System.in);
        int port;

        try {
            port = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("No paremeter found, using default port 3999.");
            port = 3999;
        }
        SurveillanceThread surveillanceThread = new SurveillanceThread(port);
        new Thread(surveillanceThread).start();
        System.out.println("MINI-LINK signal strength data collection.");
        while (true) {
            input = scan.nextLine();

            if (input.matches("^start \\d+$")) {
                int nodeSet = Integer.parseInt(input.substring(6));
                if (surveillanceThread.startCollection(nodeSet)) {
                    System.out.println("Data collection on node set " + nodeSet + " started.");
                } else {
                    System.out.println("Data collection on node set " + nodeSet + " already running.");
                }
            } else if (input.equalsIgnoreCase("stop")) {
                if (!surveillanceThread.getSber().isEmpty()) {
                    surveillanceThread.stopCollection();
                } else {
                    System.out.println("Data collection is not running right now");
                }
            } else if (input.equalsIgnoreCase("stop server")) {
                if (!surveillanceThread.getSber().isEmpty()) {
                    surveillanceThread.stopCollection();
                }
                System.out.println("Stopping server");
                System.exit(0);
            } else if (input.matches("^state \\d+$")) {
                List<MWLCollection> collections = surveillanceThread.getSber();
                int nodeSet = Integer.parseInt((input.split(" ")[1]));
                int collectionSet = -1;
                for (int i = 0; i < collections.size(); i++) {
                    if (nodeSet == collections.get(i).getNodeSet()) {
                        collectionSet = i;
                    }
                }
                if (collectionSet != -1) {
                    Date time = new Date();
                    time.setTime(collections.get(collectionSet).getRunningSince());
                    System.out.println("Data collection on node set " + nodeSet + " is running since:" + time.toString());
                } else {
                    System.out.println("Data collection on node set " + nodeSet + " is not running right now");
                }
            } else if (input.startsWith("add links")) {
                try {
                    if (surveillanceThread.getSber().isEmpty()) {
                        FileInputStream file = new FileInputStream(input.substring(10));
                        new MWLCollection().addLinks(file);
                        try {
                            file.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        System.out.println("Collection running right now please stop before adding new links.");
                    }

                } catch (FileNotFoundException ex) {
                    System.err.println("File not found.");
                    Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (input.startsWith("add nodes")) {
                FileInputStream file = null;
                try {
                    if (surveillanceThread.getSber().isEmpty()) {
                        file = new FileInputStream(input.substring(10));
                        new MWLCollection().addNodes(file);
                        try {
                            file.close();
                        } catch (IOException ex) {
                            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }else{
                        System.out.println("Collection running right now please stop before adding new nodes.");
                    }

                } catch (FileNotFoundException ex) {
                    System.err.println("File not found.");
                    Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (input.equalsIgnoreCase("get delay")) {
                System.out.println("Delay is : " + surveillanceThread.getDelay() + " ms.");
            } else if (input.startsWith("set delay")) {
                try {
                    int delay = Integer.parseInt(input.substring(10));
                    surveillanceThread.setDelay(delay);
                } catch (NumberFormatException e) {
                    System.err.println("Bad input.");
                }
            } else if (input.startsWith("set thread delay")) {
                try {
                    int thread = Integer.parseInt(input.split(" ")[3]);
                    int delay = Integer.parseInt(input.split(" ")[4]);
                    surveillanceThread.setThreadDelay(thread, delay);
                } catch (NumberFormatException e) {
                    System.err.println("Bad input.");
                }
            } else if (input.equalsIgnoreCase("read records")) {
                new DataReader().readRecords();
            } else if (input.equalsIgnoreCase("read links")) {
                new DataReader().readLinks();
            } else if (input.equalsIgnoreCase("read nodes")) {
                new DataReader().readNodes();
            } else {
                System.out.println("Unknown command");
            }

        }
    }
}
