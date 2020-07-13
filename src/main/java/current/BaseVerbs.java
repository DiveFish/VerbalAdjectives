package current;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class which stores the base verbs of verb-derived adjectives.
 *
 * Created by patricia on 2/09/17.
 */
public class BaseVerbs {

    private static Map<String, String> baseverbs;

    public BaseVerbs() throws IOException {
        baseverbs = new TreeMap();
        String file = "/home/patricia/Dokumente/A3Project/analyses/10-17/taz/smor_baseverbs.txt";
        //String file = "adjectives-10-17/baseverbs.txt";
        fillBaseVerbs(file);
    }

    private static void fillBaseVerbs(String file) throws IOException {
        baseverbs = IOUtils.stringMapFromFile(file);
    }

    public static Map<String, String> getBaseverbDict() {
        return baseverbs;
    }

    public static void setBaseverbDict(Map<String, String> baseverbs) {
        BaseVerbs.baseverbs = baseverbs;
    }

    public static Set<String> getBaseverbs() {
        return new TreeSet(baseverbs.values());
    }
}
