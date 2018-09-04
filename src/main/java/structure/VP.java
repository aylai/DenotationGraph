package structure;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Store and access VP information for captions (subject, verb, direct object indices)
 * @author aylai2
 */
public class VP {

    private HashMap<String, ArrayList<String>> vp;
    private HashMap<String, String> subj;
    private HashMap<String, HashSet<String>> dobj;

    public VP(String dir, String corpusName) {
        vp = new HashMap<>();
        subj = new HashMap<>();
        dobj = new HashMap<>();
        loadVPs(dir, corpusName);
    }

    public HashMap<String, ArrayList<String>> getVPs() {
        return vp;
    }

    /**
     * Read VP information (subj, VP, dobj) from files
     * @param dir Main graph directory
     * @param corpusName e.g. "results_20130124"
     */
    private void loadVPs(String dir, String corpusName) {
        try {
            // load subjects: subj[capId+VPid] = NP id of subject
            subj = new HashMap<>();
            BufferedReader br = new BufferedReader(new FileReader(dir + "/" + corpusName + "/" + corpusName + ".subj"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split("\t");
                subj.put(tokens[0], tokens[1].split("#")[2]);
            }
            br.close();

            // load VPs: vp[capId] -> list of VP ids for that caption
            vp = new HashMap<>();
            br = new BufferedReader(new FileReader(dir + "/" + corpusName + "/" + corpusName + ".vp"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split("\t")[0].split("#");
                String capId = tokens[0]+"#"+tokens[1];
                vp.computeIfAbsent(capId, k -> new ArrayList<>());
                vp.get(capId).add(tokens[2]);
            }
            br.close();

            // load direct objects: dobj[capId+VPid] = NP idea of direct object
            dobj = new HashMap<>();
            br = new BufferedReader(new FileReader(dir + "/" + corpusName + "/" + corpusName + ".dobj"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split("\t");
                dobj.computeIfAbsent(tokens[0], k -> new HashSet<>());
                dobj.get(tokens[0]).add(tokens[2].split("#")[2]);
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addVP(String capId, String verbId, String subjId, String dobjId) {
        vp.computeIfAbsent(capId, k -> new ArrayList<>());
        vp.get(capId).add(verbId);
        String verbIdIndex = capId + "#" + verbId;
        subj.put(verbIdIndex, subjId);
        dobj.computeIfAbsent(verbIdIndex, k -> new HashSet<>());
        dobj.get(verbIdIndex).add(dobjId);
    }

    /**
     * Return subj index, VP index, dobj index of SVO triple i:
     * -1 index means there is no subj/vp/dobj
     * -2 index means there should have been a subj/vp/dobj but it wasn't found
     * @param cap Caption
     * @param i VP index
     * @return String of SVO information
     */
    public String getVP(RewriteCaption cap, int i) {
        String capId = cap.getId();
        String verb = vp.get(capId).get(i);
        String verbId = capId + "#" + verb;
        int rSubj = -1;
        int rVp = -1;
        int rDobj = -1;
        // find subject and direct object if there is one
        Chunk sent = cap.getSent();
        ArrayList<Chunk> chunks = new ArrayList<>();
        if (sent.getType().equals("SENT")) {
            chunks = sent.getChunks();
        }
        else {
            chunks.add(sent);
        }
        String dobjId = "";
        for (int j = 0; j < chunks.size(); j++) { // iterate over chunks in coref string
            Chunk c = chunks.get(j);
            if (c.getType().equals("EN")) {
                if (subj.get(verbId) != null && subj.get(verbId).equals(c.getId())) {
                    rSubj = j;
                }
                if (dobj.get(verbId) != null && dobj.get(verbId).contains(c.getId())) {
                    rDobj = j;
                    dobjId = c.getId();
                }
            }
            else if (c.getType().equals("VP")) {
                if (verb.equals(c.getId())) {
                    rVp = j;
                }
            }
        }

        // check if we found everything we were supposed to
        if (rSubj == -1 && subj.get(verbId) != null) {
            rSubj = -2;
        }
        if (rVp == -1) {
            rVp = -2;
        }
        if (rDobj == -1 && dobj.get(verbId) != null) {
            rDobj = -2;
        }

        String subjStr = "null";
        if (subj.get(verbId) != null) {
            subjStr = subj.get(verbId);
        }
        String dobjStr = "null";
        if (dobj.get(verbId) != null) {
            dobjStr = dobjId;
            if (dobjStr.equals("")) {
                for (String s : dobj.get(verbId)) {
                    dobjStr = s;
                    break;
                }
            }
        }
        return rSubj + "\t" + rVp + "\t" + rDobj + "\t" + subjStr + "\t" + verbId + "\t" + dobjStr;
    }
}
