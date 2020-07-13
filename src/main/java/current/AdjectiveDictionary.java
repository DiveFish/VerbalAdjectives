package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Class provides the dictionary of adjectives from the taz
 * and a boolean of whether they are derived from verbs or not.
 * Dictionary created in trial/SmorAnalyzer
 *
 * Created by patricia on 3/09/17.
 */
public class AdjectiveDictionary {

    private static Map<String, Boolean> adjDict;

    public AdjectiveDictionary() throws IOException {
        adjDict = new TreeMap();
        //String file = "/home/patricia/Dokumente/A3Project/data/10-17/taz/smor_adjDict.txt";
        String file = "/home/patricia/Dokumente/A3Project/data/10-17/taz/smor_adjDict.txt";
        //String file = "/home/patricia/adjectives/dict_adjs_isVerbDerived.txt";
        readAdjDictFromFile(file);
    }

    public static void readAdjDictFromFile(String dictFile) throws IOException {
        InputStreamReader is = new InputStreamReader(new FileInputStream(new File(dictFile)));
        BufferedReader br = new BufferedReader(is);
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\t");
            adjDict.putIfAbsent(splitLine[0], Boolean.parseBoolean(splitLine[1]));
        }
        br.close();
    }

    public Map<String, Boolean> getAdjDict() {
        return adjDict;
    }

    public void setAdjDict(Map<String, Boolean> adjDict) { this.adjDict = adjDict;
    }

    public static void addMissingLemmas() throws IOException {
        Map<String, String> lemmaToForm = IOUtils.stringMapFromFile("/home/patricia/Dokumente/A3Project/data/10-17/lemmasToForm.txt");
        AdjectiveDictionary ad = new AdjectiveDictionary();
        Map<String, Boolean> dictionary = ad.getAdjDict();
        for (Map.Entry<String, String> entry : lemmaToForm.entrySet()) {
            String lemma = entry.getKey().toLowerCase();
            String form = entry.getValue().toLowerCase();
            if (!dictionary.containsKey(lemma)) {
                dictionary.putIfAbsent(lemma, dictionary.get(form));
            }
        }
        IOUtils.booleanMapToFile(dictionary, "/home/patricia/Dokumente/A3Project/data/10-17/dict_adjs_isVerbDerived.txt");
    }

    public static void getAdjsFromDir(String fileDir, String outputDir) throws InvocationTargetException, IllegalAccessException, IOException {
        Set<String> adjectives = new TreeSet();
        File dir = new File(fileDir);
        for (File subDir : dir.listFiles()) {
            for (File file : subDir.listFiles()) {
                getAdjsFromFile(file, adjectives);
            }
        }
        System.out.println("Total # of adjectives: "+adjectives.size());
        /*
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        for (String adj : adjectives) {
            out.println(adj);
        }
        */
        IOUtils.setToFile(adjectives, outputDir);
    }

    public static void getAdjsFromFile(File conllFile, Set<String> adjectives) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(conllFile)), StandardCharsets.UTF_8);
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(inputStreamReader))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                sent = CoNLLUtils.lowerCaseTokens(sent, true);

                for (Token token : sent) {
                    if (token.getPosTag().or("_").equals("ADJA") ||
                            token.getPosTag().or("_").equals("ADJD")) {
                        adjectives.add(token.getForm().or("_"));
                        adjectives.add(token.getLemma().or("_"));
                    }
                }
            }
        }
    }
}
