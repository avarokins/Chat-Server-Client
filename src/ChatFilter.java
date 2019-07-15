import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

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


public class ChatFilter {

    ArrayList<String> words = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        File file = new File(badWordsFileName);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;

            while (true) {

                line = br.readLine();

                if (line == null)
                    break;

                words.add(line);

            }

        } catch (Exception e) {
        }


    }

    public String filter(String msg) {

        for (int i = 0; i < words.size(); i++) {

            String replacement = "";


                for (int j = 0; j < words.get(i).length(); j++) {
                    replacement += "*";
                }

                String hi = words.get(i);
                msg = msg.replaceAll("(?i) "+ hi, replacement);
            }

        return msg;
    }
}
