package rewriteRules;

import structure.*;

import java.util.ArrayList;

/**
 * Drop verb modifiers (ADVP, RB)
 * @author aylai2
 */
public class DropEventMods {

    /* returns modified rewriteCaption (new rules added, string modified) */

    /**
     * Drop verb modifiers: ADVP chunks or RB chunks inside VP chunks
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(RewriteCaption cap) {
        cap.setSent("");
        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx ++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            ArrayList<structure.Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }
            // iterate backwards so if we drop something, the index of the next token doesn't change
            for (int i = chunks.size() - 1; i >= 0; i--) {
                structure.Chunk c = chunks.get(i);
                // look for [ADVP ...
                if (c.getType().equals("ADVP")) {
                    // don't drop [ADVP other]
                    if (c.toBareString().equals("other")) {
                        continue;
                    }
                    // don't drop [ADVP [color]] if preceded by [VP wear]
                    structure.Chunk cPrev = c.getPrevChunk();
                    if (cPrev != null) {
                        String cPrevStr = cPrev.toBareString();
                        if (cPrevStr.equals("wear") && cPrev.getType().equals("VP")) {
                            continue;
                        }
                    }
                    // create rewrite rule
                    ArrayList<String> left = new ArrayList<>();
                    ArrayList<String> right = new ArrayList<>();
                    // before ADVP: add to both sides of rule
                    if (sent.isFirstChunk(c)) {
                        left.add("B");
                        right.add("B");
                    } else {
                        int prevTokId = c.getPrevToken().getId();
                        left.add(Integer.toString(prevTokId));
                        right.add(Integer.toString(prevTokId));
                    }
                    // ADVP: add to right side of rule
                    right.add(c.toString());
                    // after ADVP: add to both sides of rule
                    if (sent.isLastChunk(c)) {
                        left.add("E");
                        right.add("E");
                    } else {
                        int nextTokId = c.getNextToken().getId();
                        left.add(Integer.toString(nextTokId));
                        right.add(Integer.toString(nextTokId));
                    }
                    // special case: if ADVP is last chunk and preceded by CC, then drop CC
                    structure.Chunk origSent = new structure.Chunk(sent.toString());
                    if (sent.isLastChunk(c) && !sent.isFirstChunk(c) && c.getPrevChunk().getType().equals("CC")) {
                        structure.Chunk ccChunk = c.getPrevChunk();
                        MyToken ccToken = ccChunk.getFirstToken();
                        // fix rewrite rules
                        left.remove(0);
                        right.remove(0);
                        right.add(0, ccToken.getId() + "/" + ccToken.getStr() + "/" + ccToken.getPos());
                        if (sent.isFirstToken(ccToken)) {
                            left.add(0, "B"); // instead of CC chunk preceding ADVP
                            right.add(0, "B");
                        } else {
                            String prevTokId = Integer.toString(ccToken.getPrevToken().getId());
                            left.add(0, prevTokId);
                            right.add(0, prevTokId);
                        }
                        // drop CC
                        sent.removeChunk(ccChunk, true);
                        i--;
                    }
                    // drop ADVP chunk
                    sent.removeChunk(c, true);
                    if (!sent.toBareString().equals("")) {
                        String chunkStr = c.toBareString();
                        cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+ADVP/" + chunkStr), origRootStr);
                    }
                    else {
                        cap.replaceRoot(origSent, rootIdx);
                    }
                }
                // look for [VP ... [RB ] ]
                if (c.getType().equals("VP")) {
                    ArrayList<MyToken> tokens = c.getTokens();
                    for (int j = 0; j < tokens.size(); j++) {
                        MyToken t = tokens.get(j);
                        if (t.getPos().equals("RB")) { // adverb
                            // create rewrite rule
                            ArrayList<String> left = new ArrayList<>();
                            ArrayList<String> right = new ArrayList<>();
                            // before RB: add to both sides of rule
                            int prevTokId = t.getPrevToken().getId();
                            left.add(Integer.toString(prevTokId));
                            right.add(Integer.toString(prevTokId));
                            // RB: add to right side of rule
                            String dropped = "";
                            int lastRBTokId = j - 1;
                            ArrayList<MyToken> dropTokens = new ArrayList<>();
                            for (int k = j; k < tokens.size(); k++) { // in case of multiple RBs in a row
                                MyToken tok = tokens.get(k);
                                if (!tok.getPos().equals("RB")) {
                                    break;
                                }
                                right.add(tok.getId() + "/" + tok.getStr() + "/" + tok.getPos());
                                dropped += tok.getStr() + " ";
                                dropTokens.add(tok);
                                lastRBTokId++;
                            }
                            dropped = dropped.substring(0, dropped.length() - 1);
                            // don't drop "[RB not ]"
                            if (dropped.equals("not")) {
                                continue;
                            }
                            // after RB: add to both sides of rule
                            int nextTokId = tokens.get(lastRBTokId).getNextToken().getId();
                            left.add(Integer.toString(nextTokId));
                            right.add(Integer.toString(nextTokId));
                            // remove RB internal tokens
                            for (MyToken tok : dropTokens) {
                                sent.removeToken(tok);
                                j--;
                            }
                            j++;
                            int numRules = cap.getRules().size();
                            cap.addRule(new RewriteRule(numRules, left, right, "+RB/" + dropped), origRootStr);
                            tokens = c.getTokens();
                        }
                    }
                }
            }
        }
        return cap;
    }
}
