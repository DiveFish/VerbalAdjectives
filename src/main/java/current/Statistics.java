package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by patricia on 2/09/17.
 */
public class Statistics {

    /**
     - read adjective-prepositions list
     - read baseverb-prepositions list
     - get first preposition for each
     - compare first preposition of adjective to first preposition of baseverb
     - count how often they overlap

     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String homeRoot = "/home/patricia/Dokumente/A3Project/data/10-17/taz/";
        String taz = "/home/patricia/Dokumente/A3Project/corpora/taz";

        AdjectiveDictionary dictionary = new AdjectiveDictionary();

        getRatio(homeRoot);
        getDictionarySample(homeRoot, dictionary.getAdjDict());
    }

    public static void mapLemmaAndForm(String homeRoot, String tazDirectory) throws IOException {
        Map<String, String> tokensToLemma = new TreeMap();
        File fileDirectory = new File(tazDirectory);
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                        new FileInputStream(file)))))) {
                    for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                        List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                        sent = CoNLLUtils.lowerCaseTokens(sent, true);

                        for (Token token : sent) {
                            String pos = token.getPosTag().or("_");
                            if (pos.equals("ADJA") || pos.equals("ADJD")) {
                                tokensToLemma.putIfAbsent(token.getLemma().or("_"), token.getForm().or("_"));
                            }
                        }
                    }
                }
            }
        }
        IOUtils.stringMapToFile(tokensToLemma, homeRoot+"lemmasToForms.txt");
    }

    public static void getDictionarySample(String homeRoot, Map<String, Boolean> dictionary) throws IOException {
        BaseVerbs baseverbs = new BaseVerbs();
        Map<String, String> adjVerbs = baseverbs.getBaseverbDict();

        List<String> mostFreqAdjs = IOUtils.listFromFile(homeRoot+"freqs_adjs_all_lemma_byFreq.txt");

        Map<String, Boolean> mostFrequent = new TreeMap();
        Map<String, Boolean> mostFrequentVerbal = new TreeMap();

        int isVerbalCount = 0;
        int isNonVerbalCount = 0;

        for (String adj : mostFreqAdjs) {
            Boolean isVerbal = dictionary.get(adj);
            if (mostFrequent.size() < 100) {
                if (isVerbal) {
                    mostFrequent.putIfAbsent(adj+" ("+adjVerbs.get(adj)+") ", isVerbal);
                }
                else {
                    mostFrequent.putIfAbsent(adj, isVerbal);
                }
            }
            if (isVerbalCount < 100 && isVerbal) {
                mostFrequentVerbal.putIfAbsent(adj+" ("+adjVerbs.get(adj)+") ", isVerbal);
                isVerbalCount++;
            }
            if (isNonVerbalCount < 100 && !isVerbal) {
                isNonVerbalCount++;
                mostFrequentVerbal.putIfAbsent(adj, isVerbal);
            }
        }
        IOUtils.booleanMapToFile(mostFrequentVerbal, "/home/patricia/Dokumente/A3Project/data/10-17/gold_standard/adjDict_100TrueFalse_taz.txt");

        List<String> adjectives = IOUtils.keysFromFile(homeRoot+"freqs_adjs_all_lemma.txt");
        /*
        Set<String> misses = new TreeSet();
        for (String a : adjectives) {
            if (dictionary.get(a) == null) {
                misses.add(a);
            }
        }
        IOUtils.setToFile(misses, homeRoot+"misses.txt");
        */
        isVerbalCount = 0;
        isNonVerbalCount = 0;
        mostFrequentVerbal.clear();
        while (mostFrequentVerbal.size() < 200) {
            int randIndex = new Random().nextInt(adjectives.size());
            String adj = adjectives.get(randIndex);
            Boolean isVerbal = dictionary.get(adj);
            if (isVerbalCount < 100 && isVerbal) {
                mostFrequentVerbal.putIfAbsent(adj+" ("+adjVerbs.get(adj)+") ", isVerbal);
                isVerbalCount++;
            }
            if (isNonVerbalCount < 100 && !isVerbal) {
                isNonVerbalCount++;
                mostFrequentVerbal.putIfAbsent(adj, isVerbal);
            }
        }

