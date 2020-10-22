package it.bx.fallmerayer.tfo.client;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class ServerListener extends Client implements Runnable {
    private int port;

    //Reads messages from the server and performs the corresponding action
    @Override
    public void run() {
        while (true){
            try {
                String msg = readMessage(s);
                String[] check = msg.split(" ");
                //Checks the values returned by the server
                switch (check[0]) {
                    case "OK":      //--> Login was successful
                        username = check[1];
                        port = Integer.parseInt(check[2]);
                        isLoggedIn = true;
                        System.out.println(Colors.ANSI_GREEN + "You logged in succesfully!\nAssigned UDP Port: " + port + Colors.ANSI_RESET);

                        //Starts a thread that listens to UDP messages on the port assigned by the server, which is printed in the line above
                        Thread udpthread = new Thread(udpReader);
                        udpthread.start();

                        //Start a thread that listens to multicasted UDP messages on a fixed address and port
                        Thread multicastthread = new Thread(multicastReceiver);
                        multicastthread.start();
                        break;
                    case "ACONNECTED":      //--> User is already connected
                        System.out.println(Colors.ANSI_RED + "This user is already logged in, try again!");
                        isLoggedIn = false;
                        break;
                    case "FAILED":      //Login failed: wrong credentials or non-existing user is entered during the authentication process
                        System.out.println(Colors.ANSI_RED + "Your credentials are either wrong or expired, try again!");
                        isLoggedIn = false;
                        break;
                    case "WWWSEARCH":       //Opens a browser window in the default browser with the search url that is generated in the subserver
                        System.out.println(Colors.ANSI_YELLOW + "Opening browser window with url: " + check[1] + Colors.ANSI_RESET);
                        Desktop desktop = java.awt.Desktop.getDesktop();
                        desktop.browse(new URI(check[1]));
                        break;
                    default:        //If none of the option above matches, the string is simply printed to the console
                        System.out.println(Colors.ANSI_YELLOW + msg);
                        break;
                }
            } catch (Exception e) {     //If the server disconnects, an error is printed and the program shuts down automatically with exit code 1
                System.out.println(Colors.ANSI_RED + "The server isn't available anymore!!\nShutting down...");
                System.exit(1);
            }
        }
    }

    //Listens to incoming UDP Messages/Files
    private final Runnable udpReader = new Runnable() {
        @Override
        public void run() {
            try {
                DatagramSocket ds = new DatagramSocket(port);
                byte[] buf = new byte[2121];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                while (true) {
                    ds.receive(dp);
                    String str = new String(dp.getData(), 0, dp.getLength());
                    if (str.equals("M")) {
                        System.out.println(Colors.ANSI_YELLOW + "Receiving Message...");
                        ds.receive(dp);
                        str = new String(dp.getData(), 0, dp.getLength());
                        System.out.println(Colors.ANSI_YELLOW + str);
                    } else if (str.equals("F")) {
                        System.out.println(Colors.ANSI_YELLOW + "Receiving File...");
                        ds.receive(dp);
                        String sizeString = new String(dp.getData(), 0, dp.getLength());
                        int size = Integer.parseInt(sizeString);

                        ds.receive(dp);
                        String fileName = new String(dp.getData(), 0, dp.getLength());
                        System.out.println("Named: " + fileName + Colors.ANSI_RESET);

                        buf = new byte[size];
                        dp = new DatagramPacket(buf, buf.length);
                        ds.receive(dp);
                        FileOutputStream fileOutputStream = new FileOutputStream("File" + fileName);
                        fileOutputStream.write(buf);
                        fileOutputStream.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    //Listens to messages that are multicasted to the 226.4.5.6 group on port 6900
    private final Runnable multicastReceiver = () -> {
        try {
            MulticastSocket ms = new MulticastSocket(6900);

            ms.joinGroup(InetAddress.getByName("226.4.5.6"));

            byte[] buf = new byte[1024];

            DatagramPacket dp = new DatagramPacket(buf, 1024);
            ms.receive(dp);

            String msg = new String(dp.getData(), 0, dp.getLength());
            if (msg.equals("SHUTDOWN")){        //If the message "SHUTDOWN" is read, the program exits
                System.out.println(Colors.ANSI_RED + "The server is shutting down!\nGood Bye!");
                System.exit(0);
            }
            System.out.println(Colors.ANSI_YELLOW + msg + Colors.ANSI_RESET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
}
