package trial;

import current.CoNLLUtils;
import current.Layer;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static current.DataUtils.sortByValue;
/**
 * Created by patricia on 21/08/17.
 */
public class AdjPPAnalyzer {

    private static Layer layer;

    // format: {String adjective, boolean isVerbalComposite}
    // filled in SmorAnalyzer.java
    private static Map<String, Boolean> adjDict;

    private static List<List<Token>> ppsBeforeVAdjAttr;
    private static List<List<Token>> ppsAfterVAdjAttr;
    private static List<List<Token>> ppsBeforeAdjAttr;
    private static List<List<Token>> ppsAfterAdjAttr;

    private static List<List<Token>> ppsBeforeVAdjPred;
    private static List<List<Token>> ppsAfterVAdjPred;
    private static List<List<Token>> ppsBeforeAdjPred;
    private static List<List<Token>> ppsAfterAdjPred;

    private static Map<String, Set<String>> vadjPreps;
    private static Map<String, Set<String>> adjPreps;

    private static Map<String, Integer> adjFreqs;
    private static List<String> adjsByFreq;
    private static List<Map<String, Integer>> adjPrepFreqs;

    public AdjPPAnalyzer() {
        layer = Layer.TOKEN;
        adjDict = new TreeMap();

        ppsBeforeVAdjAttr = new ArrayList();
        ppsAfterVAdjAttr = new ArrayList();
        ppsBeforeAdjAttr = new ArrayList();
        ppsAfterAdjAttr = new ArrayList();

        ppsBeforeVAdjPred = new ArrayList();
        ppsAfterVAdjPred = new ArrayList();
        ppsBeforeAdjPred = new ArrayList();
        ppsAfterAdjPred = new ArrayList();

        vadjPreps = new TreeMap();
        adjPreps = new TreeMap();

        adjFreqs = new HashMap();
        adjsByFreq = new ArrayList();
        adjPrepFreqs = new ArrayList();
    }

    public static void main(String[] args) throws IOException {
        String serverRoot = "/home/patricia/adjectives/";
        String homeRoot = "/home/patricia/Dokumente/A3Project/";

        AdjPPAnalyzer ta = new AdjPPAnalyzer();

        //File adjDictFile = new File(serverRoot+"sortedAdjDict.txt");
        //adjDictFile = new File(homeRoot+"data/August2017/sortedAdjDict.txt");
        //ta.readAdjDictFromFile(adjDictFile);
        //File tazFile = new File(homeRoot+"/taz/1998/19980102.conll.gz");
        File tazDir = new File("/data/treebanks/taz/r2/");
        //File myTazDir = new File(homeRoot+"taz_sample");
        //myTazDir = new File(homeRoot+"taz");

        File adjFreqs = new File(homeRoot+"/data/August2017/freqs_adjs.txt");
        ta.fillAdjFreqs(adjFreqs);
        File ppadjs = new File(homeRoot+"/data/August2017/freqs_ppAdjs.txt");
        File adjFreqsAllPPMod = new File(homeRoot+"/data/August2017/freqs_adjsPPAdjs.txt");
        ta.compareAdjsToPpAdjs(ppadjs, adjFreqsAllPPMod);

    }

