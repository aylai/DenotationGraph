package structure;

import utils.ArrayListUtils;
import utils.SortFrequency;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * A set of nodes (index and string) joined by edges
 * @author aylai2
 */
class Graph {

    /* main graph */
    private ArrayList<RewriteCaption> finalCaps;
    private HashMap<Integer, String> nodeIndexToString;
    private HashMap<String, Integer> nodeStringToIndex;
    private HashMap<String, HashSet<String>> nodeTokens;
    private HashMap<Integer, HashMap<String, HashSet<String>>> nodeToChunkToCapMap;
    private HashMap<Integer, HashSet<String>> nodeToCapMap;
    private HashMap<String, HashSet<Integer>> capToNodeMap;
    private HashMap<Integer, HashSet<String>> nodeToImageMap;
    private HashMap<Integer, HashSet<String>> nodeToOrigCapMap;
    private int nextNodeIndex;
    private int oldNodeIndex;
    private HashMap<String, Integer> originalGraphNodeIndex;
    private HashMap<Integer, HashMap<Integer, HashMap<String, HashSet<String>>>> edges; /* child ID, parent ID, edge type (type + \t + rule ID) */
    private HashMap<String, Integer> capToOrigNodeMap;
    private HashMap<String, HashSet<Integer>> origNodes;
    private HashMap<Integer, HashMap<String, String>> nodeToChunkToChunkTypeMap;
    private HashMap<Integer, Integer> nodeToImageCount;
    private HashMap<Integer, HashMap<Integer, Integer>> nodeCooccurCount;
    private HashSet<String> imgList;
    private HashMap<String, String> capToTokenStrMap;
    /* subgraphs */
    private HashMap<String, HashMap<Integer, HashSet<String>>> nodeToCapMapSubgraph;
    private HashMap<String, HashMap<String, HashSet<Integer>>> capToNodeMapSubgraph;
    private HashMap<String, HashSet<String>> imgListSubgraph;
    private HashMap<String, HashMap<String, String>> capToTokenStrMapSubgraph;
    private HashMap<String, HashMap<Integer, String>> nodeIndexToStringSubgraph;
    private HashMap<String, HashMap<String, Integer>> nodeStringToIndexSubgraph;
    private HashMap<String, HashMap<Integer, HashMap<String, HashSet<String>>>> nodeToChunkToCapMapSubgraph;
    private HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<String, HashSet<String>>>>> edgesSubgraph;
    private HashMap<Integer, HashMap<Integer, Double>> pmi;
    private HashMap<Integer, HashMap<Integer, Double>> cpr;
    private HashMap<String, HashSet<String>> propagated;
    private ArrayList<RewriteCaption> originalCaptions;
    private HashMap<String, HashSet<String>> svonExpansion;
    private boolean debugPrint;

    /* load existing graph from files */
    public Graph(String dir, String corpus) {
        loadGraph(dir, corpus);
    }

