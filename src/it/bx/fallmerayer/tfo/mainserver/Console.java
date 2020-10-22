package it.bx.fallmerayer.tfo.mainserver;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Scanner;

//A basic console to perform simple operations directly on the server
public class Console extends Mainserver implements Runnable{

    @Override
    public void run() {
        System.out.println(Colors.ANSI_YELLOW + "***Welcome to the Server's Console!***\nTo get help, type /help" + Colors.ANSI_RESET);
        synchronized (log){
            log.append("***Welcome to the Server's Console!***\nTo get help, type /help\n");
        }
        while (true) {
            Scanner sc = new Scanner(System.in);

            switch (sc.nextLine()){
                case "/help":       //Prints some instructions on how to use the server console
                    System.out.println(Colors.ANSI_YELLOW + "To shut down the server, type /shutdown\nTo view a list of all users, please type /userlist\nTo view only the currently active users, type /activeusers!" + Colors.ANSI_RESET);
                    synchronized (log){
                        log.append("To shut down the server, type /shutdown\nTo view a list of all users, please type /userlist\nTo view only the currently active users, type /activeusers!\n");
                    }
                    break;
                case "/shutdown":       //Writes the log to the log.txt file, then disconnects from all clients and the subserver via a UDP multicast and shuts down the main server
                    try {
                        System.out.println(Colors.ANSI_YELLOW + "Disconnecting all Clients and shutting down the Server!" + Colors.ANSI_RESET);
                        synchronized (log){
                            log.append("Disconnecting all Clients and shutting down the Server!\n");
                        }
                        BufferedWriter br = new BufferedWriter(new FileWriter("log.txt"));
                        writeMessage(subserversocket, "/log");
                        log.append("\n***Subserver Log***\n");
                        log.append(readMessage(subserversocket));
                        br.write(log.toString());
                        br.close();
                        writeUDPMulticast("SHUTDOWN");
                        System.exit(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "/userlist":       //Prints all users, independently if connected or not
                    synchronized (log) {
                        for (User u: users) {
                            System.out.println(Colors.ANSI_YELLOW + u.toString() + Colors.ANSI_RESET);
                            log.append(u.toString()).append("\n");
                        }
                    }
                    break;
                case "/activeusers":        //Prints only the connected users
                    synchronized (log) {
                        for (User u: users) {
                            if (u.isLoggedin()){
                                System.out.println(Colors.ANSI_YELLOW + u.toString() + Colors.ANSI_RESET);
                                log.append(u.toString()).append("\n");
                            }
                        }
                    }
                    break;
                default:        //Other functions are not available. If the user tries to input one, an error is shown
                    System.out.println(Colors.ANSI_YELLOW + "The requested function is not available!");
                    synchronized (log){
                        log.append("The requested function is not available!\n");
                    }
            }
        }
    }
}
