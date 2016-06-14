/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cvut.lhotamir.mwlcollection.client;

import cz.cvut.lhotamir.mwlcollection.messages.ClientQuitMessage;
import cz.cvut.lhotamir.mwlcollection.messages.Message;
import cz.cvut.lhotamir.mwlcollection.messages.ServerStopMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StartMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StateMessage;
import cz.cvut.lhotamir.mwlcollection.messages.StopMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a client that remotely connects to the server and is sending commands.
 * @author Miroslav LHOTAN
 */
public class Client {
    
    public static void main(String[] args) {
        try {
            System.out.println("GSM Collection control");
            Scanner scan = new Scanner(System.in);
            String input;
            System.out.println("Enter IP adress of data collection server:");
            input = scan.nextLine();
            System.out.println("Enter communication port:");
            int port=scan.nextInt();
            scan.nextLine();
            Socket socket;
            InetAddress host = InetAddress.getByName(input);
            socket = new Socket(host, port);
            ObjectInputStream in;
            ObjectOutputStream out;
            Message mes;
            System.out.println("Connected to: " + socket.getInetAddress());
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            
            
            while (true) {
                input = scan.nextLine();
                if (input.matches("^start \\d+$")) {
                    int nodeSet= Integer.parseInt(input.substring(6));
                    out.writeObject(new StartMessage(nodeSet));
                    mes = (Message) in.readObject();
                    if (mes.getCode() == 3) {
                        System.out.println("Starting collection");
                        continue;
                    }
                    if (mes.getCode() == 4) {
                        System.out.println("Collection already running");
                    }
                } else if (input.equalsIgnoreCase("stop")) {
                    out.writeObject(new StopMessage());
                    mes = (Message) in.readObject();
                    if (mes.getCode() == 5) {
                        System.out.println("Stopping collection");
                        continue;
                    }
                    if (mes.getCode() == 6) {
                        System.out.println("Collection is not running right now");
                    }
                } else if (input.equalsIgnoreCase("stop server")) {
                    out.writeObject(new ServerStopMessage());
                    System.out.println("Stopping server");
                    break;
                    
                } else if (input.equalsIgnoreCase("state")) {
                    out.writeObject(new StateMessage());
                    StateMessage reply=(StateMessage)in.readObject();
                    if(reply.isRunning()){
                        Date time=new Date();
                        time.setTime(reply.getRunningSince());
                        System.out.println("Data collection is running since:"+time.toString());
                    }else{
                        System.out.println("Data collection is not running right now");
                    }
                } else if (input.equalsIgnoreCase("quit")) {
                    out.writeObject(new ClientQuitMessage());
                    break;
                } else {
                    System.out.println("Unknown command");
                }
            }
            in.close();
            out.close();
            socket.close();
            System.out.println("Quitting.");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("Server disconnected");
            System.exit(1);
        }
    }
}
