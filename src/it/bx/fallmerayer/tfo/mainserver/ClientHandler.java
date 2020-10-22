package it.bx.fallmerayer.tfo.mainserver;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.Socket;
import java.util.ArrayList;

//Handles the single clients
public class ClientHandler extends Mainserver implements Runnable{
    private final Socket s;
    private User activeUser;
    private Boolean isLoggedin;
    private final int udpport;

    public ClientHandler(Socket socket, int udpport) {
        this.s = socket;
        isLoggedin = false;
        this.udpport = udpport;
    }

    @Override
    public void run() {
        String received;
        while (true) {
            try {
                //Reads messages via TCP
                received = readMessage(s);
                String[] args = received.split(" ");
                //Selects the functions to perform
                switch (args[0]) {
                    case "/help":       //Writes basic instructions on how to use the server to the client
                        writeMessage(s, "To authenticate, please write /auth username password. To quit, please write /quit. After authenticating you will have access to the following functions: " + Mainserver.functions.toString());
                        break;
                    case "/quit":       //Disconnects the client
                        if(isLoggedin){     //If the user is logged in, his name is also displayed in the server Console
                            int userIndex = searchElement(Mainserver.users, activeUser);
                            if (userIndex > 0) {
                                Mainserver.users.get(userIndex).setLoggedin(false);
                            }
                            System.out.println(Colors.ANSI_RED + activeUser.getUsername() + " logged out!" + Colors.ANSI_RESET);
                            isLoggedin = false;
                            synchronized (log){
                                log.append(activeUser.getUsername()).append(" logged out!");
                            }
                        } else {        //Otherwise an unknown user is disconnected
                            System.out.println(Colors.ANSI_RED + "Unknown User logged out!" + Colors.ANSI_RESET);
                            synchronized (log){
                                log.append("Unknown User logged out!");
                            }
                        }
                        return;
                    case "/auth":       //Starts the login process and sends a multicast with the user's name if he connected successfully
                        logIn(args[1], args[2]);
                        if(isLoggedin){
                            writeUDPMulticast(activeUser.getUsername() + " Connected to the main server!");
                        }
                        break;
                    case "/userlist":       //Writes all users to the client, independently if connected or not
                        if(isLoggedin){
                            writeMessage(s, Mainserver.users.toString());
                            break;
                        } else {
                            writeMessage(s, "You must log in to gain access for this function!");
                        } break;
                    case "/activeusers":        //Writes only the active users to the client
                        if(isLoggedin){
                            StringBuilder activeusers = new StringBuilder();
                            for (User u: users) {
                                if (u.isLoggedin()){
                                    activeusers.append(u.toString());
                                }
                            }
                            writeMessage(s, activeusers.toString());
                            break;
                        } else {
                            writeMessage(s, "You must log in to gain access for this function!");
                        } break;
                    case "/log":        //Writes the log to 02_ClientServer_TCPUDP/log.txt
                        if(isLoggedin) {
                            synchronized (log){
                                BufferedWriter br = new BufferedWriter(new FileWriter("log.txt"));
                                writeMessage(subserversocket, "/log");
                                log.append("\n***Subserver Log***\n");
                                log.append(readMessage(subserversocket));
                                br.write(log.toString());
                                br.close();
                                writeMessage(s, "Log printed successfully to 02_ClientServer_TCPUDP/log.txt");
                            }
                        }
                        break;
                    default:        //Other functions are passed to the subserver if the user is logged in, otherwise an error is shown
                        if(isLoggedin) {
                            writeMessage(subserversocket, received);
                            System.out.println("Sent " + received + " to the Subserver!");
                            String result = readMessage(subserversocket);
                            writeMessage(s, result);
                            System.out.println("Result [" + result + "] was sent back to " + activeUser.getUsername());
                            synchronized (log) {
                                log.append("Sent ").append(received).append(" to the Subserver!");
                                log.append("Result [").append(result).append("] was sent back to ").append(activeUser.getUsername());
                            }
                        } else {
                            writeMessage(s, "You must log in to gain access for this function!");
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //checks if the user exists. If it exists and it's not logged in, it writes OK to the client, otherwise it sends the respective errors
    private void logIn(String username, String password) {
        for (User u: Mainserver.users) {
            if(username.equals(u.getUsername()) && password.equals(u.getPassword()) && !u.isLoggedin()){        //--> Successful login
                try {
                    isLoggedin = true;
                    u.setLoggedin(true);
                    writeMessage(s, "OK " + username + " " + udpport);
                    System.out.println(Colors.ANSI_BLUE + username + " logged in successfully!" + Colors.ANSI_RESET);
                    activeUser=new User(u.getUsername(), u.getPassword(), true, udpport);
                    u.setUdpport(udpport);
                    synchronized (log){
                        log.append(username).append(" logged in successfully!\n");
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (username.equals(u.getUsername()) && password.equals(u.getPassword()) && u.isLoggedin()){     //--> Credentials with which the user tried to log in are already in use
                try {
                    writeMessage(s, "ACONNECTED " + username);
                    System.out.println(Colors.ANSI_RED + username + " is already logged in!" + Colors.ANSI_RESET);
                    synchronized (log){
                        log.append(username).append(" is already logged in!\n");
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        try {           //Login fails if the user doesn't exist or its credentials are wrong
            writeMessage(s, "FAILED " + username);
            System.out.println(Colors.ANSI_RED + "Login failed, username/password is wrong or the user does not exist!" + Colors.ANSI_RESET);
            synchronized (log){
                log.append("Login failed, username/password is wrong or the user does not exist!\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Searches an user in a list and returns its index
    private int searchElement(ArrayList<User> list, User user){
        for (int i = 0; i < list.size(); i++) {
            if(user.compareUsers(list.get(i))){
                return i;
            }
        }
        return -1;
    }
}
