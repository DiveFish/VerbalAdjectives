package current;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by patricia on 11/07/17.
 *
 * Notes:
 * - lower case leads to misanalyses, e.g. "abendschau"-legend is verb-derived
 */
public class SmorAnalyzer {

    private static final Pattern P_VERB = Pattern.compile("(([a-zA-Zäöüß]*)<(VPART|VPREF)>)?([a-zA-Zäöüß]+)<(\\+)?V>");
    private static final Pattern P_VERBFORMS = Pattern.compile("([a-zA-Zäöüß]+)?(<VPART>)?([a-zA-Zäöüß]+)<\\+V>(<Inf>|<PPast>|<PPres>|<[1-3]><(Sg|Pl)>)");
    private static final Pattern P_PART = Pattern.compile("(([a-zA-Zäöüß]*)<(VPART|VPREF)>)?([a-zA-Zäöüß]+)<(\\+)?V><(PPast|PPres)>");
    private static final Pattern P_COMP = Pattern.compile("(([a-zA-Zäöüß]+)<(V)>)(([a-zA-Zäöüß]+)<(V)>)");
    private static final Pattern P_SUFF_COMP = Pattern.compile("(<SUFF>([a-zA-Zäöüß]+)<V>)");
    private static final Pattern P_V = Pattern.compile("(([a-zA-Zäöüß]+)<(V?PART|V?PREF)>)?([a-zA-Zäöüß]+)<V>(<(PPres|PPast)>)?<SUFF><\\+ADJ>");
    private static final Pattern P_NN_COMP = Pattern.compile("<NN>(<SUFF>)?(([a-zA-Zäöüß]+)<(PART|PREF)>)?([a-zA-Zäöüß]+)<\\+ADJ>");
    private static final Pattern P_NonV_COMP = Pattern.compile("<V>([a-zA-Zäöüß]+)<SUFF><\\+ADJ>");
    private static final Pattern P_IER_ADJ = Pattern.compile("([a-zA-Zäöüß]+ier)(t|en|bar)");//(e(s|t|r)?){0,2}");

    private static Set<String> adjectives;
    private static Map<String, Boolean> adjDict; // format: {String adjective, boolean isVerbalComposite}
    private static Map<String, String> adjBaseverbs;

    public SmorAnalyzer() {
        adjectives = new HashSet();
        adjDict = new TreeMap();
        adjBaseverbs = new TreeMap();
    }

    public static void main(String[] args) throws IOException {
        SmorAnalyzer sa = new SmorAnalyzer();
        adjectives = IOUtils.setFromFile("/home/patricia/all-adjs.txt.sorted");

        sa.getBaseVerbs(new File("/home/patricia/adjs/"));
    }

    public void getBaseVerbs(File smorDirectory) throws IOException {
        for (File file : smorDirectory.listFiles()) {
            if (file.getName().endsWith("_smor.txt")) {
                getBaseVerbsFromFile(file);
            }
        }
        IOUtils.stringMapToFile(matchAdjsToBaseverbs(), "/home/patricia/all-adjs_verbs.txt.sorted");
    }

