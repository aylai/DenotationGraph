package structure;

import utils.ArrayListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Caption in chunk form with any rules that have been applied to it
 * @author aylai2
 */
public class RewriteCaption {

    private String id;
    private int nextTokenId; /* for rewrite rule generation, index the next new token */
    private ArrayList<Chunk> rootSentences;
    private Chunk sentence;
    private ArrayList<RewriteRule> rules;
    private HashSet<RewriteRule> ruleSet;
    private HashSet<String> rootSet;
    private int expansionLimit; /* for graph expansion, how many times to apply rewrite rules to this caption */
    private HashMap<String, HashSet<String>> hypernyms;

    public RewriteCaption(String id, int nextTokenId, String currentString) {
        this.id = id;
        this.sentence = new Chunk(currentString);
        rootSentences = new ArrayList<>();
        rootSentences.add(this.sentence);
        rootSet = new HashSet<>();
        rootSet.add(this.sentence.toString());
        this.nextTokenId = nextTokenId;
        rules = new ArrayList<>();
        ruleSet = new HashSet<>();
        expansionLimit = 0;
        hypernyms = new HashMap<>();
    }

    public RewriteCaption(String id, int nextTokenId, String currentString, HashMap<String, HashSet<String>> hypernyms) {
        this.id = id;
        this.sentence = new Chunk(currentString);
        this.nextTokenId = nextTokenId;
        rules = new ArrayList<>();
        ruleSet = new HashSet<>();
        expansionLimit = 0;
        this.hypernyms = hypernyms;
        rootSentences = new ArrayList<>();
        rootSet = new HashSet<>();
    }

    public RewriteCaption(String id, int nextTokenId, Chunk c) {
        this.id = id;
        this.sentence = c;
        this.nextTokenId = nextTokenId;
        rules = new ArrayList<>();
        ruleSet = new HashSet<>();
        expansionLimit = 0;
        hypernyms = new HashMap<>();
        rootSentences = new ArrayList<>();
        rootSet = new HashSet<>();
    }

    public RewriteCaption(String id, int nextTokenId, Chunk c, HashMap<String, HashSet<String>> hypernyms) {
        this.id = id;
        this.sentence = c;
        this.nextTokenId = nextTokenId;
        rules = new ArrayList<>();
        ruleSet = new HashSet<>();
        expansionLimit = 0;
        this.hypernyms = hypernyms;
        rootSentences = new ArrayList<>();
        rootSet = new HashSet<>();
    }

    public void addRoot(String sent) {
        if (!rootSet.contains(sent)) {
            rootSentences.add(new Chunk(sent));
            rootSet.add(sent);
        }
    }

    void addRoots(ArrayList<Chunk> sentList) {
        for (Chunk sent : sentList) {
            if (!rootSet.contains(sent)) {
                rootSentences.add(sent);
                rootSet.add(sent.toString());
            }
        }
    }

    public void replaceRoot(Chunk sent, int idx) { rootSentences.set(idx, sent); }

    public void removeRoot(int rootIdx) { rootSentences.remove(rootIdx); }

    public ArrayList<Chunk> getRoots() {
        return rootSentences;
    }

    public void setHypernyms(HashMap<String, HashSet<String>> hypernyms) {
        this.hypernyms = hypernyms;
    }

    public void addHypernym(String chunk, String hypernym) {
        hypernyms.putIfAbsent(chunk, new HashSet<>());
        hypernyms.get(chunk).add(hypernym);
    }

    public boolean hasHyponyms(String chunk) {
        return hypernyms.get(chunk) != null && hypernyms.get(chunk).size() > 0;
    }

    public HashMap<String, HashSet<String>> getHypernyms() {
        return hypernyms;
    }

    public HashSet<String> getHyponyms(String chunk) {
        if (hasHyponyms(chunk)) {
            return hypernyms.get(chunk);
        }
        return new HashSet<>();
    }

    void setExpansionLimit(int exp) {
        expansionLimit = exp;
    }

    int getExpansionLimit() {
        return expansionLimit;
    }

    public String getId() {
        return id;
    }

    public int getNextTokenId() {
        return nextTokenId;
    }

