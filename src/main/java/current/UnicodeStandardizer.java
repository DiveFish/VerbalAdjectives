package current;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by patricia on 25/11/17.
 */
public class UnicodeStandardizer {

    private static volatile boolean initialized;
    private static Map<Integer, String> unicodes;
    private static Map<Integer, String> unicodesWithoutNonAscii;

    public static String replaceChars(String token) {
        if (!initialized) {
            fillUnicodeMaps();
            initialized = true;
        }
        return replaceChars(token, unicodes);
    }

    public static String replaceCharsWithoutUmlauts(String token) {
        if (!initialized) {
            fillUnicodeMaps();
            initialized = true;
        }
        return replaceChars(token, unicodesWithoutNonAscii);
    }

    private static String replaceChars(String token, Map<Integer, String> map) {
        if (token.equals("")) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            String replacement = map.get(token.codePointAt(i));
            if (replacement == null) {
                sb.append(token.charAt(i));
            } else {
                sb.append(replacement);
            }
        }
        return sb.toString();
    }

    private static void replaceChars(int start, Map<Integer, String> unicodes, String... replacements) {
        for (String replacement : replacements) {
            if (replacement != null) {
                unicodes.put(start, replacement);
            }
            start++;
        }
    }

    private static void fillUnicodeMaps() {
        unicodes = new TreeMap<>();
        unicodesWithoutNonAscii = new TreeMap<>();

        replaceChars(0x00C0,
                unicodes,
                "A", "A", "A", "A", "AE", "A", "AE", "C", "E", "E", "E", "E", "I", "I", "I", "I");
        replaceChars(0x00D0,
                unicodes,
                "D", "N", "O", "O", "O", "O", "OE", null, null, "U", "U", "U", "UE", "Y", null, "ss");
        replaceChars(0x00E0,
                unicodes,
                "a", "a", "a", "a", "ae", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i");
        replaceChars(0x00F0,
                unicodes,
                null, "n", "o", "o", "o", "o", "oe", null, null, "u", "u", "u", "ue", "y", null, "y");
        // Latin Extended-A 0100-0170
        replaceChars(0x0130,
                unicodes,
                null, null, "IJ", "ij", "J", "j", "K", "k", "k", "L", "l", "L", "l", "L", "l", "L");
        // Aplphabetic Presentation Forms FB00-0FB4
        replaceChars(0xFB00,
                unicodes,
                "ff", "fi", "fl", "ffi", "ffl", "ft", "st", null, null, null, null, null, null, null, null, null);

        replaceChars(0x00C0,
                unicodesWithoutNonAscii,
                "A", "A", "A", "A", "AE", "A", "AE", "C", "E", "E", "E", "E", "I", "I", "I", "I");
        replaceChars(0x00D0,
                unicodesWithoutNonAscii,
                "D", "N", "O", "O", "O", "O", "OE", null, null, "U", "U", "U", "UE", "Y", null, "ß");
        replaceChars(0x00E0,
                unicodesWithoutNonAscii,
                "a", "a", "a", "a", "ä", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i");
        replaceChars(0x00F0,
                unicodesWithoutNonAscii,
                null, "n", "o", "o", "o", "o", "ö", null, null, "u", "u", "u", "ü", "y", null, "y");
        // Latin Extended-A 0100-0170
        replaceChars(0x0130,
                unicodesWithoutNonAscii,
                null, null, "IJ", "ij", "J", "j", "K", "k", "k", "L", "l", "L", "l", "L", "l", "L");
        // Aplphabetic Presentation Forms FB00-0FB4
        replaceChars(0xFB00,
                unicodesWithoutNonAscii,
                "ff", "fi", "fl", "ffi", "ffl", "ft", "st", null, null, null, null, null, null, null, null, null);
    }
}
