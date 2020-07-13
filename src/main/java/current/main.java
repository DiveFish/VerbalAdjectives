package current;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.lucene.analysis.compound.DictionaryCompoundWordTokenFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

/**
 * Created by patricia on 20/10/17.
 *
 * taz dictionary: 834459
 * wikipedia dictionary: 1395095
 * complete dictionary: 1910799
 */
public class main {

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {
        //String homeRoot = "/home/patricia/adjectives-11-17/";
        //homeRoot = "/home/patricia/Dokumente/A3Project/analyses/11-17/";
        //String wiki = "/data/treebanks/wikipedia/dewiki-20170101";
        //compoundSplitter(homeRoot);
        //Locale.setDefault(Locale.GERMANY);
        //System.setProperty("file.encoding","UTF-8");

        //AdjectiveDictionary.getAdjsFromDir("/data/treebanks/decow14ax",
        //        "/home/patricia/decow_adjectives.txt");
        //AdjectiveDictionary.getAdjsFromDir("/home/patricia/Dokumente/A3Project/corpora/decow",
        //        "/home/patricia/Dokumente/A3Project/analyses/11-17/adjectives_decow14ax.txt");

        /*
        Set<String> adjs = new HashSet();
        AdjectiveDictionary.getAdjsFromFile(new File("/data/treebanks/decow14ax/part0/decow14ax01-part0.conll.gz"), adjs);
        IOUtils.setToFile(adjs, "/home/patricia/decow_sample.txt");
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        for (String adj : adjs) {
            out.println(adj);
        }
        */
        //IOUtils.setToFile(adjs, "/home/patricia/decow_adjsSample.txt");
        //IOUtils.splitFile("/home/patricia/decow_adjectives.txt", 13);

    }

    public static void compoundSplitter(String homeRoot) throws IOException {

        Map<String, String> completeDict = new TreeMap();
        BufferedReader br = IOUtils.readFile( homeRoot+"taz/adj_dictionary_taz.txt");
        String line = "";
        while((line = br.readLine())!=null) {
            String[] entry = line.split("\t");
            completeDict.putIfAbsent(entry[0], entry[1]+"\t"+entry[2]);
        }

        br = IOUtils.readFile(  homeRoot+"wikipedia/adj_dictionary_wikipedia.txt");
        while((line = br.readLine())!=null) {
            String[] entry = line.split("\t");
            completeDict.putIfAbsent(entry[0], entry[1]+"\t"+entry[2]);
        }

        IOUtils.stringMapToFile(completeDict, homeRoot+"completeDict.txt");
/*
        String[] dict = { "Bil", "Dörr", "Motor", "Tak", "Borr", "Slag", "Hammar",
                "Pelar", "Glas", "Ögon", "Fodral", "Bas", "Fiol", "Makare", "Gesäll",
                "Sko", "Vind", "Rute", "Torkare", "Blad" };
        /*
        DictionaryCompoundWordTokenFilter tf = new DictionaryCompoundWordTokenFilter(new WhitespaceTokenizer(new StringReader("Bildörr Bilmotor Biltak Slagborr Hammarborr Pelarborr Glasögonfodral Basfiolsfodral Basfiolsfodralmakaregesäll Skomakare Vindrutetorkare Vindrutetorkarblad abba")),
                dict);
        Token t;
        while ((t=tf.next())!=null) {
            System.out.println(t);
        }
        */
    }
}
