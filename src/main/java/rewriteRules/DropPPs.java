package rewriteRules;

import structure.*;
import utils.ArrayListUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Drop PPs
 * @author aylai2
 */
public class DropPPs {

    private static HashSet<String> subjects;
    private static HashMap<String, HashSet<Integer>> groups;
    private static HashSet<String> vDropPrep; // pp3only
    private static HashSet<String> vDropBoth; // pp3both
    private static HashMap<String, Integer> prepDrop; //drop

    /**
     * Read from file the subject IDs for all captions (previously identified in the generation process)
     */
    private static void readSubjects(String dir, String corpus) {
        subjects = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir+"/"+corpus+"/"+corpus+".subj"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                subjects.add(tokens[1]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read from file the list of head nouns that can be grouped together by CC (and should be dropped together if found in PP X CC Y)
     */
    private static void readGroups(String dir) {
        groups = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir+"/graph/data/ccGroup.txt"));
            String line;
            int groupId = 0;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("")) {
                    groupId++;
                }
                else {
                    groups.putIfAbsent(line, new HashSet<>());
                    groups.get(line).add(groupId);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

   /**
     * List of verbs for which we should drop only the preposition, not the prepositional object (e.g. "climb up mountain" -> "climb mountain").
     * For any verb not in this list or vDropBoth, don't drop the PP (we don't know if it's part of the verb or not).
     */
    private static void readVDropPrep() {
        vDropPrep = new HashSet<>();
        vDropPrep.add("climb");
        vDropPrep.add("hold");
    }

    /**
     * List of verbs for which we should drop the whole PP (preposition and object).
     * For any verb not in this list of vDropPrep, don't drop the PP (we don't know if it's part of the verb or not).
     */
    private static void readVDropBoth() {
        vDropBoth = new HashSet<>();
        vDropBoth.add("bicycle");
        vDropBoth.add("bike");
        vDropBoth.add("hike");
        vDropBoth.add("jump");
        vDropBoth.add("race");
        vDropBoth.add("ride");
        vDropBoth.add("run");
        vDropBoth.add("skateboard");
        vDropBoth.add("ski");
        vDropBoth.add("slide");
        vDropBoth.add("walk");
    }

    /**
     * List of prepositions that we can drop, categorized into 3 types:
     * 1: normal preposition
     * 2: may be followed by "on", which should also be dropped
     * 3: verb particle (must be preceded by verb, may be in PRT chunk)
     */

    private static void readPrepDrop() {
        prepDrop = new HashMap<>();
        prepDrop.put("above", 1);
        prepDrop.put("across", 1);
        prepDrop.put("against", 1);
        prepDrop.put("around", 1);
        prepDrop.put("at", 1);
        prepDrop.put("behind", 1);
        prepDrop.put("beneath", 1);
        prepDrop.put("beside", 1);
        prepDrop.put("by", 1);
        prepDrop.put("down", 3);
        prepDrop.put("in", 1);
        prepDrop.put("in front of", 1);
        prepDrop.put("into", 1);
        prepDrop.put("for", 1);
        prepDrop.put("from", 1);
        prepDrop.put("near", 1);
        prepDrop.put("next to", 1);
        prepDrop.put("on", 1);
        prepDrop.put("on top of", 1);
        prepDrop.put("over", 1);
        prepDrop.put("through", 1);
        prepDrop.put("towards", 1);
        prepDrop.put("with", 2);
        prepDrop.put("under", 1);
        prepDrop.put("underneath", 1);
        prepDrop.put("up", 3);
    }

    /**
     * Removes prepositional phrases (and additional parts of the caption
     * in order to maintain grammatical correctness)
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(String dir, String corpusDir, String corpus, RewriteCaption cap) {
        if (subjects == null) {
            readSubjects(corpusDir, corpus);
        }
        if (groups == null) {
            readGroups(dir);
        }
        if (vDropPrep == null) {
            readVDropPrep();
        }
        if (vDropBoth == null) {
            readVDropBoth();
        }
        if (prepDrop == null) {
            readPrepDrop();
        }
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
            // iterate backwards through the caption, so if we find multiple chained PPs
            // we can drop them all at once (treat each PP as connected to previous prepositional object)
            for (int i = chunks.size() - 1; i >= 0; i--) {
                boolean newChunk = false;
                structure.Chunk c = chunks.get(i);
                ArrayList<structure.Chunk> updatedChunks = sent.getChunks();
                // indices
                structure.Chunk prepB; // first chunk of preposition
                structure.Chunk prepE; // last chunk of preposition
                int prepT; // highest type (1, 2, 3) of the prepositions to be dropped
                switch (c.getType()) {
                    case "PP": {
                        ArrayList<String> as = new ArrayList<>();
                        prepT = -1;
                        // check inside PP chunk for prepositions to drop
                        ArrayList<MyToken> tokens = c.getInnerTokens();
                        for (MyToken t : tokens) {
                            if (t.getPos().equals("CC")) {
                                String s = ArrayListUtils.stringListToString(as, " ");
                                if (prepDrop.get(s) == null) { // no droppable preposition before CC
                                    newChunk = true;
                                    break;
                                } else if (prepT < prepDrop.get(s)) { // new highest preposition type
                                    prepT = prepDrop.get(s);
                                }
                                as = new ArrayList<>(); // could be a preposition after CC, we need to check
                            } else {
                                as.add(t.getStr()); // add token string
                            }
                        }
                        String s = ArrayListUtils.stringListToString(as, " ");
                        if (newChunk || prepDrop.get(s) == null) { // we didn't find a droppable preposition, continue to next chunk
                            continue;
                        }
                        prepB = c; // PP chunk
                        prepE = c; // PP chunk

                        if (prepT < prepDrop.get(s)) {
                            prepT = prepDrop.get(s);
                        }
                        break;
                    }
                    case "PRT": { // PRT chunk may actually be something PPish we need to drop
                        ArrayList<String> as = new ArrayList<>();
                        prepT = -1;
                        // check inside PRT chunk - we need 3 (verb particle) as the highest preposition type seen in order to drop this chunk
                        ArrayList<MyToken> tokens = c.getInnerTokens();
                        for (MyToken t : tokens) {
                            if (t.getPos().equals("CC")) {
                                String s = ArrayListUtils.stringListToString(as, " ");
                                if (prepDrop.get(s) == null) { // no droppable preposition before CC
                                    newChunk = true;
                                    break;
                                } else if (prepT < prepDrop.get(s)) { // new highest preposition
                                    prepT = prepDrop.get(s);
                                }
                                as = new ArrayList<>(); // could be a preposition after CC, we need to check
                            } else {
                                as.add(t.getStr());
                            }
                        }
                        String s = ArrayListUtils.stringListToString(as, " ");
                        if (newChunk || prepDrop.get(s) == null) { // we didn't find a droppable preposition, goto next chunk
                            continue;
                        }
                        prepB = c; // PRT chunk
                        prepE = c; // PRT chunk
                        if (prepT < prepDrop.get(s)) { // update highest prep type seen
                            prepT = prepDrop.get(s);
                        }
                        break;
                    }
                    default:  // neither PP nor PRT chunk
                        continue;
                }

                // check if we have the second PP of a PP CC PP construction
                structure.Chunk prevChunk = c.getPrevChunk();
                if (prevChunk != null && prevChunk.getType().equals("CC") && prevChunk.getPrevChunk() != null) { // Chunk c is preceded by CC
                    structure.Chunk prevPrevChunk = prevChunk.getPrevChunk();
                    if (prevPrevChunk.getType().equals("PP")) {
                        // assume that prevPrevChunk PP has no internal CCs
                        String s = prevPrevChunk.toBareString();
                        if (prepDrop.get(s) != null) { // droppable preposition
                            if (prepT < prepDrop.get(s)) {
                                prepT = prepDrop.get(s);
                            }
                            prepB = prevPrevChunk; // beginning of first PP chunk
                        }
                    } else if (prevPrevChunk.getType().equals("PRT")) {
                        String s = prevPrevChunk.toBareString();
                        if (prepDrop.get(s) != null && prepDrop.get(s) == 3) {
                            prepT = prepDrop.get(s);
                            prepB = prevPrevChunk; // beginning of first PRT chunk
                        }
                    }
                }

                // check if we need to drop the object
                boolean skipEn = false;
                if (prepT == 3) { // verb particle
                    // look for VP chunk
                    structure.Chunk prevPrevChunk = prepB.getPrevChunk();
                    if (prevPrevChunk != null) {
                        if (!prevPrevChunk.getType().equals("VP")) {
                            continue;
                        }
                        String s = prevPrevChunk.toBareString();
                        if (vDropPrep.contains(s)) { // verb where we drop preposition, not prep obj
                            skipEn = true;
                        } else if (!vDropBoth.contains(s)) { // not a verb where we drop PP; continue to next chunk
                            continue;
                        }
                    }
                }

                // if we're not skipping the object
                structure.Chunk objB = null; // beginning of the object
                structure.Chunk objE = null; // end of the object
                structure.Chunk chunkJ = prepE.getNextChunk(); // chunk following the preposition
                structure.Chunk chunkK;
                if (!skipEn) {
                    // make sure we haven't reached the end of the caption
                    if (chunkJ == null) {
                        continue;
                    }
                    // make sure chunkJ is type EN and not a subject (don't want to drop subject)
                    if (!chunkJ.getType().equals("EN") || subjects.contains(cap.getId() + "#" + chunkJ.getId())) {
                        continue;
                    }
                    // grab trailing "of"s that are not always stored in EN chunk, e.g. "arm of person"
                    // check if this chunk chunkK is [PP of ] and is followed by an EN chunk
                    chunkK = chunkJ.getNextChunk();
                    while (chunkK != null && chunkK.getType().equals("PP") && chunkK.toBareString().equals("of") && chunkK.getNextChunk() != null && chunkK.getNextChunk().getType().equals("EN")) {
                        chunkK = chunkK.getNextChunk().getNextChunk();
                    }
                    // check if object is X CC Y
                    if (chunkK != null && chunkK.getType().equals("CC")) {
                        structure.Chunk chunkL = chunkK.getNextChunk();
                        if (chunkL != null && chunkL.getType().equals("EN")) {
                            String en1 = chunkJ.getChunkHead();
                            String en2 = chunkL.getChunkHead();
                            if (groups.get(en1) != null && groups.get(en2) != null) {
                                HashSet<Integer> intersection = new HashSet<>(groups.get(en1));
                                intersection.retainAll(groups.get(en2));
                                if (intersection.size() > 0) {
                                    chunkK = chunkL.getNextChunk();
                                    // grab trailing "of"s that are not always stored in EN chunk
                                    while (chunkK != null && chunkK.getType().equals("PP") && chunkK.toBareString().equals("of") && chunkK.getNextChunk() != null && chunkK.getNextChunk().getType().equals("EN")) {
                                        chunkK = chunkK.getNextChunk().getNextChunk();
                                    }
                                }
                            }
                        }
                    }
                    // if this preposition can have trailing "on" and we find one, add it
                    if (prepT == 2 && chunkK != null && chunkK.getType().equals("PP") && chunkK.toBareString().equals("on")) {
                        chunkK = chunkK.getNextChunk();
                    }
                    // if we have "OBJ that ...", drop the rest of the caption
                    if (chunkK != null && chunkK.getType().equals("EN") && chunkK.getChunkHead().equals("that")) {
                        chunkK = null; // past end of chunk
                    }
                    objB = chunkJ;
                    if (chunkK != null) {
                        objE = chunkK.getPrevChunk();
                    } else {
                        objE = updatedChunks.get(updatedChunks.size() - 1); // last chunk
                    }
                }

                // determine if there is previous VP chunk
                // if so, check if it is "be" or "dressed", which we want to drop (avoid "is in X" -> "is")
                structure.Chunk verbB = null;
                chunkJ = prepB.getPrevChunk();
                boolean containsBe = false;
                if (chunkJ != null && chunkJ.getType().equals("VP")) { // prev chunk is VP
                    String verbT = chunkJ.toBareString();
                    if (verbT.equals("be")) {
                        verbB = chunkJ;
                        containsBe = true;
                    } else if (verbT.equals("dressed")) {
                        verbB = chunkJ;
                    }
                }

                // look for other terms (which/who/that or CCs) that should be dropped if we drop the PP
                // "that is in X" should be dropped entirely
                // note that we are not guaranteed to drop the tokens we find here
                structure.Chunk joinB = null;
                structure.Chunk joinE;
                if (verbB == null) { // we didn't find verb to drop
                    joinE = prepB;
                } else {
                    joinE = verbB;
                }
                chunkJ = joinE.getPrevChunk();
                if (chunkJ != null) {
                    if (chunkJ.getType().equals("EN")) {
                        String joinT = chunkJ.getChunkHead();
                        if (joinT.equals("which") || joinT.equals("who") || joinT.equals("that")) {
                            joinB = chunkJ;
                        }
                    } else if (chunkJ.getType().equals("CC")) {
                        joinB = chunkJ;
                    }
                }

                // j: beginning of stuff to drop, k: end of stuff to drop
                // joinB ... joinE may or may not be dropped
                if (verbB == null) { // no verb to drop
                    chunkJ = prepB;
                } else { // verb to drop
                    chunkJ = verbB;
                }
                if (objB == null) { // don't drop prepositional object
                    chunkK = prepE;
                } else { // drop prepositional object
                    chunkK = objE;
                }
                if (joinB != null) {
                    // if the join words would be the end of a caption or a clause, drop them (so no trailing that/which/who/CC)
                    structure.Chunk chunkI = chunkK.getNextChunk();
                    if (chunkI == null) { // k is past the end of the caption
                        chunkJ = joinB;
                    } else {
                        String s = chunkI.toBareString();
                        if (s.equals("as") || s.equals("while")) {
                            chunkJ = joinB;
                        }
                    }
                    if (chunkJ != joinB) {
                        // if the next chunk would be a VP and we're dropping it, add the join words
                        // we basically assume the VP we're dropping is "be"
                        if (chunkI.getType().equals("VP")) {
                            if (verbB != null) {
                                chunkJ = joinB;
                            }
                        }
                    }
                    // if join is non-null (so PP is preceded by some connector) and we're not dropping VP,
                    // check if the next thing in the VP is "be" because we should drop that too
                    if (chunkJ != joinB && verbB == null && chunkI.getType().equals("VP")) {
                        String s = chunkI.toBareString();
                        if (s.equals("be")) {
                            chunkK = chunkI;
                        }
                    }
                }

                // make sure we're not removing the entire string
                if (sent.isFirstChunk(chunkJ) && sent.isLastChunk(chunkK)) {
                    continue;
                }
                ArrayList<String> leftMinusPP = new ArrayList<>();
                ArrayList<String> rightMinusPP = new ArrayList<>();
                // check if there's a prior chunk we can insert ourselves into
                if (chunkJ.getPrevChunk() == null || (chunkJ.getPrevToken() != null && !chunkJ.getPrevToken().getStr().equals("]"))) { // no prior chunk
                    if (chunkJ.getPrevChunk() != null) {
                        leftMinusPP.add(Integer.toString(chunkJ.getPrevToken().getId()));
                        rightMinusPP.add(Integer.toString(chunkJ.getPrevToken().getId()));
                    } else {
                        leftMinusPP.add("B");
                        rightMinusPP.add("B");
                    }
                    for (int tId = chunkJ.getStartIdx(); tId <= chunkK.getEndIdx(); tId++) {
                        MyToken tok = sent.getTokenAtPosition(tId);
                        if (containsBe && ((verbB != null && verbB.containsToken(tok)) || (joinB != null && joinB.containsToken(tok)))) {
                            continue;
                        }
                        rightMinusPP.add(tok.toString());
                    }
                    if (chunkK.getNextToken() != null) {
                        leftMinusPP.add(Integer.toString(chunkK.getNextToken().getId()));
                        rightMinusPP.add(Integer.toString(chunkK.getNextToken().getId()));
                    } else {
                        leftMinusPP.add("E");
                        rightMinusPP.add("E");
                    }
                } else { // found a prior chunk
                    if (chunkJ.getPrevToken().getPrevToken() != null) {
                        leftMinusPP.add(Integer.toString(chunkJ.getPrevToken().getPrevToken().getId()));
                        rightMinusPP.add(Integer.toString(chunkJ.getPrevToken().getPrevToken().getId()));
                    } else {
                        leftMinusPP.add("B");
                        rightMinusPP.add("B");
                    }
                    for (int tId = chunkJ.getStartIdx(); tId <= chunkK.getEndIdx(); tId++) {
                        MyToken tok = sent.getTokenAtPosition(tId);
                        if (containsBe && ((verbB != null && verbB.containsToken(tok)) || (joinB != null && joinB.containsToken(tok)))) {
                            continue;
                        }
                        rightMinusPP.add(tok.toString());
                    }
                    leftMinusPP.add(Integer.toString(chunkJ.getPrevToken().getId()));
                    rightMinusPP.add(Integer.toString(chunkJ.getPrevToken().getId()));
                }

                // get preposition being dropped
                String aprep = "";
                for (int l = prepB.getStartIdx(); l <= prepE.getEndIdx(); l++) {
                    MyToken tok = sent.getTokenAtPosition(l);
                    if (!tok.getStr().startsWith("[") && !tok.getStr().startsWith("]")) {
                        aprep += tok.getStr() + " ";
                    }
                }
                if (!aprep.equals("")) {
                    aprep = aprep.substring(0, aprep.length() - 1);
                }

                // if there is an object being dropped, label will be PP/prep/obj
                if (objB != null) {
                    // create rewrite rule for dropping everything but PP
                    ArrayList<String> leftKeepPP = new ArrayList<>();
                    ArrayList<String> rightKeepPP = new ArrayList<>();
                    structure.Chunk newSent = new structure.Chunk(sent.toString());
                    String origRootStrNew = newSent.toBareString();
                    ArrayList<structure.Chunk> toRemove = new ArrayList<>();
                    leftKeepPP.add("B");
                    rightKeepPP.add("B");
                    boolean inPP = false;
                    for (structure.Chunk curChunk : chunks) {
                        if (curChunk.equals(chunkJ)) {
                            inPP = true;
                        }
                        if (inPP) {
                            leftKeepPP.add(Integer.toString(curChunk.getFirstToken().getId()));
                            rightKeepPP.add(Integer.toString(curChunk.getFirstToken().getId()));
                        } else {
                            toRemove.add(curChunk);
                        }
                        if (curChunk.equals(chunkK)) {
                            inPP = false;
                        }
                    }

                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    if (chunkJ.getPrevChunk() != null) { // if there's a prior chunk to insert ourselves into
                        ArrayList<String> tokens = chunkJ.getPrevChunk().toStringList();
                        for (int j = 0; j < tokens.size() - 1; j++) {
                            rightKeepPP.add(1 + j, tokens.get(j));
                        }
                        rightKeepPP.add(tokens.get(tokens.size() - 1));
                        if (!chunkJ.getPrevChunk().getType().equals("EN") && !chunkJ.getPrevChunk().getType().equals("VP")) { // add previous chunks until we reach beginning of sent or find EN/VP chunk
                            structure.Chunk chunkI = chunkJ.getPrevChunk().getPrevChunk();
                            while (chunkI != null) {
                                rightKeepPP.add(1, chunkI.toString());
                                if (chunkI.getType().equals("EN") || chunkI.getType().equals("VP")) {
                                    break;
                                }
                                chunkI = chunkI.getPrevChunk();
                            }
                        }
                    } else { // add following chunks until we find EN/VP
                        structure.Chunk chunkI = chunkK.getNextChunk();
                        while (chunkI != null) {
                            rightKeepPP.add(chunkI.toString());
                            if (chunkI.getType().equals("EN") || chunkI.getType().equals("VP")) {
                                break;
                            }
                            chunkI = chunkI.getNextChunk();
                        }
                    }
                    leftKeepPP.add("E");
                    rightKeepPP.add("E");
                    if (!newSent.toBareString().equals("")) {
                        cap.addRule(new RewriteRule(cap.getRules().size(), leftKeepPP, rightKeepPP, "+PP-SENT"), origRootStrNew);
                        newRoots.add(newSent.toString());
                    }
                    // get object being dropped
                    String aobj = "";
                    for (int l = objB.getStartIdx(); l <= objE.getEndIdx(); l++) {
                        MyToken tok = sent.getTokenAtPosition(l);
                        if (!tok.getStr().startsWith("[") && !tok.getStr().startsWith("]")) {
                            aobj += tok.getStr() + " ";
                        }
                    }
                    if (!aobj.equals("")) {
                        aobj = aobj.substring(0, aobj.length() - 1);
                    }
                    // check for preceding CC (we probably already handled this case earlier, but just in case)
                    if (!leftMinusPP.get(0).equals("B") && leftMinusPP.get(1).equals("E")) {
                        structure.Chunk chunkL = sent.getChunkContainsTokenId(Integer.parseInt(leftMinusPP.get(0)));
                        if (chunkL.getType().equals("CC")) {
                            leftMinusPP.remove(0);
                            rightMinusPP.remove(0);
                            MyToken t = sent.getTokenAtPosition(chunkL.getStartIdx());
                            rightMinusPP.add(0, t.getId() + "/" + t.getStr() + "/" + t.getPos());
                            if (chunkL.getPrevToken() != null) {
                                leftMinusPP.add(0, Integer.toString(chunkL.getPrevToken().getId()));
                                rightMinusPP.add(0, Integer.toString(chunkL.getPrevToken().getId()));
                            } else {
                                leftMinusPP.add(0, "B");
                                rightMinusPP.add(0, "B");
                            }
                            chunkJ = chunkJ.getPrevChunk();
                        }
                    }
                    // add rule: remove PP
                    cap.addRule(new RewriteRule(cap.getRules().size(), leftMinusPP, rightMinusPP, "+PP/" + aprep + "/" + aobj), origRootStrNew);
                } else { // there is no object being dropped, label will be PP/prep
                    // check for preceding CC (we probably already handled this case earlier, but just in case)
                    if (!leftMinusPP.get(0).equals("B")) {
                        structure.Chunk chunkL = sent.getChunkContainsTokenId(Integer.parseInt(leftMinusPP.get(0)));
                        if (chunkL.getType().equals("CC")) {
                            leftMinusPP.remove(0);
                            rightMinusPP.remove(0);
                            MyToken t = sent.getTokenAtPosition(chunkL.getEndIdx());
                            rightMinusPP.add(0, t.toString());
                            if (chunkL.getPrevToken() != null) {
                                leftMinusPP.add(0, Integer.toString(chunkL.getPrevToken().getId()));
                                rightMinusPP.add(0, Integer.toString(chunkL.getPrevToken().getId()));
                            } else {
                                leftMinusPP.add(0, "B");
                                rightMinusPP.add(0, "B");
                            }
                            chunkJ = chunkJ.getPrevChunk();
                        }
                    }
                    // add rule: remove PP
                    cap.addRule(new RewriteRule(cap.getRules().size(), leftMinusPP, rightMinusPP, "+PP/" + aprep), origRootStr);
                }
                // update caption, removing tokens and chunks
                structure.Chunk chunkI = chunkJ;
                while (chunkI != null) {
                    structure.Chunk nextChunk = chunkI.getNextChunk();
                    sent.removeChunk(chunkI, true);
                    if (chunkI == chunkK) {
                        break;
                    }
                    chunkI = nextChunk;
                }
                if (i > chunks.size()) { // reset indexing if we removed multiple chunks
                    i = chunks.size();
                }
            }
        }
        for (String root : newRoots) {
            cap.addRoot(root);
        }
        return cap;
    }
}