    /* generate graph from preprocessed files */
    public Graph(String dir, String corpusName, ArrayList<RewriteCaption> preList, ArrayList<RewriteCaption> npCaptions, ArrayList<RewriteCaption> captions, String extendFilename, String outDir, VP verbInfo, PrintWriter out, boolean debugPrint) {
        this.debugPrint = debugPrint;
        propagated = new HashMap<>();
        svonExpansion = new HashMap<>();
        System.out.println("creating NP subgraph");
        makeNPSubgraph(dir, corpusName, npCaptions, preList, extendFilename);
        propagateImages();
        System.out.println("creating VP subgraph");
        makeVPSubgraph(dir, corpusName, captions);
        propagateImages();
        try {
            System.out.println("creating full graph");
            makeGraph(captions, extendFilename, outDir);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        addOrigEdges(captions, preList, verbInfo);
        propagateImages();
        // print propagate images
        HashMap<String, Integer> propagateCounts = new HashMap<>();
        for (String edge : propagated.keySet()) {
            propagateCounts.put(edge, propagated.get(edge).size());
        }
        ArrayList<String> sortedEdges = SortFrequency.getSortedFrequency(propagateCounts);
        if (debugPrint) {
            for (String edge : sortedEdges) {
                out.print(edge);
                for (String capId : propagated.get(edge)) {
                    out.print("\t" + capId);
                }
                out.println();
            }
        }
    }

    private void makeNPSubgraph(String dir, String corpusName, ArrayList<RewriteCaption> npCaptions, ArrayList<RewriteCaption> captions, String extendFilename) {
        nodeIndexToString = new HashMap<>();
        nodeStringToIndex = new HashMap<>();
        nodeTokens = new HashMap<>();
        nodeToChunkToCapMap = new HashMap<>();
        nodeToCapMap = new HashMap<>();
        capToNodeMap = new HashMap<>();
        nodeToOrigCapMap = new HashMap<>();
        originalGraphNodeIndex = new HashMap<>();
        edges = new HashMap<>();
        nextNodeIndex = 0;
        // if we are extending a previous graph
        if (!extendFilename.equals("")) {
            // grab index of previous graph
            try {
                BufferedReader br = new BufferedReader(new FileReader(extendFilename + "graph/node.idx"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.trim().split("\t");
                    int thisIndex = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        originalGraphNodeIndex.put(tokens[1], thisIndex); // map string to index
                    }
                    else {
                        originalGraphNodeIndex.put("", thisIndex);
                    }
                    if (nextNodeIndex <= thisIndex) {
                        nextNodeIndex = thisIndex + 1;
                    }
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // grab NP specific index of previous graph
            // we'll use these as EN chunks that we've already seen in order to generate strings
            try {
                BufferedReader br = new BufferedReader(new FileReader(extendFilename + "graph/np.idx"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] tokens = line.trim().split("\t");
                    int nodeIdx = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        nodeStringToIndex.put(tokens[1], nodeIdx); // map string to index
                        nodeIndexToString.put(nodeIdx, tokens[1]); // map node index to string
                    }
                    nodeToOrigCapMap.put(nodeIdx, new HashSet<>());
                    nodeToChunkToCapMap.put(nodeIdx, new HashMap<>());
                    nodeToCapMap.put(nodeIdx, new HashSet<>());
                    if (nextNodeIndex <= nodeIdx) {
                        nextNodeIndex = nodeIdx + 1;
                    }
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // find EN strings to use: grab fully reduced form from trans06.txt and original(ish) form from pre.id
        // we want to avoid going from [ice] [hockey player] to [ice] [player] after this
        for (RewriteCaption cap : npCaptions) {
            capToNodeMap.put(cap.getId(), new HashSet<>());
            svonExpansion.put(cap.getId(), new HashSet<>());
            for (Chunk sent : cap.getRoots()) {
                ArrayList<Chunk> chunks = new ArrayList<>();
                if (sent.getType().equals("SENT")) {
                    chunks = sent.getChunks();
                } else {
                    chunks.add(sent);
                }
                chunks.stream().filter(c -> c.getType().equals("EN")).forEach(c -> {
                    String s = c.toBareString();
                    if (!s.equals("")) {
                        String[] tokens = s.split("\\s+");
                        if (nodeStringToIndex.get(s) == null) { // this node string doesn't have a node index
                            if (originalGraphNodeIndex.get(s) != null) { // found in original graph
                                int x = originalGraphNodeIndex.get(s);
                                nodeStringToIndex.put(s, x); // update node index
                                nodeIndexToString.put(x, s);
                                nodeToOrigCapMap.put(x, new HashSet<>());
                                nodeToChunkToCapMap.put(x, new HashMap<>());
                                nodeToCapMap.put(x, new HashSet<>());
                                addNodeTokens(cap.getId(), c, x);
                            } else if ((tokens.length != 2 || !tokens[0].equals(tokens[1]))) { // avoid "word word" nodes
                                nodeStringToIndex.put(s, nextNodeIndex);
                                nodeIndexToString.put(nextNodeIndex, s);
                                nodeToOrigCapMap.put(nextNodeIndex, new HashSet<>());
                                nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                                nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                                addNodeTokens(cap.getId(), c, nextNodeIndex);
                                nextNodeIndex++;
                            }
                        } else {
                            addNodeTokens(cap.getId(), c, nodeStringToIndex.get(s));
                        }
                    }
                });
            }
        }

        for (RewriteCaption cap : captions) { // pre.final captions
            capToNodeMap.putIfAbsent(cap.getId(), new HashSet<>());
            svonExpansion.putIfAbsent(cap.getId(), new HashSet<>());
            for (Chunk sent : cap.getRoots()) {
                ArrayList<Chunk> chunks = new ArrayList<>();
                if (sent.getType().equals("SENT")) {
                    chunks = sent.getChunks();
                } else {
                    chunks.add(sent);
                }
                for (Chunk c : chunks) {
                    if (c.getType().equals("EN")) {
                        String s = c.toBareString();
                        if (s.equals("")) {
                            continue;
                        }
                        String[] tokens = s.split("\\s+");
                        if (tokens.length == 2 && tokens[0].equals(tokens[1])) {
                            continue;
                        }
                        if (nodeStringToIndex.get(s) == null) { // this node string doesn't have a node index
                            int idx;
                            if (originalGraphNodeIndex.get(s) != null) { // found in original graph
                                idx = originalGraphNodeIndex.get(s);
                                nodeStringToIndex.put(s, idx); // update node index
                                nodeIndexToString.put(idx, s);
                                addNodeTokens(cap.getId(), c, idx);
                                nodeToOrigCapMap.put(idx, new HashSet<>());
                                nodeToChunkToCapMap.put(idx, new HashMap<>());
                                nodeToCapMap.put(idx, new HashSet<>());
                                if (idx == nextNodeIndex) {
                                    nextNodeIndex++;
                                }
                            } else {
                                idx = nextNodeIndex;
                                nodeStringToIndex.put(s, idx); // update node index
                                nodeIndexToString.put(idx, s);
                                nodeToOrigCapMap.put(idx, new HashSet<>());
                                nodeToChunkToCapMap.put(idx, new HashMap<>());
                                nodeToCapMap.put(idx, new HashSet<>());
                                addNodeTokens(cap.getId(), c, idx);
                                if (idx == nextNodeIndex) {
                                    nextNodeIndex++;
                                }
                            }
                        } else {
                            addNodeTokens(cap.getId(), c, nodeStringToIndex.get(s));
                        }
                    }
                }
            }
        }

        // for graph generation, we determine which edges connect the strings/nodes we've already found
        // for each caption, we grab EN chunks and try to apply all rules to them
        for (RewriteCaption cap : npCaptions) { // trans.np captions
            String capId = cap.getId();
            ArrayList<RewriteRule> rules = cap.getRules();
            for (Chunk sent : cap.getRoots()) {
                ArrayList<Chunk> chunks = new ArrayList<>();
                if (sent.getType().equals("SENT")) {
                    chunks = sent.getChunks();
                }
                else {
                    chunks.add(sent);
                }
                for (Chunk c : chunks) {
                    if (c.getType().equals("EN")) {
                        // create temporary RewriteCaption for this EN chunk
                        RewriteCaption enChunk = new RewriteCaption(capId, -1, ArrayListUtils.stringListToString(c.toStringList(), " "));
                        enChunk.addRules(rules);
                        HashSet<String> usables = new HashSet<>();
                        usables.add("ALL");
                        // generate all possible strings using rewrite rules
                        HashMap<Chunk, HashMap<String, HashSet<Chunk>>> edgesTemp = enChunk.generateSentences(usables, 0);
                        // result is a set of edges
                        // grab the nodes on either side of each edge
                        for (Chunk resultChunk : edgesTemp.keySet()) {
                            int s = processNodeNP(resultChunk, cap);
                            if (s == -1) {
                                continue;
                            }
                            for (String l : edgesTemp.get(resultChunk).keySet()) {
                                // genSentLOOP
                                boolean nextGenSentLoop = false;
                                for (Chunk t1 : edgesTemp.get(resultChunk).get(l)) {
                                    if (nextGenSentLoop) {
                                        break;
                                    }
                                    int t = processNodeNP(t1, cap);
                                    if (t == -1) {
                                        continue;
                                    }
                                    // grab the edge if the nodes are not the same
                                    if (t != s) {
                                        String[] al = l.split(","); // list of rules used
                                        String type = rules.get(Integer.parseInt(al[0])).getType().split("/")[0]; // type(s) of the first rule
                                        ArrayList<String> ltype = new ArrayList<>(); // text string representing link type
                                        ArrayList<Integer> lid = new ArrayList<>(); // list of rule IDs
                                        ltype.add(type);
                                        // check that we have matching types
                                        // if not, abort (we don't handle this case)
                                        // also build the link ID (first type, followed by slashes)
                                        for (String ruleIdStr : al) {
                                            int ruleId = Integer.parseInt(ruleIdStr);
                                            ArrayList<String> ax = new ArrayList<>(Arrays.asList(rules.get(ruleId).getType().split("/")));
                                            String ax0 = ax.remove(0);
                                            if (!type.equals(ax0)) {
                                                nextGenSentLoop = true;
                                                break;
                                            }
                                            lid.add(ruleId);
                                            ltype.addAll(ax);
                                        }
                                        type = ltype.stream().collect(Collectors.joining("/"));
                                        Collections.sort(lid);
                                        edges.putIfAbsent(t, new HashMap<>());
                                        edges.get(t).putIfAbsent(s, new HashMap<>());
                                        edges.get(t).get(s).putIfAbsent(type.substring(1), new HashSet<>());
                                        edges.get(t).get(s).get(type.substring(1)).add(capId + "#" + lid.stream().map(Object::toString).collect(Collectors.joining(",")));
                                        svonExpansion.get(capId).add(type.substring(1).split("/")[0]);
                                        nodeToCapMap.get(t).add(capId);
                                        nodeToCapMap.get(s).add(capId);
                                        capToNodeMap.get(capId).add(t);
                                        capToNodeMap.get(capId).add(s);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        // output NP subgraph
        try {
            PrintWriter out = new PrintWriter(dir + "/" + corpusName + "/graph/np.idx");
            for (int i : nodeIndexToString.keySet()) {
                out.println(i + "\t" + nodeIndexToString.get(i));
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // NP node-tree
        try {
            PrintWriter out = new PrintWriter(new File(dir + "/" + corpusName +  "/graph/np-tree.txt"));
            for (int childId : edges.keySet()) {
                for (int parentId : edges.get(childId).keySet()) {
                    for (String edgeStr : edges.get(childId).get(parentId).keySet()) {
                        out.print(childId+"\t"+edgeStr+"\t"+parentId);
                        for (String capId : edges.get(childId).get(parentId).get(edgeStr)) {
                            out.print("\t"+capId);
                        }
                        out.println();
                    }
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeVPSubgraph(String dir, String corpusName, ArrayList<RewriteCaption> capFinal) {
        // load VPs
        VP verbInfo = new VP(dir , corpusName);
        HashMap<String, ArrayList<String>> vp = verbInfo.getVPs(); // capId -> list of VP ids
        // list rules to use
        HashSet<String> usables = new HashSet<>();
        usables.add("+ADVP");
        usables.add("+NPART");
        usables.add("+NPHEAD");
        usables.add("+NPMOD");
        usables.add("+RB");
        usables.add("+Xof");
        usables.add("+ofY");
        usables.add("+Xor");
        usables.add("+orY");
        usables.add("+Xto");
        usables.add("+toY");

        // generate the graph
        // do full generation on all SVO triples we find for each caption
        for (RewriteCaption cap : capFinal) {
            String capId = cap.getId();
            capToNodeMap.putIfAbsent(capId, new HashSet<>());
            ArrayList<RewriteRule> rules = cap.getRules();
            for (Chunk sent : cap.getRoots()) {
                ArrayList<Chunk> chunks = new ArrayList<>();
                if (sent.getType().equals("SENT")) {
                    chunks = sent.getChunks();
                }
                else {
                    chunks.add(sent);
                }
                int numVPs = 0;
                if (vp.get(capId) != null) {
                    numVPs = vp.get(capId).size();
                }
                for (int i = 0; i < numVPs; i++) {
                    // grab the ith SVO triple
                    String str = verbInfo.getVP(cap, i);
                    String[] tokens = str.split("\t");
                    int vpIdx = Integer.parseInt(tokens[1]);
                    int dobjIdx = Integer.parseInt(tokens[2]);
                    String sdobj = tokens[5];

                    // if missing VP, skip
                    if (vpIdx == -2) {
                        continue;
                    }
                    // if we didn't find dobj, add empty dobj EN identifier
                    if (sdobj.equals("null") || dobjIdx == -2) {
                        sdobj = "";
                    }

                    // grab SVO
                    ArrayList<String> c = new ArrayList<>();
                    Chunk end = chunks.get(vpIdx).getNextChunk();
                    if (dobjIdx >= 0) { // there is a dobj chunk
                        end = chunks.get(dobjIdx).getNextChunk();
                    }
                    Chunk chunkJ = chunks.get(vpIdx);
                    while (chunkJ != end) {
                        c.add(chunkJ.toString());
                        chunkJ = chunkJ.getNextChunk();
                    }

                    // generate strings using SVO and selected rules
                    RewriteCaption enChunk = new RewriteCaption(capId, -1, ArrayListUtils.stringListToString(c, " "));
                    enChunk.addRules(rules);
                    HashMap<Chunk, HashMap<String, HashSet<Chunk>>> edgesTemp = enChunk.generateSentences(usables, 0);
                    // take the set of edges and add each node in an edge to the graph, then add edge
                    for (Chunk resultChunk : edgesTemp.keySet()) {
                        int s = processNodeVP(resultChunk, cap, sdobj);
                        if (s == -1) {
                            continue;
                        }
                        for (String l : edgesTemp.get(resultChunk).keySet()) {
                            // genSentLOOP
                            boolean nextGenSentLoop = false;
                            for (Chunk t1 : edgesTemp.get(resultChunk).get(l)) {
                                if (nextGenSentLoop) {
                                    break;
                                }
                                int t = processNodeVP(t1, cap, sdobj);
                                if (t == -1) {
                                    continue;
                                }
                                // grab the edge if the nodes are not the same
                                if (t != s) {
                                    String[] al = l.split(","); // list of rules used
                                    String type = rules.get(Integer.parseInt(al[0])).getType().split("/")[0]; // type(s) of the first rule
                                    ArrayList<String> ltype = new ArrayList<>(); // text string representing link type
                                    ArrayList<Integer> lid = new ArrayList<>(); // list of rule IDs
                                    ltype.add(type);
                                    // check that we have matching types
                                    // if not, abort (we don't handle this case)
                                    // also build the link ID (first type, followed by slashes)
                                    for (String ruleIdStr : al) {
                                        int ruleId = Integer.parseInt(ruleIdStr);
                                        ArrayList<String> ax = new ArrayList<>(Arrays.asList(rules.get(ruleId).getType().split("/")));
                                        String ax0 = ax.remove(0);
                                        if (!type.equals(ax0)) {
                                            nextGenSentLoop = true;
                                            break;
                                        }
                                        lid.add(ruleId);
                                        ltype.addAll(ax);
                                    }
                                    type = String.join("/", ltype);
                                    Collections.sort(lid);
                                    edges.putIfAbsent(t, new HashMap<>());
                                    edges.get(t).putIfAbsent(s, new HashMap<>());
                                    edges.get(t).get(s).putIfAbsent(type.substring(1), new HashSet<>());
                                    edges.get(t).get(s).get(type.substring(1)).add(capId + "#" + lid.stream().map(Object::toString).collect(Collectors.joining(",")));
                                    svonExpansion.get(capId).add(type.substring(1).split("/")[0]);
                                    nodeToCapMap.get(t).add(capId);
                                    nodeToCapMap.get(s).add(capId);
                                    capToNodeMap.get(capId).add(t);
                                    capToNodeMap.get(capId).add(s);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addNodeTokens(String capId, Chunk c, int nodeId) {
        nodeTokens.putIfAbsent(capId, new HashSet<>());
        String entry = nodeId + "\t" + c.toString();
        nodeTokens.get(capId).add(entry);
    }

    /* does not create new nodes */
    private int processNodeNP(Chunk c, RewriteCaption rCap) {
        String p = c.toBareString();
        String id = rCap.getId();
        if (nodeStringToIndex.get(p) == null) { // nodeIndex should already be populated (including from originalGraphNodeIndex)
            return -1;
        }
        // update capToOrigNodeMap, cap, chunk
        int i = nodeStringToIndex.get(p);
        nodeToOrigCapMap.get(i).add(id+"\t"+ArrayListUtils.stringListToString(c.toStringList(), " "));
        nodeToCapMap.get(i).add(id);
        capToNodeMap.get(id).add(i);
        nodeToChunkToCapMap.get(i).putIfAbsent(c.toStringNoIndex(), new HashSet<>());
        nodeToChunkToCapMap.get(i).get(c.toStringNoIndex()).add(id);
        addNodeTokens(rCap.getId(), c, nodeStringToIndex.get(p));
        return i;
    }

    private int processNodeVP(Chunk c, RewriteCaption rCap, String dobjId) {
        String p = c.toBareString();
        if (p.equals("")) {
            return -1;
        }
        String[] tokens = p.split("\\s+");
        if (tokens.length == 2 && tokens[0].equals(tokens[1])) {
            return -1;
        }
        String capId = rCap.getId();
        int r;
        // get an index
        if (nodeStringToIndex.get(p) == null) {
            if (originalGraphNodeIndex.get(p) != null) {
                r = originalGraphNodeIndex.get(p);
            }
            else {
                r = nextNodeIndex;
                nextNodeIndex++;
            }
            nodeStringToIndex.put(p, r);
            nodeIndexToString.put(r, p);
            addNodeTokens(capId, c, r);
            nodeToOrigCapMap.put(r, new HashSet<>());
            nodeToChunkToCapMap.put(r, new HashMap<>());
            nodeToCapMap.put(r, new HashSet<>());
        }
        else {
            r = nodeStringToIndex.get(p);
            addNodeTokens(capId, c, r);
            nodeToOrigCapMap.putIfAbsent(r, new HashSet<>());
            nodeToCapMap.putIfAbsent(r, new HashSet<>());
            nodeToChunkToCapMap.putIfAbsent(r, new HashMap<>());
        }

        String cStr = c.toString();
        nodeToOrigCapMap.get(r).add(capId+"\t"+cStr);
        nodeToCapMap.get(r).add(capId);
        capToNodeMap.putIfAbsent(capId, new HashSet<>());
        capToNodeMap.get(capId).add(r);
        nodeToChunkToCapMap.get(r).putIfAbsent(c.toStringNoIndex(), new HashSet<>());
        nodeToChunkToCapMap.get(r).get(c.toStringNoIndex()).add(capId);

        // if there's a direct object
        if (!dobjId.equals("")) {
            Chunk ayChunk = new Chunk(cStr);
            // find the right EN chunk and extract the direct object
            // generate TVERB link between VP and direct object
            ArrayList<Chunk> chunks = ayChunk.getChunks();
            for (Chunk chunkJ : chunks) {
                if (chunkJ.getType().equals("EN") && chunkJ.getId().equals(dobjId)) {
                    int s = processNodeVP(chunkJ, rCap, "");
                    if (s == -1) {
                        continue;
                    }
                    if (r != s) {
                        edges.putIfAbsent(r, new HashMap<>());
                        edges.get(r).putIfAbsent(s, new HashMap<>());
                        edges.get(r).get(s).putIfAbsent("TVERB", new HashSet<>());
                        edges.get(r).get(s).get("TVERB").add(capId);
                        svonExpansion.get(capId).add("TVERB");
                    }
                    break;
                }
            }
        }
        return r;
    }

    // Generate the main denotation graph
    // In general, we will only generate strings produced by multiple captions.
    // So any time we add a string to the denotation graph, we run all possible
    // extracting rules on it (to create smaller/less detailed strings). We only
    // run expanding rules if multiple captions generate the string.
    private void makeGraph(ArrayList<RewriteCaption> captions, String extendFilename, String outDir) throws Exception {

        // print output
        PrintWriter out = new PrintWriter(new File(outDir + "/out.txt"));

        // initialize variables
        nodeToChunkToChunkTypeMap = new HashMap<>();

        // Structures to hold the state of a caption when we pause processing
        HashMap<String, ArrayList<String>> graphSent = new HashMap<>(); // for node string, which caption is being held
        HashMap<String, ArrayList<Chunk>> graphState = new HashMap<>(); // for node string, which full string is being held
        HashMap<String, HashSet<String>> graphVisit = new HashMap<>(); // for node string, which caption + full strings (token sequence) have we used to visit this node
        HashMap<Integer, HashSet<Integer>> child = new HashMap<>(); // stores the tree structure
        HashMap<String, ArrayList<String>> incompleteEdges = new HashMap<>(); // for expanding an old graph, this tracks edges we skip adding until we've seen the new leaf node more than once

        // load the data structures from NP and VP subgraphs
        // VP subgraph has all strings produced thus far
        for (String nodeStr : nodeStringToIndex.keySet()) {
            int nodeId = nodeStringToIndex.get(nodeStr);
            child.put(nodeId, new HashSet<>());
        }

        // if we're extending a previous graph, load the caption maps and index file
        boolean add = false;
        if (!extendFilename.equals("")) {
            add = true;
            HashSet<Integer> hx = new HashSet<>();
            try {
                BufferedReader br = new BufferedReader(new FileReader(extendFilename + "graph/node-cap.map"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] temp = line.trim().split("\t");
                    if (temp.length > 1) {
                        hx.add(Integer.parseInt(temp[0]));
                    }
                }
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                BufferedReader br = new BufferedReader(new FileReader(extendFilename + "graph/node.idx"));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] temp = line.trim().split("\t");
                    int nodeId = Integer.parseInt(temp[0]);
                    nodeStringToIndex.put(temp[1], nodeId);
                    nodeIndexToString.put(nodeId, temp[1]);
                    nodeToCapMap.putIfAbsent(nodeId, new HashSet<>());
                    child.put(nodeId, new HashSet<>());
                    if (nextNodeIndex <= nodeId) {
                        nextNodeIndex = nodeId + 1;
                    }
                    if (hx.contains(nodeId)) {
                        graphVisit.put(temp[1], new HashSet<>());
                    }
                }
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // store all the rules for each caption: any caption may be paused, and we will need the rules to resume expanding
        HashMap<String, ArrayList<RewriteRule>> capRules = new HashMap<>();
        HashMap<String, HashSet<String>> done = new HashMap<>(); // records which caption + full strings have already been expanded
        oldNodeIndex = nextNodeIndex - 1;
        boolean newNode;
        imgList = new HashSet<>();
        // generate the graph
        for (RewriteCaption cap : captions) {
            String capId = cap.getId();
            String imgId = capId.split("#")[0];
            imgList.add(imgId);
            // store the rules for this caption
            if (debugPrint) {
                out.println("caption: " + capId);
            }
            done.putIfAbsent(capId, new HashSet<>());
            capRules.putIfAbsent(capId, new ArrayList<>(cap.getRules()));

            // create a work queue for all calls to generateSentences
            // We have only positive rewrite rules; they use one-step expansion (limit -1)

            LinkedBlockingQueue<RewriteCaption> queue = new LinkedBlockingQueue<>();
            for (Chunk sent : cap.getRoots()) {
                RewriteCaption rCap = new RewriteCaption(capId, cap.getNextTokenId(), sent);
                rCap.addRules(new ArrayList<>(capRules.get(capId)));
                rCap.setExpansionLimit(-1);
                queue.add(rCap);
                if (debugPrint) {
                    out.println("add to queue " + sent);
                }
            }

            while (queue.size() > 0) {
                if (debugPrint) {
                    out.println(queue.size());
                }
                RewriteCaption nextCap = queue.poll();
                capId = nextCap.getId();
                Chunk sent = nextCap.getSent();
                String tokenStr = ArrayListUtils.intListToString(sent.toIds(), " ");
                int expansionLimit = nextCap.getExpansionLimit();
                if (debugPrint) {
                    out.println("take from queue " + capId + " " + sent.toString() + " " + tokenStr + " " + expansionLimit);
                }

                // check if we've already done this expansion
                if (done.get(capId).contains(tokenStr)) {
                    if (debugPrint) {
                        out.println("already done");
                    }
                    continue;
                }

                // check if this initial phrase has been visited
                String kStrSent = sent.toBareString();
                if (isBadNode(kStrSent)) {
                    continue;
                }
                if (debugPrint) {
                    out.println("*source node " + kStrSent);
                }
                String idsTemp = capId + "," + ArrayListUtils.intListToString(sent.toIds(), " ");
                if (debugPrint) {
                    out.println("sids " + idsTemp);
                }

                /* don't want to compute expanded edges for this node yet */
                newNode = false;
                if (isNewNode(out, graphVisit.get(kStrSent), add, kStrSent)) {

                    newNode = true;
                    if (debugPrint) {
                        out.println("string not generated before");
                    }
                    // if not, record that this node + full string reached the new node
                    // next time a new expansion reaches this node, we'll continue expanding
                    graphSent.put(kStrSent, new ArrayList<>());
                    if (debugPrint) {
                        out.println("clear record " + kStrSent);
                    }
                    graphSent.get(kStrSent).add(capId);
                    graphState.put(kStrSent, new ArrayList<>());
                    graphState.get(kStrSent).add(sent);
                    // record this node as visited
                    graphVisit.putIfAbsent(kStrSent, new HashSet<>());

                    // create data structures for this node
                    if (nodeStringToIndex.get(kStrSent) == null) {
                        if (debugPrint) {
                            out.println("new node " + nextNodeIndex + " *" + kStrSent + "*");
                        }
                        nodeStringToIndex.put(kStrSent, nextNodeIndex);
                        nodeIndexToString.put(nextNodeIndex, kStrSent);
                        nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                        child.put(nextNodeIndex, new HashSet<>());
                        nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                        addNodeTokens(capId, sent, nextNodeIndex);
                        nextNodeIndex++;
                    }
                    else {
                        addNodeTokens(capId, sent, nodeStringToIndex.get(kStrSent));
                    }

                }
                // create data structures for this node
                if (nodeStringToIndex.get(kStrSent) == null) {
                    if (debugPrint) {
                        out.println("new node " + nextNodeIndex + " *" + kStrSent + "*");
                    }
                    nodeStringToIndex.put(kStrSent, nextNodeIndex);
                    nodeIndexToString.put(nextNodeIndex, kStrSent);
                    nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                    child.put(nextNodeIndex, new HashSet<>());
                    nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                    addNodeTokens(capId, sent, nextNodeIndex);
                    nextNodeIndex++;
                }
                else {
                    addNodeTokens(capId, sent, nodeStringToIndex.get(kStrSent));
                }
                // record that the caption can produce this string

                int sId = nodeStringToIndex.get(kStrSent);
                nodeToCapMap.get(sId).add(capId);
                capToNodeMap.putIfAbsent(capId, new HashSet<>());
                capToNodeMap.get(capId).add(sId);
                // note that particular chunking produces this string
                nodeToChunkToCapMap.putIfAbsent(sId, new HashMap<>());
                nodeToChunkToCapMap.get(sId).putIfAbsent(sent.toStringNoIndex(), new HashSet<>());
                nodeToChunkToCapMap.get(sId).get(sent.toStringNoIndex()).add(capId);
                if (newNode) {
                    continue;
                }

                if (!done.get(capId).contains(tokenStr)) {
                    done.get(capId).add(tokenStr);
                    if (debugPrint) {
                        out.println("add to done");
                    }
                }

                // generate the sentence
                HashSet<String> usables = new HashSet<>();
                usables.add("ALL");
                HashMap<Chunk, HashMap<String, HashSet<Chunk>>> edgesTemp = nextCap.generateSentences(usables, expansionLimit);
                if (debugPrint) {
                    out.println("generating sentences " + nextCap.toString());
                }

                // process all the source nodes in the returned edges
                for (Chunk k : edgesTemp.keySet()) {
                    String kStr = k.toBareString();
                    if (kStr.equals("")) {
                        continue;
                    }
                    if (debugPrint) {
                        out.println("*source node " + kStr);
                    }
                    String ids = capId + "," + ArrayListUtils.intListToString(k.toIds(), " ");
                    if (debugPrint) {
                        out.println("sids " + ids);
                    }

                    if (isNewPathToVisitedNode(out, graphVisit.get(kStr), ids, add, kStr)) {
                        // if we haven't generated this node from this particular caption + full string before
                        // need to do positive rewrite rule expansion, since there exists another way to reach this node
                        // and the visual denotation of this string could be > 1
                        if (debugPrint) {
                            out.println("not generated before with this cap/string");
                        }

                        // check if we have a stored caption/full string that also need expanding
                        if (graphSent.get(kStr) != null) {
                            String capIdStored = graphSent.get(kStr).get(0);
                            if (!capIdStored.equals(capId)) {
                                for (int i = 0; i < graphSent.get(kStr).size(); i++) {
                                    String idStore = graphSent.get(kStr).get(i);
                                    addNodeTokens(idStore, k, nodeStringToIndex.get(kStr));
                                    if (debugPrint) {
                                        out.println("found stored string " + kStr + " " + idStore);
                                    }
                                    RewriteCaption rCapNew = new RewriteCaption(idStore, nextCap.getNextTokenId(), graphState.get(kStr).get(i));
                                    rCapNew.addRules(capRules.get(idStore));
                                    rCapNew.setExpansionLimit(-1);
                                    queue.add(rCapNew);
                                    if (debugPrint) {
                                        out.println("add to queue " + rCapNew.toString());
                                    }
                                }
                                graphSent.remove(kStr);
                                graphState.remove(kStr);
                                if (debugPrint) {
                                    out.println("remove record k " + kStr);
                                }
                            }
                            else {
                                boolean isNew = true;
                                for (Chunk temp : graphState.get(kStr)) {
                                    if (k.equals(temp)) {
                                        isNew = false;
                                        break;
                                    }
                                }
                                if (isNew) {
                                    graphSent.get(kStr).add(capId);
                                    graphState.get(kStr).add(k);
                                }
                            }
                        }
                    }

                    // record this node as visited
                    graphVisit.putIfAbsent(kStrSent, new HashSet<>());
                    graphVisit.get(kStrSent).add(idsTemp);

                    // process the edges and destination nodes of the source node
                    for (String l : edgesTemp.get(k).keySet()) {
                        if (debugPrint) {
                            out.println("-edge " + l);
                        }
                        // genSentLOOP
                        boolean nextGenSentLoop = false;
                        for (Chunk t : edgesTemp.get(k).get(l)) {
                            if (nextGenSentLoop) {
                                break;
                            }
                            String tStr = t.toBareString();
                            if (isBadNode(tStr)) {
                                continue;
                            }
                            if (debugPrint) {
                                out.println("**destination node " + tStr);
                            }
                            String tIds = capId + "," + ArrayListUtils.intListToString(t.toIds(), " ");
                            if (debugPrint) {
                                out.println("tids " + tIds);
                            }

                            // if this is entirely new node, pause because visual denotation size is 1
                            if (isNewNode(out, graphVisit.get(tStr), add, tStr)) {
                                if (debugPrint) {
                                    out.println("pause new node");
                                }
                                graphVisit.put(tStr, new HashSet<>());
                                graphSent.put(tStr, new ArrayList<>());
                                if (debugPrint) {
                                    out.println("clear t " + tStr);
                                }
                                graphSent.get(tStr).add(capId);
                                graphState.put(tStr, new ArrayList<>());
                                graphState.get(tStr).add(t);
                                // create data structures for this node
                                if (nodeStringToIndex.get(tStr) == null) {
                                    if (debugPrint) {
                                        out.println("new node " + nextNodeIndex + " *" + tStr + "*");
                                    }
                                    nodeStringToIndex.put(tStr, nextNodeIndex);
                                    nodeIndexToString.put(nextNodeIndex, tStr);
                                    nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                                    child.put(nextNodeIndex, new HashSet<>());
                                    nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                                    addNodeTokens(capId, t, nextNodeIndex);
                                    nextNodeIndex++;
                                }
                                else {
                                    addNodeTokens(capId, t, nodeStringToIndex.get(tStr));
                                }
                            }
                            else if (isNewPathToVisitedNode(out, graphVisit.get(tStr), tIds, add, tStr)) {
                                // create data structures for this node
                                if (nodeStringToIndex.get(tStr) == null) {
                                    if (debugPrint) {
                                        out.println("new node " + nextNodeIndex + " *" + tStr + "*");
                                    }
                                    nodeStringToIndex.put(tStr, nextNodeIndex);
                                    nodeIndexToString.put(nextNodeIndex, tStr);
                                    nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                                    child.put(nextNodeIndex, new HashSet<>());
                                    nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                                    addNodeTokens(capId, t, nextNodeIndex);
                                    nextNodeIndex++;
                                }
                                else {
                                    addNodeTokens(capId, t, nodeStringToIndex.get(tStr));
                                }
                                // this is a string generated by applying a rewrite rule, and we've encountered an occupied node
                                // so we need to perform positive rewrite rule expansion
                                if (debugPrint) {
                                    out.println("old node, unvisited");
                                }
                                RewriteCaption rCapNext = new RewriteCaption(capId, nextCap.getNextTokenId(), t);
                                rCapNext.addRules(capRules.get(capId));
                                rCapNext.setExpansionLimit(-1);
                                queue.add(rCapNext);
                                if (debugPrint) {
                                    out.println("add to queue " + rCapNext.toString());
                                }
                                // if there was a paused caption at this node, continue processing it
                                if (graphSent.get(tStr) != null) {
                                    String capIdStored = graphSent.get(tStr).get(0);
                                    if (!capIdStored.equals(capId)) {
                                        for (int i = 0; i < graphSent.get(tStr).size(); i++) {
                                            String idStore = graphSent.get(tStr).get(i);
                                            addNodeTokens(idStore, t, nodeStringToIndex.get(tStr));
                                            if (debugPrint) {
                                                out.println("found stored string " + tStr + " " + idStore);
                                            }
                                            RewriteCaption rCapNew = new RewriteCaption(idStore, nextCap.getNextTokenId(), graphState.get(tStr).get(i));
                                            if (debugPrint) {
                                                out.println("add to queue " + rCapNew.toString());
                                            }
                                            rCapNew.addRules(capRules.get(idStore));
                                            rCapNew.setExpansionLimit(-1);
                                            queue.add(rCapNew);
                                        }
                                        // add incomplete edges
                                        if (incompleteEdges.get(tStr) != null) {
                                            for (String edgeStr : incompleteEdges.get(tStr)) {
                                                String[] edgeTemp = edgeStr.split("\t");
                                                int tId = nodeStringToIndex.get(edgeTemp[0]);
                                                int kId = nodeStringToIndex.get(edgeTemp[1]);
                                                edges.putIfAbsent(tId, new HashMap<>());
                                                edges.get(tId).putIfAbsent(kId, new HashMap<>());
                                                edges.get(tId).get(kId).putIfAbsent(edgeTemp[2], new HashSet<>());
                                                edges.get(tId).get(kId).get(edgeTemp[2]).add(edgeTemp[3]);
                                                String capIdTemp = edgeTemp[3].split("#")[0] + "#" + edgeTemp[3].split("#")[1];
                                                svonExpansion.get(capIdTemp).add(edgeTemp[2].split("/")[0]);
                                                if (debugPrint) {
                                                    out.println("store edge " + tId + " " + edgeTemp[2] + " " + kId + " " + edgeTemp[3]);
                                                }
                                            }
                                            incompleteEdges.remove(tStr);
                                        }
                                        graphSent.remove(tStr);
                                        graphState.remove(tStr);
                                        if (debugPrint) {
                                            out.println("remove record " + tStr);
                                        }
                                    }
                                    else {
                                        boolean isNew = true;
                                        for (Chunk temp : graphState.get(tStr)) {
                                            if (t.equals(temp)) {
                                                isNew = false;
                                                break;
                                            }
                                        }
                                        if (isNew) {
                                            graphSent.get(tStr).add(capId);
                                            graphState.get(tStr).add(t);
                                        }
                                    }
                                }
                            }
                            // create data structures for this node
                            if (nodeStringToIndex.get(tStr) == null) {
                                if (debugPrint) {
                                    out.println("new node " + nextNodeIndex + " *" + tStr + "*");
                                }
                                nodeStringToIndex.put(tStr, nextNodeIndex);
                                nodeIndexToString.put(nextNodeIndex, tStr);
                                nodeToCapMap.put(nextNodeIndex, new HashSet<>());
                                child.put(nextNodeIndex, new HashSet<>());
                                nodeToChunkToCapMap.put(nextNodeIndex, new HashMap<>());
                                addNodeTokens(capId, t, nextNodeIndex);
                                nextNodeIndex++;
                            }
                            else {
                                addNodeTokens(capId, t, nodeStringToIndex.get(tStr));
                            }
                            // record that we've visited this node
                            graphVisit.putIfAbsent(tStr, new HashSet<>());
                            graphVisit.get(tStr).add(tIds);
                            if (debugPrint) {
                                out.println("visited " + tStr + " " + tIds);
                            }
                            // record that the caption can produce this string
                            int tId = nodeStringToIndex.get(tStr);
                            nodeToCapMap.get(tId).add(capId);
                            capToNodeMap.putIfAbsent(capId, new HashSet<>());
                            capToNodeMap.get(capId).add(tId);
                            // note that particular chunking produces this string
                            nodeToChunkToCapMap.putIfAbsent(tId, new HashMap<>());
                            nodeToChunkToCapMap.get(tId).putIfAbsent(t.toStringNoIndex(), new HashSet<>());
                            nodeToChunkToCapMap.get(tId).get(t.toStringNoIndex()).add(capId);

                            // store the edge
                            if (tId != sId) {
                                String[] al = l.split(",");
                                String type = nextCap.getRules().get(Integer.parseInt(al[0])).getType().split("/")[0];
                                ArrayList<Integer> lid = new ArrayList<>();
                                String lType = type;

                                // check that we have matching types
                                // if not, abort: we don't handle this case
                                // build the link ID as well
                                for (String alStr : al) {
                                    String[] ax = nextCap.getRules().get(Integer.parseInt(alStr)).getType().split("/");
                                    if (!ax[0].equals(type)) {
                                        nextGenSentLoop = true;
                                        break;
                                    }
                                    lid.add(nextCap.getRules().get(Integer.parseInt(alStr)).getId());
                                    for (int i = 1; i < ax.length; i++) {
                                        lType += "/" + ax[i];
                                    }
                                }
                                Collections.sort(lid);
                                String lidStr = ArrayListUtils.intListToString(lid, ",");
                                String edgeStr = lType.substring(1);
                                edges.putIfAbsent(tId, new HashMap<>());
                                edges.get(tId).putIfAbsent(sId, new HashMap<>());
                                edges.get(tId).get(sId).putIfAbsent(edgeStr, new HashSet<>());
                                edges.get(tId).get(sId).get(edgeStr).add(capId + "#" + lidStr);
                                svonExpansion.get(capId).add(edgeStr.split("/")[0]);
                                if (debugPrint) {
                                    out.println("store edge " + tId + " " + edgeStr + " " + sId + " " + capId + "#" + lidStr);
                                }
                                child.get(sId).add(tId);
                            }
                        }
                    }
                }
            }
        }
        // find missing edges between existing nodes
        for (String parentStr : graphSent.keySet()) {
            if (nodeStringToIndex.get(parentStr) == null) {
                continue;
            }
            int parentId = nodeStringToIndex.get(parentStr);
            for (int i = 0; i < graphSent.get(parentStr).size(); i++) {
                String capId = graphSent.get(parentStr).get(i);
                Chunk parentChunk = graphState.get(parentStr).get(i);
                ArrayList<RewriteRule> rules = capRules.get(capId);
                RewriteCaption cap = new RewriteCaption(capId, 1, parentChunk);
                cap.setExpansionLimit(-1);
                cap.addRules(rules);
                HashSet<String> usables = new HashSet<>();
                usables.add("ALL");
                HashMap<Chunk, HashMap<String, HashSet<Chunk>>> edgesTemp = cap.generateSentences(usables, -1);
                for (Chunk k : edgesTemp.keySet()) {
                    String kStr = k.toBareString();
                    int sId = nodeStringToIndex.get(kStr);
                    if (kStr.equals("")) {
                        continue;
                    }
                    // process the edges and destination nodes of the source node
                    for (String l : edgesTemp.get(k).keySet()) {
                        for (Chunk t : edgesTemp.get(k).get(l)) {
                            String tStr = t.toBareString();
                            if (isBadNode(tStr)) {
                                continue;
                            }
                            if (nodeStringToIndex.get(tStr) != null) {
                                int tId = nodeStringToIndex.get(tStr);
                                if (edges.get(tId) != null && edges.get(tId).get(sId) != null && edges.get(tId).get(sId).size() > 0) {
                                    continue; // there's already an edge between these nodes
                                }
                                HashSet<String> tCapIds = nodeToCapMap.get(tId);
                                if (tCapIds.contains(capId) && tId != sId) { // same caption
                                    // add new edge
                                    String[] al = l.split(",");
                                    String type = cap.getRules().get(Integer.parseInt(al[0])).getType().split("/")[0];
                                    ArrayList<Integer> lid = new ArrayList<>();
                                    String lType = type;

                                    // check that we have matching types
                                    // if not, abort: we don't handle this case
                                    // build the link ID as well
                                    for (String alStr : al) {
                                        String[] ax = cap.getRules().get(Integer.parseInt(alStr)).getType().split("/");
                                        if (!ax[0].equals(type)) {
                                            break;
                                        }
                                        lid.add(cap.getRules().get(Integer.parseInt(alStr)).getId());
                                        for (int j = 1; j < ax.length; j++) {
                                            lType += "/" + ax[j];
                                        }
                                    }
                                    Collections.sort(lid);
                                    String lidStr = ArrayListUtils.intListToString(lid, ",");
                                    String edgeStr = lType.substring(1);
                                    edges.putIfAbsent(tId, new HashMap<>());
                                    edges.get(tId).putIfAbsent(sId, new HashMap<>());
                                    edges.get(tId).get(sId).putIfAbsent(edgeStr, new HashSet<>());
                                    edges.get(tId).get(sId).get(edgeStr).add(capId + "#" + lidStr);
                                    svonExpansion.get(capId).add(edgeStr.split("/")[0]);
                                    if (debugPrint) {
                                        out.println("ADDED NEW EDGE " + parentStr + " store edge " + tId + " " + tStr + " " + edgeStr + " " + sId + " " + parentStr + " " + capId + "#" + lidStr);
                                    }
                                    child.get(sId).add(tId);
                                }
                            }
                        }
                    }
                }
            }
        }
        out.close();
    }

    private boolean isNewNode(PrintWriter out, HashSet<String> graphVisit, boolean add, String nodeStr) {
        if (debugPrint) {
            out.println("new node?");
        }
        if (add) {
            if (graphVisit != null) {
                if (debugPrint) {
                    out.println("graph visit");
                }
                return false;
            }
            if (debugPrint) {
                out.println(nodeStringToIndex.get(nodeStr) == null);
            }
            return nodeStringToIndex.get(nodeStr) == null;
        }
        else {
            if (graphVisit != null) {
                if (debugPrint) {
                    out.println("graph visit");
                }
                return false;
            }
            if (debugPrint) {
                out.println("true");
            }
            return true;
        }
    }

    private boolean isNewPathToVisitedNode(PrintWriter out, HashSet<String> graphVisit, String ids, boolean add, String nodeStr) {
        if (debugPrint) {
            out.println("new path?");
        }
        if (graphVisit != null) {
            if (debugPrint) {
                out.println("graph visit " + !graphVisit.contains(ids));
            }
            return !graphVisit.contains(ids);
        }
        else if (add) {
            if (nodeStringToIndex.get(nodeStr) != null) {
                if (debugPrint) {
                    out.println(nodeStringToIndex.get(nodeStr) <= oldNodeIndex);
                }
                if (nodeStringToIndex.get(nodeStr) <= oldNodeIndex) {
                    return true;
                }
            }
            if (debugPrint) {
                out.println("false");
            }
            return false;
        }
        else {
            if (debugPrint) {
                out.println("false");
            }
            return false;
        }
    }

    private void addOrigEdges(ArrayList<RewriteCaption> finalCaps, ArrayList<RewriteCaption> preCaps, VP verbInfo) {
        origNodes = new HashMap<>();
        // load VPs
        HashMap<String, ArrayList<String>> vp = verbInfo.getVPs(); // capId -> list of VP ids

        // load root node for each caption
        HashMap<String, HashSet<String>> root = new HashMap<>();
        for (RewriteCaption cap : finalCaps) {
            String capId = cap.getId();
            root.putIfAbsent(capId, new HashSet<>());
            String str = cap.getSent().toBareString();
            if (nodeStringToIndex.get(str) != null) {
                root.get(capId).add(str);
            }
            else {
                addNode(cap.getSent(), capId);
                if (nodeStringToIndex.get(str) != null) {
                    root.get(capId).add(str);
                }
            }
        }

        // create subtree of all edges produced by a given caption
        HashMap<Integer, HashSet<Integer>> subtree = new HashMap<>();
        for (int childId : edges.keySet()) {
            for (int parentId : edges.get(childId).keySet()) {
                subtree.putIfAbsent(parentId, new HashSet<>());
                subtree.get(parentId).add(childId);
            }
        }

        // load the original captions: before applying rewrite rules, but after normalization
        // we will create a node for each, original caption and add ORIG edges to some nodes as well as
        // some SVO edges from the original caption
        capToOrigNodeMap = new HashMap<>();
        for (RewriteCaption cap : preCaps) {
            // for caption, we need to identify all of its leaf nodes
            // start by using the root node from initial.rewrite and search its children until we hit leaves
            String capId = cap.getId();
            origNodes.putIfAbsent(capId, new HashSet<>());
            if (root.get(capId) == null) {
                continue;
            }
            HashSet<String> roots = root.get(capId);
            HashSet<Integer> leaves = new HashSet<>();
            for (String rootStr : roots) {
                HashSet<Integer> visit = new HashSet<>();
                LinkedBlockingQueue<Integer> q = new LinkedBlockingQueue<>();
                q.add(nodeStringToIndex.get(rootStr)); // add this caption's root node
                while (q.size() > 0) {
                    int nodeId = q.poll();
                    int k = 0;
                    // get all children of this node that are generated by the caption
                    if (subtree.get(nodeId) != null) {
                        for (int childId : subtree.get(nodeId)) {
                            for (String edgeStr : edges.get(childId).get(nodeId).keySet()) {
                                if (edges.get(childId).get(nodeId).get(edgeStr).contains(capId)) {
                                    k++;
                                    if (!visit.contains(childId)) {
                                        visit.add(childId);
                                        q.add(childId);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    // if we have no children, this is a leaf
                    if (k == 0) {
                        leaves.add(nodeId);
                    }
                }
            }

            // add the original caption as a node
            int sId = addNode(new Chunk(cap.getSent().toString()), capId);
            if (sId == -1) {
                continue;
            }
            origNodes.get(capId).add(sId);
            capToOrigNodeMap.put(capId, sId);
            // add ORIG edges from it to all of the leaf nodes
            for (int childId : leaves) {
                addLink(childId, "ORIG", sId, capId);
            }

            // add NPs
            ArrayList<Chunk> chunks = new ArrayList<>();
            if (cap.getSent().getType().equals("SENT")) {
                chunks = cap.getSent().getChunks();
            } else {
                chunks.add(cap.getSent());
            }
            for (Chunk c : chunks) {
                if (c.getType().equals("EN")) {
                    int nodeId = addNode(c, capId);
                    origNodes.get(capId).add(nodeId);
                    if (nodeId != -1) {
                        addLink(nodeId, "SENT", sId, capId);
                    }
                }
            }
            // get SVO triples from original caption
            if (vp.get(capId) == null) {
                continue;
            }
            int numVPs = vp.get(capId).size();
            for (int i = 0; i < numVPs; i++) {
                // grab the ith SVO triple
                String str = verbInfo.getVP(cap, i);
                String[] temp = str.split("\t");
                int subjIdx = Integer.parseInt(temp[0]);
                int vpIdx = Integer.parseInt(temp[1]);
                int dobjIdx = Integer.parseInt(temp[2]);

                // if component is missing, skip
                // todo this probably shouldn't happen - print error msg
                if (vpIdx == -2 || dobjIdx == -2 || subjIdx == -2) {
                    continue;
                }

                // find beginning and end of VP
                int vpE = vpIdx;
                boolean drop = false;
                if (dobjIdx != -1) {
                    vpE = dobjIdx;
                }

                // grab simple sentence
                ArrayList<String> az = new ArrayList<>();
                ArrayList<Chunk> chunksVP = new ArrayList<>();
                String verb = "";
                if (cap.getSent().getType().equals("SENT")) {
                    chunksVP = cap.getSent().getChunks();
                }
                else {
                    chunksVP.add(cap.getSent());
                }
                for (int k = 0; k < chunksVP.size(); k++) {
                    if (k == subjIdx || (k >= vpIdx && k <= vpE)) {
                        az.add(chunksVP.get(k).toString());
                        verb = chunksVP.get(k).getLastToken().getPrevToken().getStr();
                    }
                    else {
                        drop = true;
                    }
                }

                // if we dropped anything, add a COMPLEX or COMPLEX-VERB link
                int simpleN = sId;
                if (drop) {
                    if (!verb.equals("be") && !(verb.equals("wear") && dobjIdx == -1)) {
                        int tId = addNode(new Chunk(ArrayListUtils.stringListToString(az, " ")), capId);
                        origNodes.get(capId).add(tId);
                        if (tId != -1) {
                            if (subjIdx == -1) { // no subject, so add COMPLEX-VERB link (parent verb phrase, child complex sentence)
                                addLink(tId, "COMPLEX-VERB", sId, capId);
                            } else { // subject found, so add COMPLEX link (parent simple sentence, child complex sentence)
                                addLink(tId, "COMPLEX", sId, capId);
                                simpleN = tId;
                                verb = nodeIndexToString.get(tId);
                            }
                        }
                    }
                }

                // if there's a subject, make subject-verb phrase extraction edges
                // the resulting nodes should already exist in NP and VP subgraphs
                int vpN = simpleN;
                if (subjIdx != -1) {
                    az = new ArrayList<>();
                    for (int k = 0; k < chunksVP.size(); k++) {
                        if (k == subjIdx) {
                            az.add(chunksVP.get(k).toString());
                        }
                    }
                    if (!verb.endsWith(" be") && !verb.endsWith(" wear")) {
                        int tId = addNode(new Chunk(ArrayListUtils.stringListToString(az, " ")), capId);
                        origNodes.get(capId).add(tId);
                        if (tId != -1) {
                            addLink(tId, "VERB", simpleN, capId);
                        }
                    }
                    az = new ArrayList<>();
                    for (int k = 0; k < chunksVP.size(); k++) {
                        if (k >= vpIdx && k <= vpE) {
                            az.add(chunksVP.get(k).toString());
                        }
                    }
                    if (!verb.endsWith(" be") && !verb.endsWith(" wear")) {
                        int tId = addNode(new Chunk(ArrayListUtils.stringListToString(az, " ")), capId);
                        origNodes.get(capId).add(tId);
                        if (tId != -1) {
                            addLink(tId, "SUBJ", simpleN, capId);
                            vpN = tId;
                        }
                    }
                }
                // if there's a direct object, extract it
                if (dobjIdx != -1) {
                    // add TVERB edge (dobj parent, verb + dobj child)
                    az = new ArrayList<>();
                    for (int k = 0; k < chunksVP.size(); k++) {
                        if (k == dobjIdx) {
                            az.add(chunksVP.get(k).toString());
                        }
                    }
                    int tId = addNode(new Chunk(ArrayListUtils.stringListToString(az, " ")), capId);
                    origNodes.get(capId).add(tId);
                    if (tId != -1) {
                        addLink(tId, "TVERB", vpN, capId);
                    }
                    // add DOBJ edge (verb parent, verb + dobj child)
                    az = new ArrayList<>();
                    verb = "";
                    for (int k = 0; k < chunksVP.size(); k++) {
                        if (k == vpIdx) {
                            az.add(chunksVP.get(k).toString());
                            verb = chunksVP.get(k).getLastToken().getPrevToken().getStr();
                        }
                    }
                    if (!verb.equals("be") && !verb.equals("wear")) {
                        tId = addNode(new Chunk(ArrayListUtils.stringListToString(az, " ")), capId);
                        origNodes.get(capId).add(tId);
                        if (tId != -1) {
                            addLink(tId, "DOBJ", vpN, capId);
                        }
                    }
                }
            }
        }
    }

    private int addNode(Chunk c, String capId) {
        // check if we need to assign a new index
        String str = c.toBareString();
        if (str.equals("")) {
            return -1;
        }
        String[] tokens = str.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                if (tokens[i].equals(tokens[i-1])) {
                    return -1; // don't create node with "word word" duplication
                }
            }
        }
        if (tokens.length == 2 && tokens[0].equals(tokens[1])) {
            return -1; // don't create "word word" node
        }
        if (nodeStringToIndex.get(str) == null) {
            nodeStringToIndex.put(str, nextNodeIndex);
            nodeIndexToString.put(nextNodeIndex, str);
            addNodeTokens(capId, c, nextNodeIndex);
            nextNodeIndex++;
        }
        else {
            addNodeTokens(capId, c, nodeStringToIndex.get(str));
        }
        int nodeId = nodeStringToIndex.get(str);

        // add chunking information
        nodeToChunkToCapMap.putIfAbsent(nodeId, new HashMap<>());
        String chunkStr = c.toStringNoIndex();
        nodeToChunkToCapMap.get(nodeId).putIfAbsent(chunkStr, new HashSet<>());
        nodeToChunkToCapMap.get(nodeId).get(chunkStr).add(capId);

        // add the caption to the node's node-caption map
        nodeToCapMap.putIfAbsent(nodeId, new HashSet<>());
        nodeToCapMap.get(nodeId).add(capId);
        capToNodeMap.putIfAbsent(capId, new HashSet<>());
        capToNodeMap.get(capId).add(nodeId);
        return nodeId;
    }

    private boolean isBadNode(String s) {
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

    // potentially add a new edge to the denotation graph
    private void addLink(int parentId, String edgeStr, int childId, String capId) {
        // make sure this isn't a looping edge
        if (childId != parentId) {
            // add the edge and caption that generates the edge
            edges.putIfAbsent(childId, new HashMap<>());
            edges.get(childId).putIfAbsent(parentId, new HashMap<>());
            edges.get(childId).get(parentId).putIfAbsent(edgeStr, new HashSet<>());
            edges.get(childId).get(parentId).get(edgeStr).add(capId);
            svonExpansion.putIfAbsent(capId, new HashSet<>());
            svonExpansion.get(capId).add(edgeStr.split("/")[0]);
            nodeToCapMap.putIfAbsent(childId, new HashSet<>());
            nodeToCapMap.get(childId).add(capId);
            nodeToCapMap.putIfAbsent(parentId, new HashSet<>());
            nodeToCapMap.get(parentId).add(capId);
            capToNodeMap.putIfAbsent(capId, new HashSet<>());
            capToNodeMap.get(capId).add(childId);
            capToNodeMap.get(capId).add(parentId);
        }
    }

    private void propagateImages() {
        HashSet<Integer> incorrect = new HashSet<>(); // set of all potentially incorrect nodes
        // iterate over edges. Any parent node is potentially incorrect
        HashMap<Integer, HashSet<Integer>> edgesTemp = new HashMap<>();
        for (int childId : edges.keySet()) {
            for (int parentId : edges.get(childId).keySet()) {
                edgesTemp.putIfAbsent(parentId, new HashSet<>());
                edgesTemp.get(parentId).add(childId);
                incorrect.add(parentId);
            }
        }
        // go through incorrect nodes and check for any without incorrect children
        // we can update these nodes as correct (remove from HashSet)
        int old = -1;
        HashSet<Integer> addedChild = new HashSet<>();
        while (incorrect.size() > 0) {
            // check for cycle
            if (incorrect.size() == old) {
                System.out.println("CYCLE");
                for (int nodeId : incorrect) {
                    System.out.println(nodeId + "/" + nodeIndexToString.get(nodeId));
                    for (String capId : nodeToCapMap.get(nodeId)) {
                        System.out.println("\t" + capId);
                    }
                }
                System.out.println();
                System.exit(1);
            }
            else {
                old = incorrect.size();
            }

            // for each incorrect node
            for (Iterator<Integer> iterator = incorrect.iterator(); iterator.hasNext();) {
                int nodeId = iterator.next();
                // check if there are incorrect children
                boolean good = true;
                HashSet<Integer> children = edgesTemp.get(nodeId);
                for (int childId : children) {
                    if (incorrect.contains(childId)) {
                        good = false;
                        break;
                    }
                }
                // if not, update the node's captions and flag as correct
                if (good) {
                    iterator.remove();
                    for (int childId : children) {
                        nodeToCapMap.get(childId).stream().filter(capId -> !nodeToCapMap.get(nodeId).contains(capId)).forEach(capId -> {
                            // add this caption to node->cap map
                            nodeToCapMap.get(nodeId).add(capId);
                            capToNodeMap.putIfAbsent(capId, new HashSet<>());
                            capToNodeMap.get(capId).add(nodeId);
                            if (!addedChild.contains(childId)) {
                                String edge = nodeIndexToString.get(childId) + " -> " + nodeIndexToString.get(nodeId);
                                propagated.putIfAbsent(edge, new HashSet<>());
                                propagated.get(edge).add(capId);
                            }
                            addedChild.add(nodeId);
                        });
                    }
                }
            }
        }
    }

    // create train, test, dev subgraphs from the defined splits
    void makeSubgraphs(String dir, String corpus) {
        imgListSubgraph = new HashMap<>();
        nodeToCapMapSubgraph = new HashMap<>();
        capToNodeMapSubgraph = new HashMap<>();
        nodeIndexToStringSubgraph = new HashMap<>();
        nodeStringToIndexSubgraph = new HashMap<>();
        capToTokenStrMapSubgraph = new HashMap<>();
        nodeToChunkToCapMapSubgraph = new HashMap<>();
        edgesSubgraph = new HashMap<>();
        if (capToTokenStrMap == null) {
            readTokens(dir, corpus);
        }
        ArrayList<String> splits = new ArrayList<>();
        splits.add("train");
        splits.add("dev");
        splits.add("test");
        try {
            for (String split : splits) {
                File splitFile = new File(dir + "/" + corpus + "/img_" + split + ".lst");
                if (splitFile.exists()) {
                    makeSubgraph(split, dir + "/" + corpus + "/img_" + split + ".lst");
                    if (split.equals("train")) {
                        countNodes(10, 1);
                        calcPMI();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readTokens(String dir, String corpus) {
        capToTokenStrMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir + "/" + corpus + "/graph/token.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                capToTokenStrMap.put(tokens[0], tokens[1]);
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeSubgraph(String split, String filename) {
        // get the images that are in this subgraph
        imgListSubgraph.put(split, new HashSet<>());
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (imgList.contains(line)) { // check that image is also in the main denotation graph
                    imgListSubgraph.get(split).add(line);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // get subset of tokenized strings for this subgraph
        capToTokenStrMapSubgraph.put(split, new HashMap<>());
        for (String capId : capToTokenStrMap.keySet()) {
            String imgId = capId.split("#")[0];
            if (imgListSubgraph.get(split).contains(imgId)) {
                capToTokenStrMapSubgraph.get(split).put(capId, capToTokenStrMap.get(capId));
            }
        }

        // caption to node map: only use captions of images we're keeping
        HashSet<Integer> nodeTemp = new HashSet<>();
        capToNodeMapSubgraph.put(split, new HashMap<>());
        for (String capId : capToNodeMap.keySet()) {
            String imgId = capId.split("#")[0];
            if (imgListSubgraph.get(split).contains(imgId)) {
                capToNodeMapSubgraph.get(split).put(capId, new HashSet<>(capToNodeMap.get(capId)));
                nodeTemp.addAll(capToNodeMap.get(capId));
            }
        }

        // node to caption map: check if it's a node we're keeping
        // and remove captions for images we're not using
        nodeToCapMapSubgraph.put(split, new HashMap<>());
        nodeToCapMap.keySet().stream().filter(nodeTemp::contains).forEach(nodeId -> {
            nodeToCapMapSubgraph.get(split).put(nodeId, new HashSet<>());
            for (String capId : nodeToCapMap.get(nodeId)) {
                String imgId = capId.split("#")[0];
                if (imgListSubgraph.get(split).contains(imgId)) {
                    nodeToCapMapSubgraph.get(split).get(nodeId).add(capId);
                }
            }
        });

        // node index
        nodeIndexToStringSubgraph.put(split, new HashMap<>());
        nodeStringToIndexSubgraph.put(split, new HashMap<>());
        nodeIndexToString.keySet().stream().filter(nodeTemp::contains).forEach(nodeId -> {
            nodeIndexToStringSubgraph.get(split).put(nodeId, nodeIndexToString.get(nodeId));
            nodeStringToIndexSubgraph.get(split).put(nodeIndexToString.get(nodeId), nodeId);
        });

        // type-chunk: check if it's a node we're keeping; remove captions for images we don't use
        nodeToChunkToCapMapSubgraph.put(split, new HashMap<>());
        nodeToChunkToCapMap.keySet().stream().filter(nodeTemp::contains).forEach(nodeId -> {
            nodeToChunkToCapMapSubgraph.get(split).put(nodeId, new HashMap<>());
            for (String c : nodeToChunkToCapMap.get(nodeId).keySet()) {
                nodeToChunkToCapMapSubgraph.get(split).get(nodeId).put(c, new HashSet<>());
                for (String capId : nodeToChunkToCapMap.get(nodeId).get(c)) {
                    String imgId = capId.split("#")[0];
                    if (imgListSubgraph.get(split).contains(imgId)) {
                        nodeToChunkToCapMapSubgraph.get(split).get(nodeId).get(c).add(capId);
                    }
                }
            }
        });

        // edges: keep edges that link nodes we're keeping
        // only save captions of those edges if they're for an image we're keeping
        edgesSubgraph.put(split, new HashMap<>());
        for (int childId : edges.keySet()) {
            edges.get(childId).keySet().stream().filter(parentId -> nodeTemp.contains(childId) && nodeTemp.contains(parentId)).forEach(parentId -> {
                edgesSubgraph.get(split).putIfAbsent(childId, new HashMap<>());
                edgesSubgraph.get(split).get(childId).putIfAbsent(parentId, new HashMap<>());
                for (String edgeStr : edgesSubgraph.get(split).get(childId).get(parentId).keySet()) {
                    edgesSubgraph.get(split).get(childId).get(parentId).putIfAbsent(edgeStr, new HashSet<>());
                    for (String capId : edgesSubgraph.get(split).get(childId).get(parentId).get(edgeStr)) {
                        String imgId = capId.split("#")[0];
                        if (imgListSubgraph.get(split).contains(imgId)) {
                            edgesSubgraph.get(split).get(childId).get(parentId).get(edgeStr).add(capId);
                        }
                    }
                }
            });
        }
    }

    // assume we only ever do this for the train split of the graph
    private void countNodesExtend(String dir, int threshold) {
        String[] tokensTemp = dir.split("/");
        String corpus = tokensTemp[tokensTemp.length-1];
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir + "/" + corpus + ".split"));
            String line;
            String trainFilename = "";
            while ((line = br.readLine()) != null) {
                String[] temp = line.trim().split("\t");
                //makeSubgraph(temp[0], dir + "/" + corpus + "/" + temp[1]);
                if (temp[0].equals("train")) {
                    trainFilename = dir + "/" + temp[1];
                    //countNodes(10, 1);
                    //calcPMI();
                }
            }
            br.close();
            // get the images that are in training subgraph
            imgListSubgraph = new HashMap<>();
            imgListSubgraph.put("train", new HashSet<>());
            br = new BufferedReader(new FileReader(trainFilename));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                imgListSubgraph.get("train").add(line);
            }
            br.close();
            br = new BufferedReader(new FileReader(dir + "/graph/node-cap.map"));
            nodeToImageCount = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                int size = 0;
                for (int i = 1; i < tokens.length; i++) {
                    String imgId = tokens[i].split("#")[0];
                    if (imgListSubgraph.get("train").contains(imgId)) {
                        size++;
                    }
                }
                if (size >= threshold) {
                    nodeToImageCount.put(nodeId, size);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // assume we only ever do this for the train split of the graph
    private void countNodes(int threshold, int coThreshold) {
        nodeToImageCount = new HashMap<>();
        HashMap<String, HashSet<Integer>> imageNodes = new HashMap<>();
        // count the number of images for each node
        for (int nodeId : nodeToCapMapSubgraph.get("train").keySet()) {
            HashSet<String> imageIds = nodeToCapMapSubgraph.get("train").get(nodeId).stream().map(capId -> capId.split("#")[0]).collect(Collectors.toCollection(HashSet::new));
            int n = imageIds.size();
            if (n >= threshold) { // check that size of visual denotation is above the threshold
                nodeToImageCount.put(nodeId, n);
                for (String capId : nodeToCapMapSubgraph.get("train").get(nodeId)) {
                    String imgId = capId.split("#")[0];
                    imageNodes.computeIfAbsent(imgId, k -> new HashSet<>());
                    imageNodes.get(imgId).add(nodeId);
                }
            }
        }

        // generate co-occurrence counts
        nodeCooccurCount = new HashMap<>();
        for (String imgId : imageNodes.keySet()) {
            ArrayList<Integer> nodes = new ArrayList<>(imageNodes.get(imgId));
            Collections.sort(nodes);
            for (int i = 0; i < nodes.size() - 1; i++) {
                int iNode = nodes.get(i);
                nodeCooccurCount.computeIfAbsent(iNode, k -> new HashMap<>());
                for (int j = i + 1; j < nodes.size(); j++) {
                    int jNode = nodes.get(j);
                    nodeCooccurCount.get(iNode).putIfAbsent(jNode, 0);
                    int count = nodeCooccurCount.get(iNode).get(jNode);
                    nodeCooccurCount.get(iNode).put(jNode, count + 1);
                }
            }
        }

        // delete cooccurrence counts that are below the threshold
        for (int iNode : nodeCooccurCount.keySet()) {
            nodeCooccurCount.get(iNode).keySet().stream().filter(jNode -> nodeCooccurCount.get(iNode).get(jNode) < coThreshold).forEach(jNode -> {
                nodeCooccurCount.get(iNode).remove(jNode);
                if (nodeCooccurCount.get(iNode).size() == 0) {
                    nodeCooccurCount.remove(iNode);
                }
            });
        }
    }

    // assume we only do this for the train split of the graph
    private void calcPMI() {
        pmi = new HashMap<>();
        cpr = new HashMap<>();
        int n = imgListSubgraph.get("train").size(); // n = total number of images the denotation graph is over
        // for each pair of nodes (that pass the thresholds)
        for (int n1 : nodeCooccurCount.keySet()) {
            // p(x) = <# images in denotation of x> / n
            double px = nodeToImageCount.get(n1) / (double)n;
            for (int n2 : nodeCooccurCount.get(n1).keySet()) {
                double py = nodeToImageCount.get(n2) / (double) n;
                double pxy = nodeCooccurCount.get(n1).get(n2) / (double) n;
                // pmi(x,y) = log(p(x,y) / (p(x) * p(y)))
                double pmiVal = Math.log(pxy / (px * py));
                // normalize pmi
                double npmi = pmiVal / (-1 * Math.log(pxy));
                // store pmi and cpr
                if (n1 < n2) {
                    pmi.putIfAbsent(n1, new HashMap<>());
                    pmi.get(n1).put(n2, npmi);
                }
                else {
                    pmi.putIfAbsent(n2, new HashMap<>());
                    pmi.get(n2).put(n1, npmi);
                }
                cpr.putIfAbsent(n1, new HashMap<>());
                cpr.putIfAbsent(n2, new HashMap<>());
                cpr.get(n1).put(n2, nodeCooccurCount.get(n1).get(n2) / (double) nodeToImageCount.get(n2)); // p(n1 | n2)
                cpr.get(n2).put(n1, nodeCooccurCount.get(n1).get(n2) / (double) nodeToImageCount.get(n1)); // p(n2 | n1)
            }
        }
    }

    private ArrayList<String> getNodeImg(int id) {
        if (nodeToImageMap == null) {
            nodeToImageMap = new HashMap<>();
        }
        if (nodeToCapMap.get(id) == null) { // this node has no captions/images
            return new ArrayList<>();
        }
        else if (nodeToImageMap.get(id) != null) { // already stored this node's images
            ArrayList<String> list = new ArrayList<>(nodeToCapMap.get(id));
            Collections.sort(list);
            return list;
        }
        // get this node's images from the caption map
        HashSet<String> caps = nodeToCapMap.get(id);
        HashSet<String> img = caps.stream().map(capId -> capId.split("#")[0]).collect(Collectors.toCollection(HashSet::new));
        nodeToImageMap.put(id, nodeToCapMap.get(id)); // store in image map
        ArrayList<String> list = new ArrayList<>(img);
        Collections.sort(list);
        return list;
    }

    private String typeChunk(String chunkStr) {
        String type = "SN";
        boolean onlyEN = false;
        if (chunkStr.startsWith("[PP")) {
            type = "PP";
        }
        else if (chunkStr.startsWith("[VP")) {
            String[] tokens = chunkStr.split("\\s+");
            int depth = 0;
            boolean and = false;
            boolean foundEN = false;
            for (String token : tokens) {
                if (depth == 0) {
                    if (token.equals("and")) {
                        and = true;
                    } else if (token.equals("[EN")) {
                        foundEN = true;
                    }
                }
                if (token.startsWith("[")) {
                    depth++;
                } else if (token.equals("]")) {
                    depth--;
                }
            }
            if (and && foundEN) {
                type = "SN";
            }
            else {
                type = "VP";
            }
        }
        else if (chunkStr.startsWith("[EN")) {
            String[] tokens = chunkStr.split("\\s+");
            int depth = 0;
            for (int i = 0; i < tokens.length; i++) {
                if (tokens[i].startsWith("[")) {
                    depth++;
                }
                else if (tokens[i].equals("]")) {
                    depth--;
                    if (depth == 0) {
                        if (i == tokens.length-1) {
                            onlyEN = true;
                        }
                        break;
                    }
                }
            }
        }
        if (onlyEN) {
            type = "EN";
        }
        return type;
    }

    // VP node: starts with VP chunk
    // EN node: consists of a single EN chunk
    // SN node: everything else
    void typeChunkAll() {
        for (int nodeId : nodeToChunkToCapMap.keySet()) {
            nodeToChunkToChunkTypeMap.put(nodeId, new HashMap<>());
            for (String c : nodeToChunkToCapMap.get(nodeId).keySet()) {
                String type = typeChunk(c);
                nodeToChunkToChunkTypeMap.get(nodeId).put(c, type);
            }
        }
    }


    void printGraph(String extendFilename, String dir) {
        // count images from old graph
        if (!extendFilename.equals("") && nodeToImageCount == null) {
            countNodesExtend(extendFilename, 10);
        }
        ArrayList<Integer> nodes = new ArrayList<>(nodeIndexToString.keySet());
        Collections.sort(nodes);
        // images
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/img.lst"));
            imgList.forEach(out::println);
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // nodes
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/node.idx"));
            for (int i : nodes) {
                out.println(i + "\t" + nodeIndexToString.get(i));
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // node-tree
        try {
            PrintWriter out = new PrintWriter(new File(dir + "/node-tree.txt"));
            for (int childId : edges.keySet()) {
                for (int parentId : edges.get(childId).keySet()) {
                    for (String edgeStr : edges.get(childId).get(parentId).keySet()) {
                        out.print(childId+"\t"+edgeStr+"\t"+parentId);
                        for (String capId : edges.get(childId).get(parentId).get(edgeStr)) {
                            out.print("\t"+capId);
                        }
                        out.println();
                    }
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // node-cap.map
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/node-cap.map"));
            for (int i : nodes) {
                out.print(i);
                for (String capId : nodeToCapMap.get(i)) {
                    out.print("\t" + capId);
                }
                out.println();
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // node tokens
        try {
            PrintWriter out = new PrintWriter(new File(dir + "/cap-node.coref"));
            for (String capId : nodeTokens.keySet()) {
                for (String entry : nodeTokens.get(capId)) {
                    out.println(capId + "\t" + entry);
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // chunking
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/node-chunk.txt"));
            for (int i : nodes) {
                if (nodeToChunkToCapMap.get(i) == null) {
                    continue;
                }
                for (String chunkStr : nodeToChunkToCapMap.get(i).keySet()) {
                    out.print(i + "\t" + chunkStr);
                    for (String capId : nodeToChunkToCapMap.get(i).get(chunkStr)) {
                        out.print("\t" + capId);
                    }
                    out.println();
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // chunk type
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/type-chunk.txt"));
            for (int i : nodes) {
                if (nodeToChunkToChunkTypeMap.get(i) == null || nodeToChunkToCapMap.get(i) == null) {
                    continue;
                }
                for (String chunkStr : nodeToChunkToChunkTypeMap.get(i).keySet()) {
                    out.print(i + "\t" + nodeToChunkToChunkTypeMap.get(i).get(chunkStr) + "\t" + chunkStr);
                    for (String capId : nodeToChunkToCapMap.get(i).get(chunkStr)) {
                        out.print("\t" + capId);
                    }
                    out.println();
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // orig edges
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/cap-orig.map"));
            if (capToOrigNodeMap != null) {
                for (String capId : capToOrigNodeMap.keySet()) {
                    out.println(capId + "\t" + capToOrigNodeMap.get(capId));
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // node-image map
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/node-img.map"));
            for (int i : nodes) {
                out.print(i);
                ArrayList<String> images = getNodeImg(i);
                for (String img : images) {
                    out.print("\t" + img);
                }
                out.println();
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // cap-node map
        try {
            PrintWriter out = new PrintWriter(new File(dir+"/cap-node.map"));
            if (capToNodeMap != null) {
                for (String capId : capToNodeMap.keySet()) {
                    out.print(capId);
                    for (int i : capToNodeMap.get(capId)) {
                        out.print("\t" + i);
                    }
                    out.println();
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // subgraphs
        if (nodeIndexToStringSubgraph != null) {
            for (String subgraph : nodeIndexToStringSubgraph.keySet()) {
                File file = new File(dir+"/"+subgraph);
                if (!file.exists()) {
                    if (file.mkdir()) {
                        System.out.println(subgraph+" directory is created!");
                    } else {
                        System.out.println("Failed to create directory!");
                    }
                }
                nodes = new ArrayList<>(nodeIndexToStringSubgraph.get(subgraph).keySet());
                Collections.sort(nodes);
                // nodes
                try {
                    PrintWriter out = new PrintWriter(new File(dir + "/" + subgraph + "/node.idx"));
                    for (int i : nodes) {
                        out.println(i + "\t" + nodeIndexToStringSubgraph.get(subgraph).get(i));
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // image list
                try {
                    PrintWriter out = new PrintWriter(new File(dir + "/" + subgraph + "/img.lst"));
                    imgListSubgraph.get(subgraph).forEach(out::println);
                    out.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if (subgraph.equals("train")) {
                    // node count
                    try {
                        PrintWriter out = new PrintWriter(new File(dir + "/" + subgraph + "/node-image.cnt"));
                        for (int id1 : nodeCooccurCount.keySet()) {
                            for (int id2 : nodeCooccurCount.get(id1).keySet()) {
                                out.println(id1 + "\t" + id2 + "\t" + nodeToImageCount.get(id1) + "\t" + nodeToImageCount.get(id2) + "\t" + nodeCooccurCount.get(id1).get(id2));
                            }
                        }
                        out.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    // node PMI
                    try {
                        PrintWriter out = new PrintWriter(new File(dir + "/" + subgraph + "/node-image.pmi"));
                        for (int id1 : pmi.keySet()) {
                            for (int id2 : pmi.get(id1).keySet()) {
                                out.println(pmi.get(id1).get(id2) + "\t" + cpr.get(id1).get(id2) + "\t" + cpr.get(id2).get(id1) + "\t" + nodeToImageCount.get(id1) + "\t" + id1 + "\t" + nodeToImageCount.get(id2) + "\t" + id2);
                            }
                        }
                        out.close();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadGraph(String dir, String corpus) {
        String corpusDir = dir + "/" + corpus + "/graph/";
        // final simplified captions
        finalCaps = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/initial.rewrite"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                if (tokens.length != 3) {
                    continue;
                }
                finalCaps.add(new RewriteCaption(tokens[0], Integer.parseInt(tokens[1]), new Chunk(tokens[2])));
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // captions
        originalCaptions = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(dir + "/" + corpus + "/tmp/graph/pre.final"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                originalCaptions.add(new RewriteCaption(tokens[0], Integer.parseInt(tokens[1]), new Chunk(tokens[2])));
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // caption node tokens
        nodeTokens = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/cap-node.coref"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                String capId = tokens[0];
                nodeTokens.putIfAbsent(capId, new HashSet<>());
                nodeTokens.get(capId).add(tokens[1]+"\t"+tokens[2]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // image list
        imgList = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/img.lst"));
            String line;
            while ((line = br.readLine()) != null) {
                imgList.add(line.trim());
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // nodes
        nodeIndexToString = new HashMap<>();
        nodeStringToIndex = new HashMap<>();
        nextNodeIndex = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/node.idx"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                nodeIndexToString.put(nodeId, tokens[1]);
                nodeStringToIndex.put(tokens[1], nodeId);
                if (nodeId >= nextNodeIndex) {
                    nextNodeIndex = nodeId + 1;
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // node tree
        edges = new HashMap<>();
        svonExpansion = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/node-tree.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int childId = Integer.parseInt(tokens[0]);
                String str = tokens[1];
                int parentId = Integer.parseInt(tokens[2]);
                edges.putIfAbsent(childId, new HashMap<>());
                edges.get(childId).putIfAbsent(parentId, new HashMap<>());
                edges.get(childId).get(parentId).putIfAbsent(str, new HashSet<>());
                for (int i = 3; i < tokens.length; i++) {
                    edges.get(childId).get(parentId).get(str).add(tokens[i]);
                    String capIdTemp = tokens[i].split("#")[0] + "#" + tokens[i].split("#")[1];
                    svonExpansion.putIfAbsent(capIdTemp, new HashSet<>());
                    svonExpansion.get(capIdTemp).add(str.split("/")[0]);
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // node-cap map
        nodeToCapMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/node-cap.map"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                nodeToCapMap.put(nodeId, new HashSet<>());
                for (int i = 1; i < tokens.length; i++) {
                    nodeToCapMap.get(nodeId).add(tokens[i]);
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // cap-node.map
        capToNodeMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/cap-node.map"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                capToNodeMap.put(tokens[0], new HashSet<>());
                for (int i = 1; i < tokens.length; i++) {
                    capToNodeMap.get(tokens[0]).add(Integer.parseInt(tokens[i]));
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // chunking
        nodeToChunkToCapMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/node-chunk.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                nodeToChunkToCapMap.putIfAbsent(nodeId, new HashMap<>());
                nodeToChunkToCapMap.get(nodeId).put(tokens[1], new HashSet<>());
                for (int i = 2; i < tokens.length; i++) {
                    nodeToChunkToCapMap.get(nodeId).get(tokens[1]).add(tokens[i]);
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // chunk type
        nodeToChunkToChunkTypeMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/type-chunk.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                nodeToChunkToChunkTypeMap.putIfAbsent(nodeId, new HashMap<>());
                nodeToChunkToChunkTypeMap.get(nodeId).put(tokens[2], tokens[1]);
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // cap-orig
        capToOrigNodeMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/cap-orig.map"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                capToOrigNodeMap.put(tokens[0], Integer.parseInt(tokens[1]));
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // node-img
        nodeToImageMap = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(corpusDir + "/node-img.map"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.trim().split("\t");
                int nodeId = Integer.parseInt(tokens[0]);
                nodeToImageMap.put(nodeId, new HashSet<>());
                for (int i = 1; i < tokens.length; i++) {
                    nodeToImageMap.get(nodeId).add(tokens[i]);
                }
            }
            br.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
