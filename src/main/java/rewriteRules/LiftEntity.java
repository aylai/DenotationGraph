package rewriteRules;

import structure.Chunk;
import structure.RewriteCaption;
import structure.RewriteRule;
import utils.ArrayListUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collections;

/**
 * Replaces words with their hypernyms
 * @author aylai2
 */
public class LiftEntity {

    /**
     * Terms for people that can also be objects.
     * Assume subjects are people, non-subjects are objects;
     * use lexicon on these terms only if they're subjects.
     */
    private static HashSet<String> peopleSubj;

    /**
     * List of age terms that can be dropped if they are the first word
     * of a multi-word head noun to form a more generic head noun.
     */
    private static HashSet<String> ageTerms;

    /**
     * List of subjects identified for all captions
     */
    private static HashSet<String> subjects;

    /**
     * Hypernym lexicon, created using Perl hash ordering
     */
    private static HashMap<String, ArrayList<String>> lexicon;

    private static void readPeopleSubj() {
        peopleSubj = new HashSet<>();
        peopleSubj.add("batter");
        peopleSubj.add("diner");
        peopleSubj.add("pitcher");
        peopleSubj.add("speaker");
    }

    private static void readAgeTerms() {
        ageTerms = new HashSet<>();
        ageTerms.add("adult");
        ageTerms.add("baby");
        ageTerms.add("child");
        ageTerms.add("teen");
        ageTerms.add("toddler");
    }

