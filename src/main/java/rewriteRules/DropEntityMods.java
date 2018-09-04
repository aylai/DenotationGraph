package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;
import utils.ArrayListUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Drops entity modifiers (NPM, NPMC)
 * @author aylai2
 */
public class DropEntityMods {

    /**
     * List of entity modifiers that can only be restored if the original
     * head noun is in place.  They're all size/age modifiers - i.e., "a
     * large mouse" is not a "large animal".
     */
    private static HashSet<String> contextualModifiers;

    /**
     * Read list of context-sensitive entity modifiers from file
     */
    private static void readContextualModifiers(String dir) {
        contextualModifiers = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir+"/graph/data/entmod.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                contextualModifiers.add(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Identifies entity-modifying NPM and NPMC chunks that can be dropped and removes the internal tokens
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(String dir, RewriteCaption cap) {
        if (contextualModifiers == null) {
            readContextualModifiers(dir);
        }
        for (Chunk sent : cap.getRoots()) {
            String origRootStr = sent.toBareString();
            ArrayList<Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }
            // iterate over chunks in caption
            for (Iterator<Chunk> iterator = chunks.iterator(); iterator.hasNext(); ) {
                Chunk c = iterator.next();
                // look for [EN [NP [NPM ] ] ] chunk
                if (c.getType().equals("EN")) { // look for modifiers of EN chunks
                    String headEntity = c.getChunkHead();
                    // don't remove modifiers of "hair" (usually these are color nouns
                    // and we don't want transformation "person with black hair" -> "person with hair"
                    if (headEntity.equals("hair")) {
                        continue;
                    }
                    // retrieve chunks on the next level down in chunk c
                    ArrayList<Chunk> chunksL1 = c.getChunks();
                    for (Chunk c1 : chunksL1) {
                        if (!c1.getType().equals("NP")) {
                            continue;
                        }
                        ArrayList<Chunk> chunksL2 = c1.getChunks();
                        for (Chunk c2 : chunksL2) {
                            if (!c2.getType().equals("NPM")) {
                                continue;
                            }
                            // check if NPM contains NPMC chunks (multiple modifiers that can each be dropped independently)
                            ArrayList<Chunk> chunksL3 = c2.getChunks();
                            if (chunksL3.size() > 0 && chunksL3.get(0).getType().equals("NPMC")) { // NPM chunk contains NPMC chunks
                                for (int i = 0; i < chunksL3.size(); i++) { // iterate over all (presumably) NPMC chunks
                                    Chunk c3 = chunksL3.get(i);
                                    if (!c3.getType().equals("NPMC")) {
                                        continue;
                                    }
                                    ArrayList<String> left = new ArrayList<>();
                                    ArrayList<String> right = new ArrayList<>();
                                    String dropStr;
                                    // add first token "[NPMC" idx to both sides
                                    left.add(Integer.toString(c3.getFirstToken().getId()));
                                    right.add(Integer.toString(c3.getFirstToken().getId()));
                                    // add NPMC chunk tokens to right side and dropStr
                                    dropStr = c3.toBareString();
                                    right.addAll(c3.toStringListInner());
                                    // add closing bracket to both sides
                                    left.add(Integer.toString(c3.getLastToken().getId()));
                                    right.add(Integer.toString(c3.getLastToken().getId()));
                                    // check if modifier is contextual
                                    if (contextualModifiers.contains(dropStr)) {
                                        // get list of all following "[NPMC" tokens
                                        for (int j = i + 1; j < chunksL3.size(); j++) {
                                            int startIdx = chunksL3.get(j).getFirstToken().getId();
                                            left.add(Integer.toString(startIdx));
                                            right.add(Integer.toString(startIdx));
                                        }
                                        // add NPM closing bracket to left and right sides
                                        left.add(Integer.toString(c2.getLastToken().getId()));
                                        right.add(Integer.toString(c2.getLastToken().getId()));
                                        // add following NPH chunk to left and right sides (the NPH noun must be present to add this modifier)
                                        Chunk nphChunk = c2.getNextChunk();
                                        ArrayList<String> chunkIdx = ArrayListUtils.intListToStrList(nphChunk.toIds());
                                        left.addAll(chunkIdx);
                                        right.addAll(chunkIdx);
                                        // drop NPMC chunk
                                        sent.removeChunkKeepBrackets(c3, true);
                                        // create new rewrite rule
                                        int numRules = cap.getRules().size();
                                        cap.addRule(new RewriteRule(numRules, left, right, "+NPMOD/" + dropStr), origRootStr);
                                    } else {
                                        // drop NPMC chunk
                                        sent.removeChunkKeepBrackets(c3, true);
                                        // create new rewrite rule
                                        int numRules = cap.getRules().size();
                                        cap.addRule(new RewriteRule(numRules, left, right, "+NPMOD/" + dropStr), origRootStr);
                                    }
                                }
                            } else { // NPM chunk does not contain NPMC chunks
                                ArrayList<String> left = new ArrayList<>();
                                ArrayList<String> right = new ArrayList<>();
                                String dropStr;
                                // add first token "[NPM" idx to both sides
                                left.add(Integer.toString(c2.getFirstToken().getId()));
                                right.add(Integer.toString(c2.getFirstToken().getId()));
                                // add NPM chunk tokens to right side and dropStr
                                dropStr = c2.toBareString();
                                right.addAll(c2.toStringListInner());
                                // add closing bracket token to both sides
                                left.add(Integer.toString(c2.getLastToken().getId()));
                                right.add(Integer.toString(c2.getLastToken().getId()));
                                // check if modifier is contextual
                                if (contextualModifiers.contains(dropStr)) {
                                    // add following NPH chunk to left and right sides (the NPH noun must be present to add this modifier)
                                    Chunk nphChunk = c2.getNextChunk();
                                    ArrayList<String> chunkIdx = ArrayListUtils.intListToStrList(nphChunk.toIds());
                                    left.addAll(chunkIdx);
                                    right.addAll(chunkIdx);
                                }
                                // drop NPM chunk
                                sent.removeChunkKeepBrackets(c2, true);
                                // create new rewrite rule
                                int numRules = cap.getRules().size();
                                cap.addRule(new RewriteRule(numRules, left, right, "+NPMOD/" + dropStr), origRootStr);
                            }
                        }
                    }
                }
            }
        }
        return cap;
    }
}