        IOUtils.booleanMapToFile(mostFrequentVerbal, "/home/patricia/Dokumente/A3Project/data/10-17/gold_standard/adjDict_100TrueFalse_randomized_taz.txt");
    }

    public void getPPOverlap(String homeRoot, Map<String, Boolean> dictionary) throws IOException {
        List<String> adjs = IOUtils.keysFromFile(homeRoot + "freqs_adjs_pp_lemma.txt");
        List<String> adjPPsRaw = IOUtils.listFromFile(homeRoot + "freqs_pps_adjs_pp_lemma.txt");
        Map<String, String> adjPPs = new TreeMap();
        for (int i = 0; i < adjs.size(); i++) {
            String[] line = adjPPsRaw.get(i).split(",");
            String prep;
            line = line[0].split("=");
            prep = line[0].substring(1);
            adjPPs.putIfAbsent(adjs.get(i), prep);
        }
        List<String> ppBaseverbs = IOUtils.keysFromFile(homeRoot + "freqs_baseverbs_pp_lemma.txt");
        List<String> baseverbPPsRaw = IOUtils.listFromFile(homeRoot + "freqs_pps_baseverbs_pp_lemma.txt");
        Map<String, String> baseverbPPs = new HashMap();
        for (int i = 0; i < ppBaseverbs.size(); i++) {
            String[] line = baseverbPPsRaw.get(i).split(",");
            String prep;
            line = line[0].split("=");
            prep = line[0].substring(1);
            baseverbPPs.putIfAbsent(ppBaseverbs.get(i), prep);
        }

        // print adj + prep
        // print verb + prep
        Map<String, String> adjBaseverbs = IOUtils.stringMapFromFile(homeRoot + "baseverbs.txt");
        int overlap = 0;
        int total = 0;
        for (Map.Entry entry : adjPPs.entrySet()) {
            if (dictionary.get(entry.getKey())) {
                if (baseverbPPs.get(adjBaseverbs.get(entry.getKey())) != null) {
                    System.out.println(entry.getKey()+": "+entry.getValue());
                    System.out.println(adjBaseverbs.get(entry.getKey())+": "+baseverbPPs.get(adjBaseverbs.get(entry.getKey())));
                    System.out.println();
                    if (entry.getValue().equals(baseverbPPs.get(adjBaseverbs.get(entry.getKey())))) {
                        overlap++;
                    }
                    total++;
                }
                else { // Not all adjective baseverbs are contained in taz
                    System.out.println(entry.getKey());
                    System.out.println(adjBaseverbs.get(entry.getKey()));
                    System.out.println();
                }
            }
        }
        System.out.println("Total of PP-modfied verb-derived adjectives: " + total);
        System.out.println("Overlapping " + overlap);
        System.out.println((double) overlap / (total / 100) );
    }


    /**
     * Get 30 most frequent PP-modified adjectives along with their most frequent PPs.
     *
     * @param homeRoot
     * @throws IOException
     */
    public static void getFrequentPrepositions(String homeRoot) throws IOException {

        Map<String, String> adjPPMap = new HashMap();
        List<String> ppadjs = IOUtils.keysFromFile(homeRoot+"freqs_adjs_pp_lemma.txt");
        List<String> pps = IOUtils.listFromFile(homeRoot+"freqs_pps_adjs_pp_lemma.txt");
        if (ppadjs.size() == pps.size()) {
            for (int i = 0; i < ppadjs.size(); i++) {
                adjPPMap.putIfAbsent(ppadjs.get(i), pps.get(i));
            }
        }
        Map<String, Integer> ppAdjFreqs = IOUtils.intMapFromFile(homeRoot+"freqs_adjs_pp_lemma.txt");
        ppAdjFreqs = DataUtils.sortByValue(ppAdjFreqs);

        FileWriter fw = new FileWriter(new File(homeRoot+"dict_adj_pps.txt"));
        int counter = 0;
        for (Map.Entry<String, Integer> entry : ppAdjFreqs.entrySet()) {
            String adj = entry.getKey();
            if (counter < 30) {
                fw.write(adj+"\t"+adjPPMap.get(adj)+"\n\n");
            }
        }
        fw.close();
    }

    /**
     * Get modification ratio for PP-modified adjectives and adjective-baseverbs
     * @throws IOException
     */
    public static void getRatio(String homeRoot) throws IOException {

        String adj_file = homeRoot+"freqs_adjs_all_lemma.txt";
        Map<String, Integer> adjectives = IOUtils.intMapFromFile(adj_file);

        String adjPP_file = homeRoot+"freqs_adjs_pp_lemma.txt";
        Map<String, Integer> adjectives_pp = IOUtils.intMapFromFile(adjPP_file);

        Map<String, Integer> mapByAdjFreq = new TreeMap();
        Map<String, Integer> mapByModRatio = new TreeMap();
        adjectives_pp = DataUtils.sortByValue(adjectives_pp);   //sort by frequency, highest first

        StringBuilder sb = new StringBuilder();
        sb.append("Adjectives\tFreqs\tPP-Modified\t% Modified\n");
        int count = 0;
        for (Map.Entry<String, Integer> entry : adjectives_pp.entrySet()) {
            String adj = entry.getKey();
            if (count < 1000) {
                int freq_pp = entry.getValue();
                int freq = 0;
                if (adjectives.get(adj) != null)
                    freq = adjectives.get(adj);
                int ratio = (int) (((double) freq_pp / (double) freq) * 100);
                mapByAdjFreq.putIfAbsent(adj + "\t" + freq_pp + "\t" + ratio, freq);
                mapByModRatio.putIfAbsent(adj + "\t" + freq + "\t" + freq_pp, ratio);
                sb.append(adj + "\t" + freq + "\t" + freq_pp + "\t" + ratio + "\n");
            }
            count++;
        }
        mapByAdjFreq = DataUtils.sortByValue(mapByAdjFreq);
        mapByModRatio = DataUtils.sortByValue(mapByModRatio);

        IOUtils.intMapToFile(mapByAdjFreq, homeRoot+"freqs_ratio_adjectives_byFreqs.txt");
        IOUtils.intMapToFile(mapByModRatio, homeRoot+"freqs_ratio_adjectives_byRatio.txt");

        FileWriter fw = new FileWriter(new File(homeRoot+"freqs_ratio_adjectives.txt"));
        fw.write(sb.toString());
        fw.close();

    }


}
