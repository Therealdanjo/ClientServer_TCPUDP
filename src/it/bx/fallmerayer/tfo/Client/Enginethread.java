package it.bx.fallmerayer.tfo.Client;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Enginethread extends Client implements Runnable {

    @Override
    public void run() {
        System.out.println(Colors.ANSI_PURPLE + "***Welcome!***\nIf you need help, type /help\nTo send messages to other users, type /msg [port1,port2,...] [message]\nTo send files to other users, type /send [port1,port2,...] [filepath]");
        //Reads a string from the user and performs the corresponding action
        while (true) {
            Scanner sc = new Scanner(System.in);
            String msg = sc.nextLine();
            //Splits the arguments
            String[] functions = msg.split(" ");
            switch (functions[0]) {
                case "/msg":            //Sends a Message via UDP to the specified user(s)
                    if (isLoggedIn) {
                        String[] portstosend = functions[1].split(",");
                        for (String s: portstosend) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 2; i < functions.length; i++) {
                                sb.append(functions[i]).append(" ");
                            }
                            writeUDPMessage(Integer.parseInt(s), sb.toString());
                        }
                    } else {
                        System.out.println(Colors.ANSI_RED + "You Must be logged in to send messages to other users!" + Colors.ANSI_RESET);
                    }
                    break;
                case "/send":       //Sends a file via UDP to the specified user(s)
                    if (isLoggedIn) {
                        String[] portstosend = functions[1].split(",");
                        for (String s: portstosend) {
                            try {
                                sendUdpFile(Integer.parseInt(s), functions[2]);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println(Colors.ANSI_RED + "You Must be logged in to send messages to other users!" + Colors.ANSI_RESET);
                    }
                    break;
                case "/quit":       //Disconnects from the server and exits the program
                    isLoggedIn = false;
                    System.out.println(Colors.ANSI_PURPLE + "Goodbye, see you soon!");
                    try {
                        writeMessage(s, "/quit");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                default:        //If none of the above options matches, the function is passed to the main server
                    try {
                        writeMessage(s, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    //Writes a UDP message on the localhost
    private void writeUDPMessage(int port, String message) {
        message = username + ":" + " " + message;
        try {
            DatagramSocket ds = new DatagramSocket();
            InetAddress ip = InetAddress.getByName("localhost");
            String info = "M";
            DatagramPacket dpInfo = new DatagramPacket(info.getBytes(), info.length(), ip, port);
            DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), ip, port);
            ds.send(dpInfo);
            ds.send(dp);
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Sends a file via UDP on the localhost
    private void sendUdpFile(int port, String filepath) throws IOException {   
        File file = new File(filepath);
        long size = file.length();
        String fileName = file.getName();
        byte[] bytes = Files.readAllBytes(Paths.get(filepath));

        try {
            DatagramSocket ds = new DatagramSocket();
            InetAddress ip = InetAddress.getByName("localhost");
            String info = "F";
            DatagramPacket dpInfo = new DatagramPacket(info.getBytes(), info.length(), ip, port);//dem client sagen, welcher typ geschictk wird
            ds.send(dpInfo);

            String sizeString = "" + size;
            dpInfo = new DatagramPacket(sizeString.getBytes(), sizeString.length(), ip, port);
            ds.send(dpInfo);

            dpInfo = new DatagramPacket(fileName.getBytes(), fileName.length(), ip, port);//filename senden
            ds.send(dpInfo);

            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, ip, port);
            ds.send(dp);
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
