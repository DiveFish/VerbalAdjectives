package trial;

import current.Layer;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;
import eu.danieldk.nlp.conllx.reader.CONLLReader;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by patricia on 10/08/17.
 */
public class CodeStorage {

    Layer layer = Layer.LEMMA;

    /**
     * Add smor analysis to taz files.
     * TODO: Method needs to be completed and tested!
     *
     * @param conllFile
     * @throws IOException
     */
    public void addSmorAnalysis(File conllFile) throws IOException {

        // unzip gz file
        // find adjectives / if dict contains token
        // add smor analysis to feature field:
        // String feats = sent.get(0).getFeatures().or("_");
        // feats += "/adjDict.get(token)

        try (CONLLReader conllReader = new CONLLReader(new BufferedReader(new InputStreamReader(new GZIPInputStream(
                new FileInputStream(conllFile)))))) {
            for (Sentence sentence = conllReader.readSentence(); sentence != null; sentence = conllReader.readSentence()) {
                for (Token token : sentence.getTokens()) {
                    String value = layer == Layer.LEMMA ?
                            token.getLemma().or("_") :
                            token.getForm().or("_");
                }
            }
        }

        BufferedWriter writer = null;
        try {
            GZIPOutputStream zip = new GZIPOutputStream(new FileOutputStream(conllFile));
            writer = new BufferedWriter(new OutputStreamWriter(zip, "UTF-8"));

            String[] data = new String[] { "this", "is", "some",
                    "data", "in", "a", "list" };

            for (String line : data) {
                writer.append(line);
                writer.newLine();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
