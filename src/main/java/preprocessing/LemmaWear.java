package preprocessing;

import structure.Chunk;
import structure.RewriteCaption;
import utils.TransFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Normalizes instances of "dressed (up) in [clothing]" and "in [clothing]" to "wear [clothing]"
 * @author aylai2
 */
public class LemmaWear {

    private static HashSet<String> clothing; /* Original clothing lexicon */
    private static HashSet<String> defClothing; /* Updated clothing lexicon from Bryan's project 2014/06/06 */

    private static void readClothing(String dir) {
        clothing = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir+"/preprocessing/graph/data/clothing.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                clothing.add(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readDefClothing(String dir) {
        defClothing = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir+"/preprocessing/graph/data/lexiconsNew/clothing.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                defClothing.add(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds instances of "[VP dressed ] ([PP/PRT up ]) [PP/PRT in ] [EN -clothing- ]" and "[PP in ] [EN -clothing- ]"
     * and normalizes to "[VP wear ] [EN -clothing- ]"
     * Also prints to file a list of "identified" clothing terms that are not in the lexicon and are probably wrong (mischunked)
     * @param cap Caption to normalize
     * @return Normalized caption
     */
    public static RewriteCaption applyNorm(String dir, RewriteCaption cap, boolean useLexicon, PrintWriter out) {
        if (clothing == null) {
            readClothing(dir);
        }
        if (defClothing == null) {
            readDefClothing(dir);
        }
        Chunk sent = cap.getSent();
        ArrayList<Chunk> chunks = new ArrayList<>();
        if (sent.getType().equals("SENT")) {
            chunks = sent.getChunks();
        }
        else {
            chunks.add(sent);
        }
        int vpCount = 0;
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            // look for "wear", "dressed in", and "dressed up in"
            // convert all to "wear" and save clothing terms
            // look for VP chunk
            if (c.getType().equals("VP") && c.getTokenLength() == 3) { // assumes VP chunk contains 1 word
                Chunk chunkJ = c.getNextChunk();
                String enType = "";
                if (chunkJ == null) {
                    continue;
                }
                int j = -1;
                // look for "[VP x ]"
                if (c.toBareString().equals("wear")) {
                    if (chunkJ.getType().equals("EN")) {
                        enType = chunkJ.getId();
                        j = i + 1;
                    }
                }
                // look for "[VP dressed ] [PP/PRT x ]
                else if (c.toBareString().equals("dressed") && (chunkJ.getType().equals("PP") || chunkJ.getType().equals("PRT")) && chunkJ.getTokenLength() == 3) {
                    // look for "[VP dressed ] [PP/PRT in ] [EN"
                    if (chunkJ.toBareString().equals("in") && chunkJ.getNextChunk() != null && chunkJ.getNextChunk().getType().equals("EN")) {
                        enType = chunkJ.getNextChunk().getId();
                        j = i + 1;
                        c.getFirstToken().getNextToken().setStr("wear");
                        c.getFirstToken().getNextToken().setPos(c.getId());
                        int id = chunkJ.getLastToken().getId();
                        c.getLastToken().setId(id);
                        sent.removeChunk(chunkJ, true);
                    }
                    // look for [VP dressed ] [PP/PRT up ] [PP/PRT in ] [EN"
                    else if (chunkJ.toBareString().equals("up") && chunkJ.getNextChunk() != null
                            && (chunkJ.getNextChunk().getType().equals("PP") || chunkJ.getNextChunk().getType().equals("PRT"))
                            && chunkJ.getNextChunk().toBareString().equals("in")
                            && chunkJ.getNextChunk().getNextChunk() != null && chunkJ.getNextChunk().getNextChunk().getType().equals("EN")) {
                        enType = chunkJ.getNextChunk().getNextChunk().getId();
                        j = i + 1;
                        c.getFirstToken().getNextToken().setStr("wear");
                        c.getFirstToken().getNextToken().setPos(c.getId());
                        int id = chunkJ.getNextChunk().getLastToken().getId();
                        c.getLastToken().setId(id);
                        sent.removeChunk(chunkJ.getNextChunk(), true);
                        sent.removeChunk(chunkJ, true);
                    }
                }
                // j is pointer to next chunk after VP chunk
                // find next NPH chunk and assume it's a piece of clothing: if not in lexicon, print to file
                if (!useLexicon) {
                    if (j != -1) {
                        Chunk en = chunks.get(j);
                        for (Chunk chunkK : en.getAllChunks()) {
                            if (chunkK.getType().equals("NPH") && !defClothing.contains(chunkK.toBareString()) && clothing.contains(chunkK.toBareString())) {
                                out.println(cap.getId() + "\t" + enType + "\t" + sent.toString());
                            }
                        }
                    }
                }
            }
            // look for "[PP in ] [EN ... "
            else if (c.getType().equals("PP") && c.toBareString().equals("in") && c.getNextChunk() != null && c.getNextChunk().getType().equals("EN")) {
                for (Chunk k : c.getNextChunk().getAllChunks()) {
                    if (k.getType().equals("NPH"))  {
                        if (defClothing.contains(k.toBareString())) {
                            // change first token of c (opening bracket)
                            c.getFirstToken().setChunkType("VP");
                            c.getFirstToken().setChunkId("VPwear"+vpCount);
                            vpCount++;
                            // change string token of c
                            c.getFirstToken().getNextToken().setStr("wear");
                            c.getFirstToken().getNextToken().setPos("VB");
                        }
                        break;
                    }
                }
            }
        }
        return cap;
    }

    public static void main(String[] args) {
        String dir = args[0];
        String dataDir = args[1];
        String corpus = args[2];
        try {
            String inFilename = dir+"/"+corpus+"/tmp/graph/pre.there";
            PrintWriter out = new PrintWriter(new File(dir+"/"+corpus+"/tmp/graph/pre.wear.new"));
            PrintWriter temp = new PrintWriter(new File("wear_errors.txt"));

            TransFileReader reader = new TransFileReader(inFilename);
            ArrayList<String> captionIds = reader.getCaptions();
            for (String capId : captionIds) {
                RewriteCaption rCap = new RewriteCaption(capId, reader.getTokenCountIn(capId), reader.getCaptionIn(capId));
                RewriteCaption newCap = LemmaWear.applyNorm(dataDir, rCap, true, temp);
                out.println(capId+"\t"+newCap.getNextTokenId()+"\t"+newCap.getSent());
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}