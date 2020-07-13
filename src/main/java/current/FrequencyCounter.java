package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static current.DataUtils.sortByValue;

/**
 * Created by patricia on 2/09/17.
 */
public class FrequencyCounter {

    private static Map<String, Integer> adjFreqs;
    private static Map<String, Integer> ppAdjFreqs;
    private static Map<String, Integer> baseverbFreqs;
    private static Map<String, Integer> ppBaseverbFreqs;

    private static List<Map<String, Integer>> ppFreqs;

    private static Set<String> baseVerbs;

    public FrequencyCounter() throws IOException {
        adjFreqs = new TreeMap();
        ppAdjFreqs = new TreeMap();
        baseverbFreqs = new TreeMap();
        ppBaseverbFreqs = new TreeMap();

        ppFreqs = new ArrayList();

        BaseVerbs bv = new BaseVerbs();
        baseVerbs = bv.getBaseverbs();
    }

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {
        //String root = "/home/patricia/Dokumente/A3Project/data/10-17/";
        //String taz = "/home/patricia/Dokumente/A3Project/corpora/taz";

        String root = "/home/patricia/adjectives-10-17/";
        String taz = "/data/treebanks/taz/r2/";
        System.out.println("Processing frequencies");

        FrequencyCounter fc = new FrequencyCounter();
        // Get frequencies of (PP-modified and non-modified) ADJ and baseverb lemmas
        fc.freqs(taz);
        IOUtils.intMapToFile(adjFreqs, root+"freqs_adjs_all_lemma.txt");
        IOUtils.intMapToFile(ppAdjFreqs, root+"freqs_adjs_pp_lemma.txt");
        IOUtils.intMapToFile(baseverbFreqs, root+"freqs_baseverbs_all_lemma.txt");
        IOUtils.intMapToFile(ppBaseverbFreqs, root+"freqs_baseverbs_pp_lemma.txt");

        System.out.println("Processing prepositions");

        // Get PP frequencies for each (PP-modified and non-modified) ADJ
        List<String> adjs = IOUtils.keysFromFile(root+"freqs_adjs_all_lemma.txt"); //ADJ - freq
        fc.getPrepFreqs(taz, adjs,"A");
        IOUtils.mapListToFile(ppFreqs, root+"freqs_pps_adjs_all_lemma.txt"); //{PP-freq, ...}

        List<String> ppadjs = IOUtils.keysFromFile(root+"freqs_adjs_pp_lemma.txt"); //PP-ADJ - freq
        ppFreqs.clear();
        fc.getPrepFreqs(taz, ppadjs,"A");
        IOUtils.mapListToFile(ppFreqs, root+"freqs_pps_adjs_pp_lemma.txt"); //{PP-freq, ...}


        // Get PP frequencies for each baseverb
        ppFreqs.clear();
        List<String> verbs = IOUtils.keysFromFile(root+"freqs_baseverbs_all_lemma.txt"); //verb - freq
        fc.getPrepFreqs(taz, verbs,"V");
        IOUtils.mapListToFile(ppFreqs, root+"freqs_pps_baseverbs_all_lemma.txt"); //{PP-freq, ...}

        ppFreqs.clear();
        List<String> ppverbs= IOUtils.keysFromFile(root+"freqs_baseverbs_pp_lemma.txt"); //PP-verb -freq
        fc.getPrepFreqs(taz, ppverbs,"V");
        IOUtils.mapListToFile(ppFreqs, root+"freqs_pps_baseverbs_pp_lemma.txt"); //{PP-freq, ...}
    }

    public static void freqs(String fileDir) throws InvocationTargetException, IllegalAccessException, IOException {
        File dir = new File(fileDir);
        for (File subDir : dir.listFiles()) {
            for (File f : subDir.listFiles()) {
                getFreqs(f);
            }
        }
    }