    /**
     * Get statistics on how often adjectives occur with and without a modifying PP.
     * Precondition: adjFreqs needs to be filled
     *
     * @param ppadjs
     * @throws IOException
     */
    private void compareAdjsToPpAdjs(File ppadjs, File outputFile) throws IOException {
        InputStreamReader is = new InputStreamReader(new FileInputStream(ppadjs));
        BufferedReader br = new BufferedReader(is);
        FileWriter fw = new FileWriter(outputFile);
        String line;
        fw.write("Adjective\tAll\tPP-modified\tRatio\n");
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\t");
            float allFreq = adjFreqs.get(splitLine[0]);
            float ppFreq = Integer.parseInt(splitLine[1]);
            fw.write(splitLine[0]+"\t"+(int)allFreq+"\t"+(int)ppFreq+"\t"+(int)(ppFreq/allFreq*100.0)+"\n");
        }
        br.close();
        fw.close();
    }

    /**
     *
     * @param outputFileVadj
     * @param outputFileAdj
     * @param adjsByFreqs
     * @throws IOException
     */
    private static void splitAdjFreqsToFiles(File outputFileVadj, File outputFileAdj, File adjsByFreqs) throws IOException {
        FileWriter fwVadj = new FileWriter(outputFileVadj);
        FileWriter fwAdj = new FileWriter(outputFileAdj);
        String line;
        InputStreamReader is = new InputStreamReader(new FileInputStream(adjsByFreqs));
        BufferedReader br = new BufferedReader(is);
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\t");

            if (adjDict.get(splitLine[0])) {
                fwVadj.write(splitLine[0] + "\t" + splitLine[1]+"\n");
            } else {
                fwAdj.write(splitLine[0] + "\t" + splitLine[1]+"\n");
            }
        }
        br.close();
        fwVadj.close();
        fwAdj.close();
    }

    /**
     * Fill adjDict from a file.
     * @param dictFile The file from which the adjective-isVerbalComposite pairs are read
     * @throws IOException
     */
    public static void readAdjDictFromFile(File dictFile) throws IOException {
        InputStreamReader is = new InputStreamReader(new FileInputStream(dictFile));
        BufferedReader br = new BufferedReader(is);
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\t");
            adjDict.putIfAbsent(splitLine[0], Boolean.parseBoolean(splitLine[1]));
        }
        br.close();
    }

    /**
     * Find verb and adjective stem of verb-derived adjective
     */
    private void stemmer() {

    }

    public static void adjPrepFreqs(File fileDirectory) throws IOException {
        // Fill adjPrepFreqs with empy HashMaps
        for (int i = 0; i < adjsByFreq.size(); i ++) {
            Map<String, Integer> prep = new TreeMap();
            adjPrepFreqs.add(prep);
        }

        // For all files, get prepositions and their frequency
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getAdjPrepFreqs(file);
            }
        }

        //Sort each list of prepositions by their frequency
        for (int i = 0; i < adjPrepFreqs.size(); i++) {
            adjPrepFreqs.set(i, sortByValue(adjPrepFreqs.get(i)));
        }
    }

    public static void getAdjPrepFreqs(File conllFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or("_").equals("PP") &&
                            (sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJA") ||
                                    sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJD"))) {

                        int headPos = adjsByFreq.indexOf(sent.get(token.getHead().or(-1)).getForm().or("_").toLowerCase());
                        String tokenForm = token.getForm().or("_").toLowerCase();

                        if (adjPrepFreqs.get(headPos).containsKey(tokenForm)) {
                            adjPrepFreqs.get(headPos).put(tokenForm, adjPrepFreqs.get(headPos).get(tokenForm)+1);
                        }
                        else {
                            adjPrepFreqs.get(headPos).putIfAbsent(tokenForm, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get adjectives with their frequency - ordered by frequency -
     * from file generated by adjFreqs.
     * Also fill list of adjectives, sorted by their frequency
     * (automatically ordered since adjFreqFile contains adjs in order,
     * most to least frequent adjective)
     *
     * @param adjFreqFile The file which contains all ajdectives and their frequency
     * @throws IOException
     */
    public static void fillAdjFreqs(File adjFreqFile) throws IOException {
        InputStreamReader is = new InputStreamReader(new FileInputStream(adjFreqFile));
        BufferedReader br = new BufferedReader(is);
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\t");
            adjFreqs.putIfAbsent(splitLine[0], Integer.parseInt(splitLine[1]));
            adjsByFreq.add(splitLine[0]);
        }
        br.close();
    }

    public static void adjFreqs(File fileDirectory) throws IOException {
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getAdjFreqs(file);
            }
        }
    }

    private static void getAdjFreqs(File conllFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or("_").equals("PP") &&
                            (sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJA") ||
                                    sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJD"))) {
                        ;
                        String adj = sent.get(token.getHead().or(-1)).getForm().or("_").toLowerCase();

                        if (adjFreqs.get(adj) == null) {
                            adjFreqs.putIfAbsent(adj, 1);
                        }
                        else {
                            adjFreqs.put(adj, adjFreqs.get(adj)+1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get distribution of PPs, in particular those which have an ADJA/ADJD as their head.
     *
     * @param fileDirectory
     * @throws IOException
     */
    public void ppDistrs(File fileDirectory) throws Exception {
        if (adjDict.isEmpty())
            throw new Exception("AdjDict not filled.");

        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getPPDistr(file);
            }
        }
    }

    /**
     *
     * Find prepositional phrases and their distribution in the taz corpus.
     * Distinguish between PPs preceding and PPs following their nominal head.
     *
     * @param conllFile A single taz file
     * @throws IOException
     */
    public static void getPPDistr(File conllFile) throws IOException {

        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                boolean hasPP = false;
                int ppPos = -1;

                for (Token token : sent) {
                    if (token.getDepRel().or("_").equals("PP")) {
                        hasPP = true;
                        ppPos = token.getID();
                        break;
                    }
                }

                if (hasPP) {

                    int ppHead = sent.get(ppPos).getHead().or(-1);
                    Token head = sent.get(ppHead);

                    String headForm = head.getForm().or("_").toLowerCase();
                    String ppForm = sent.get(ppPos).getForm().or("_").toLowerCase();

                    // Check if head of PP is ADJA or ADJD
                    if (head.getPosTag().or ("_").equals("ADJA") ||
                            head.getPosTag().or ("_").equals("ADJD")) {
                        int headPos = head.getID();

                        //TODO: Remove as soon as dict is complete
                        if (! adjDict.containsKey(headForm)) {
                            System.out.println(sent.get(headPos)+" NOT in dict");
                            break;
                        }

                        sent.set(headPos, CoNLLUtils.createToken(headPos, "**"+headForm+"**"));
                        sent.set(ppPos, CoNLLUtils.createToken(ppPos, "**"+ppForm+"**"));

                        if (head.getPosTag().or("_").equals("ADJA")) {
                            if (headPos >= ppPos) { // PP occurs before ADJ head
                                if (adjDict.get(headForm)) {  // ADJ is verb-derived
                                    ppsBeforeVAdjAttr.add(sent);
                                    if (vadjPreps.containsKey(headForm))
                                        vadjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        vadjPreps.putIfAbsent(headForm, verbs);
                                    }
                                } else {  // ADJ is non-verb-derived
                                    ppsBeforeAdjAttr.add(sent);
                                    if (adjPreps.containsKey(headForm))
                                        adjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        adjPreps.putIfAbsent(headForm, verbs);
                                    }
                                }
                            } else {  // PP occurs after ADJ head
                                if (adjDict.get(headForm)) {  // ADJ is verb-derived
                                    ppsAfterVAdjAttr.add(sent);
                                    if (vadjPreps.containsKey(headForm))
                                        vadjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        vadjPreps.putIfAbsent(headForm, verbs);
                                    }
                                } else {  // ADJ is non-verb-derived
                                    ppsAfterAdjAttr.add(sent);
                                    if (adjPreps.containsKey(headForm))
                                        adjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        adjPreps.putIfAbsent(headForm, verbs);
                                    }
                                }
                            }
                        }
                        else if (head.getPosTag().or("_").equals("ADJD")) {
                            if (headPos >= ppPos) { // PP occurs before ADJ head
                                if (adjDict.get(headForm)) {  // ADJ is verb-derived
                                    ppsBeforeVAdjPred.add(sent);
                                    if (vadjPreps.containsKey(headForm))
                                        vadjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        vadjPreps.putIfAbsent(headForm, verbs);
                                    }
                                } else {  // ADJ is non-verb-derived
                                    ppsBeforeAdjPred.add(sent);
                                    if (adjPreps.containsKey(headForm))
                                        adjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        adjPreps.putIfAbsent(headForm, verbs);
                                    }
                                }
                            } else {  // PP occurs after ADJ head
                                if (adjDict.get(headForm)) {  // ADJ is verb-derived
                                    ppsAfterVAdjPred.add(sent);
                                    if (vadjPreps.containsKey(headForm))
                                        vadjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        vadjPreps.putIfAbsent(headForm, verbs);
                                    }
                                } else {  // ADJ is non-verb-derived
                                    ppsAfterAdjPred.add(sent);
                                    if (adjPreps.containsKey(headForm))
                                        adjPreps.get(headForm).add(ppForm);
                                    else {
                                        Set<String> verbs = new TreeSet();
                                        verbs.add(ppForm);
                                        adjPreps.putIfAbsent(headForm, verbs);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Map<String, Boolean> getAdjDict() {
        return adjDict;
    }

    public void setAdjDict(Map<String, Boolean> adjDict) { this.adjDict = adjDict;
    }

    public Map<String, Set<String>> getVadjPreps() {
        return vadjPreps;
    }

    public void setVadjPreps(Map<String, Set<String>> vadjPreps) {
        this.vadjPreps = vadjPreps;
    }
}
