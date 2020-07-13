package trial;

import current.CoNLLUtils;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by patricia on 23/08/17.
 */
public class TopologicalFieldAnalyzer {

    private static final Pattern P_TF = Pattern.compile("tf:[a-zA-Z]{1,2}");

    private static int vf_vadjs = 0;
    private static int mf_vadjs = 0;
    private static int nf_vadjs = 0;

    private static int vf_adjs = 0;
    private static int mf_adjs = 0;
    private static int nf_adjs = 0;

    public static void main(String[] args) throws IOException {
        String serverRoot = "/home/patricia/adjectives/";
        String homeRoot = "/home/patricia/Dokumente/A3Project/";

        AdjPPAnalyzer appa = new AdjPPAnalyzer();
        File adjDictFile = new File(serverRoot+"sortedAdjDict.txt");
        adjDictFile = new File(homeRoot+"data/August2017/dict_sortedAdjs.txt");
        appa.readAdjDictFromFile(adjDictFile);

        TopologicalFieldAnalyzer tfa = new TopologicalFieldAnalyzer();
        File dir = new File("/data/treebanks/taz/r2/");
        dir = new File(homeRoot+"taz/");
        tfa.fieldCounts(dir, appa.getAdjDict());

        System.out.println("ADJs");
        System.out.println("vf\t\tmf\t\tnf");
        System.out.println(vf_adjs+"\t"+mf_adjs+"\t"+nf_adjs+"\n");

        System.out.println("VADJs");
        System.out.println("vf\t\tmf\t\tnf");
        System.out.println(vf_vadjs+"\t"+mf_vadjs+"\t"+nf_vadjs);
    }

    public void fieldCounts(File directory, Map<String, Boolean> adjDict) throws IOException {
        for (File dir : directory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getFieldCounts(file, adjDict);
            }
        }
    }

    private void getFieldCounts(File conllFile, Map<String, Boolean> adjDict) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                        new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or("_").equals("PP") &&
                            (sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJA") ||
                                    sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJD"))) {

                        boolean isVerbDerived = adjDict.get(sent.get(token.getHead().or(-1)).getForm().or("_").toLowerCase());
/*
                        // Get sentence with adj and pp
                        StringBuilder sb = new StringBuilder();
                        for (Token t : sent) {
                            sb.append(t.getForm().or("_")+" ");
                        }
                        System.out.println(token.getForm().or("_")+" "+sent.get(token.getHead().or(-1)).getForm().or("_")+"\n"+sb.toString());
*/
                        String features = token.getFeatures().or("_");
                        Matcher mTF = P_TF.matcher(features);
                        String fieldName = "";
                        if (mTF.find()) {
                            fieldName = mTF.group(0).substring(3);
                        }

                        if (fieldName.equals("VF")) {
                            if (isVerbDerived) {
                                vf_vadjs++;
                            } else {
                                vf_adjs++;
                            }
                        } else if (fieldName.equals("MF")) {
                            if (isVerbDerived) {
                                mf_vadjs++;
                            } else {
                                mf_adjs++;
                            }
                        } else if (fieldName.equals("NF")) {
                            if (isVerbDerived) {
                                nf_vadjs++;
                            } else {
                                nf_adjs++;
                            }
                        } else
                            continue;
                    }
                }
            }
        }
    }
}