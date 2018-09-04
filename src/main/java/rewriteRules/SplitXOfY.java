package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;
import utils.ArrayListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Split "X of Y"
 * @author aylai2
 */
public class SplitXOfY {

    /**
     * Find "X of Y" cases and drop "X of" and "of Y" where applicable
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
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
            for (Chunk c : chunks) {
                // look for "[EN [ ] [PP of ] [ ] ]"
                if (!c.getType().equals("EN")) {
                    continue;
                }
                // second chunk in EN must be [PP of ]
                ArrayList<Chunk> children = c.getChunks();
                if (children.size() == 3 && children.get(1).getType().equals("PP") && children.get(1).toBareString().equals("of")) {
                    Chunk chunkX = children.get(0); // first NP chunk
                    Chunk chunkOfPP = children.get(1); // PP "of" chunk
                    Chunk chunkY = children.get(2); // second NP chunk
                    String headStr = c.getChunkHead();
                    // generate "X of Y" -> "X"
                    ArrayList<String> left = new ArrayList<>();
                    ArrayList<String> right = new ArrayList<>();
                    // if "body of water", do not generate rule "body of water" -> "body"
                    // don't generate "sort/kind/type of Y" -> "sort/kind/type"
                    if (!headStr.equals("body/water") && !headStr.startsWith("sort/") && !headStr.startsWith("kind/") && !headStr.startsWith("type/") && !headStr.equals("bunch/person")) {
                        Chunk newSent = new Chunk(sent.toString());
                        // left side of rule is: [EN [X_NP [PP [Y] ] where closing bracket belongs to EN chunk
                        right.add(Integer.toString(c.getFirstToken().getId()));
                        right.add(Integer.toString(chunkX.getFirstToken().getId()));
                        right.add(chunkOfPP.toString());
                        right.add(chunkY.toString());
                        right.add(Integer.toString(c.getLastToken().getId()));
                        // right side of rule is: [EN [X_NP ] where closing bracket belongs to EN chunk
                        left.add(Integer.toString(c.getFirstToken().getId()));
                        left.add(Integer.toString(chunkX.getFirstToken().getId()));
                        left.add(Integer.toString(c.getLastToken().getId()));
                        newSent.removeChunk(chunkY, true);
                        newSent.removeChunk(chunkOfPP, true);
                        // create new rewrite rule
                        if (!newSent.toBareString().equals("")) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+ofY"), origRootStr);
                            // check if we need to create rewrite rules for hyponyms
                            for (Chunk yInner : chunkY.getAllChunks()) {
                                String yInnerStr = yInner.toString();
                                if (cap.hasHyponyms(yInnerStr)) {
                                    // create new rule
                                    String rightStr = ArrayListUtils.stringListToString(right, " ");
                                    HashSet<String> hyponyms = cap.getHyponyms(yInnerStr);
                                    for (String hyponym : hyponyms) {
                                        String rightStrNew = rightStr.replace(yInnerStr, hyponym);
                                        cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+ofY"), origRootStr);
                                    }
                                }
                            }
                            newRoots.add(newSent.toString());
                            toRemoveRoots.add(rootIdx);
                        }
                    }

                    // generate "X of Y" -> "Y"
                    Chunk newSent = new Chunk(sent.toString());
                    left = new ArrayList<>();
                    right = new ArrayList<>();
                    // left side of rule is: [EN [X] [PP [Y_NP ] where closing bracket belongs to EN chunk
                    right.add(Integer.toString(c.getFirstToken().getId()));
                    right.add(chunkX.toString());
                    right.add(chunkOfPP.toString());
                    right.add(Integer.toString(chunkY.getFirstToken().getId()));
                    right.add(Integer.toString(c.getLastToken().getId()));
                    // right side of rule is: [EN [Y_NP ] where closing bracket belongs to EN chunk
                    left.add(Integer.toString(c.getFirstToken().getId()));
                    left.add(Integer.toString(chunkY.getFirstToken().getId()));
                    left.add(Integer.toString(c.getLastToken().getId()));
                    newSent.removeChunk(chunkOfPP, true);
                    newSent.removeChunk(chunkX, true);
                    // create new rewrite rule
                    if (!newSent.toString().equals("")) {
                        cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+Xof"), origRootStr);
                        // check if we need to create rewrite rules for hyponyms
                        for (Chunk xInner : chunkX.getAllChunks()) {
                            String xInnerStr = xInner.toString();
                            if (cap.hasHyponyms(xInnerStr)) {
                                // create new rule
                                String rightStr = ArrayListUtils.stringListToString(right, " ");
                                HashSet<String> hyponyms = cap.getHyponyms(xInnerStr);
                                for (String hyponym : hyponyms) {
                                    String rightStrNew = rightStr.replace(xInnerStr, hyponym);
                                    cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+Xof"), origRootStr);
                                }
                            }
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

