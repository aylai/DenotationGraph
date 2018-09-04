package preprocessing;

import structure.Chunk;
import structure.RewriteCaption;
import utils.TransFileReader;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by alai on 9/2/15.
 */
public class FixGirlChild {

    public static RewriteCaption fixGirlChild(RewriteCaption cap) {
        Chunk sent = cap.getSent();
        if (!sent.toString().contains("girl_child")) {
            return cap;
        }
        String newSent = sent.toString().replaceAll("girl_child", "girl");
        cap.setSent(newSent);
        return cap;
    }

    public static void main(String[] args) {
        String dir = args[0];
        String corpus = args[1];
        try {
            String inFilename = dir+"/"+corpus+"/tmp/graph/pre.girl";
            PrintWriter out = new PrintWriter(new File(inFilename+".new"));
            TransFileReader reader = new TransFileReader(inFilename);
            ArrayList<String> captionIds = reader.getCaptions();
            for (String capId : captionIds) {
                RewriteCaption rCap = new RewriteCaption(capId, reader.getTokenCountIn(capId), reader.getCaptionIn(capId));
                RewriteCaption newCap = FixGirlChild.fixGirlChild(rCap);
                out.println(capId+"\t"+newCap.getNextTokenId()+"\t"+newCap.getSent());
            }
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
