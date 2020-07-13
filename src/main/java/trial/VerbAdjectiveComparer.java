package trial;

import current.CoNLLUtils;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static current.DataUtils.sortByValue;

/**
 * Created by patricia on 27/08/17.
 */
public class VerbAdjectiveComparer {

    private static final Pattern P_VERB = Pattern.compile("([a-zA-Zäöüß]*)<(VPART|VPREF)?>([a-zA-Zäöüß]+)<V>");// = Pattern.compile("([a-zA-Z]*)<VPART>([a-zA-Z]+)<V>");
    private static Map<String, String> adjVerbStems;
    private static Map<String, Set<String>> verbPPs;
    private static List<Map<String, Integer>> vPrepFreqs;
    private static Set<String> baseVerbs;

    public VerbAdjectiveComparer() {
        adjVerbStems = new TreeMap();
        verbPPs = new TreeMap();
        vPrepFreqs = new ArrayList();
        baseVerbs = new TreeSet();
    }

    public static void main(String[] args) throws Exception {
        //String serverRoot = "/home/patricia/adjectives/";
        String homeRoot = "/home/patricia/Dokumente/A3Project/";

        String taz = homeRoot+"taz";
        //String taz = "/data/treebanks/taz/r2/";
        File tazDir = new File(taz);

        VerbAdjectiveComparer vac = new VerbAdjectiveComparer();
        /*
        vac.fillVerbStemMap(homeRoot+"/data/August2017/verbs_withAdjs.txt");
        vac.verbPPs(tazDir);

        adjectives.AdjPPAnalyzer appa = new adjectives.AdjPPAnalyzer();
        File adjDict = new File(homeRoot+"/data/August2017/dict_sortedAdjs.txt");
        //File adjDict = new File(serverRoot+"sortedAdjDict.txt");
        appa.readAdjDictFromFile(adjDict);
        appa.ppDistrs(tazDir);

        vac.verbPPs(tazDir);
        vac.getAdjVersusVerbPPs(appa.getVadjPreps());

        vPrepFreqs(tazDir);
        */
        String smor = homeRoot+"data/smorAdjAnalysis.txt";
        String missedSmor = homeRoot+"data/smorMissedAdjAnalysis.txt";
        vac.getBaseVerbs(smor);
        vac.getBaseVerbs(missedSmor);

        File baseVerbs = new File(homeRoot+"data/Sept2017/adjBaseverb.txt");
        FileWriter fw = new FileWriter(baseVerbs);
        for (Map.Entry enty : adjVerbStems.entrySet()) {
            fw.write(enty.getKey()+"\t"+enty.getValue()+"\n");
        }
        fw.close();
    }

    /**
     *
     *
     */
    public void getAdjVersusVerbPPs(Map<String, Set<String>> vadjPreps) {
        for (Map.Entry<String, String> entry : adjVerbStems.entrySet()) {
            // A verb can be the base of different adjectives
            if (vadjPreps.containsKey(entry.getKey()) && verbPPs.containsKey(entry.getValue())) {
                vadjPreps.get(entry.getKey()).retainAll(verbPPs.get(entry.getValue()));
                System.out.println(entry.getKey() + " " + entry.getValue() + "\t" + vadjPreps.get(entry.getKey()));
            }
        }
    }

