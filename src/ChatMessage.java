import java.io.Serializable;

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


final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    private String message;
    private int type;
    private String recipient;


    public ChatMessage(int type, String recipient, String message) {
        this.type = type;
        this.message = message;
        this.recipient = recipient;
    }


    public ChatMessage(String message, int type) {

        this.type = type;
        this.message = message;
        recipient = null;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRecipient() {
        return recipient;
    }


}
