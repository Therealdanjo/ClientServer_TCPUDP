package it.bx.fallmerayer.tfo.client;

import it.bx.fallmerayer.tfo.utilities.Colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    protected static Socket s;
    protected static String username;
    protected static boolean isLoggedIn = false;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            //New socket that communicates with the server
            s = new Socket("localhost", 42069);
            //Starts the thread that is responsible for getting an input from the user and writing it to the server or other threads
            executorService.execute(new Enginethread());

            //Starts the thread that reads all incoming messages and performs the corresponding activity. This thread handles both TCP and UDP communications
            executorService.execute(new ServerListener());
        } catch (IOException e) {
            //If the server fails to connect, a corresponding message is printed and the program exits automatically
            System.out.println(Colors.ANSI_RED + "Failed to connect, Try again!");
        }

    }

    //Writes a TCP message
    public static void writeMessage(Socket s, String message) throws Exception {
        PrintWriter printWriter = new PrintWriter(s.getOutputStream(), true);
        printWriter.println(message);
    }

    //Reads a TCP message
    public static String readMessage(Socket s) throws Exception{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        return bufferedReader.readLine();
    }
}
