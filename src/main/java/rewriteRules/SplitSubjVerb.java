package rewriteRules;

import structure.*;
import utils.ArrayListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Extract subject + verb, verb + object from complex sentences
 * Created by alai on 6/26/15.
 */
public class SplitSubjVerb {


    /**
     * List of verb types X categorized by how they split in case of "X to Y":
     * 1: can drop "X to"
     * 2: can drop "to Y"
     * 3: can drop both "X to" and "to Y"
     */
    private static HashMap<String, Integer> splitTo;

    /**
     * Populate splitTo list
     */
    private static void loadSplitVerbs() {
        splitTo = new HashMap<>();

        splitTo.put("appear", 1);
        splitTo.put("attempt", 1);
        splitTo.put("be about", 1);
        splitTo.put("begin", 1);
        splitTo.put("go", 1);
        splitTo.put("seem", 1);
        splitTo.put("start", 1);
        splitTo.put("struggle", 1);
        splitTo.put("try", 1);
        splitTo.put("reach", 1);

        splitTo.put("line up", 2);
        splitTo.put("pause", 2);
        splitTo.put("wait", 2);

        splitTo.put("bend down", 3);
        splitTo.put("bend over", 3);
        splitTo.put("crouch", 3);
        splitTo.put("dive", 3);
        splitTo.put("gather", 3);
        splitTo.put("kneel", 3);
        splitTo.put("kneel down", 3);
        splitTo.put("lean in", 3);
        splitTo.put("lean over", 3);
        splitTo.put("leap", 3);
        splitTo.put("jump", 3);
        splitTo.put("jump up", 3);
        splitTo.put("reach out", 3);
        splitTo.put("reach up", 3);
        splitTo.put("run", 3);
        splitTo.put("sit", 3);
        splitTo.put("sit down", 3);
        splitTo.put("stop", 3);
        splitTo.put("walk", 3);
        splitTo.put("wind up", 3);
        splitTo.put("work", 3);
    }

