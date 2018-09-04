package rewriteRules;

import structure.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Split "X or Y"
 * @author aylai2
 */
public class SplitXOrY {

    /**
     * Identify "X or Y" instances and drop "X or" and "or Y" where applicable
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(RewriteCaption cap) {
        HashSet<Integer> toRemoveRoots = new HashSet<>();
        ArrayList<String> newRoots = new ArrayList<>();
        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            ArrayList<structure.Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }
            for (structure.Chunk chunkX : chunks) {
                // look for "[EN ] [CC or ] [EN ]"
                if (!chunkX.getType().equals("EN") || chunkX.getNextChunk() == null) {
                    continue;
                }
                structure.Chunk or = chunkX.getNextChunk();
                if (!or.getType().equals("CC") || !or.toBareString().equals("or")) {
                    continue;
                }
                structure.Chunk chunkY = or.getNextChunk();
                if (!chunkY.getType().equals("EN")) {
                    continue;
                }
                // rule "X or Y" -> "X"
                // left = [EN1 ... ] [CC or ] [EN2 ...]
                ArrayList<String> left = new ArrayList<>();
                left.add(Integer.toString(chunkX.getLastToken().getPrevToken().getId()));
                left.add(Integer.toString(chunkY.getLastToken().getId()));
                ArrayList<String> right = new ArrayList<>();
                structure.Chunk newSent = new structure.Chunk(sent.toString());
                right.add(Integer.toString(chunkX.getLastToken().getPrevToken().getId()));
                right.add(chunkX.getLastToken().toString());
                right.add(or.toString());
                right.add(chunkY.getFirstToken().toString());
                right.addAll(chunkY.toStringListInner());
                right.add(Integer.toString(chunkY.getLastToken().getId()));
                newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(chunkX.getLastToken())));
                newSent.removeChunk(or, true);
                for (MyToken t : chunkY.getInnerTokens()) {
                    newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(t)));
                }
                newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(chunkY.getFirstToken())));
                // create new rewrite rule
                if (!newSent.toBareString().equals("")) {
                    newSent = new structure.Chunk(newSent.toString());
                    cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+orY"), origRootStr);
                    newRoots.add(newSent.toString());
                    toRemoveRoots.add(rootIdx);
                }

                // rule "X or Y" -> "Y"
                // left = [EN1 ... ] [CC or ] [EN2 ...]
                // right = EN1 opening bracket + EN2 internal tokens + EN2 closing bracket
                right = new ArrayList<>();
                newSent = new structure.Chunk(sent.toString());
                left = new ArrayList<>();
                left.add(Integer.toString(chunkX.getFirstToken().getId()));
                left.add(Integer.toString(chunkY.getFirstToken().getNextToken().getId()));

                right.add(Integer.toString(chunkX.getFirstToken().getId()));
                right.addAll(chunkX.toStringListInner());
                right.add(chunkX.getLastToken().toString());
                right.add(or.toString());
                right.add(chunkY.getFirstToken().toString());
                right.add(Integer.toString(chunkY.getFirstToken().getNextToken().getId()));
                newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(chunkY.getFirstToken())));
                for (MyToken t : chunkX.getInnerTokens()) {
                    newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(t)));
                }
                newSent.removeToken(newSent.getTokenAtPosition(newSent.getTokenPosition(chunkX.getLastToken())));
                newSent.removeChunk(or, true);
                // create new rewrite rule
                if (!newSent.toBareString().equals("")) {
                    newSent = new structure.Chunk(newSent.toString());
                    cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+Xor"), origRootStr);
                    newRoots.add(newSent.toString());
                    toRemoveRoots.add(rootIdx);
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
