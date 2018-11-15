import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class ChatFilter {

    ArrayList <String> words = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        File file = new File(badWordsFileName);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String line;

            while(true) {

                line = br.readLine();

                if(line == null)
                    break;

                words.add(line);

            }

        } catch (Exception e) {}


    }

    public String filter(String msg) {

        for (int i = 0 ; i < words.size() ; i++ ) {

            String replacement ="";

            if(msg.contains(words.get(i))) {
                for (int j = 0 ; j < words.get(i).length() ; j++ ) {
                    replacement += "*";
                }

                msg.replace(words.get(i),replacement);
            }
        }



        return msg;
    }
}
