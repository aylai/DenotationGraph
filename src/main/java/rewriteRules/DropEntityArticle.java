package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Drops entity modifiers (NPD)
 * @author aylai2
 */
public class DropEntityArticle {

    /* returns modified rewriteCaption (new rules added, string modified) */

    /**
     * Drops articles: tokens inside NPD chunks (unless the NPD chunk is "no" or "each")
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(RewriteCaption cap) {
        for (Chunk sent : cap.getRoots()) {
            String origRootStr = sent.toBareString();
            ArrayList<Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }
            // iterate over all chunks
            for (Iterator<Chunk> iterator = chunks.iterator(); iterator.hasNext(); ) {
                Chunk c = iterator.next();
                // look for [EN [NP [NPD ... ] ] ] chunk
                if (!c.getType().equals("EN")) {
                    continue;
                }
                ArrayList<Chunk> children1 = c.getChunks();
                for (Chunk c1 : children1) {
                    if (!c1.getType().equals("NP")) {
                        continue;
                    }
                    ArrayList<Chunk> children2 = c1.getChunks();
                    for (Chunk c2 : children2) {
                        if (!c2.getType().equals("NPD")) {
                            continue;
                        }
                        // create rewrite rule to drop tokens inside NPD chunk
                        ArrayList<String> left = new ArrayList<>();
                        ArrayList<String> right = new ArrayList<>();
                        String dropStr;
                        // add first token "[NPD" idx to both sides
                        left.add(Integer.toString(c2.getFirstToken().getId()));
                        right.add(Integer.toString(c2.getFirstToken().getId()));
                        // add NPD chunk tokens to right side and dropStr
                        dropStr = c2.toBareString();
                        right.addAll(c2.toStringListInner());
                        // add closing bracket token to both sides
                        left.add(Integer.toString(c2.getLastToken().getId()));
                        right.add(Integer.toString(c2.getLastToken().getId()));
                        // don't drop NPD if [NPD no ] or [NPD each ]
                        if (!dropStr.equals("no") && !dropStr.equals("each")) {
                            // create new rewrite rule
                            int numRules = cap.getRules().size();
                            // drop NPM chunk
                            sent.removeChunkKeepBrackets(c2, true);
                            cap.addRule(new RewriteRule(numRules, left, right, "+NPART/" + dropStr), origRootStr);
                            break;
                        }
                    }
                }
            }
        }
        return cap;
    }
}
