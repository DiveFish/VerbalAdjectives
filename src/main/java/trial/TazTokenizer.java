package trial;

import current.Layer;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class TazTokenizer {

    private static Set<String> tokens;
    private static Layer layer;

    public TazTokenizer() throws IOException {
        tokens = new HashSet();
        layer = Layer.TOKEN;
    }

    public static void main(String[] args) throws IOException {
        /*
        if(args.length != 2) {
            System.out.println("Usage: fileDirectory, outputDirectory");
        }
        File fileDirectory = new File(args[0]);
        ppDistrs(fileDirectory);
        saveToFile(args[1], tokens);
        */

        TazTokenizer tok = new TazTokenizer();
        File fileDirectory = new File("/data/treebanks/taz/r2/");
        //fileDirectory = new File("/home/patricia/Dokumente/A3Project/taz/");
        tok.processFiles(fileDirectory);
        //tok.saveToFile("./tokens.txt", tokens);
    }


    public void saveToFile(String fileDirectory, Set<String> tokens) throws IOException {

        FileWriter fw = new FileWriter(new File(fileDirectory));
        for (String token : tokens) {
            fw.write(token);
        }
        fw.close();
    }


    public void processFiles(File fileDirectory) throws IOException {
        for (File dir : fileDirectory.listFiles()) {
            File subDir = new File(dir.getAbsolutePath());
            for (File file : subDir.listFiles()) {
                processFile(file);
            }
        }
    }

    private void processFile(File conllFile) throws IOException {
        //System.out.println(conllFile);
        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                for (Token token : sentence.getTokens()) {
                    String value = layer == Layer.LEMMA ?
                            token.getLemma().or("_") :
                            token.getForm().or("_");
                    if (token.getPosTag().or("_").equals("ADJA") || token.getPosTag().or("_").equals("ADJD")) {
                        tokens.add(value);
                    }
                    if (//token.getLemma().or("_").contains("#") &&
                            token.getForm().or("_").contains("ge") && (
                            token.getForm().or("_").endsWith("tes") ||
                            token.getForm().or("_").endsWith("ter")) &&
                            !(token.getPosTag().or("_").startsWith("VV") ||
                            token.getPosTag().or("_").startsWith("PT")) &&
                            !token.getPosTag().or("_").startsWith("NN")) {
                            //token.getPosTag().or("_").contains("ADJ")) {
                        System.out.println(token.getForm().or("_")+" - "+
                        token.getLemma().or("_")+" - "+
                        token.getPosTag().or("_"));
                    }
                }
            }
        }
    }
}
