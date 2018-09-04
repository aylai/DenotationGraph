package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Set up input/output captions from trans*.txt files to test rewrite rule code
 * Created by alai on 7/3/15.
 */
public class TransFileReader {

    private ArrayList<String> captions;
    private HashMap<String, String> captionIn;
    private HashMap<String, ArrayList<String>> rulesIn;
    private HashMap<String, Integer> tokenCountIn;
    private HashMap<String, String> captionOut;
    private HashMap<String, ArrayList<String>> rulesOut;
    private HashMap<String, Integer> tokenCountOut;

    public TransFileReader(String inFilename, String outFilename) {
        readInFile(inFilename);
        readOutFile(outFilename);
    }

    public TransFileReader(String inFilename) {
        readInFile(inFilename);
    }

    private void readInFile(String inFilename) {
        captions = new ArrayList<>();
        tokenCountIn = new HashMap<>();
        captionIn = new HashMap<>();
        rulesIn = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(inFilename));
            String line;
            ArrayList<String> rules = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split("\t");
                if (tokens.length == 5) { // rewrite rule
                    rules.add(tokens[2]+"\t"+tokens[3]+"\t"+tokens[4]);
                }
                else if (tokens.length == 3) { // caption
                    String capId = tokens[0];
                    captions.add(capId);
                    tokenCountIn.put(capId, Integer.parseInt(tokens[1]));
                    captionIn.put(capId, tokens[2]);
                    rulesIn.put(capId, rules);
                    rules = new ArrayList<>();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getCaptions() {
        return captions;
    }

    public String getCaptionIn(String capId) {
        return captionIn.get(capId);
    }

    public int getTokenCountIn(String capId) {
        return tokenCountIn.get(capId);
    }

    private void readOutFile(String outFilename) {
        tokenCountOut = new HashMap<>();
        captionOut = new HashMap<>();
        rulesOut = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(outFilename));
            String line;
            ArrayList<String> rules = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                String[] tokens = line.split("\t");
                if (tokens.length == 5) { // rewrite rule
                    rules.add(tokens[2]+"\t"+tokens[3]+"\t"+tokens[4]);
                }
                else if (tokens.length == 3) { // caption
                    String capId = tokens[0];
                    tokenCountOut.put(capId, Integer.parseInt(tokens[1]));
                    captionOut.put(capId, tokens[2]);
                    rulesOut.put(capId, rules);
                    rules = new ArrayList<>();
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCaptionOut(String capId) {
        return captionOut.get(capId);
    }

    public int getTokenCountOut(String capId) {
        return tokenCountOut.get(capId);
    }

    public ArrayList<String> getRulesIn(String capId) {
        return rulesIn.get(capId);
    }

    public ArrayList<String> getRulesOut(String capId) {
        return rulesOut.get(capId);
    }
}
