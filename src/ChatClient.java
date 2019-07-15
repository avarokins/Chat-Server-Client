import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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


final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;
    static int a;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            a = 1;
            socket = new Socket(server, port);
            System.out.println("Connection accepted " + socket.getRemoteSocketAddress().toString());
        } catch (IOException e) {
            System.out.println("No server found!");
            System.exit(0);
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        ChatClient client;


        if (args.length == 0) {
            client = new ChatClient("localhost", 1500, "Anonymous");
        } else if (args.length == 1) {
            client = new ChatClient("localhost", 1500, args[0]);
        } else if (args.length == 2) {
            client = new ChatClient("localhost", Integer.parseInt(args[1]), args[0]);
        } else {
            client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);
        }


        // Create your client and start it

        client.start();

        Scanner sc = new Scanner(System.in);

        while (true) {


            String line = sc.nextLine();


            if (line.equals("/logout")) {

                client.sendMessage(new ChatMessage(line, 1));
                System.out.println("Server has closed the connection");
                a = 0;

                try {
                    client.socket.close();
                    client.sInput.close();
                    client.sOutput.close();

                } catch (Exception e) {
                }

                break;

            }

            client.sendMessage(new ChatMessage(line, 0));

        }

    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {

            while (a == 1) {

                try {
                    String msg = (String) sInput.readObject();

                    System.out.print(msg);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    Date date = new Date();


                    if (msg.equals("Username already exists!\nExiting!")) {
                        System.exit(0);
                    }


                } catch (IOException | ClassNotFoundException e) {
                }
            }
        }
    }
}