    public void setNextTokenId(int i) {
        nextTokenId = i;
    }

    public void addRule(RewriteRule rule, String resultSent) {
        if (!ruleSet.contains(rule) && !isBadString(resultSent)) {
            rules.add(rule);
            ruleSet.add(rule);
        }
    }

    private boolean isBadString(String s) {
        if (s.equals("")) {
            return true;
        }
        String[] tokens = s.split("\\s+");
        if (tokens.length == 2 && tokens[0].equals(tokens[1])) {
            return true;
        }
        else if (tokens[tokens.length-1].equals("and")) {
            return true;
        }
        return false;
    }

    void addRules(ArrayList<RewriteRule> ruleList) {
        for (RewriteRule rule : ruleList) {
            if (!ruleSet.contains(rule)) {
                rules.add(rule);
                ruleSet.add(rule);
            }
        }
    }

    public Chunk getSent() {
        return sentence;
    }

    public void setSent(String newString) {
        sentence = new Chunk(newString);
    }

    public void setSent(Chunk sent) {
        sentence = sent;
    }

    public ArrayList<RewriteRule> getRules() {
        return rules;
    }

    private void setRules(ArrayList<RewriteRule> ruleList) {
        rules = ruleList;
    }

    @Override
    public boolean equals(Object o) {
        RewriteCaption r = (RewriteCaption) o;
        return this.toString().equals(r.toString());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        String temp = "";
        temp += id + "\t" + nextTokenId + "\t" + sentence;
        for (RewriteRule r : rules) {
            temp += r.toString() + "\n";
        }
        return temp;
    }

