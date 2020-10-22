package it.bx.fallmerayer.tfo.mainserver;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mainserver {

    private static final Vector<ClientHandler> clients = new Vector<>();
    protected static ArrayList<User> users = new ArrayList<>();
    protected static final ArrayList<String> functions = new ArrayList<>();
    protected static Socket subserversocket;
    protected static final StringBuilder log = new StringBuilder();

    public static void main(String[] args) throws Exception {

        //Imports the users from a csv --> not done in the client handler to avoid multiple access to the file --> faster execution
        importUsers();

        //Creates the server socket and the socket that temporarily stores client requests
        ServerSocket ss = new ServerSocket(42069);
        Socket s;

        //Users will get a port for UDP Communication starting from 50000
        int portassignment = 50000;

        //ExecutorService to start all ClientHandler threads
        ExecutorService executorService = Executors.newCachedThreadPool();

        //Sets the basic server Functions that are available for the clients and gets the functions from the subserver
        getFunctions(ss);
        //Starts the console that is usable directly from the server
        executorService.execute(new Console());

        //running loop for getting client requests
        while (true) {
            //Accept the incoming request
            s = ss.accept();

            System.out.println(Colors.ANSI_PURPLE + "Received new Client request: " + s);
            System.out.println("Creating a new handler for this client...");

            //Create a new handler object for handling this request
            ClientHandler newclient = new ClientHandler(s, portassignment);

            //add this client to active clients list
            clients.add(newclient);
            System.out.println("Client added to active clients list" + Colors.ANSI_RESET);
            //start the thread.
            executorService.execute(newclient);

            //increment the port so that the next user gets the next port
            portassignment++;

            //Write th above actions to the log
            synchronized (log) {
                log.append("Received new Client request: ").append(s).append("\n");
                log.append("Creating a new handler for this client...\n");
                log.append("Client added to active clients list\n");
            }
        }
    }

    //Reads a String via TCP
    public static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    //writes a String via TCP
    public static void writeMessage(Socket s, String message) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream());
        printWriter.println(message);
        printWriter.flush();
    }

    //Reads the users from the users.csv file and imports them into the user list
    private static void importUsers() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("src/it/bx/fallmerayer/tfo/mainserver/users.csv"));
        String line;
        while ((line = br.readLine()) != null){
            String[] values = line.split(";");
            users.add(new User(values[0], values[1], false, 0));
        }
    }

    //Inserts this server's basic functions in the "functions" list, then connects to the subserver, reads the available subserver functions and inserts them into the "functions" list
    private static void getFunctions(ServerSocket ss) throws Exception {
        functions.add("/help");
        functions.add("/quit");
        functions.add("/auth");
        functions.add("/userlist");
        functions.add("/activeusers");
        functions.add("/log");

        System.out.println(Colors.ANSI_CYAN + "Waiting for the Subserver...");

        subserversocket = ss.accept();
        String readFunctions = readMessage(subserversocket);
        String[] subserverfunctions = readFunctions.split(";");
        synchronized (functions){
            functions.addAll(Arrays.asList(subserverfunctions));
        }

        System.out.println(Colors.ANSI_GREEN + "Subserver connected successfully!" + Colors.ANSI_RESET);

        synchronized (log) {
            log.append("Waiting for the Subserver...\n");
            log.append("Subserver connected successfully!\n");
        }
    }

    //Writes a multicast on the multicast address 226.4.5.6 on port 6900
    protected static void writeUDPMulticast(String message) throws IOException {
        String group = "226.4.5.6";     //Multicast Group Address

        MulticastSocket ms = new MulticastSocket(6900);

        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(group), 6900);
        ms.send(datagramPacket);
    }

}
