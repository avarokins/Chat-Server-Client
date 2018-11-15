import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    String fileName;


    private ChatServer(int port, String fileName) {
        this.port = port;
        this.fileName = fileName;
    }

    private ChatServer(int port) {
        this.port = port;
        this.fileName = "badwords.txt";
    }

    private ChatServer() {
        this.port = 1500;
        this.fileName = "badwords.txt";
    }


    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        System.out.println("Banned Words File: " + fileName);
        System.out.println("Banned Words: ");

        ChatFilter cf = new ChatFilter(fileName);
        for (int i = 0; i < cf.words.size(); i++) {
            System.out.println(cf.words.get(i));
        }

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket;
            while (true) {
                SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
                String date = s.format(new Date());
                System.out.println(date + " Server waiting for Clients on port " + port + ".");
                socket = serverSocket.accept();

                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);

                try {
                    t.join();
                } catch (Exception e) {
                }

                clients.add((ClientThread) r);
                t.start();

                String date2 = s.format(new Date());
                System.out.println(date2 + " " + clients.get(uniqueId - 1).username + " just connected.");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server;
        int len = args.length;
        int temp;
        switch (len) {
            case 1:
                temp = Integer.parseInt(args[0]);
                server = new ChatServer((temp));
                break;
            case 2:
                temp = Integer.parseInt(args[0]);
                server = new ChatServer(temp, args[1]);
                break;
            default:
                server = new ChatServer(1500, "badwords.txt");
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

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private boolean writeMessage(String msg) {

            ChatMessage obj = new ChatMessage(msg, 0);

            try {
                sOutput.writeObject(obj);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private synchronized void remove(int id) {
            for (int i = 0; i < clients.size(); i++) {
                if (id == clients.get(i).id)
                    clients.remove(i);
            }
        }

        private synchronized void broadcast(String message) {

            ChatFilter cf = new ChatFilter(fileName);
            message = cf.filter(message);
            SimpleDateFormat d = new SimpleDateFormat("HH:mm:ss");
            String date = d.format(new Date());
            System.out.println(date + " " + message);

            int i = 0;
            while (i < clients.size()) {
                clients.get(i).writeMessage(date + " " + message);
                i++;
            }
        }


        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client

            while (true) {

                try {
                    ChatMessage chatMessage = (ChatMessage) sInput.readObject();

                    if (chatMessage == null)
                        break;

                    if (chatMessage.getType() == 0) {
                        if (chatMessage.getRecipient() != null) {
                            SimpleDateFormat s = new SimpleDateFormat("HH:mm:ss");
                            String date = s.format(new Date());
                            // directMessage(username + " -> " + chatMessage.getRecipient() + ": " + chatMessage.getMessage(), chatMessage.getRecipient());
                            writeMessage(date + " " + username + " -> " + chatMessage.getRecipient() + ": " + chatMessage.getMessage());
                        } else {
                            if (chatMessage.getMessage().equals("/list")) {
                                //
                                int current = -1;
                                for (int i = 0; i < clients.size(); i++) {
                                    if (clients.get(i).username == username) {
                                        current = i;
                                        break;
                                    }
                                }

                                for (int i = 0; i < clients.size(); i++) {
                                    if (clients.get(i).username.equals(username))
                                        continue;
                                    else
                                        clients.get(current).writeMessage(clients.get(i).username);
                                }
                            } else {

                                broadcast(username + ": " + chatMessage.getMessage());
                            }

                            //

                        }


                    } else {
                        try {
                            sOutput.close();
                            sInput.close();
                            socket.close();
                            System.out.println(username + " disconnected with a LOGOUT message.");
                            //return;
                        } catch (IOException e) {}
                    }


                } catch (Exception e) {
                }


            }


        }
    }
}