    /**
     * Get prepositions for all adjective base verbs in taz.
     *
     * @param fileDirectory
     * @throws IOException
     */
    public void verbPPs(File fileDirectory) throws IOException {
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getVerbPPs(file);
            }
        }
    }

    /**
     * Find all prepositions which go with the adjective base verbs in file.
     *
     * @param conllFile
     * @throws IOException
     */
    private static void getVerbPPs(File conllFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or ("_").equals("PP") &&
                            baseVerbs.contains(sent.get(token.getHead().or(-1)).getLemma().or("_"))) {

                        String verb = sent.get(token.getHead().or(-1)).getLemma().or("_").toLowerCase();
                        if (!verbPPs.containsKey(verb)) {
                            Set<String> pps = new TreeSet();
                            pps.add(token.getForm().or("_").toLowerCase());
                            verbPPs.putIfAbsent(verb, pps);
                        }
                        else {
                            verbPPs.get(verb).add(token.getForm().or("_").toLowerCase());
                        }
                    }
                }
            }
        }
    }

    /**
     * Read adjective - verb stem map from file.
     *
     * @param inputFile
     * @throws IOException
     */
    public void fillVerbStemMap(String inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            adjVerbStems.putIfAbsent(line.split("\t")[0], line.split("\t")[1]);
            baseVerbs.add(line.split("\t")[1]);
        }
        br.close();
    }

    /**
     * Save adjective verb stems in a file.
     *
     * @param outputFile The file to store the adjective - base verb map in
     * @throws IOException
     */
    private static void printAdjVerbMap(String outputFile) throws IOException {
        FileWriter fw = new FileWriter(new File(outputFile), false);
        for(Map.Entry<String, String> entry : adjVerbStems.entrySet()) {
            fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        fw.close();
    }

    /**
     * Find the base verb for each verb-derived adjective.
     *
     * @param smoranalysis The file which contains the smor analysis
     * @throws IOException
     */
    private void getBaseVerbs(String smoranalysis) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(smoranalysis)));
        String line;
        String adjective = "";
        String verb = "";
        int prevLineLength = Integer.MAX_VALUE;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("analyze>")) {
                // add verb stem to list of verb stems
                if (line.length() < 9) {
                    continue;
                }
                else {  // (line.length() >= 9) {
                    if (! verb.equals("")) {
                        adjVerbStems.putIfAbsent(adjective, verb);
                    }
                    adjective = line.substring(9).toLowerCase();
                    verb = "";
                }
                prevLineLength = Integer.MAX_VALUE;
            } else if (line.length() < prevLineLength) {    // prefer shorter analyses over longer ones
                if ((line.contains("<ADJ>") || (line.contains("<+ADJ>"))) && line.contains("<V>")) {
                    //isVerbalComposite = true -> get verb stem
                    Matcher mVerb = P_VERB.matcher(line);
                    if (mVerb.find()) {
                        verb = mVerb.group(1)+mVerb.group(3);
                    }
                }
                prevLineLength = line.length();
            } else {
                prevLineLength = line.length();
            }
        }
    }


    public static void vPrepFreqs(File fileDirectory) throws IOException {
        // Fill vPrepFreqs with empy HashMaps
        for (int i = 0; i < baseVerbs.size(); i ++) {
            Map<String, Integer> prep = new TreeMap();
            vPrepFreqs.add(prep);
        }

        List<String> verbs = new ArrayList();
        for (String verb : baseVerbs) {
            verbs.add(verb);
        }

        // For all files, get prepositions and their frequency
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getVPrepFreqs(file, verbs);
            }
        }

        //Sort each list of prepositions by their frequency
        for (int i = 0; i < vPrepFreqs.size(); i++) {
            vPrepFreqs.set(i, sortByValue(vPrepFreqs.get(i)));
        }

        for (int j = 0; j < verbs.size(); j++) {
            System.out.println(verbs.get(j)+"\t"+vPrepFreqs.get(j));
        }
    }

    public static void getVPrepFreqs(File conllFile, List<String> verbs) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or ("_").equals("PP") &&
                            baseVerbs.contains(sent.get(token.getHead().or(-1)).getLemma().or("_"))) {

                        int headPos = verbs.indexOf(sent.get(token.getHead().or(-1)).getLemma().or("_").toLowerCase());
                        String ppForm = token.getForm().or("_").toLowerCase();

                        if (vPrepFreqs.get(headPos).containsKey(ppForm)) {
                            vPrepFreqs.get(headPos).put(ppForm, vPrepFreqs.get(headPos).get(ppForm)+1);
                        }
                        else {
                            vPrepFreqs.get(headPos).putIfAbsent(ppForm, 1);
                        }
                    }
                }
            }
        }
    }
}