    /**
     * Read all caption subjects from file
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
     * Read hypernym lexicon from file and apply my recreation of the lexicon word ordering
     */
    private static void readLexicon(String dir, String corpus) {
        // read lexicon file
        lexicon = new HashMap<>();
        File lexiconFile = new File(dir+"/"+corpus+"/"+corpus+".lexicon");
        if (!lexiconFile.exists()) {
            lexiconFile = new File("preprocessing/graph/data/lexiconNew.txt");
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(lexiconFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                String word = tokens[0];
                String hypernym = tokens[1];
                lexicon.putIfAbsent(word, new ArrayList<>());
                lexicon.get(word).add(hypernym);
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Replaces head nouns with their hypernym(s) according to previously computed hypernym lexicon
     * @param cap Caption to be modified
     * @return Modified caption with applicable rules added
     */
    public static RewriteCaption applyRule(String dir, String corpusDir, String corpus, RewriteCaption cap) {
        boolean multipleHypernyms = false;
        ArrayList<String> hypernymCandidates = new ArrayList<>();
        HashSet<RewriteRule> addedRules = new HashSet<>();
        if (peopleSubj == null) {
            readPeopleSubj();
        }
        if (ageTerms == null) {
            readAgeTerms();
        }
        if (subjects == null) {
            readSubjects(corpusDir, corpus);
        }
        if (lexicon == null) {
            readLexicon(dir, corpus);
        }
        ArrayList<String> newRoots = new ArrayList<>();
        HashSet<Integer> toRemoveRoots = new HashSet<>();

        for (int rootIdx = 0; rootIdx < cap.getRoots().size(); rootIdx++) {
            Chunk sent = cap.getRoots().get(rootIdx);
            String origRootStr = sent.toBareString();
            int nextTokenId = cap.getNextTokenId();
            ArrayList<Chunk> chunks = new ArrayList<>();
            if (sent.getType().equals("SENT")) {
                chunks = sent.getChunks();
            } else {
                chunks.add(sent);
            }

            HashMap<String, String> visit;
            ArrayList<String> queue;
            HashMap<String, String> mostGeneric;
            for (Chunk c : chunks) {
                // look for [EN [NP [NPH ] ] ]
                if (!c.getType().equals("EN")) {
                    continue;
                }
                String entityId = cap.getId() + "#" + c.getId();
                ArrayList<Chunk> children1 = c.getChunks();
                for (Chunk c1 : children1) {
                    if (!c1.getType().equals("NP")) {
                        continue;
                    }
                    ArrayList<Chunk> children2 = c1.getChunks();
                    for (Chunk c2 : children2) {
                        if (!c2.getType().equals("NPH")) {
                            continue;
                        }
                        String genericHeadNoun = "";
                        String headNoun = c2.toBareString(); // words of this chunk
                        String headNounMeta = ArrayListUtils.stringListToString(c2.toStringListInner(), " "); // idx/word/pos of this chunk
                        // if the head noun is an ambiguous person term and is not a subject, ignore it
                        if (peopleSubj.contains(headNoun) && !subjects.contains(entityId)) {
                            continue;
                        }

                        // initialize variables
                        int changed = 0; // number of times we have rewritten NPH chunk
                        visit = new HashMap<>(); // the set of strings we've already generated, with string -> string + metadata (token IDs). Also includes generated rewrite rules
                        queue = new ArrayList<>(); // rewrite rules that we want to consider, in the form "<left side>\t<right side>"
                        mostGeneric = new HashMap<>();

                        visit.put(headNoun, headNounMeta); // we've visited original string
                        mostGeneric.put(headNoun, headNounMeta);
                        // add all hypernyms of headNoun to queue
                        if (lexicon.get(headNoun) != null) {
                            ArrayList<String> hypernyms = lexicon.get(headNoun);
                            queue.addAll(hypernyms.stream().map(h -> h + "\t" + headNoun).collect(Collectors.toList()));
                            mostGeneric.remove(headNoun);
                        }

                        // if first word is an age term, we can go to the more generic term by dropping age term
                        // e.g. 'adult teacher' -> 'teacher'
                        String[] tokens = headNoun.split("\\s+");
                        if (ageTerms.contains(tokens[0]) && tokens.length > 1) {
                            queue.add(tokens[0] + "\t" + headNoun);
                            mostGeneric.remove(headNoun);
                        }

                        for (int i = 0; i < queue.size(); i++) {
                            String item = queue.get(i);
                            // retrieve next rewrite rule to process
                            tokens = item.split("\t");
                            String left = tokens[0];
                            String right = tokens[1];

                            // process this rewrite rule
                            visit.put(item, "1");
                            changed++;

                            // we haven't seen left string before, we need to assign new token IDs
                            if (visit.get(left) == null) {
                                String metaTemp = "";
                                String[] tokensTemp = left.split("\\s+");
                                for (String token : tokensTemp) {
                                    metaTemp += nextTokenId + "/" + token + "/NN ";
                                    nextTokenId++;
                                }
                                metaTemp = metaTemp.substring(0, metaTemp.length() - 1);
                                visit.put(left, metaTemp);
                            }

                            // get left side sequence of token IDs
                            ArrayList<String> origChunk = new ArrayList<>();
                            origChunk.add(c2.getFirstToken().toString());
                            ArrayList<String> hypernymChunk = new ArrayList<>();
                            hypernymChunk.add(c2.getFirstToken().toString());
                            String leftTokenIds = "";
                            String leftMeta = visit.get(left);
                            mostGeneric.put(left, leftMeta);
                            genericHeadNoun = leftMeta;
                            tokens = leftMeta.split("\\s+");
                            for (String token : tokens) {
                                String[] temp = token.split("/");
                                leftTokenIds += temp[0] + " ";
                                origChunk.add(token);
                            }
                            leftTokenIds = leftTokenIds.substring(0, leftTokenIds.length() - 1);

                            // add "[NPH" token ID and closing bracket token ID to rules
                            int startTokenId = c2.getFirstToken().getId();
                            int endTokenId = c2.getLastToken().getId();
                            origChunk.add(c2.getLastToken().toString());
                            hypernymChunk.add(visit.get(right));
                            hypernymChunk.add(c2.getLastToken().toString());
                            String leftStr = startTokenId + " " + leftTokenIds + " " + endTokenId;
                            String rightStr = startTokenId + " " + visit.get(right) + " " + endTokenId;

                            // create new rewrite rule
                            int numRules = cap.getRules().size();
                            RewriteRule newRule = new RewriteRule(numRules, leftStr, rightStr, "+NPHEAD/" + right);
                            if (!addedRules.contains(newRule)) {
                                cap.addRule(newRule, origRootStr);
                                addedRules.add(newRule);
                            }
                            // add hypernym
                            cap.addHypernym(ArrayListUtils.stringListToString(origChunk, " "), ArrayListUtils.stringListToString(hypernymChunk, " "));

                            // identify new possible rewrite rules: add all hypernyms of left to queue
                            if (lexicon.get(left) != null) {
                                ArrayList<String> hypernyms = lexicon.get(left);
                                queue.addAll(hypernyms.stream().map(h -> h + "\t" + left).collect(Collectors.toList()));
                                mostGeneric.remove(left);
                            }
                        }
                        // if we applied any rewrite rules, replace NPH chunk in caption with more generic head noun
                        if (changed > 0) {
                            if (mostGeneric.size() > 1) {
                                multipleHypernyms = true;
                                for (String words : mostGeneric.keySet()) {
                                    hypernymCandidates.add(ArrayListUtils.stringListToString(c2.toStringListInner(), " ") + "\t" + mostGeneric.get(words));
                                }
                            } else {
                                sent.replaceTokens(c2.getStartIdx() + 1, c2.getEndIdx() - 1, genericHeadNoun.split("\\s+"));
                            }
                        }
                    }
                }
            }
            cap.setNextTokenId(nextTokenId);
            cap.setHypernyms(LiftEntity.combineHypernyms(cap.getHypernyms()));
            if (multipleHypernyms) {
                // create multiple captions for each root hypernym
                String capStr = sent.toString();
                for (String candidate : hypernymCandidates) {
                    String[] temp = candidate.split("\t");
                    newRoots.add(capStr.replace(temp[0], temp[1]));
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
        for (String newRoot : newRoots) {
            cap.addRoot(newRoot);
        }
        return cap;
    }

    private static HashMap<String, HashSet<String>> combineHypernyms(HashMap<String, HashSet<String>> oldHypernyms) {
        HashSet<String> toRemove = new HashSet<>();
        for (String hypernym : oldHypernyms.keySet()) {
            boolean remove = false;
            for (String hypernym2 : oldHypernyms.keySet()) {
                if (toRemove.contains(hypernym2)) {
                    continue;
                }
                if (oldHypernyms.get(hypernym2).contains(hypernym)) {
                    remove = true;
                    oldHypernyms.get(hypernym2).addAll(oldHypernyms.get(hypernym));
                }
            }
            if (remove) {
                toRemove.add(hypernym);
            }
        }
        toRemove.forEach(oldHypernyms::remove);
        return oldHypernyms;
    }
}