    /**
     * Applies a single RewriteRule to the sentence
     * @param i ID of rule to use
     * @param usedRules Set of rules already applied to caption
     * @return Resulting string
     */
    public String applyRule(int i, HashSet<Integer> usedRules) {
        // rules can only be applied once, do not reapply rule if already used
        if (usedRules.contains(i)) {
            return "";
        }

        ArrayList<String> left = rules.get(i).getLeft();
        ArrayList<String> right = rules.get(i).getRight();
        ArrayList<String> pre = new ArrayList<>();
        ArrayList<String> post = new ArrayList<>();
        HashMap<String, String> ids = new HashMap<>();
        int j = 0;
        int k = 0;

        // first and last token of left and right must match
        if (!left.get(0).equals(right.get(0)) || !left.get(left.size()-1).equals(right.get(right.size()-1))) {
            return "";
        }

        // if first token is beginning of string marker
        if (left.get(0).equals("B")) {
            // first token of string must match second token of left side
            // or second token of left side is Kleene star
            // or left side has to be empty string
            if (!left.get(1).equals("#STAR#") && !left.get(1).equals("E") && !Integer.toString(sentence.getTokenAtPosition(0).getId()).equals(left.get(1))) {
                return "";
            }
            k++;
        }
        else { // otherwise find the first token of left in the string
            // store everything before the first token in pre
            boolean foundJ = false;
            for (j = 0; j < sentence.getTokenLength(); j++) {
                if (Integer.toString(sentence.getTokenAtPosition(j).getId()).equals(left.get(0))) {
                    foundJ = true;
                    break;
                }
                pre.add(sentence.getTokenAtPosition(j).toString());
            }
            if (!foundJ) {
                return "";
            }
        }

        // check that we can match the left side with the current string
        // k: index into left side
        // j: index into string
        // grab the chunk of the string represented by each token in the rule - we'll use these to generate replacement IDs
        boolean kleene = false;
        while (k < left.size()) {
            if (left.get(k).equals("#STAR#")) {
                kleene = true;
            }
            if (kleene) {
                if (k < left.size() - 1) { // at least one more left token/chunk to check
                    // check next token
                    Chunk chunkJ = sentence.getChunkStartsAtToken(j);
                    if (j < sentence.getTokenLength() && Integer.toString(sentence.getTokenAtPosition(j).getId()).equals(left.get(k + 1))) {
                        k++;
                        if (chunkJ != null) { // matched chunk open bracket, find next chunk
                            ids.put(left.get(k), chunkJ.toString());
                            if (chunkJ.getNextChunk() != null) {
                                j = chunkJ.getNextChunk().getStartIdx();
                            } else {
                                j = sentence.getTokenPosition(chunkJ.getLastToken().getNextToken());
                            }
                        }
                        else { // matched token, get next token
                            ids.put(left.get(k), sentence.getTokenAtPosition(j).toString());
                            j += 1;
                        }
                        kleene = false;
                    }
                    else { // no match, check next token
                        j += 1;
                    }
                    // check if we've run out of string to match
                    if (j >= sentence.getTokenLength()) {
                        k++;
                        kleene = false;
                        if (!left.get(k).equals("E")) {
                            return "";
                        }
                    }
                }
            }
            // check if we're at the end of string token
            else if (left.get(k).equals("E")) {
                // if so, we need to be at end of string and at last token of left hand side
                if (k != left.size()-1 || j < sentence.getTokenLength()-1) {
                    return "";
                }
            }
            // otherwise, if there are at least 2 more tokens remaining ("A B"),
            // we need to figure out if they match where we are in the current string
            // (is "A" the current token and "B" the next token - either directly after "A" or the next chunk)
            // this determines what part of the string "A" represents - token or chunk
            // since we're checking the next 2 tokens, we don't need to check if there is only one token left
            else if (k < left.size() - 1) {
                // if next token is end of string marker
                if (left.get(k+1).equals("E")) {
                    Chunk chunkJ = sentence.getChunkStartsAtToken(j);
                    // either this is the last token of the string
                    if ((j + 1) >= sentence.getTokenLength()) {
                        ids.put(left.get(k), sentence.getTokenAtPosition(j).toString());
                        j += 1;
                    }
                    // or it must be the last chunk of the string (with no following tokens)
                    else if (chunkJ != null && chunkJ.getNextChunk() == null) {
                        ids.put(left.get(k), chunkJ.toString());
                        j = sentence.getTokenPosition(chunkJ.getLastToken()) + 1;
                    }
                    else {
                        return "";
                    }
                }
                else {
                    // otherwise, this must not be last token of string
                    Chunk chunkJ = sentence.getChunkStartsAtToken(j);
                    // either next left-token is Kleene star...
                    if (left.get(k+1).equals("#STAR#")) {
                        if (chunkJ != null) { // matched chunk open bracket, find next chunk
                            ids.put(left.get(k), chunkJ.toString());
                            if (chunkJ.getNextChunk() != null) {
                                j = chunkJ.getNextChunk().getStartIdx();
                            }
                            else if (chunkJ.getLastToken().getNextToken() != null){
                                j = sentence.getTokenPosition(chunkJ.getLastToken().getNextToken());
                            }
                            else {
                                j = sentence.getTokenLength();
                            }
                        }
                        else { // matched token, get next token
                            ids.put(left.get(k), sentence.getTokenAtPosition(j).toString());
                            j += 1;
                        }
                        k++;
                        kleene = true;
                    }
                    // or next token must match...
                    else if ((j + 1) < sentence.getTokenLength() && Integer.toString(sentence.getTokenAtPosition(j + 1).getId()).equals(left.get(k+1))) {
                        ids.put(left.get(k), sentence.getTokenAtPosition(j).toString());
                        j += 1;
                    }
                    // or the next chunk must match
                    else if (chunkJ != null && ((chunkJ.getNextChunk() != null && chunkJ.getNextChunk().getFirstToken() != null && Integer.toString(chunkJ.getNextChunk().getFirstToken().getId()).equals(left.get(k+1))) || (chunkJ.getLastToken().getNextToken() != null && Integer.toString(chunkJ.getLastToken().getNextToken().getId()).equals(left.get(k+1))))) {
                        ids.put(left.get(k), chunkJ.toString());
                        if (chunkJ.getNextChunk() != null) {
                            j = chunkJ.getNextChunk().getStartIdx();
                        }
                        else {
                            j = sentence.getTokenPosition(chunkJ.getLastToken().getNextToken());
                        }
                    }
                    else {
                        return "";
                    }
                }
            }
            if (!kleene) {
                k++;
            }
        }

        // store the remainder of the string in post
        while (j < sentence.getTokenLength()) {
            post.add(sentence.getTokenAtPosition(j).toString());
            j++;
        }

        // generate replacement: process right hand side of rule using ids when we encounter token ID by itself (this should be on left hand side)
        // we don't need to use the last token of the right hand side because that's in post
        // we do put first token of rule into mid even though it won't change
        ArrayList<String> mid = new ArrayList<>();
        for (j = 0; j < right.size() -1; j++) {
            if (right.get(j).contains("/")) {
                mid.add(right.get(j));
            }
            else if (!right.get(j).equals("B")) {
                if (ids.get(right.get(j)) == null) {
                    System.exit(0); // die
                }
                mid.add(ids.get(right.get(j)));
            }
        }

        // resulting string is pre + mid + post
        return ArrayListUtils.stringListToString(pre, " ") + " " + ArrayListUtils.stringListToString(mid, " ") + " " + ArrayListUtils.stringListToString(post, " ");
    }