    /**
     * Splits complex sentences into "subject + VP" and "verb + dobj"
     * Also generates rules to split up verb TOs, e.g. "jump to catch frisbee" -> "jump" and "catch frisbee"
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(RewriteCaption cap, VP verbInfo) {

        HashMap<String, ArrayList<String>> vpMap = verbInfo.getVPs();
        // load SVO triples and light verbs
        // load split verb types
        if (splitTo == null) {
            loadSplitVerbs();
        }
        String capId = cap.getId();
        HashSet<Integer> toRemoveRoots = new HashSet<>();
        ArrayList<String> newRoots = new ArrayList<>();
        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            ArrayList<String> modifiedCaps = new ArrayList<>();
            ArrayList<String> endStrings = new ArrayList<>();
            ArrayList<String> startStrings = new ArrayList<>();
            HashSet<String> simpleSentStrings = new HashSet<>();
            // get SVOs that are supposed to be in this caption
            int countVPs = 0;
            if (vpMap.get(capId) != null) {
                countVPs = vpMap.get(capId).size();
            }


            for (int i = 0; i < countVPs; i++) {
                ArrayList<structure.Chunk> chunks = new ArrayList<>();
                if (sent.getType().equals("SENT")) {
                    chunks = sent.getChunks();
                } else {
                    chunks.add(sent);
                }
                // get the ith SVO
                cap.setSent(sent);
                String str = verbInfo.getVP(cap, i);
                String[] tokens = str.split("\t");
                int subj = Integer.parseInt(tokens[0]);
                int vp = Integer.parseInt(tokens[1]);
                int dobj = Integer.parseInt(tokens[2]);
                String ssubj = tokens[3];
                String svp = tokens[4];
                String sdobj = tokens[5];
                // check for missing SVO
                if (vp == -2) {
                    continue;
                }
                if (subj == -2) {
                    continue;
                }
                // vpE is the end of the VP (includes dobj if it exists)
                int vpE = vp;
                if (dobj >= 0) {
                    vpE = dobj;
                }
                if (subj == -1) {
                    if (chunks.size() == 3 && vp == 1 && dobj == 2 && chunks.get(0).getType().equals("EN")) {
                        subj = 0;
                    } else if (chunks.size() == 2 && vp == 1 && chunks.get(0).getType().equals("EN")) {
                        subj = 0;
                    }
                }
                // generate rule to extract SVO from complex sentence
                int drop = 0; // are we actually going to drop anything?
                int split = 0; // is there a subject and a VP?
                ArrayList<String> left = new ArrayList<>();
                ArrayList<String> right = new ArrayList<>();
                structure.Chunk newSent = new structure.Chunk(sent.toString());
                structure.Chunk svoSent = new structure.Chunk(sent.toString());
                ArrayList<String> endStrList = new ArrayList<>();
                ArrayList<String> startStrList = new ArrayList<>();
                String verb = "";
                left.add("B");
                right.add("B");
                ArrayList<structure.Chunk> toRemove = new ArrayList<>();
                int newSubj = subj;
                int newVp = vp;
                int newDobj = dobj;
                int newVpE = vpE;
                for (int j = 0; j < chunks.size(); j++) {
                    String tokenIdStr = Integer.toString(chunks.get(j).getFirstToken().getId());
                    if (j == subj) {
                        left.add(tokenIdStr);
                        right.add(tokenIdStr);
                        endStrList.add(chunks.get(j).toBareString());
                        startStrList.add(chunks.get(j).toBareString());
                        split = split | 1;
                    } else if (j >= vp && j <= vpE) { // inside the VP
                        left.add(tokenIdStr);
                        right.add(tokenIdStr);
                        endStrList.add(chunks.get(j).toBareString());
                        startStrList.add(chunks.get(j).toBareString());
                        if (j == vp) {
                            verb = chunks.get(j).toBareString();
                        }
                        split = split | 2;
                    } else {
                        right.add(chunks.get(j).toString());
                        endStrList.add(chunks.get(j).toBareString());
                        toRemove.add(chunks.get(j));
                        drop = 1;
                        if (j < subj) {
                            newSubj--;
                        }
                        if (j < vp) {
                            newVp--;
                        }
                        if (j < vpE) {
                            newVpE--;
                        }
                        if (j < dobj) {
                            newDobj--;
                        }
                    }
                }
                for (int j = toRemove.size() - 1; j >= 0; j--) {
                    newSent.removeChunk(toRemove.get(j), true);
                    svoSent.removeChunk(toRemove.get(j), true);
                }
                left.add("E");
                right.add("E");
                String endStr = ArrayListUtils.stringListToString(endStrList, " ");
                String startStr = ArrayListUtils.stringListToString(startStrList, " ");
                // if the rule is actually going to drop something, generate the rule
                if (drop == 1 && !verb.equals("be")) {
                    // differentiate between extracting SVO and extracting VP
                    if (split == 2) {
                        if (!newSent.toBareString().equals("")) {
                            if (right.size() - left.size() <= 5) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+COMPLEX-VERB"), origRootStr);
                                endStrings.add(endStr);
                                startStrings.add(startStr);
                                modifiedCaps.add(newSent.toString());

                                // add NP captions
                                for (int j = 0; j < chunks.size(); j++) {
                                    if (j == subj || (j >= vp && j <= vpE)) {
                                        continue;
                                    }
                                    structure.Chunk c = chunks.get(j);
                                    if (c.getType().equals("EN")) {
                                        ArrayList<String> leftNP = new ArrayList<>();
                                        ArrayList<String> rightNP = new ArrayList<>();
                                        endStrList = new ArrayList<>();
                                        startStrList = new ArrayList<>();
                                        leftNP.add("B");
                                        leftNP.add(Integer.toString(c.getFirstToken().getId()));
                                        leftNP.add("E");
                                        startStrList.add(c.toBareString());
                                        // remove all other chunks
                                        structure.Chunk newSentNP = new structure.Chunk(sent.toString());
                                        String origRootStrNP = newSentNP.toBareString();
                                        rightNP.add("B");
                                        ArrayList<structure.Chunk> toRemoveNP = new ArrayList<>();
                                        for (int k = 0; k < chunks.size(); k++) {
                                            if (k == j) {
                                                rightNP.add(Integer.toString(c.getFirstToken().getId()));
                                                endStrList.add(chunks.get(k).toBareString());
                                                continue;
                                            }
                                            rightNP.add(chunks.get(k).toString());
                                            endStrList.add(chunks.get(k).toBareString());
                                            toRemoveNP.add(chunks.get(k));
                                        }
                                        rightNP.add("E");
                                        String endStrNP = ArrayListUtils.stringListToString(endStrList, " ");
                                        String startStrNP = ArrayListUtils.stringListToString(startStrList, " ");
                                        for (int k = toRemoveNP.size() - 1; k >= 0; k--) {
                                            newSentNP.removeChunk(toRemoveNP.get(k), true);
                                        }
                                        if (!newSentNP.toBareString().equals("")) {
                                            if (rightNP.size() - leftNP.size() <= 5) {
                                                cap.addRule(new RewriteRule(cap.getRules().size(), leftNP, rightNP, "+SENT"), origRootStrNP);
                                                endStrings.add(endStrNP);
                                                startStrings.add(startStrNP);
                                                modifiedCaps.add(newSentNP.toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (!newSent.toBareString().equals("")) {
                            if (right.size() - left.size() <= 5) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+COMPLEX"), origRootStr);
                                endStrings.add(endStr);
                                startStrings.add(startStr);
                                modifiedCaps.add(newSent.toString());

                                // add NP captions
                                for (int j = 0; j < chunks.size(); j++) {
                                    if (j == subj || (j >= vp && j <= vpE)) {
                                        continue;
                                    }
                                    structure.Chunk c = chunks.get(j);
                                    if (c.getType().equals("EN")) {
                                        ArrayList<String> leftNP = new ArrayList<>();
                                        ArrayList<String> rightNP = new ArrayList<>();
                                        endStrList = new ArrayList<>();
                                        startStrList = new ArrayList<>();
                                        leftNP.add("B");
                                        leftNP.add(Integer.toString(c.getFirstToken().getId()));
                                        leftNP.add("E");
                                        startStrList.add(c.toBareString());
                                        // remove all other chunks
                                        structure.Chunk newSentNP = new structure.Chunk(sent.toString());
                                        String origRootStrNP = newSentNP.toBareString();
                                        rightNP.add("B");
                                        ArrayList<structure.Chunk> toRemoveNP = new ArrayList<>();
                                        for (int k = 0; k < chunks.size(); k++) {
                                            if (k == j) {
                                                rightNP.add(Integer.toString(c.getFirstToken().getId()));
                                                endStrList.add(chunks.get(k).toBareString());
                                                continue;
                                            }
                                            rightNP.add(chunks.get(k).toString());
                                            endStrList.add(chunks.get(k).toBareString());
                                            toRemoveNP.add(chunks.get(k));
                                        }
                                        rightNP.add("E");
                                        String endStrNP = ArrayListUtils.stringListToString(endStrList, " ");
                                        String startStrNP = ArrayListUtils.stringListToString(startStrList, " ");
                                        for (int k = toRemoveNP.size() - 1; k >= 0; k--) {
                                            newSentNP.removeChunk(toRemoveNP.get(k), true);
                                        }
                                        if (!newSentNP.toBareString().equals("")) {
                                            if (rightNP.size() - leftNP.size() <= 5) {
                                                cap.addRule(new RewriteRule(cap.getRules().size(), leftNP, rightNP, "+SENT"), origRootStrNP);
                                                endStrings.add(endStrNP);
                                                startStrings.add(startStrNP);
                                                modifiedCaps.add(newSentNP.toString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                chunks = new ArrayList<>();
                if (svoSent.getType().equals("SENT")) {
                    chunks = svoSent.getChunks();
                } else {
                    chunks.add(svoSent);
                }
                subj = newSubj;
                vp = newVp;
                vpE = newVpE;
                dobj = newDobj;
                ArrayList<String> simpleSentList = new ArrayList<>();
                simpleSentList.add("B");
                for (structure.Chunk c : chunks) {
                    simpleSentList.add(Integer.toString(c.getFirstToken().getId()));
                }
                simpleSentList.add("E");
                simpleSentStrings.add(ArrayListUtils.stringListToString(simpleSentList, " "));

                // if there is subject and VP, generate rules to split them
                if (split == 3) {
                    // generate rule to grab subject
                    left = new ArrayList<>();
                    right = new ArrayList<>();
                    newSent = new structure.Chunk(svoSent.toString());
                    endStrList = new ArrayList<>();
                    startStrList = new ArrayList<>();
                    left.add("B");
                    right.add("B");
                    toRemove = new ArrayList<>();
                    for (int j = 0; j < chunks.size(); j++) {
                        String tokenIdStr = Integer.toString(chunks.get(j).getFirstToken().getId());
                        if (j == subj) {
                            left.add(tokenIdStr);
                            right.add(tokenIdStr);
                            endStrList.add(chunks.get(j).toBareString());
                            startStrList.add(chunks.get(j).toBareString());
                        } else if (j == vp) {
                            right.add(chunks.get(j).toString());
                            endStrList.add(chunks.get(j).toBareString());
                            toRemove.add(chunks.get(j));
                        } else {
                            toRemove.add(chunks.get(j));
                        }
                    }
                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    left.add("E");
                    right.add("E");
                    endStr = ArrayListUtils.stringListToString(endStrList, " ");
                    startStr = ArrayListUtils.stringListToString(startStrList, " ");
                    if (!newSent.toBareString().equals("")) {
                        if (right.size() - left.size() <= 5) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+VERB"), origRootStr);

                            endStrings.add(endStr);
                            startStrings.add(startStr);
                            modifiedCaps.add(newSent.toString());
                            // check if we need to create rewrite rules for hyponyms
                            String rightStr = ArrayListUtils.stringListToString(right, " ");
                            for (String hypernym : cap.getHypernyms().keySet()) {
                                if (rightStr.contains(hypernym)) {
                                    HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                    for (String hyponym : hyponyms) {
                                        String rightStrNew = rightStr.replace(hypernym, hyponym);
                                        cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+VERB"), origRootStr);
                                    }
                                }
                            }
                        }
                    }

                    // generate rule to grab VP
                    left = new ArrayList<>();
                    right = new ArrayList<>();
                    newSent = new structure.Chunk(svoSent.toString());
                    endStrList = new ArrayList<>();
                    startStrList = new ArrayList<>();
                    verb = "";
                    left.add("B");
                    right.add("B");
                    toRemove = new ArrayList<>();
                    for (int j = 0; j < chunks.size(); j++) {
                        String tokenIdStr = Integer.toString(chunks.get(j).getFirstToken().getId());
                        if (j == subj) {
                            right.add(chunks.get(j).toString());
                            endStrList.add(chunks.get(j).toBareString());
                            toRemove.add(chunks.get(j));
                        } else if (j == vp) {
                            left.add(tokenIdStr);
                            right.add(tokenIdStr);
                            endStrList.add(chunks.get(j).toBareString());
                            startStrList.add(chunks.get(j).toBareString());
                            if (j == vp) {
                                verb = chunks.get(j).getLastToken().getPrevToken().getStr();
                            }
                        } else if (j == dobj) {
                            toRemove.add(chunks.get(j));
                        }
                    }
                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    endStr = ArrayListUtils.stringListToString(endStrList, " ");
                    startStr = ArrayListUtils.stringListToString(startStrList, " ");
                    if (!verb.equals("be")) {
                        if (!newSent.toBareString().equals("")) {
                            if (right.size() - left.size() <= 5) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+SUBJ"), origRootStr);

                                startStrings.add(startStr);
                                endStrings.add(endStr);
                                modifiedCaps.add(newSent.toString());
                                // check if we need to create rewrite rules for hyponyms
                                String rightStr = ArrayListUtils.stringListToString(right, " ");
                                for (String hypernym : cap.getHypernyms().keySet()) {
                                    if (rightStr.contains(hypernym)) {
                                        HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                        for (String hyponym : hyponyms) {
                                            String rightStrNew = rightStr.replace(hypernym, hyponym);
                                            cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+SUBJ"), origRootStr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // if there is VP and it includes direct object, generate rules to split them
                if ((split & 2) != 0 && dobj >= 0) {
                    // generate rule to grab dobj from VP
                    left = new ArrayList<>();
                    right = new ArrayList<>();
                    newSent = new structure.Chunk(svoSent.toString());
                    endStrList = new ArrayList<>();
                    startStrList = new ArrayList<>();
                    left.add("B");
                    right.add("B");
                    toRemove = new ArrayList<>();
                    for (int j = 0; j < chunks.size(); j++) {
                        if (j >= vp && j <= vpE) { // inside VP (might be multiple chunks, incl. dobj)
                            if (j == dobj) {
                                int k = j;
                                String tokenIdStr = Integer.toString(chunks.get(k).getFirstToken().getId());
                                left.add(tokenIdStr);
                                right.add(tokenIdStr);
                                endStrList.add(chunks.get(j).toBareString());
                                startStrList.add(chunks.get(j).toBareString());
                                ArrayList<structure.Chunk> jChunks = chunks.get(j).getChunks();
                                for (k = 0; k < jChunks.size(); k++) {
                                    tokenIdStr = Integer.toString(jChunks.get(k).getFirstToken().getId());
                                    left.add(tokenIdStr);
                                    right.add(tokenIdStr);
                                }
                                tokenIdStr = Integer.toString(chunks.get(j).getLastToken().getId());
                                left.add(tokenIdStr);
                                right.add(tokenIdStr);
                            } else {
                                right.add(chunks.get(j).toString());
                                endStrList.add(chunks.get(j).toBareString());
                                toRemove.add(chunks.get(j));
                            }
                        } else {
                            toRemove.add(chunks.get(j));
                        }
                    }
                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    left.add("E");
                    right.add("E");
                    endStr = ArrayListUtils.stringListToString(endStrList, " ");
                    startStr = ArrayListUtils.stringListToString(startStrList, " ");
                    if (!newSent.toBareString().equals("")) {
                        if (right.size() - left.size() <= 5) {
                            cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+TVERB"), endStr);

                            endStrings.add(endStr);
                            startStrings.add(startStr);
                            modifiedCaps.add(newSent.toString());
                            // check if we need to create rewrite rules for hyponyms
                            String rightStr = ArrayListUtils.stringListToString(right, " ");
                            for (String hypernym : cap.getHypernyms().keySet()) {
                                if (rightStr.contains(hypernym)) {
                                    HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                    for (String hyponym : hyponyms) {
                                        String rightStrNew = rightStr.replace(hypernym, hyponym);
                                        cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+TVERB"), origRootStr);
                                    }
                                }
                            }
                        }
                    }

                    // generate rule to grab verb (including particle) from VP chunk
                    left = new ArrayList<>();
                    right = new ArrayList<>();
                    newSent = new structure.Chunk(svoSent.toString());
                    endStrList = new ArrayList<>();
                    startStrList = new ArrayList<>();
                    verb = "";
                    toRemove = new ArrayList<>();
                    for (int j = 0; j < chunks.size(); j++) {
                        String tokenIdStr = Integer.toString(chunks.get(j).getFirstToken().getId());
                        if (j >= vp && j <= dobj) {
                            if (j < dobj) {
                                left.add(tokenIdStr);
                                right.add(tokenIdStr);
                                endStrList.add(chunks.get(j).toBareString());
                                startStrList.add(chunks.get(j).toBareString());
                                if (chunks.get(j).getTokens().size() > 1) {
                                    verb = chunks.get(j).getLastToken().getPrevToken().getStr();
                                } else {
                                    verb = chunks.get(j).getLastToken().getStr();
                                }
                            } else {
                                right.add(chunks.get(j).toString());
                                endStrList.add(chunks.get(j).toBareString());
                                toRemove.add(chunks.get(j));
                            }
                        } else {
                            toRemove.add(chunks.get(j));
                        }
                    }
                    for (int j = toRemove.size() - 1; j >= 0; j--) {
                        newSent.removeChunk(toRemove.get(j), true);
                    }
                    left.add("E");
                    right.add("E");
                    endStr = ArrayListUtils.stringListToString(endStrList, " ");
                    startStr = ArrayListUtils.stringListToString(startStrList, " ");
                    if (!verb.equals("be")) {
                        if (!newSent.toBareString().equals("")) {
                            if (right.size() - left.size() <= 5) {
                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+DOBJ"), endStr);
                                endStrings.add(endStr);
                                startStrings.add(startStr);
                                modifiedCaps.add(newSent.toString());
                                // check if we need to create rewrite rules for hyponyms
                                String rightStr = ArrayListUtils.stringListToString(right, " ");
                                for (String hypernym : cap.getHypernyms().keySet()) {
                                    if (rightStr.contains(hypernym)) {
                                        HashSet<String> hyponyms = cap.getHyponyms(hypernym);
                                        for (String hyponym : hyponyms) {
                                            String rightStrNew = rightStr.replace(hypernym, hyponym);
                                            cap.addRule(new RewriteRule(cap.getRules().size(), ArrayListUtils.stringListToString(left, " "), rightStrNew, "+DOBJ"), endStr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // if VP, check if it contains "TO" and needs to be split up
                if ((split & 2) != 0) {
                    String seenTokens = ""; // tokens on the left side of TO
                    // look for TOs in VP chunk
                    ArrayList<MyToken> vpTokens = chunks.get(vp).getTokens();
                    for (int j = 0; j < vpTokens.size(); j++) {
                        MyToken t = vpTokens.get(j);
                        String tokenStr = t.getStr();
                        if (!tokenStr.startsWith("[") && !tokenStr.startsWith("]")) { // not opening or closing bracket
                            if (t.getPos().equals("TO")) {
                                // see if left side of TO indicates it should be split
                                if (!seenTokens.equals("")) {
                                    seenTokens = seenTokens.substring(0, seenTokens.length() - 1);
                                }
                                if (splitTo.get(seenTokens) != null) {
                                    if ((splitTo.get(seenTokens) & 1) != 0) {
                                        // generate rule to drop "X to"
                                        left = new ArrayList<>();
                                        right = new ArrayList<>();
                                        newSent = new structure.Chunk(svoSent.toString());
                                        String origRootStrSVO = newSent.toBareString();
                                        endStrList = new ArrayList<>();
                                        startStrList = new ArrayList<>();
                                        String vpTokenStr = Integer.toString(chunks.get(vp).getFirstToken().getId());
                                        left.add(vpTokenStr);
                                        right.add(vpTokenStr);
                                        endStrList.add(chunks.get(vp).toBareString());
                                        if (dobj > -1) {
                                            newSent.removeChunk(chunks.get(dobj), true);
                                        }
                                        if (subj > -1) {
                                            newSent.removeChunk(chunks.get(subj), true);
                                        }
                                        for (int k = 1; k <= j; k++) {
                                            right.add(vpTokens.get(k).toString());
                                            if (vpTokens.get(k).isWord()) {
                                                newSent.removeToken(vpTokens.get(k));
                                            }
                                        }
                                        for (int k = j + 1; k < vpTokens.size(); k++) {
                                            String kTokenStr = Integer.toString(vpTokens.get(k).getId());
                                            left.add(kTokenStr);
                                            right.add(kTokenStr);
                                            if (vpTokens.get(k).isWord()) {
                                                startStrList.add(vpTokens.get(k).getStr());
                                            }
                                        }
                                        endStr = ArrayListUtils.stringListToString(endStrList, " ");
                                        startStr = ArrayListUtils.stringListToString(startStrList, " ");
                                        if (!newSent.toBareString().equals("")) {
                                            if (right.size() - left.size() <= 5) {
                                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+Xto"), origRootStrSVO);
                                                endStrings.add(endStr);
                                                startStrings.add(startStr);
                                                modifiedCaps.add(newSent.toString());
                                            }
                                        }
                                    }

                                    if ((splitTo.get(seenTokens) & 2) != 0) {
                                        // generate rule to drop "to Y"
                                        left = new ArrayList<>();
                                        right = new ArrayList<>();
                                        newSent = new structure.Chunk(svoSent.toString());
                                        String origRootStrN = newSent.toBareString();
                                        endStrList = new ArrayList<>();
                                        startStrList = new ArrayList<>();
                                        String vpTokenStr = Integer.toString(chunks.get(vp).getFirstToken().getId());
                                        left.add(vpTokenStr);
                                        right.add(vpTokenStr);
                                        endStrList.add(chunks.get(vp).toBareString());
                                        startStrList.add(chunks.get(vp).toBareString());
                                        if (dobj > -1) {
                                            newSent.removeChunk(chunks.get(dobj), true);
                                        }
                                        if (subj > -1) {
                                            newSent.removeChunk(chunks.get(subj), true);
                                        }
                                        for (int k = 1; k < j; k++) {
                                            String kTokenStr = Integer.toString(vpTokens.get(k).getId());
                                            left.add(kTokenStr);
                                            right.add(kTokenStr);
                                            endStrList.add(vpTokens.get(k).toString());
                                            startStrList.add(vpTokens.get(k).toString());
                                        }
                                        for (int k = j; k < vpTokens.size() - 1; k++) {
                                            right.add(vpTokens.get(k).toString());
                                            endStrList.add(vpTokens.get(k).toString());
                                            newSent.removeToken(vpTokens.get(k));
                                        }
                                        vpTokenStr = Integer.toString(chunks.get(vp).getLastToken().getId());
                                        left.add(vpTokenStr);
                                        right.add(vpTokenStr);
                                        endStrList.add(chunks.get(vp).toBareString());
                                        startStrList.add(chunks.get(vp).toBareString());

                                        // if there is direct object, drop it ("X to Y DOBJ" -> "X" not "X DOBJ")
                                        // we need subject/verb split rules that don't involve a direct object since it can disappear when we drop "to Y" peter note
                                        if (dobj >= 0) {
                                            // if subject, generate rules for new split
                                            if (split == 3) {
                                                // generate SUBJ Y -> SUBJ rule
                                                ArrayList<String> leftY = new ArrayList<>();
                                                ArrayList<String> rightY = new ArrayList<>();
                                                ArrayList<String> endStrYList = new ArrayList<>();
                                                ArrayList<String> startStrYList = new ArrayList<>();
                                                Chunk newNewSent = new structure.Chunk(svoSent.toString());
                                                String origRootStrNew = newNewSent.toBareString();
                                                leftY.add("B");
                                                rightY.add("B");
                                                ArrayList<structure.Chunk> toRemoveY = new ArrayList<>();
                                                for (int k = 0; k < chunks.size(); k++) {
                                                    if (k == subj) {
                                                        String kTokenStr = Integer.toString(chunks.get(k).getFirstToken().getId());
                                                        leftY.add(kTokenStr);
                                                        rightY.add(kTokenStr);
                                                        endStrYList.add(chunks.get(k).toBareString());
                                                        startStrYList.add(chunks.get(k).toBareString());
                                                    } else if (k == vp) {
                                                        rightY.add(chunks.get(k).toString());
                                                        endStrYList.add(chunks.get(k).toBareString());
                                                        toRemoveY.add(chunks.get(k));
                                                    } else {
                                                        toRemoveY.add(chunks.get(k));
                                                    }
                                                }
                                                for (int k = toRemoveY.size() - 1; k >= 0; k--) {
                                                    newNewSent.removeChunk(toRemoveY.get(k), true);
                                                }
                                                leftY.add("E");
                                                rightY.add("E");
                                                String endStrY = ArrayListUtils.stringListToString(endStrYList, " ");
                                                String startStrY = ArrayListUtils.stringListToString(startStrYList, " ");
                                                if (!newNewSent.toBareString().equals("")) {
                                                    if (rightY.size() - leftY.size() <= 5) {
                                                        cap.addRule(new RewriteRule(cap.getRules().size(), leftY, rightY, "+VERB"), origRootStrNew);
                                                        endStrings.add(endStrY);
                                                        startStrings.add(startStrY);
                                                        modifiedCaps.add(newNewSent.toString());
                                                    }
                                                }

                                                // generate SUBJ Y -> Y rule
                                                leftY = new ArrayList<>();
                                                rightY = new ArrayList<>();
                                                newNewSent = new structure.Chunk(svoSent.toString());
                                                origRootStrNew = newNewSent.toBareString();
                                                endStrYList = new ArrayList<>();
                                                startStrYList = new ArrayList<>();
                                                leftY.add("B");
                                                rightY.add("B");
                                                toRemoveY = new ArrayList<>();
                                                for (int k = 0; k < chunks.size(); k++) {
                                                    if (k == subj) {
                                                        rightY.add(chunks.get(k).toString());
                                                        endStrYList.add(chunks.get(k).toBareString());
                                                        startStrYList.add(chunks.get(k).toBareString());
                                                        toRemoveY.add(chunks.get(k));
                                                    } else if (k == vp) {
                                                        for (String leftTok : left) {
                                                            leftY.add(leftTok);
                                                            rightY.add(leftTok);
                                                        }
                                                        endStrYList.add(chunks.get(k).toBareString());
                                                    } else {
                                                        toRemoveY.add(chunks.get(k));
                                                    }
                                                }
                                                for (int k = toRemoveY.size() - 1; k >= 0; k--) {
                                                    newNewSent.removeChunk(toRemoveY.get(k), true);
                                                }
                                                leftY.add("E");
                                                rightY.add("E");
                                                endStrY = ArrayListUtils.stringListToString(endStrYList, " ");
                                                startStrY = ArrayListUtils.stringListToString(startStrYList, " ");
                                                if (!newNewSent.toBareString().equals("")) {
                                                    if (rightY.size() - leftY.size() <= 5) {
                                                        cap.addRule(new RewriteRule(cap.getRules().size(), leftY, rightY, "+SUBJ"), origRootStrNew);
                                                        endStrings.add(endStrY);
                                                        startStrings.add(startStrY);
                                                        modifiedCaps.add(newNewSent.toString());
                                                    }
                                                }
                                            }

                                            // add direct object - technically wrong, doesn't go far enough, but seems to work
                                            left.add(Integer.toString(chunks.get(vp).getNextChunk().getFirstToken().getId()));
                                            left.add("E");
                                            right.add("E");
                                            endStr = ArrayListUtils.stringListToString(endStrList, " ");
                                            startStr = ArrayListUtils.stringListToString(startStrList, " ");
                                        }
                                        if (!newSent.toBareString().equals("")) {
                                            if (right.size() - left.size() <= 5) {
                                                cap.addRule(new RewriteRule(cap.getRules().size(), left, right, "+toY"), origRootStrN);
                                                endStrings.add(endStr);
                                                startStrings.add(startStr);
                                                modifiedCaps.add(newSent.toString());
                                            }
                                        }
                                    }
                                }
                            }
                            seenTokens += t.getStr() + " ";
                        }
                    }
                }
            }

            boolean rootsAdded = false;
            for (int i = 0; i < modifiedCaps.size(); i++) {
                String startStr = startStrings.get(i);
                boolean keep = true;
                for (int k = 0; k < modifiedCaps.size(); k++) {
                    if (i == k) {
                        continue;
                    }
                    if (startStr.equals(endStrings.get(k))) {
                        keep = false;
                        break;
                    } else if (simpleSentStrings.contains(startStr)) {
                        keep = false;
                        break;
                    } else if (modifiedCaps.get(i).equals(modifiedCaps.get(k)) && i < k) {
                        keep = false;
                        break;
                    }
                }
                if (keep) {
                    newRoots.add(modifiedCaps.get(i));
                    rootsAdded = true;
                }
            }
            if (rootsAdded) {
                toRemoveRoots.add(rootIdx);
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
        cap.setSent("");
        return cap;
    }
}
