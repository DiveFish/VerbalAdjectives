package current;

import de.danielnaber.jwordsplitter.AbstractWordSplitter;
import de.danielnaber.jwordsplitter.GermanWordSplitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricia on 20/10/17.
 */
public class HeadDetector {

    public static void main(String[] args) throws IOException {
        HeadDetector headDetector = new HeadDetector();
        String wordList = "/home/patricia/Dokumente/A3Project/data/10-17/wordsToSplit.txt";
        headDetector.getHead(wordList);
    }

    public static List<String> getHead(String wordList) throws IOException {
        List<String> heads = new ArrayList();
        AbstractWordSplitter splitter = new GermanWordSplitter(true);
        splitter.setStrictMode(false);

        List<String> words = IOUtils.listFromFile(wordList);
        for (String word : words) {
            List<String> parts = splitter.splitWord(word);
            System.out.println(parts.get(parts.size() - 1));
        }
        return heads;
    }
}