    /**
     * Generate all possible edges from this RewriteCaption
     * @param usables Rule types to use
     * @param numRuleApplications Valid number of rule applications:
     *                            - positive number: total number of strings to generate before stopping
     *                            - 0: generate as many strings as possible
     *                            - negative number: depth-limited, only generate strings from at most N rule applications
     * @return Resulting chunks map to index of generating rule map to originating chunk
     */
    HashMap<Chunk, HashMap<String, HashSet<Chunk>>> generateSentences(HashSet<String> usables, int numRuleApplications) {
        int numRules = rules.size();
        // assume the initial string is the sentence from this RewriteCaption
        HashMap<Chunk, HashSet<Integer>> usedRules = new HashMap<>(); // set of rules already applied to this Chunk
        usedRules.put(sentence, new HashSet<>());
        HashMap<Chunk, HashMap<String, HashSet<Chunk>>> link = new HashMap<>(); // set of edges out of this Chunk
        link.put(sentence, new HashMap<>());
        HashSet<Chunk> cset = new HashSet<>(); // set of chunks at this depth
        cset.add(sentence);

        do {
            // nset: set of strings generated by the strings in cset, will be operated on during the next round
            HashSet<Chunk> nset = new HashSet<>();
            for (Chunk csetStr : cset) {
                link.putIfAbsent(csetStr, new HashMap<>());
                RewriteCaption newCap = new RewriteCaption(this.id, this.nextTokenId, csetStr);
                newCap.setRules(this.rules);
                Chunk newSent = newCap.getSent();
                // set of rules already applied to this string
                HashSet<Integer> done = new HashSet<>();

                // check for paired NPHEADs
                // we want "person and person" to go straight to "man and woman", not to go through "person and woman" and "man and person"
                for (Chunk chunkI : newSent.getChunks()) {
                    // look for EN CC EN
                    if (chunkI.getType().equals("EN")) {
                        Chunk chunkJ = chunkI.getNextChunk();
                        if (chunkJ != null && chunkJ.getType().equals("EN")) {
                            // check if ENs have the same head
                            String headI = chunkI.getChunkHead();
                            String headJ = chunkJ.getChunkHead();
                            if (headI.equals(headJ)) {
                                Chunk res = new Chunk("");
                                int k;
                                int l = 0;

                                // go through rules and find NPHEAD rules that apply to first EN
                                // TODO we only use the first rule we find that applies
                                for (k = 0; k < numRules; k++) {
                                    ArrayList<String> left = rules.get(k).getLeft();
                                    if (left.get(0).equals(Integer.toString(chunkI.getStartIdx())) && left.get(left.size()-1).equals(Integer.toString(chunkI.getNextChunk().getStartIdx()))) {
                                        // we can apply this rule
                                        String[] az = rules.get(k).getType().split("/");
                                        String depType = az[0];
                                        if (!usables.contains("ALL")) {
                                            if (!usables.contains(depType)) {
                                                continue; // this is not an allowed rule
                                            }
                                        }
                                        if (depType.equals("NPHEAD")) {
                                            res = new Chunk(newCap.applyRule(k, usedRules.get(csetStr)));
                                            if (!res.toString().equals("")) {
                                                break; // we found one rule
                                            }
                                        }
                                    }
                                }
                                if (!res.toString().equals("")) {
                                    newCap = new RewriteCaption(this.id, this.nextTokenId, res);
                                    // find NPHEAD rule for second EN
                                    // again use only first applicable rule
                                    res = new Chunk("");
                                    for (l = 0; l < numRules; l++) {
                                        ArrayList<String> left = rules.get(l).getLeft();
                                        if (left.get(0).equals(Integer.toString(chunkI.getStartIdx())) && left.get(left.size() - 1).equals(Integer.toString(chunkI.getNextChunk().getStartIdx()))) {
                                            String[] az = rules.get(l).getType().split("/");
                                            String depType = az[0];
                                            if (!usables.contains("ALL")) {
                                                if (!usables.contains(depType)) {
                                                    continue; // not an allowed rule
                                                }
                                            }
                                            if (depType.equals("NPHEAD")) {
                                                res = new Chunk(newCap.applyRule(l, usedRules.get(csetStr)));
                                                if (!res.toString().equals("")) {
                                                    break; // we found one rule
                                                }
                                            }
                                        }
                                    }
                                }

                                // if we found NPHEAD rules for both EN chunks, apply at same time and mark as done
                                if (!res.toString().equals("")) {
                                    if (usedRules.get(res) != null) {
                                        usedRules.put(res, new HashSet<>());
                                        if (numRuleApplications > 0) {
                                            numRuleApplications--;
                                            if (numRuleApplications == 0) {
                                                return link; // set of edges for this string
                                            }
                                        }
                                    }
                                    if (!usedRules.get(res).contains(k)) {
                                        usedRules.get(res).add(k);
                                        nset.add(res);
                                    }
                                    if (!usedRules.get(res).contains(l)) {
                                        usedRules.get(res).add(l);
                                        nset.add(res);
                                    }
                                    for (int x : usedRules.get(csetStr)) { // already applied rules
                                        if (usedRules.get(res).contains(x)) {
                                            nset.add(res);
                                        }
                                    }
                                    String lid = k+","+l;
                                    link.get(csetStr).putIfAbsent(lid, new HashSet<>());
                                    link.get(csetStr).get(lid).add(res);
                                    done.add(k);
                                    done.add(l);

                                }
                            }
                        }
                    }
                }
                // try applying all rules to string
                for (int i = 0; i < numRules; i++) {
                    // check if we used this rule in the previous section
                    if (done.contains(i)) {
                        continue;
                    }
                    // check if this is a rule we want to use
                    String[] az = rules.get(i).getType().split("/");
                    String depType = az[0];
                    if (!usables.contains("ALL")) {
                        if (!usables.contains(depType)) {
                            continue; // not an allowed rule
                        }
                    }
                    // see what happens when we apply the rule
                    Chunk res = new Chunk(newCap.applyRule(i, usedRules.get(csetStr)));
                    if (!res.toString().equals("")) {
                        // init hash entries for new string
                        if (usedRules.get(res) == null) {
                            usedRules.put(res, new HashSet<>());
                            // check if we've produced enough strings
                            if (numRuleApplications > 0) {
                                numRuleApplications--;
                                if (numRuleApplications == 0) {
                                    return link;
                                }
                            }
                        }
                        // if we haven't produced the resulting string using this rule, note that we can do this
                        // add resulting string to next set of strings
                        if (!usedRules.get(res).contains(i)) {
                            usedRules.get(res).add(i);
                            nset.add(res);
                        }
                        // propagate rules that generated the current string as rules that can generate the resulting string
                        nset.addAll(usedRules.get(csetStr).stream().filter(x -> !usedRules.get(res).contains(x)).map(x -> res).collect(Collectors.toList()));
                        // add an edge
                        String lid = Integer.toString(i);
                        link.get(csetStr).putIfAbsent(lid, new HashSet<>());
                        link.get(csetStr).get(lid).add(res);
                    }
                }
            }
            if (numRuleApplications < 0) {
                numRuleApplications++;
                if (numRuleApplications == 0) {
                    return link;
                }
            }
            cset = nset;
        }
        while (cset.size() > 0);
        return link;
    }

}
