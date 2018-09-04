package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by alai on 12/5/15.
 *
 * Extract any internal NPs from the caption and create a new root caption for each NP
 */
public class ExtractNP {

    public static RewriteCaption applyRule(RewriteCaption cap) {
        HashSet<Integer> toRemoveRoots = new HashSet<>();
        ArrayList<String> newRoots = new ArrayList<>();
        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            ArrayList<Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }
            if (chunks.size() == 1) {
                continue;
            }
            for (int i = 0; i < chunks.size(); i++) {
                Chunk c = chunks.get(i);
                if (c.getType().equals("EN")) {
                    ArrayList<String> left = new ArrayList<>();
                    ArrayList<String> right = new ArrayList<>();
                    left.add("B");
                    left.add(Integer.toString(c.getFirstToken().getId()));
                    left.add("E");
                    // remove all other chunks
                    Chunk newSent = new Chunk(sent.toString());
                    right.add("B");
                    ArrayList<Chunk> toRemove = new ArrayList<>();
                    for (int j = 0; j < chunks.size(); j++) {
                        if (j == i) {
                            right.add(Integer.toString(c.getFirstToken().getId()));
                            continue;
                        }
                        right.add(chunks.get(j).toString());
                        toRemove.add(chunks.get(j));
                    }
                    right.add("E");
                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    if (!newSent.toBareString().equals("")) {
                        if (right.size() - left.size() <= 5) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+SENT"), origRootStr);
                        }
                        newRoots.add(newSent.toString());
                        toRemoveRoots.add(rootIdx);
                    }
                }
            }
        }
        ArrayList<Integer> removeRootsList = new ArrayList(toRemoveRoots);
        Collections.sort(removeRootsList);
        for (int listIdx = removeRootsList.size() - 1; listIdx >= 0; listIdx--) {
            int idxRemove = removeRootsList.get(listIdx);
            cap.removeRoot(idxRemove);
        }
        for (String root : newRoots) {
            cap.addRoot(root);
        }
        return cap;
    }
}