    private void getBaseVerbsFromFile(File smoranalysis) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(smoranalysis));
        String line;
        String adjective = "";
        String verb = "NON-V";
        int shortestLineLength = Integer.MAX_VALUE;
        boolean prioUsed = false;
        String shortestLine = "";
        String feature = "";

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("analyze>")) {
                // add verb stem to list of verb stems
                if (line.length() >= 9) {
                    if (!verb.equals("") && !(adjective.equals(""))) {
                        if (verb.equals("ieren") || verb.equals("NON-V")) {
                            Matcher mIer = P_IER_ADJ.matcher(adjective);
                            if (mIer.find()) {
                                feature = getBaseverbFeature(shortestLine);
                                adjBaseverbs.putIfAbsent(adjective, mIer.group(1)+"en"+"\t"+feature);
                            }
                            else {
                                adjBaseverbs.putIfAbsent(adjective, "NON-V");
                            }
                        }
                        else {
                            feature = getBaseverbFeature(shortestLine);
                            adjBaseverbs.putIfAbsent(adjective, verb+"\t"+feature);
                        }
                    }
                    adjective = line.substring(9).toLowerCase();
                    verb = "NON-V";
                    prioUsed = false;
                    shortestLine = "";
                }
                shortestLineLength = Integer.MAX_VALUE;
            } else if (line.length() <= shortestLineLength) {    // prefer shorter analyses over longer ones
                if ((line.contains("<ADJ>") || (line.contains("<+ADJ>"))) &&
                        (line.contains("<V>") || line.contains("<+V>")) && !prioUsed) {
                    Matcher mVerb = P_VERB.matcher(line);
                    if (mVerb.find()) {
                        if (mVerb.group(2) == null) {
                            verb = mVerb.group(4);
                        } else {
                            verb = mVerb.group(2) + mVerb.group(4);
                        }
                        shortestLine = line;
                    }
                } else if (line.contains("V><PPres>") || (line.contains("V><PPast>"))) {
                    Matcher mPart = P_PART.matcher(line);
                    if (mPart.find()) {
                        if (mPart.group(2) == null) {
                            verb = mPart.group(4);
                        } else {
                            verb = mPart.group(2) + mPart.group(4);
                        }
                        shortestLine = line;
                    }
                    prioUsed = true;
                } else if ((line.contains("<ADJ>") || line.contains("<+ADJ>")) && !prioUsed) {
                    verb = "NON-V";
                }
                if (line.contains("<V>") || line.contains("<+V>")) {

                    Matcher mPart = P_SUFF_COMP.matcher(line);
                    if (mPart.find()) {
                        verb = mPart.group(2);
                        prioUsed = true;
                        shortestLine = line;
                    }

                    Matcher mPart2 = P_V.matcher(line);
                    if (mPart2.find()) {
                        if (mPart2.group(2) == null) {
                            verb = mPart2.group(4);
                        }
                        else {
                            verb = mPart2.group(2)+mPart2.group(4);
                        }
                        shortestLine = line;
                        prioUsed = true;
                    }

                    Matcher mPart3 = P_COMP.matcher(line);
                    if (mPart3.find()) {
                        verb = mPart3.group(2) + mPart3.group(5);
                        prioUsed = true;
                    }

                    Matcher mVForm = P_VERBFORMS.matcher(line);
                    if (mVForm.find()) {
                        if (mVForm.group(1) == null) {
                            verb = mVForm.group(3);
                        }
                        else {
                            verb = mVForm.group(1) + mVForm.group(3);
                        }
                        shortestLine = line;
                        prioUsed = true;
                    }

                    if (verb.endsWith("ier")) {
                        verb = verb+"en";
                    }
                }
                if (line.contains("+ADJ")) {
                    Matcher mNN = P_NN_COMP.matcher(line);
                    if (mNN.find()) {
                        verb = "NON-V";
                        prioUsed = true;
                    }
                }
                shortestLineLength = line.length();
            }
        }
        if (!verb.equals("") && !(adjective.equals(""))) {
            if (verb.equals("ieren") || verb.equals("NON-V")) {
                Matcher mIer = P_IER_ADJ.matcher(adjective);
                if (mIer.find()) {
                    feature = getBaseverbFeature(shortestLine);
                    adjBaseverbs.putIfAbsent(adjective, mIer.group(1)+"en"+"\t"+feature);
                }
                else {
                    adjBaseverbs.putIfAbsent(adjective, "NON-V");
                }
            }
            else {
                feature = getBaseverbFeature(shortestLine);
                adjBaseverbs.putIfAbsent(adjective, verb+"\t"+feature);
            }
        }
    }

    /**
     * Check whether verb-derived adjective is an infinitive,
     * participle or none of these.
     *
     * @param smorText The smor analysis of the adjective
     * @return
     */
    private String getBaseverbFeature(String smorText) {
        String feature = "";

        if (smorText.contains("PPres")) {
            feature = "present_participle";
        }
        else if (smorText.contains("PPast")) {
            feature = "past_participle";
        }
        else if (smorText.contains("<Inf>")) {
            feature = "infinitive";
        }
        if (feature.equals("")) {
            return "other";
        }

        return feature;
    }

    /**
     * Check whether all adjectives have been retrieved from the smor analysis.
     * Otherwise, add these adjectives with a "NON-V" label to the map.
     *
     * @return The map of adjective and isVerbalCompositve + feature
     */
    public Map<String, String> matchAdjsToBaseverbs() {
        Map<String, String> adjectiveFeatures = new TreeMap();
        assert !adjectives.isEmpty() : "Adjective list has not been initialized";
        assert !adjBaseverbs.isEmpty() : "Baseverb list has not been initialized";
        for (String adjective : adjectives) {
            if (adjBaseverbs.get(adjective) == null) {
                adjectiveFeatures.putIfAbsent(adjective, "NON-V");
            }
            else {
                adjectiveFeatures.putIfAbsent(adjective, adjBaseverbs.get(adjective));
            }
        }
        return adjectiveFeatures;
    }

    /**
     * Create a dictionary of adjectives and their smor analysis.
     * Read from file containing detailed smor analysis and look
     * for cases where "ADJ" and "<V>" occur in same line.
     *
     * Does length of line correlate with simplicity of analysis?
     * i.e. the shorter the line the more straight-forward and possibly more correct the analysis?
     *
     * @param inputFile The file form which all adjectives are saved in the dict
     * @throws IOException
     */
    public void fillAdjDict(File inputFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            String entry = "";
            int shortestLine = Integer.MAX_VALUE;
            boolean isVerbalComposite = false;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("analyze>")) {
                    adjDict.putIfAbsent(entry, isVerbalComposite);
                    if (line.length() >= 9) {
                        entry = line.substring(9).toLowerCase();
                    }
                    shortestLine = Integer.MAX_VALUE;
                    isVerbalComposite = false;
                } else if (line.length() <= shortestLine) {    // prefer shorter analyses over longer ones
                    if (((line.contains("<ADJ>") || (line.contains("<+ADJ>"))) && line.contains("<V>")) ||
                            line.contains("<+V><PPres>") || line.contains("<+V><PPast>")){  // participles
                        isVerbalComposite = true;
                    }
                    // put another else if condition which makes isVerbalComposite false for
                    // heads not being verbal
                    else {
                        isVerbalComposite = false;
                    }
                    shortestLine = line.length();
                }
            }
            adjDict.remove("");
        }
    }

    /**
     * Print is-verb-derived adjective dictionary to a file.
     *
     * @param outputFile The file to which the adjDict is printed
     * @throws IOException
     */
    private static void printAdjDictToFile(File outputFile) throws IOException {
        FileWriter fw = new FileWriter(outputFile, false);
        for(Map.Entry<String, Boolean> entry : adjDict.entrySet()) {
            if (entry.getValue()) {
                fw.write(entry.getKey() + "\ttrue\n");
            }
            else {
                fw.write(entry.getKey() + "\tfalse\n");
            }
        }
        fw.close();
    }

    public static void findAllMissedWords(String adjFile, File smorDir, String outputFile) throws IOException {
        Set<String> tokens = IOUtils.setFromFile(adjFile);
        Set<String> missedTokens = new TreeSet();
        for (File f : smorDir.listFiles()) {
            if (f.getName().endsWith("_smor.txt")) {
                findMissedWords(tokens, f, missedTokens);
            }
        }
        IOUtils.setToFile(missedTokens, outputFile);
    }

    /**
     * Use list of unique tokens and the file including the smor analysis.
     * Compare the words in the analysis file and check which of the unique
     * tokens are not in there.
     *
     * @param tokens The list of unique adjective tokens in the corpus
     * @param smorAnalysis The file where the smor analysis has been saved
     * @throws IOException
     */
    private static void findMissedWords(Set<String> tokens, File smorAnalysis, Set<String> missedTokens) throws IOException {

        Set<String> smorList = new HashSet();
        try (BufferedReader br = new BufferedReader(new FileReader(smorAnalysis))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("analyze>") && line.length() >= 9) {
                    System.out.println(line.substring(9));
                    smorList.add(line.substring(9));
                }
            }
        }
        missedTokens.removeAll(smorList);
    }

    private static void findMissedWords(String tokens, File adjDir, String outputFile) throws IOException {
        Set<String> adjectives = IOUtils.setFromFile(tokens);
        for (File f : adjDir.listFiles()) {
            if (! f.getName().endsWith("_smor.txt")) {
                Set<String> foundAdjectives = IOUtils.setFromFile(f.getPath());
                adjectives.removeAll(foundAdjectives);
            }
        }
        IOUtils.setToFile(adjectives, outputFile);
    }

}
