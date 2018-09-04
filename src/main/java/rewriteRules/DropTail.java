package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;
import structure.MyToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Drop trailing "and" and "while" (fixing errors of previous scripts)
 * @author aylai2
 */
public class DropTail {

    /**
     * Drop trailing "and" and "while" that were left at the end of the caption by mistake
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(RewriteCaption cap) {
        // list of words we want to drop from the end of the caption
        HashSet<String> drop = new HashSet<>();
        drop.add("and");
        drop.add("while");
        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            if (sent.getTokenLength() < 2) { // sentence must be at least 2 words long
                return cap;
            }
            Chunk c = sent.getLastChunk();
            MyToken t = sent.getLastToken();
            if (c != null && drop.contains(c.toBareString())) {
                // drop this chunk
                ArrayList<String> left = new ArrayList<>();
                ArrayList<String> right = new ArrayList<>();
                left.add(Integer.toString(c.getPrevToken().getId()));
                right.add(Integer.toString(c.getPrevToken().getId()));
                right.add(c.toString());
                left.add("E");
                right.add("E");
                sent.removeChunk(c, true); // delete chunk
                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+DROP/" + t.getStr()), origRootStr);
            } else if (drop.contains(t.getStr())) {
                // drop this token
                ArrayList<String> left = new ArrayList<>();
                ArrayList<String> right = new ArrayList<>();
                left.add(Integer.toString(t.getPrevToken().getId()));
                right.add(Integer.toString(t.getPrevToken().getId()));
                right.add(t.toString());
                left.add("E");
                right.add("E");
                sent.removeToken(t); // delete token
                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+DROP/" + t.getStr()), origRootStr);
            }
        }
        return cap;
    }
}
