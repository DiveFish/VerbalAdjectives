package current;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Class provides utilities for reading from and writing to files.
 *
 * Created by patricia on 2/09/17.
 */
public class IOUtils {

    public static void main(String[] args) throws IOException {
        String homeRoot = "/home/patricia/Dokumente/A3Project/analyses/03-18/";
        splitFile(homeRoot, "/home/patricia/Dokumente/A3Project/analyses/03-18/all-adjs-no-q.txt.sorted.txt", 10);
    }

    public static BufferedReader readFile(String fileName) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(fileName)));
    }

    /**
     * USAGE:
         String taz = "/data/treebanks/taz/r2/";
         FrequencyCounter fc = new FrequencyCounter();
         Method m = fc.getClass().getMethod("getFreqs", File.class);
         IOUtils.processFiles(m, taz);
     * @param method
     * @param fileDir
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void processFiles(Method method, String fileDir) throws InvocationTargetException, IllegalAccessException, IOException {
        File dir = new File(fileDir);
        for (File subDir : dir.listFiles()) {
            for (File f : subDir.listFiles()) {
                method.invoke(new FrequencyCounter(), f);
            }
        }
    }

    public static void setToFile(Set<String> set, String outputFile) throws IOException {
        OutputStream outputStream = new FileOutputStream(outputFile);
        Writer outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");

        for (String s : set) {
            outputStreamWriter.write(s+"\n");
        }
        outputStreamWriter.close();
    }

    public static void listToFile(List<String> list, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (String s : list) {
            fw.write(s+"\n");
        }
        fw.close();
    }

    public static void mapListToFile(List<Map<String, Integer>> list, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (Map<String, Integer> entry : list) {
            fw.write(entry+"\n");
        }
        fw.close();
    }

    public static void intMapToFile(Map<String, Integer> map, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (Map.Entry entry : map.entrySet()) {
            fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        fw.close();
    }

    public static void stringMapToFile(Map<String, String> map, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (Map.Entry entry : map.entrySet()) {
            fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        fw.close();
    }

    public static void booleanMapToFile(Map<String, Boolean> map, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (Map.Entry entry : map.entrySet()) {
            fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        fw.close();
    }

    public static void valuesToFile(Map<String, String> map, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        Set<String> values = new TreeSet();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            values.add(entry.getValue());
        }
        for (String value : values) {
            fw.write(value+"\n");
        }
        fw.close();
    }
    public static void keysToFile(Map<String, Integer> map, String output) throws IOException {
        FileWriter fw = new FileWriter(output);
        for (Map.Entry entry : map.entrySet()) {
            fw.write(entry.getKey()+"\n");
        }
        fw.close();
    }

    public static Set<String> setFromFile(String input) throws IOException {
        Set<String> set = new TreeSet();
        BufferedReader br = readFile(input);
        String line = "";
        while((line = br.readLine())!=null) {
            set.add(line.trim());
        }
        return set;
    }

    public static List<String> listFromFile(String input) throws IOException {
        List<String> list = new ArrayList();
        BufferedReader br = readFile(input);
        String line = "";
        int lines = 0;
        while((line = br.readLine())!=null) {
            list.add(line.trim());
            System.out.println(lines);
            lines++;
        }
        return list;
    }

    public static Map<String, Integer> intMapFromFile(String input) throws IOException {
        Map<String, Integer> map = new TreeMap();
        BufferedReader br = readFile(input);
        String line = "";
        while((line = br.readLine())!=null) {
            String entry = line.split("\t")[0];
            Integer value = Integer.parseInt(line.split("\t")[1]);
            map.putIfAbsent(entry, value);
        }
        return map;
    }

    public static Map<String, Boolean> booleanMapFromFile(String input) throws IOException {
        Map<String, Boolean> map = new TreeMap();
        BufferedReader br = readFile(input);
        String line = "";
        while((line = br.readLine())!=null) {
            String entry = line.split("\t")[0];
            Boolean value = Boolean.parseBoolean(line.split("\t")[1]);
            map.putIfAbsent(entry, value);
        }
        return map;
    }

    public static Map<String, String> stringMapFromFile(String input) throws IOException {
        Map<String, String> map = new TreeMap();
        BufferedReader br = readFile(input);
        String line;
        while((line = br.readLine())!=null) {
            String[] splitLine = line.split("\t");
            String entry = splitLine[0];
            String value = splitLine[1];
            map.putIfAbsent(entry, value);
        }
        return map;
    }

    public static List<String> keysFromFile(String input) throws IOException {
        List<String> list = new ArrayList();
        BufferedReader br = readFile(input);
        String line = "";
        while((line = br.readLine())!=null) {
            String entry = line.split("\t")[0];
            list.add(entry);
        }
        return list;
    }

    /**
     * Sorts the lower-case version of the input and prints it to the output file.
     *
     * @param input The file with the input to be sorted and converted to lowercase
     * @param output The lower-cased and sorted input
     * @throws IOException
     */
    private static void lowercaseSortTokens(File input, File output) throws IOException {
        Set<String> tokens = new TreeSet();
        try (BufferedReader br = new BufferedReader(new FileReader(input))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!tokens.contains(line)) {
                    tokens.add(line);
                }
            }
            FileWriter fw = new FileWriter(output);
            for (String token : tokens) {
                fw.write(token+"\n");
            }
            fw.close();
        }
    }

    /**
     * TODO: Debug! Not all lines from original file are piped into new files
     *
     * @param fileName
     * @param numOfSplits
     * @throws IOException
     */
    public static void splitFile(String homeRoot, String fileName, int numOfSplits) throws IOException {

        List<Integer> splitters = new ArrayList();
        for (int i = 1; i <= numOfSplits; i++) {
            splitters.add(i*1000000);
        }

        List<String> list = new ArrayList();
        BufferedReader br = readFile(fileName);
        String line = "";

        int numOfLines = 0;
        for (Integer split : splitters) {
            System.out.println(split);
            while((line = br.readLine()) != null) {
                list.add(line.trim());
                numOfLines++;
                if (numOfLines == split) {
                    int idx = splitters.indexOf(split)+1;
                    IOUtils.listToFile(list, homeRoot+"all-adjs"+idx+".txt");
                    list.clear();
                    break;
                }
            }
        }
    }

}
