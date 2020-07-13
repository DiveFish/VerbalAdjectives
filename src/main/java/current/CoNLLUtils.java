package current;

import com.google.common.base.Optional;
import eu.danieldk.nlp.conllx.CONLLToken;
import eu.danieldk.nlp.conllx.Sentence;
import eu.danieldk.nlp.conllx.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Calls to format and arrange CoNLL data.
 *
 * Created by patricia on 10/08/17.
 */
public class CoNLLUtils {

    public static List<Token> lowerCaseTokens(List<Token> sent, boolean removeMetaLemmas) {
        List<Token> s = new ArrayList();
        for (Token token : sent) {
            String form = token.getForm().or("_").toLowerCase();
            Optional<String> formToken = Optional.of(form);
            Optional<String> lemma = Optional.of(token.getLemma().or("_").toLowerCase());
            if (removeMetaLemmas) {
                String lemmaString = token.getLemma().or("_").toLowerCase();
                lemmaString = cleanMetaLemma(lemmaString);
                lemma =  Optional.of(lemmaString);
            }
            Token t = new CONLLToken(token.getID(),
                    formToken,
                    lemma,
                    token.getCoarsePOSTag(),
                    token.getPosTag(),
                    token.getFeatures(),
                    token.getHead(),
                    token.getDepRel(),
                    token.getPHead(),
                    token.getPDepRel());
            s.add(t);
        }
        return s;
    }

    private static String cleanMetaLemma(String lemma) {

        if (lemma.equals("#"))
            return lemma;

        if (lemma.equals("%") || lemma.equals("%%"))
            return lemma;

        if (lemma.contains("#")) {
            String[] completeLemma = lemma.split("#");
            if (completeLemma.length >= 2) {
                return completeLemma[completeLemma.length-2] + completeLemma[completeLemma.length-1];
            }
        }
        else if (lemma.contains("%")) {
            String[] completeLemma = lemma.split("%");
            return completeLemma[0];
        }
        return lemma;
    }

    public static List<Token> addToken(Sentence sentence, int position, String nodeName) {
        List<Token> sent = new ArrayList();
        sent.addAll(sentence.getTokens());
        sent.add(position, createToken(position, nodeName));

        return sent;
    }

    public static Token createToken(int position, String nodeName) {
        Optional<String> optStr = Optional.absent();
        Optional<Integer> optInt = Optional.absent();
        Optional<String> node = Optional.of(nodeName);

        return new CONLLToken(position, node, node, node, node, optStr, optInt, node, optInt, node);
    }

    public static String sentenceToString(List<Token> sent, boolean lemma) {
        StringBuilder sb = new StringBuilder();
        for (Token t : sent) {
            if (lemma) {
                sb.append(t.getLemma().or("_")+" ");
            }
            else {
                sb.append(t.getForm().or("_") + " ");
            }
        }
        return sb.toString();
    }
}