    /**
     * Get frequencies of attributive and predicative adjectives (ADJA and ADJD)
     * along with the frequencies of the verbal adjective base verbs. For both
     * adjectives and verbs, get the PP-modified adjectives and verbs separately.
     *
     * @param conllFile
     * @throws IOException
     */
    public static void getFreqs(File conllFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                sent = CoNLLUtils.lowerCaseTokens(sent, true);

                for (Token token : sent) {

                    String lemma = token.getLemma().or("_");
                    String dependency = token.getDepRel().or("_");

                    if (dependency.equals("PP") || dependency.equals("OBJP")) {

                        int head = getConjunctiveHead(token, sent);
                        // Not root token && don't add head twice if modified by more than 1 PP
                        if (head > -1) {
                            String headTag = sent.get(head).getPosTag().or("_");

                            // Adjectives modified by a PP
                            if (headTag.equals("ADJA") || headTag.equals("ADJD")) {
                                String adjective = sent.get(head).getLemma().or("_");
                                addToFreqMap(adjective, ppAdjFreqs);
                            }
                            // Base verbs modified by a PP
                            else if (headTag.startsWith("V")) {
                                String verb = sent.get(head).getLemma().or("_");
                                if (baseVerbs.contains(verb)) {
                                    addToFreqMap(verb, ppBaseverbFreqs);
                                }
                            }
                        }
                    }
                    String tag = token.getPosTag().or("_");
                    // Adjectives
                    if (tag.equals("ADJA") || tag.equals("ADJD") ) {
                        addToFreqMap(lemma, adjFreqs);
                    }
                    // Base verbs
                    else if (tag.startsWith("V")) {
                        if (baseVerbs.contains(lemma)) {
                            addToFreqMap(lemma, baseverbFreqs);
                        }
                    }
                }
            }
        }
    }

    private static void addToFreqMap(String word, Map<String, Integer> freqMap) {
        if (freqMap.get(word) == null) {
            freqMap.putIfAbsent(word, 1);
        } else {
            freqMap.put(word, freqMap.get(word) + 1);
        }
    }

    /**
     * Get head of specified token. Take conjunctions (PoS tag starts with "KO" or token is ",")
     * into consideration and get adjectival ADJA/ADJD or verbal V head if exists.
     *
     * @param token The token to find the head of
     * @param sent The sentence in which the token occurs
     * @return The index of the token's head, -1 for no adjectival or verbal head
     */
    private static int getConjunctiveHead(Token token, List<Token> sent) {
        int originalHead = token.getHead().or(-1);
        int head = token.getHead().or(-1);
        while (head >= 0) {
            String headTag = sent.get(head).getPosTag().or("_");
            if (headTag.equals("ADJ") || headTag.equals("ADJD") || headTag.startsWith("V")) {
                return token.getHead().or(-1);
            } else if (headTag.startsWith("KO") ||
                    sent.get(head).getLemma().or("_").equals(",")) {
                // Move to current token's head
                token = sent.get(head);
                head = token.getHead().or(-1);
            }
            else {
                break;
            }
        }
        return originalHead;
    }

    public static void getPrepFreqs(String directory, List<String> headLemmas, String tagFilter) throws IOException {
        File fileDirectory = new File(directory);
        // Fill ppFreqs with empty HashMaps
        for (int i = 0; i < headLemmas.size(); i ++) {
            Map<String, Integer> prep = new TreeMap();
            ppFreqs.add(prep);
        }

        // For all files, get prepositions and their frequency
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                prepFreqs(file, headLemmas, tagFilter);
            }
        }

        //Sort each list of prepositions by their frequency
        for (int i = 0; i < ppFreqs.size(); i++) {
            ppFreqs.set(i, sortByValue(ppFreqs.get(i)));
        }
    }

    public static void prepFreqs(File conllFile, List<String> headLemmas, String tagFilter) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                sent = CoNLLUtils.lowerCaseTokens(sent, true);

                for (Token token : sent) {int head1 = -1;

                    if (token.getHead().or(-1) > -1) {
                        head1 = sent.get(token.getHead().or(-1)).getID();
                    }
                    int head2 = -1;
                    if (head1 > -1 && sent.get(head1).getHead().or(-1) > -1) {
                        head2 = sent.get(sent.get(head1).getHead().or(-1)).getID();
                    }

                    String lemma = token.getLemma().or("_");
                    String dependency = token.getDepRel().or("_");
                    int head = getConjunctiveHead(token, sent);
                    if (head > -1 && (dependency.equals("PP") || dependency.equals("OBJP"))) {

                        String headTag = sent.get(head).getPosTag().or("_");
                        int headHead = sent.get(head).getHead().or(-1);
                        // ADJ-PP or V-PP
                        if ((tagFilter.equals("A") && (headTag.equals("ADJA") || headTag.equals("ADJD"))) ||
                                (tagFilter.equals("V") && headTag.startsWith("V"))) {
                            int listPos = headLemmas.indexOf(sent.get(head).getLemma().or("_"));
                            if (listPos > -1) {
                                Map<String, Integer> preps = ppFreqs.get(listPos);
                                addToFreqMap(lemma, preps);
                            }
                        }
                        // Multiword prepositions and circumpositions: PP1-ADJ-PP2 or PP1-V-PP2
                        // PP2 headed by PP1, PP1 headed by ADJ/V
                        else if (headHead > -1) {

                            String headDep = sent.get(head).getDepRel().or("_");
                            Token contentHead = sent.get(headHead);
                            String contentHeadTag = contentHead.getPosTag().or("_");

                            if ((head > contentHead.getID()) &&
                                    (headDep.equals("PP") || headDep.equals("OBJP")) &&
                                    ((tagFilter.equals("A") && (contentHeadTag.equals("ADJA") || contentHeadTag.equals("ADJD"))) ||
                                    (tagFilter.equals("V") && contentHeadTag.startsWith("V")))) {

                                int listPos = headLemmas.indexOf(contentHead.getLemma().or("_"));
                                if (listPos > -1 ) {
                                    String mainPrep = sent.get(head).getLemma().or("_");
                                    String combinedPrep = mainPrep + " " + lemma;
                                    Map<String, Integer> preps = ppFreqs.get(listPos);
                                    // Add combined PP (PP1 and PP2)
                                    addToFreqMap(combinedPrep, preps);

                                    // Reduce freq or remove entry for main prep (PP1 without PP2)
                                    Integer freq = preps.get(mainPrep);
                                    if (freq == null) {
                                        break;
                                    } else if (freq > 1) {
                                        preps.put(mainPrep, freq - 1);
                                    } else if (freq == 1) {
                                        preps.remove(mainPrep);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
