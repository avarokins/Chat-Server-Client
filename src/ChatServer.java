import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;


    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer() {
        port = 1500;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
                ServerSocket serverSocket = new ServerSocket(port);
                Socket socket;
                while(true) {
                    socket = serverSocket.accept();

                    Runnable r = new ClientThread(socket, uniqueId++);
                    Thread t = new Thread(r);
                    clients.add((ClientThread) r);
                    t.start();
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
        ChatServer server = new ChatServer(1500);
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

            ChatMessage obj = new ChatMessage(msg,0);

            try {
                sOutput.writeObject(obj);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private synchronized void remove (int id) {
            for(int i = 0 ; i< clients.size(); i++) {
                if(id == clients.get(i).id)
                    clients.remove(i);
            }
        }

        private synchronized void broadcast (String message) {

            for(int i = 0 ; i < clients.size() ; i++ ) {
                clients.get(i).writeMessage(message);
            }
            System.out.println(message);
        }



        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            try {
                cm = (ChatMessage) sInput.readObject();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println(username + ": Ping");


           try {
               if (cm.type == 0) {
                   writeMessage(cm.message);
               } else {
                   socket.close();
                   sInput.close();
                   sOutput.close();
               }

           } catch (Exception e) {e.printStackTrace();}

            broadcast(cm.message);



            // Send message back to the client
            try {
                sOutput.writeObject("Pong");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
