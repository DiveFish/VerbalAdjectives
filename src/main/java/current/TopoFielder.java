package current;

import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Created by patricia on 23/08/17.
 */
public class TopoFielder {

    private static final Pattern P_TF = Pattern.compile("tf:[a-zA-Z]{1,2}");

    private static int vf_adjs_attr = 0;
    private static int mf_adjs_attr = 0;
    private static int nf_adjs_attr = 0;

    private static int vf_adjs_pred = 0;
    private static int mf_adjs_pred = 0;
    private static int nf_adjs_pred= 0;

    public static void main(String[] args) throws IOException {
        String serverRoot = "/home/patricia/adjectives/";
        String homeRoot = "/home/patricia/Dokumente/A3Project/";

        TopoFielder tf = new TopoFielder();
        File dir = new File("/data/treebanks/taz/r2/");
        //dir = new File(homeRoot+"taz/");
        tf.fieldCounts(dir);

        System.out.println("Attributive adjectives with PP in...");
        System.out.println("vf\t\tmf\t\tnf");
        System.out.println(vf_adjs_attr+"\t"+mf_adjs_attr+"\t"+nf_adjs_attr+"\n");

        System.out.println("Predicative adjectives with PP in...");
        System.out.println("vf\t\tmf\t\tnf");
        System.out.println(vf_adjs_pred+"\t"+mf_adjs_pred+"\t"+nf_adjs_pred);
    }

    public void fieldCounts(File directory) throws IOException {
        for (File dir : directory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                getFieldCounts(file);
            }
        }
    }

    private void getFieldCounts(File conllFile) throws IOException {
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {

                List<Token> sent = CoNLLUtils.addToken(sentence, 0, "ROOT");

                for (Token token : sent) {
                    if (token.getDepRel().or("_").equals("PP") &&
                            (sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJA") ||
                                    sent.get(token.getHead().or(-1)).getPosTag().or("_").equals("ADJD"))) {
                        Token adjective = sent.get(token.getHead().or(-1));
                        boolean attr = adjective.getPosTag().or("_").equals("ADJA");
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
                            if (attr) {
                                vf_adjs_attr++;
                            } else {
                                vf_adjs_pred++;
                            }
                        } else if (fieldName.equals("MF")) {
                            if (attr) {
                                mf_adjs_attr++;
                            } else {
                                mf_adjs_pred++;
                            }
                        } else if (fieldName.equals("NF")) {
                            if (attr) {
                                nf_adjs_attr++;
                            } else {
                                nf_adjs_pred++;
                            }
                        } else
                            continue;
                    }
                }
            }
        }
    }
}