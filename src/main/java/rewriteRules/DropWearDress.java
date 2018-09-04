package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;
import structure.VP;
import utils.ArrayListUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Drop "wear X" and "dressed in/as/for X"
 * @author aylai2
 */
public class DropWearDress {

    /**
     * Words with the same index are head nouns that can be grouped together by a CC
     * (they should be dropped together if found in PP X CC Y)
     */
    private static HashMap<String, HashSet<Integer>> groups;

    /**
     * Read from file the list of groups of head nouns
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
     * Drops instances of "[VP wear ] [EN ... ]" and "[VP dressed] [PP in/as/for ] [EN ... ]"
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(String dir, RewriteCaption cap, VP verbInfo) {
        // load entity head nouns that can be grouped together
        if (groups == null) {
            readGroups(dir);
        }
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
                // look for VP chunk that is not last chunk in caption
                if (!c.getType().equals("VP") || c.getNextChunk() == null) {
                    continue;
                }
                Chunk chunkJ; // first chunk to drop
                Chunk chunkK; // last chunk to drop
                String en1;
                String verbId;
                String subjId = "";
                HashSet<String> dobjId = new HashSet<>();
                // verb is "dressed"
                if (c.toBareString().equals("dressed")) {
                    verbId = c.getId();
                    chunkJ = c.getNextChunk();
                    // next chunk is EN, grab entity head noun
                    if (chunkJ.getType().equals("EN")) {
                        chunkK = chunkJ;
                        en1 = chunkJ.getChunkHead();
                    }
                    // if "dressed in/as/for" is followed by EN chunk, grab entity head noun
                    else if (chunkJ.getType().equals("PP") && chunkJ.getNextChunk() != null &&
                            (chunkJ.toBareString().equals("in") ||
                                    chunkJ.toBareString().equals("as") ||
                                    chunkJ.toBareString().equals("for")) &&
                            chunkJ.getNextChunk().getType().equals("EN")) {
                        chunkJ = chunkJ.getNextChunk();
                        chunkK = chunkJ;
                        en1 = chunkJ.getChunkHead();
                        dobjId.add(chunkJ.getId());
                    } else {
                        continue;
                    }
                }
                // verb is "wear"
                else if (c.toBareString().equals("wear") && c.getNextChunk().getType().equals("EN")) {
                    verbId = c.getId();
                    chunkJ = c.getNextChunk();
                    chunkK = chunkJ;
                    en1 = chunkJ.getChunkHead();
                    dobjId.add(chunkJ.getId());
                } else {
                    continue;
                }

                // check if direct object is X CC Y where head nouns are in the same group; if so, store these X/Y dobj chunks
                Chunk obj1 = null;
                Chunk obj2 = null;
                if (chunkK.getNextChunk() != null && chunkK.getNextChunk().getType().equals("CC") &&
                        chunkK.getNextChunk().getNextChunk() != null &&
                        chunkK.getNextChunk().getNextChunk().getType().equals("EN")) {
                    String en2 = chunkK.getNextChunk().getNextChunk().getChunkHead();
                    if (groups.get(en1) != null && groups.get(en2) != null) {
                        HashSet<Integer> intersection = new HashSet<>(groups.get(en1));
                        intersection.retainAll(groups.get(en2));
                        if (intersection.size() > 0) {
                            obj1 = chunkK;
                            chunkK = chunkK.getNextChunk().getNextChunk(); // en2 chunk
                            obj2 = chunkK;
                            dobjId.add(chunkK.getId());
                        }
                    }
                }

                Chunk startWearPhrase = c;
                if (c.getPrevChunk() != null && c.getPrevChunk().getType().equals("EN")) {
                    startWearPhrase = c.getPrevChunk(); // attempt to add subject of WEAR chunk
                }
                if (startWearPhrase.getType().equals("EN")) {
                    subjId = startWearPhrase.getId();
                }
                // add missing verb (subj, dobj) info for this verb
                for (String dobjIdStr : dobjId) {
                    verbInfo.addVP(cap.getId(), verbId, subjId, dobjIdStr);
                }
                // create rewrite rule
                if (!sent.isFirstChunk(startWearPhrase) || !sent.isLastChunk(chunkK)) { // don't drop entire caption
                    // rule: B ... E -> B ... wear X ... E
                    ArrayList<String> left = new ArrayList<>();
                    ArrayList<String> right = new ArrayList<>();
                    ArrayList<Chunk> toRemove = new ArrayList<>();
                    Chunk newSent = new Chunk(sent.toString());
                    // rule: B person wear X E -> B person wear X ... E
                    ArrayList<String> leftWear = new ArrayList<>();
                    ArrayList<String> rightWear = new ArrayList<>();
                    ArrayList<Chunk> toRemoveWear = new ArrayList<>();
                    Chunk newWearSent = new Chunk(sent.toString());
                    // rule: if "wear X CC Y"
                    ArrayList<String> leftWear1 = new ArrayList<>();
                    ArrayList<String> rightWear1 = new ArrayList<>();
                    ArrayList<Chunk> toRemove1 = new ArrayList<>();
                    Chunk newWearSent1 = new Chunk(sent.toString());
                    // rule: if "wear X CC Y"
                    ArrayList<String> leftWear2 = new ArrayList<>();
                    ArrayList<String> rightWear2 = new ArrayList<>();
                    ArrayList<Chunk> toRemove2 = new ArrayList<>();
                    Chunk newWearSent2 = new Chunk(sent.toString());

                    // add preceding token to both sides of rule
                    Chunk chunkI = chunks.get(0);
                    left.add("B");
                    leftWear.add("B");
                    right.add("B");
                    rightWear.add("B");
                    boolean inWearChunk = false;
                    boolean foundCC = false;
                    boolean addWear = false;
                    while (chunkI != null) {
                        if (chunkI.equals(startWearPhrase)) { // beginning of WEAR chunks (incl subject)
                            // check if VP dress/wear chunk is preceded by CC chunk
                            if (chunkI.getPrevChunk() != null && chunkI.getPrevChunk().getType().equals("CC")) {
                                foundCC = true;
                            }
                            addWear = true;
                        }
                        if (chunkI.equals(c)) { // start of WEAR chunk (not incl subj)
                            inWearChunk = true;
                        }
                        if (!addWear) {
                            toRemoveWear.add(chunkI);
                            toRemove1.add(chunkI);
                            toRemove2.add(chunkI);
                            rightWear.add(chunkI.toString());
                        } else {
                            leftWear.add(Integer.toString(chunkI.getFirstToken().getId()));
                            if (obj1 != null) { // we have NP CC NP to deal with
                                if (chunkI.getType().equals("CC")) {
                                    rightWear1.add(chunkI.toString());
                                    rightWear2.add(chunkI.toString());
                                    toRemove1.add(chunkI);
                                    toRemove2.add(chunkI);
                                } else if (chunkI.equals(obj1)) {
                                    leftWear1.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear1.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear2.add(chunkI.toString());
                                    toRemove2.add(chunkI);
                                } else if (chunkI.equals(obj2)) {
                                    leftWear2.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear2.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear1.add(chunkI.toString());
                                    toRemove1.add(chunkI);
                                } else if (!(chunkI.equals(startWearPhrase) && !chunkI.equals(c))) {
                                    leftWear1.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear1.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    leftWear2.add(Integer.toString(chunkI.getFirstToken().getId()));
                                    rightWear2.add(Integer.toString(chunkI.getFirstToken().getId()));
                                }
                            }
                            rightWear.add(Integer.toString(chunkI.getFirstToken().getId()));
                        }
                        if (inWearChunk) {
                            right.add(chunkI.toString());
                            toRemove.add(chunkI);
                        } else {
                            // check if VP dress/wear chunk is preceded by CC chunk
                            right.add(Integer.toString(chunkI.getFirstToken().getId()));
                            if (!foundCC) {
                                left.add(Integer.toString(chunkI.getFirstToken().getId()));
                                foundCC = false;
                            }
                        }
                        if (chunkI.equals(chunkK)) { // end of WEAR chunks
                            inWearChunk = false;
                            addWear = false;
                        }
                        chunkI = chunkI.getNextChunk();
                    }
                    // drop chunks
                    for (int i = toRemove.size() - 1; i >= 0; i--) {
                        newSent.removeChunk(toRemove.get(i), true);
                    }
                    for (int i = toRemoveWear.size() - 1; i >= 0; i--) {
                        newWearSent.removeChunk(toRemoveWear.get(i), true);
                    }
                    for (int i = toRemove1.size() - 1; i >= 0; i--) {
                        newWearSent1.removeChunk(toRemove1.get(i), true);
                    }
                    for (int i = toRemove2.size() - 1; i >= 0; i--) {
                        newWearSent2.removeChunk(toRemove2.get(i), true);
                    }
                    left.add("E");
                    right.add("E");
                    leftWear.add("E");
                    rightWear.add("E");
                    leftWear1.add("E");
                    rightWear1.add("E");
                    leftWear2.add("E");
                    rightWear2.add("E");

                    // add rewrite rule: add rest of sentence to WEAR phrase
                    if (!newWearSent.toBareString().equals("")) {
                        if (rightWear.size() - leftWear.size() <= 5) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), leftWear, rightWear, "+VERB"), origRootStr);
                            // check if we need to create rewrite rules for hyponyms
                            String rightStr = ArrayListUtils.stringListToString(rightWear, " ");
                            cap.getHypernyms().keySet().stream().filter(hypernym -> rightStr.contains(hypernym)).forEach(hypernym -> {
                                HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                for (String hyponym : hyponyms) {
                                    String rightStrNew = rightStr.replace(hypernym, hyponym);
                                    cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(leftWear, " "), rightStrNew, "+VERB"), origRootStr);
                                }
                            });
                        }
                        if (obj1 == null) {
                            newRoots.add(newWearSent.toString());
                            toRemoveRoots.add(rootIdx);
                        } else {
                            // create sent1
                            if (!newWearSent1.toBareString().equals("")) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), leftWear1, rightWear1, "+CC-WEAR"), newWearSent.toBareString());
                                newRoots.add(newWearSent1.toString());
                                toRemoveRoots.add(rootIdx);
                            }
                            // create sent2
                            if (!newWearSent2.toBareString().equals("")) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), leftWear2, rightWear2, "+CC-WEAR"), newWearSent.toBareString());
                                newRoots.add(newWearSent2.toString());
                                toRemoveRoots.add(rootIdx);
                            }
                        }
                    }
                    // add rewrite rule: add WEAR phrase
                    if (!newSent.toBareString().equals("")) {
                        if (right.size() - left.size() <= 5) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+VERB"), origRootStr);
                            // check if we need to create rewrite rules for hyponyms
                            String rightStr1 = ArrayListUtils.stringListToString(right, " ");
                            for (String hypernym : cap.getHypernyms().keySet()) {
                                if (rightStr1.contains(hypernym)) {
                                    HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                    for (String hyponym : hyponyms) {
                                        String rightStrNew = rightStr1.replace(hypernym, hyponym);
                                        cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+VERB"), origRootStr);
                                    }
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
