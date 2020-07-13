package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by patricia on 3/09/17.
 */
public class Distributions {

    private static Map<String, Boolean> adjDict;

    private static int ppsBeforeAdjAttr;
    private static int ppsAfterAdjAttr;
    private static int ppsBeforeAdjPred;
    private static int ppsAfterAdjPred;

    private static int ppsBeforeVAdjAttr;
    private static int ppsAfterVAdjAttr;
    private static int ppsBeforeVAdjPred;
    private static int ppsAfterVAdjPred;

    private static int ppsBeforeVAdvAttr;
    private static int ppsAfterVAdvAttr;
    private static int ppsBeforeVAdvPred;
    private static int ppsAfterVAdvPred;

    public Distributions() throws IOException {

        ppsBeforeVAdjAttr = 0;
        ppsAfterVAdjAttr = 0;
        ppsBeforeAdjAttr = 0;
        ppsAfterAdjAttr = 0;

        ppsBeforeVAdjPred = 0;
        ppsAfterVAdjPred = 0;
        ppsBeforeAdjPred = 0;
        ppsAfterAdjPred = 0;

        ppsBeforeVAdvAttr = 0;
        ppsAfterVAdvAttr = 0;
        ppsBeforeVAdvPred = 0;
        ppsAfterVAdvPred = 0;

        AdjectiveDictionary dictionary = new AdjectiveDictionary();
        adjDict = dictionary.getAdjDict();
    }

    public static void main(String[] args) throws Exception {
        Distributions distributions = new Distributions();
        String taz = "/home/patricia/Dokumente/A3Project/corpora/taz";
        //String taz = "/data/treebanks/taz/r2/";
        distributions.ppDistrs(taz);
    }

    public void ppDistrs(String directory) throws Exception {
        if (adjDict.isEmpty())
            throw new Exception("AdjDict not filled.");

        File fileDirectory = new File(directory);
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getPPDistr(file);
            }
        }
        System.out.printf("# PPs before attributive verb-derived adj: %d\n", ppsBeforeVAdjAttr);
        System.out.printf("# PPs after attributive verb-derived adj: %d\n", ppsAfterVAdjAttr);
        System.out.printf("# PPs before predicative verb-derived adj: %d\n", ppsBeforeVAdjPred);
        System.out.printf("# PPs after predicative verb-derived adj: %d\n", ppsAfterVAdjPred);
        System.out.printf("# PPs before attributive adj: %d\n", ppsBeforeAdjAttr);
        System.out.printf("# PPs after attributive adj: %d\n", ppsAfterAdjAttr);
        System.out.printf("# PPs before predicative adj: %d\n", ppsBeforeAdjPred);
        System.out.printf("# PPs after predicative adj: %d\n", ppsAfterAdjPred);
        System.out.printf("# PPs before attributive adv: %d\n", ppsBeforeVAdvAttr);
        System.out.printf("# PPs after attributive adv: %d\n", ppsAfterVAdvAttr);
        System.out.printf("# PPs before predicative adv: %d\n", ppsBeforeVAdvPred);
        System.out.printf("# PPs after predicative adv: %d\n", ppsAfterVAdvPred);
        int sum = ppsBeforeVAdjAttr+ppsAfterVAdjAttr+ppsBeforeAdjAttr+ppsAfterAdjAttr
                +ppsBeforeVAdjPred+ppsAfterVAdjPred+ppsBeforeAdjPred+ppsAfterAdjPred
                +ppsBeforeVAdjAttr+ppsBeforeVAdvPred+ppsAfterVAdvAttr+ppsAfterVAdvPred;
        System.out.println(sum); //make sure lemmas and tokens return the same total number of sentences/instances
    }

    /**
     * Find prepositional phrases and their distribution in the taz corpus.
     * Distinguish between
     * - PPs preceding and PPs following their adjective head.
     * - attributive and predicative adjectives.
     * - verb-derived adjectives and non-verbal adjectives
     *   (based on adjective dictionary {String-adjective : Boolean-isVerbderived}
     * @param conllFile A single taz file
     * @throws IOException
     */
    public static void getPPDistr(File conllFile) throws IOException {

        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");
                sent = CoNLLUtils.lowerCaseTokens(sent, true);

                for (Token token : sent) {
                    if ((token.getDepRel().or("_").equals("PP") || token.getDepRel().or("_").equals("OBJP")) &&
                            (sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJA") ||
                                    sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJD"))) {
                        int ppPos = token.getID();
                        int adjPos = token.getHead().or(-1);
                        Token adjective = sent.get(adjPos);
                        String adjLemma = adjective.getLemma().or("_");
                        String adjDepRel = adjective.getDepRel().or("_");

                        if (!adjDict.containsKey(adjLemma)) {
                            System.out.println(adjLemma + " NOT in dict");
                            System.out.println(conllFile.getName());
                            break;
                        }

                        if (adjective.getPosTag().or("_").equals("ADJA")) { // attributive adj
                            if (adjPos >= ppPos) { // PP occurs before ADJ head
                                if (adjDict.get(adjLemma)) {  // ADJ is verb-derived
                                    if (adjDepRel.equals("ADV")) {  // verb-derived ADJ is ADV
                                        ppsBeforeVAdvAttr++;
                                    }
                                    else {  // verb-derived ADJ is non-ADV
                                        ppsBeforeVAdjAttr++;
                                    }
                                } else {  // ADJ is non-verb-derived
                                    ppsBeforeAdjAttr++;
                                }
                            } else {  // PP occurs after ADJ head
                                if (adjDict.get(adjLemma)) {
                                    if (adjDepRel.equals("ADV")) {
                                        ppsAfterVAdvAttr++;
                                    }
                                    else {
                                        ppsAfterVAdjAttr++;
                                    }
                                } else {
                                    ppsAfterAdjAttr++;
                                }
                            }
                        } else if (adjective.getPosTag().or("_").equals("ADJD")) { //predicative adj
                            if (adjPos >= ppPos) {
                                if (adjDict.get(adjLemma)) {
                                    if (adjDepRel.equals("ADV")) {
                                        ppsBeforeVAdvPred++;
                                    }
                                    else {
                                        ppsBeforeVAdjPred++;
                                    }
                                } else {
                                    ppsBeforeAdjPred++;
                                }
                            } else {
                                if (adjDict.get(adjLemma)) {
                                    if (adjDepRel.equals("ADV")) {
                                        ppsAfterVAdvPred++;
                                    }
                                    else {
                                        ppsAfterVAdjPred++;
                                    }
                                } else {
                                    ppsAfterAdjPred++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
