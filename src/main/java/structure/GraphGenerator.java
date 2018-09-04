package structure;

import org.apache.commons.io.FileUtils;
import rewriteRules.*;
import utils.TransFileReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Generate graph from preprocessed files
 * Created by alai on 9/5/15.
 */
public class GraphGenerator {

    public GraphGenerator(String dir, String corpus, String corpusDir, String extendDir, String generationType, boolean addBoundingBoxes, boolean debugPrint) throws IOException {
        if (generationType.equals("full") || generationType.equals("start")) {
            PrintWriter outPropagate = null;
            if (debugPrint) {
                outPropagate = new PrintWriter(new FileWriter(new File(corpusDir + "/" + corpus + "/propagate_image.txt")));
            }
            Date date = new Date();
            System.out.println(date.toString());

            String outDir = corpusDir + "/" + corpus + "/graph/";
            File file = new File(outDir);
            if (!file.exists()) {
                if (file.mkdir()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            File source = new File(corpusDir + "/" + corpus + "/tmp/graph/pre.id");
            File dest = new File(corpusDir + "/" + corpus + "/graph/initial.coref");
            try {
                FileUtils.copyFile(source, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String inFilename = corpusDir + "/" + corpus + "/tmp/graph/pre.final";
            String outFilename = corpusDir + "/" + corpus + "/tmp/graph/trans.final";
            TransFileReader reader = new TransFileReader(inFilename, outFilename);
            ArrayList<String> captionIds = reader.getCaptions();
            ArrayList<RewriteCaption> tempList = captionIds.stream().map(capId -> new RewriteCaption(capId, reader.getTokenCountIn(capId), reader.getCaptionIn(capId))).collect(Collectors.toCollection(ArrayList::new));
            // preprocessing
            VP verbInfo = new VP(corpusDir, corpus);

            // create rewrite rules
            ArrayList<RewriteCaption> preList = new ArrayList<>();
            ArrayList<RewriteCaption> npCap = new ArrayList<>();
            ArrayList<RewriteCaption> captions = new ArrayList<>();
            for (RewriteCaption caption : tempList) {
                String preStr = caption.getSent().toString();
                RewriteCaption temp1 = DropEventMods.applyRule(caption);
                RewriteCaption temp2 = DropEntityMods.applyRule(dir, temp1);
                RewriteCaption temp3 = DropEntityArticle.applyRule(temp2);
                RewriteCaption temp4 = LiftEntity.applyRule(dir, corpusDir, corpus, temp3);
                RewriteCaption tempCap = new RewriteCaption(temp4.getId(), caption.getNextTokenId(), new Chunk(preStr));
                tempCap.addRules(temp4.getRules());
                tempCap.addRoots(temp4.getRoots());
                preList.add(tempCap);
                RewriteCaption temp5 = SplitXOfY.applyRule(temp4);
                RewriteCaption temp6 = SplitXOrY.applyRule(temp5);
                tempCap = new RewriteCaption(temp6.getId(), temp6.getNextTokenId(), new Chunk(temp6.getSent().toString()));
                tempCap.addRules(temp6.getRules());
                tempCap.addRoots(temp6.getRoots());
                npCap.add(tempCap);
                RewriteCaption temp7 = DropPPs.applyRule(dir, corpusDir, corpus, temp6);
                RewriteCaption temp8 = DropWearDress.applyRule(dir, temp7, verbInfo);
                RewriteCaption temp9 = DropTail.applyRule(temp8);
                RewriteCaption temp10 = SplitSubjVerb.applyRule(temp9, verbInfo);
                RewriteCaption temp11 = ExtractNP.applyRule(temp10);
                captions.add(temp11);
            }
            printCaptions(outDir, captions);

            // generate new graph
            Graph fullGraph = new Graph(corpusDir, corpus, preList, npCap, captions, extendDir, outDir, verbInfo, outPropagate, debugPrint);
            if (generationType.equals("full")) {
                File split1 = new File(corpusDir + "/" + corpus + "/img_train.lst");
                File split2 = new File(corpusDir + "/" + corpus + "/img_test.lst");
                File split3 = new File(corpusDir + "/" + corpus + "/img_dev.lst");
                fullGraph.typeChunkAll();
                if (split1.exists() || split2.exists() || split3.exists()) {
                    System.out.println("subgraphs");
                    fullGraph.makeSubgraphs(corpusDir, corpus);
                }
            }
            fullGraph.printGraph(extendDir, outDir);
            date = new Date();
            System.out.println(date.toString());
            if (debugPrint) {
                outPropagate.close();
            }
        }
        else if (generationType.equals("finish")) {
            String outDir = corpusDir + "/" + corpus + "/graph/";
            Date date = new Date();
            System.out.println(date.toString());
            File split1 = new File(corpusDir + "/" + corpus + "/img_train.lst");
            File split2 = new File(corpusDir + "/" + corpus + "/img_test.lst");
            File split3 = new File(corpusDir + "/" + corpus + "/img_dev.lst");
            Graph fullGraph = new Graph(corpusDir, corpus);
            System.out.println("type chunk");
            fullGraph.typeChunkAll();
            if (split1.exists() || split2.exists() || split3.exists()) {
                System.out.println("subgraphs");
                fullGraph.makeSubgraphs(corpusDir, corpus);
            }
            fullGraph.printGraph(extendDir, outDir);
            date = new Date();
            System.out.println(date.toString());
        }
    }

    private void printCaptions(String outDir, ArrayList<RewriteCaption> captions) {
        try {
            PrintWriter out = new PrintWriter(new File(outDir+"/initial.rewrite"));
            for (RewriteCaption c : captions) {
                for (RewriteRule rule : c.getRules()) {
                    out.println(rule.getId() + "\t" + rule);
                }
                for (Chunk rootSent : c.getRoots()) {
                    out.println(c.getId() + "\t" + c.getNextTokenId() + "\t" + rootSent);
                }
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String dir = "preprocessing/";
        String corpus = "mpe_test_corpus";
        String corpusDir = "corpora/";
        String extendDir = "";
        String generationType = "start";
        boolean addBoundingBoxes = true;
        boolean debugPrint = false;
        if (args.length == 5) {
            dir = args[0];
            corpus = args[1];
            corpusDir = args[2];
            generationType = args[3];
            addBoundingBoxes = Boolean.parseBoolean(args[4]);
        }
        new GraphGenerator(dir, corpus, corpusDir, extendDir, generationType, addBoundingBoxes, debugPrint);
    }
}
