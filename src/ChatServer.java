

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Chat Server
 *
 * This is a sever-client based chat application.
 *
 * @author Avarokin Raj Saini, lab sec 8
 * @author Drishti Agarwala, lab sec 8
 *
 * @version September 22, 2018
 */


final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private String filename;
    int a = 0;
    public static String fName;


    private synchronized void broadcast(String message) {


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();

        for (int i = 0; i < clients.size(); i++) {
            try {
                clients.get(i).writeMessage(message);
            } catch (Exception e) {
            }

        }
    }


    private synchronized void directMessage(String message, String username) {


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();


        System.out.println(sdf.format(date) + " " + message);

        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).username.equals(username) || clients.get(i).username.equals(clients.get(a).username))
                try {
                    clients.get(i).writeMessage(message);
                } catch (Exception e) {
                }

        }
    }


    private synchronized void remove(int id) {
        clients.remove(id);
    }


    private ChatServer(int port, String filename) {
        this.port = port;
        this.filename = filename;

    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            System.out.println("Banned words file : " + filename + "\nBanned words:");


            BufferedReader br = new BufferedReader(new FileReader(filename));

            while (true) {

                String temp = br.readLine();

                if (temp == null)
                    break;

                System.out.println(temp);


            }

            System.out.println();

            while (true) {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();

                System.out.println(sdf.format(date) + " : Server waiting for clients on port " + port);

                Socket socket = serverSocket.accept();


                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);

                System.out.println(sdf.format(date) + " " + ((ClientThread) r).username + " just connected.");

                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).username.equals(((ClientThread) r).username)) {
                        ((ClientThread) r).sOutput.writeObject("Username already exists!\nExiting!");
                        ((ClientThread) r).close();
                        break;
                    }
                }

                clients.add((ClientThread) r);


                t.start();
            }
        } catch (IOException e) {}
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {

        ChatServer server;

        if (args.length == 0) {
            server = new ChatServer(1500, "badwords.txt");
            fName = "badwords.txt";
        } else if (args.length == 1) {
            server = new ChatServer(Integer.parseInt(args[0]), "badwords.txt");
            fName = "badwords.txt";
        } else {
            server = new ChatServer(Integer.parseInt(args[0]), args[1]);
            fName = args[1];
        }
        server.start();
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;


        private void close() {

            try {
                socket.close();
                sOutput.close();
                sInput.close();
            } catch (Exception e) {
            }
        }


        private boolean writeMessage(String msg) {


            ChatFilter cf = new ChatFilter("badwords.txt");
            msg = cf.filter(msg);


            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();

            if (socket.isConnected()) {


                try {
                    sOutput.writeObject(sdf.format(date) + " " + msg + "\n");
                } catch (Exception e) {
                }

                return true;
            }

            return false;
        }


        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {}

        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client

            while (true) {


                try {

                    cm = (ChatMessage) sInput.readObject();

                } catch (IOException | ClassNotFoundException e) {

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();

                    System.out.println(sdf.format(date) + " " + username + " just disconnected!");

                    for(int i = 0 ; i < clients.size() ; i++ ) {
                        if(clients.get(i).username.equals(username)) {
                            clients.remove(i);
                            break;
                        }
                    }

                    break;
                }

                String[] words = cm.getMessage().split(" ");

                String msg1 = "";


                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                Date date = new Date();

                if (cm.getType() == 1) {

                    System.out.println(sdf.format(date) + " " + username + " disconnected with a LOGOUT message.");
                    close();
                    break;


                } else if (words[0].equals("/msg")) {

                    boolean enter = false;

                    for (int i = 0; i < clients.size(); i++) {
                        if (clients.get(i).username.equals(words[1]))
                            enter = true;
                    }

                    if (!enter) {
                        try {


                            sOutput.writeObject(sdf.format(date) + " The recipient does not exist!\n");
                        } catch (Exception e) {
                        }


                    } else {


                        for (int i = 2; i < words.length; i++) {
                            if (i == words.length - 1)
                                msg1 = msg1 + words[i];
                            else
                                msg1 = msg1 + words[i] + " ";
                        }

                        msg1 = username + " - > " + words[1] + " : " + msg1;

                        for (int i = 0; i < clients.size(); i++) {
                            if (clients.get(i).username.equals(username))
                                a = i;
                        }

                        if (!words[1].equals(username))
                            directMessage(msg1, words[1]);
                        else {

                            try {
                                sOutput.writeObject(sdf.format(date) + " Cannot direct message yourself!\n");
                            } catch (Exception e) {
                            }
                        }

                    }

                } else if (words[0].equals("/list") && words.length == 1) {

                    for (int i = 0; i < clients.size(); i++) {
                        if (!clients.get(i).username.equals(username)) {
                            //

                            try {
                                sOutput.writeObject(clients.get(i).username + "\n");
                            } catch (Exception e) {
                            }
                            //
                        }
                    }


                } else {


                    ChatFilter cf = new ChatFilter(fName);

                    String msg = cf.filter(cm.getMessage());

                    System.out.println(sdf.format(date) + " " + username + " : " + msg);

                    // Send message back to the client
                    broadcast(username + " : " + msg);
                }
            }

        }
    }
}

