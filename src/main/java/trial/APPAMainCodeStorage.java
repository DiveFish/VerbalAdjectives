package trial;

/**
 * Created by patricia on 10/08/17.
 */
public class APPAMainCodeStorage {

    public static void main(String[] args) {

        // Get distribution of prepositions with adjective head (preceding or following their head)
        /*

        System.out.println("Attributive adjective distribution");
        System.out.printf("# of PPs with verb-derived ADJ, preceding the ADJ: %d\n", ppsBeforeVAdjAttr.size());
        System.out.printf("# of PPs with non-verbal ADJ, preceding the ADJ: %d\n", ppsBeforeAdjAttr.size());
        System.out.printf("# of PPs with verb-derived ADJ, following the ADJ: %d\n", ppsAfterVAdjAttr.size());
        System.out.printf("# of PPs with non-verbal ADJ, following the ADJ: %d\n", ppsAfterAdjAttr.size());

        System.out.println("Predicative adjective distribution");
        System.out.printf("# of PPs with verb-derived ADJ, preceding the ADJ: %d\n", ppsBeforeVAdjPred.size());
        System.out.printf("# of PPs with non-verbal ADJ, preceding the ADJ: %d\n", ppsBeforeAdjPred.size());
        System.out.printf("# of PPs with verb-derived ADJ, following the ADJ: %d\n", ppsAfterVAdjPred.size());
        System.out.printf("# of PPs with non-verbal ADJ, following the ADJ: %d\n", ppsAfterAdjPred.size());
        */

        // Split adjective frequency table by verb-derived and non-verbal adjectives
        /*
        File vadjsByFreqs = new File(homeRoot+"data/August2017/vppadjsByFreqs.txt");
        File nonvadjsByFreqs = new File(homeRoot+"data/August2017/nonVppadjsByFreqs.txt");
        File adjsByFreqs = new File(homeRoot+"data/August2017/ppAdjsByFreqs.txt");
        ta.splitAdjFreqsToFiles(vadjsByFreqs, nonvadjsByFreqs, adjsByFreqs);
         */

        // Get only adjectives which are modified by a PP
        /*
        adjFreqs(tazDir);
        Map<String, Integer> adjByFreqs = sortByValue(adjFreqs);
        FileWriter fw = new FileWriter(serverRoot+"ppAdjsByFreqs.txt");
        //FileWriter fw = new FileWriter(homeRoot+"data/August2017/ppAdjsByFreqs.txt");
        for (Map.Entry<String, Integer> entry : adjByFreqs.entrySet()) {
            fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
        }
        fw.close();
         */

        // Get sample sentences for pp preceding/following (non-)verb-derived adjective head
        /*
        System.out.println("\n\nPreposition precedes verb-derived adjective");
        for (int i = 0; i < 30; i++) {
            System.out.print("\n"+i+1+". ");
            for (Token t : ppsBeforeVAdj.get(i)) {
                System.out.print(t.getForm().or("_")+" ");
            }
            System.out.println();
        }
        System.out.println("\n\nPreposition follows verb-derived adjective");
        for (int i = 0; i < 30; i++) {
            System.out.print("\n"+i+1+". ");
            for (Token t : ppsAfterVAdj.get(i)) {
                System.out.print(t.getForm().or("_")+" ");
            }
            System.out.println();
        }
        System.out.println("\n\nPreposition preceded non-verb-derived adjective");
        for (int i = 0; i < 30; i++) {
            System.out.print("\n"+i+1+". ");
            for (Token t : ppsBeforeAdj.get(i)) {
                System.out.print(t.getForm().or("_")+" ");
            }
            System.out.println();
        }
        System.out.println("\n\nPreposition follows non-verb-derived adjective");
        for (int i = 0; i < 30; i++) {
            System.out.print("\n"+i+1+". ");
            for (Token t : ppsAfterAdj.get(i)) {
                System.out.print(t.getForm().or("_")+" ");
            }
            System.out.println();
        }
        */

        // Print adjective frequencies to file
        /*
        adjFreqs(tazDir);
        Map<String, Integer> adjByFreqs = sortByValue(adjFreqs);
        FileWriter fw = new FileWriter(serverRoot+"vadjsByFreqs.txt");
        //fw = new FileWriter(homeRoot+"data/adjsByFreqs.txt");
        for (Map.Entry<String, Integer> entry : adjByFreqs.entrySet()) {
            if (adjDict.get(entry.getKey())) {
                fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
        }
        fw.close();
        */

        // Get (non-)verb-derived adjectives and their frequencies separately
        /*
        File vadjsByFreqs = new File(homeRoot+"data/August2017/vadjsByFreqs.txt");
        File nonvadjsByFreqs = new File(homeRoot+"data/August2017/nonVadjsByFreqs.txt");
        File adjsByFreqs = new File(homeRoot+"data/August2017/adjsByFreqs.txt");
        sa.printAllAdjsToFiles(vadjsByFreqs, nonvadjsByFreqs, adjsByFreqs);
         */

        // Lower-case tokens from a list of tokens
        /*
        sa.lowercaseTokens(new File("/home/patricia/Dokumente/A3Project/data/uniqAdjTokens_mixedCasing.txt"),
                new File("/home/patricia/Dokumente/A3Project/data/uniqAdjTokens.txt"));
         */

        //Find tokens which have been missed in first run of smor analysis
        /*
        File uniqTokens = new File("/home/patricia/adjectives/uniqAdjs.txt");
        uniqTokens = new File("/home/patricia/Dokumente/A3Project/data/uniqAdjs.txt");
         */

        // Create adj frequency file
        /*
        adjFreqs(tazDir);
        Map<String, Integer> adjByFreqs = sortByValue(adjFreqs);
        FileWriter fw = new FileWriter("/home/patricia/adjectives/adjsByFreqs.txt");
        //fw = new FileWriter("/home/patricia/Dokumente/A3Project/data/adjsByFreqs.txt");
        for (Map.Entry<String, Integer> entry : adjByFreqs.entrySet()) {
            fw.write(entry.getKey()+"\t"+entry.getValue()+"\n");
        }
        fw.close();
         */


        // Get adjective-preposition frequencies
        /*
        File adjFreqs = new File(serverRoot+"adjsByFreqs.txt");
        //adjFreqs = new File(homeRoot+"data/August2017/adjsByFreqs.txt");
        fillAdjFreqs(adjFreqs);
        //adjPrepFreqs(myTazDir);
        adjPrepFreqs(tazDir);

        File adjPrepFreqFile = new File(serverRoot+"adjPrepFreqs.txt");
        //adjPrepFreqFile = new File(homeRoot+"data/August2017/adjPrepFreqs.txt");
        FileWriter fw = new FileWriter(adjPrepFreqFile);
        for (int i = 0; i < adjsByFreq.size(); i++) {
            StringBuilder sb = new StringBuilder();
            Map<String, Integer> m = adjPrepFreqs.get(i);
            sb.append(adjsByFreq.get(i)+": {");
            for (Map.Entry<String, Integer> e : m.entrySet()) {
                sb.append(e.getKey()+": "+e.getValue()+"; ");
            }
            sb.deleteCharAt(sb.length()-2);
            sb.deleteCharAt(sb.length()-1);
            sb.append("}\n");
            fw.write(sb.toString());
        }
        fw.close();
        */


        // Get prepositions for verb-derived and non-verb-derived adjective heads
        /*
        Set<String> vPrepositions = new TreeSet();
        //System.out.println("\nAdjective -- Prepositions\n");
        for (Map.Entry<String, Set<String>> entrySet : verbaladjPreps.entrySet()) {
            //System.out.println(entrySet);
            vPrepositions.addAll(entrySet.getValue());
        }

        System.out.println("\n\nPrepositions going with verb-derived adjectives:\n");
        System.out.println(vPrepositions);

        Set<String> prepositions = new TreeSet();
        for (Map.Entry<String, Set<String>> entrySet : adjPreps.entrySet()) {
            //System.out.println(entrySet);
            prepositions.addAll(entrySet.getValue());
        }

        System.out.println("\nPrepositions going with NON-verb-derived adjectives:\n");
        System.out.println(prepositions);

        Set vPrepositionsCopy = new TreeSet();
        vPrepositionsCopy.addAll(vPrepositions);
        vPrepositionsCopy.removeAll(prepositions);
        System.out.println("\n\nPrepositions exclusively going with verb-derived adjectives");
        System.out.println(vPrepositionsCopy);

        Set prepositionsCopy = new TreeSet();
        prepositionsCopy.addAll(prepositions);
        prepositionsCopy.removeAll(vPrepositions);
        System.out.println("\nPrepositions exclusively going with NON-verb-derived adjectives");
        System.out.println(prepositionsCopy);

        */
    }
}
