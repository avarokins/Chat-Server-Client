import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    static int a;
    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
        a=1;
    }

    private ChatClient(String username, int port) {
        this.server = "localhost";
        this.port = port;
        this.username = username;
        a=1;
    }

    private ChatClient(String username) {
        this.server = "localhost";
        this.port = 1500;
        this.username = username;
        a=1;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
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
    private synchronized void sendMessage(ChatMessage msg) {
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

        // Create your client and start it



        ChatClient chatClient;

        // Create your client and start it

        int len = args.length;

        switch (len) {

            case 1:
                chatClient = new ChatClient(args[0]);
                break;

            case 2:
                int temp = Integer.parseInt(args[1]);
                chatClient = new ChatClient(args[0], temp);
                break;

            case 3:
                int temp2 = Integer.parseInt(args[1]);
                chatClient = new ChatClient(args[2], temp2, args[0]);
                break;

            default:
                chatClient = new ChatClient("localhost", 1500, "Bob");

        }



        chatClient.start();
        System.out.println("Connection accepted " + chatClient.server);

        while (true) {



        Scanner sc = new Scanner(System.in);
            String temp = sc.nextLine();

            // Send an empty message to the server


            if(!temp.equals("/logout")) {
                chatClient.sendMessage(new ChatMessage(temp, 0));
            }

            //else if() {

             else {
                //client.sendMessage(new ChatMessage(temp, 1));
                try {
                    chatClient.sInput.close();
                    chatClient.sOutput.close();
                    chatClient.socket.close();
                    a = 0;
                    System.out.println("Server has closed the connection");
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    String msg = sInput.readObject().toString();
                    System.out.print(msg);

                    if(a == 0)
                        break;

                }
            } catch (IOException | ClassNotFoundException e) {
            }
        }
    }
}
