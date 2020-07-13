package trial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by patricia on 26/07/17.
 */
public class Node {

    private String word;
    private String tag;
    private String depRel;
    private int head; //idx of head
    private List<Node> dependents;

    public Node(String word, String tag, String depRel, int head) {
        this.word = word;
        this.tag = tag;
        this.depRel = depRel;
        this.head = head;
        this.dependents = new ArrayList();
    }

    public void addDependent(Node dependent) {
        dependents.add(dependent);
    }

    public static void addDependents(List<Node> dependents) {
        dependents.addAll(dependents);
    }

    public Node findDependentPhrase(String phrase) {
        for (Node dep : dependents) {
            if (dep.getTag().equals(phrase) || dep.getTag().startsWith(phrase)) {
                return dep;
            }
            else
                findDependentPhrase(dep.getDependents(), phrase);
        }
        return null;
    }

    public Node findDependentPhrase(List<Node> n, String phrase) {
        for (Node dep : n) {
            if (dep.getTag().equals(phrase) || dep.getTag().startsWith(phrase)) {
                return dep;
            }
            else
                findDependentPhrase(dep.getDependents(), phrase);
        }
        return null;
    }

    public String getWord() {
        return word;
    }

    public String getTag() {
        return tag;
    }

    public String getDepRel() {
        return depRel;
    }

    public int getHead() {
        return head;
    }

    public  List<Node> getDependents() {
        return dependents;
    }
}
