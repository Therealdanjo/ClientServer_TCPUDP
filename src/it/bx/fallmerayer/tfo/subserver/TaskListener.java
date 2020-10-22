package it.bx.fallmerayer.tfo.subserver;

import it.bx.fallmerayer.tfo.utilities.Colors;

//Reads and processes the tasks sent by the main server
public class TaskListener extends Subserver implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                String receivedTask = readMessage(s);
                String[] args = receivedTask.split(" ");

                //If the command is available execute it, otherwise send back fail
                if (functionIsAvailable(args[0])) {
                    System.out.println(Colors.ANSI_WHITE + "Received and performing the task: " + receivedTask);
                    String result = execute(args);
                    writeMessage(s, result);
                    System.out.println(Colors.ANSI_GREEN + "Sent processed result [" + result + "] back to the main Server!");

                    synchronized (log) {
                        log.append("Received and performing the task: ").append(receivedTask).append("\n");
                        log.append("Sent processed result [").append(result).append("] back to the main Server!\n");
                    }
                } else {
                    writeMessage(s, "Function not available!");
                }
            } catch (Exception e) {
                System.out.println(Colors.ANSI_RED + "The server isn't available anymore!!\nShutting down...");
                System.exit(1);
            }
        }
    }

    //Executes the given task and returns the result as formatted String which is ready to be sent back to the user
    private String execute(String[] task) {
        switch (task[0]) {
            case "/date" : return java.time.LocalDate.now().toString();     //Returns the current date
            case "/time" : return java.time.LocalTime.now().toString();     //Returns the current time
            case "/advancedhelp": return "Type /date to get the current date. Type /time to get the current time. Type /calc [number1] [operator] [number2] to calculate the result! With /log you can get the subserver's file log. Type /www [query] to search for it on the internet!";
            case "/calc" :      //Calculates a number of operations, if a wrong operator is sent, an error is sent back to the user
                try {
                    Double n1 = Double.parseDouble(task[1]);        //First number
                    Double n2 = Double.parseDouble(task[3]);        //Second number
                    switch (task[2]) {
                        case "+" : return n1 + " + " + n2 + " = " + (n1 + n2);
                        case "-" : return n1 + " - " + n2 + " = " + (n1 - n2);
                        case "*" : return n1 + " * " + n2 + " = " + (n1 * n2);
                        case "/" :
                            if (n2 != 0){
                                return n1 + " / " + n2 + " = " + (n1 / n2);
                            } else {
                                return "You can't divide by 0!";
                            }
                        default:
                            return "The operator which you requested isn't available!";
                    }
                } catch (NumberFormatException e){
                    return "Please input two numbers in order to perform a calculation!";
                }
            case "/log" :       //Returns the subserver's log (only usable from the main server, not from other clients!)
                synchronized (log) {
                    return log.toString();
                }
            case "/www":        //Returns a google search URL built from the passed characters
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < task.length; i++) {
                    builder.append(task[i]).append("%20");
                }
                return "WWWSEARCH https://www.google.com/search?q=" + builder.toString();
        }
        return "Something went wrong :(";
    }

    //Checks if a passed function is in the availablefunctions-list
    private boolean functionIsAvailable(String function){
        for (String i: availablefunctions) {
            if(i.equals(function)){
                return true;
            }
        }
        return false;
    }
}
