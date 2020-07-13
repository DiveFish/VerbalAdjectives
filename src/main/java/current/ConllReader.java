package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Created by patricia on 7/11/17.
 */
public class ConllReader {

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        //String dir = "/home/patricia/Dokumente/A3Project/corpora/taz";
        String dir = "/home/patricia/Dokumente/A3Project/corpora/taz";
        processFiles(dir);
    }

    public static void processFiles(String fileDir) throws InvocationTargetException, IllegalAccessException, IOException {
        Set<String> sampleSentences = new TreeSet();
        File dir = new File(fileDir);
        for (File subDir : dir.listFiles()) {
            for (File f : subDir.listFiles()) {
                processFile(f, sampleSentences);
            }
        }
        String homeRoot = "/home/patricia/Dokumente/A3Project/data/11-17/";
        IOUtils.setToFile(sampleSentences, homeRoot+"sampleSentences.txt");
    }

    public static void processFile(File conllFile, Set<String> sampleSentences) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                sent = CoNLLUtils.lowerCaseTokens(sent, true);

                for (Token token : sent) {

                    String dependency = token.getDepRel().or("_");

                    if ((dependency.equals("PP") || dependency.equals("OBJP")) && (sent.size() <= 10)) {

                        int head = token.getHead().or(-1);
                        // Not root token && don't add head twice if modified by more than 1 PP
                        if (head > -1) {
                            String headTag = sent.get(head).getPosTag().or("_");

                            // Adjectives modified by a PP
                            if ((headTag.equals("ADJA") || headTag.equals("ADJD")) && dependency.equals("PP")) {
                                sampleSentences.add("ADJ> "+CoNLLUtils.sentenceToString(sent, false));
                            }
                            else if (headTag.startsWith("V") && dependency.equals("OBJP")) {
                                String headHeadLemma = sent.get(sent.get(head).getHead().or(-1)).getLemma().or("_");
                                if (headHeadLemma.equals("werden") || headHeadLemma.equals("bleiben") || headHeadLemma.equals("sein")) {
                                    sampleSentences.add("V> " + CoNLLUtils.sentenceToString(sent, false));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
