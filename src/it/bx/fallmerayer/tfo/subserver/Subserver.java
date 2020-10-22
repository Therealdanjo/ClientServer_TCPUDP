package it.bx.fallmerayer.tfo.subserver;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Subserver {

    protected static Socket s;
    protected static ArrayList<String> availablefunctions = new ArrayList<>();
    protected static StringBuilder log = new StringBuilder();

    public static void main(String[] args) {
        //Adds the available subserver functions to the list
        availablefunctions.add("/advancedhelp");
        availablefunctions.add("/date");
        availablefunctions.add("/time");
        availablefunctions.add("/calc");
        availablefunctions.add("/www");

        //ExecutorService that starts various activities
        ExecutorService executorService = Executors.newCachedThreadPool();
        //Starts the multicast receiver --> communication when a new user connected to the main server and shutdown information from the server
        executorService.execute(multicastReceiver);
        try {
            //Connects to the server
            s = new Socket("localhost", 42069);
            writeAvailableFunctions();

            //Starts the thread that processes the requests sent by the main server
            executorService.execute(new TaskListener());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //Writes a String via TCP
    protected static void writeMessage(Socket s, String message) throws IOException {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
        printWriter.println(message);
    }

    //Reads a String via TCP
    protected static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }

    //Writes the available functions to the server
    private static void writeAvailableFunctions() throws IOException {
        System.out.println(Colors.ANSI_CYAN + "Connecting to the Main Server...");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < availablefunctions.size(); i++) {
            if (i == availablefunctions.size()-1){
                sb.append(availablefunctions.get(i));
                break;
            }
            sb.append(availablefunctions.get(i));
            sb.append(";");
        }
        writeMessage(s, sb.toString());
        System.out.println(Colors.ANSI_GREEN + "Connection succesful!");

        synchronized (log) {
            log.append("Connecting to the Main Server...\n");
            log.append("Connection successful!\n");
        }
    }

    //Listens to messages that are multicasted to the 226.4.5.6 group on port 6900
    private static final Runnable multicastReceiver = new Runnable() {
        @Override
        public void run() {
            try {
                MulticastSocket ms = new MulticastSocket(6900);

                ms.joinGroup(InetAddress.getByName("226.4.5.6"));

                byte[] buf = new byte[1024];

                DatagramPacket dp = new DatagramPacket(buf, 1024);
                ms.receive(dp);

                String msg = new String(dp.getData(), 0, dp.getLength());
                if (msg.equals("SHUTDOWN")){        //If "SHUTDOWN" is received the subserver stops
                    System.out.println(Colors.ANSI_RED + "The server is shutting down!\nGood Bye!");
                    System.exit(0);
                }
                System.out.println(Colors.ANSI_YELLOW + msg + Colors.ANSI_RESET);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

}
