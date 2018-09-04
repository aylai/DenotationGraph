package structure;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Describe string transformation of a caption by dropping or adding tokens
 * @author aylai2
 */
public class RewriteRule implements Comparable<RewriteRule> {

    private int id;
    private ArrayList<String> left; /* indices of caption tokens */
    private ArrayList<String> right; /* indices of caption tokens, plus tokens that are added/dropped */
    private String type;

    public RewriteRule(int id, String leftStr, String rightStr, String type) {
        this.id = id;
        left = new ArrayList<>();
        Collections.addAll(left, leftStr.split("\\s+"));
        right = new ArrayList<>();
        Collections.addAll(right, rightStr.split("\\s+"));
        this.type = type;
    }

    public RewriteRule(int id, ArrayList<String> left, ArrayList<String> right, String type) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public RewriteRule(RewriteRule rule) {
        this.id = rule.id;
        this.left = rule.left;
        this.right = rule.right;
        this.type = rule.type;
    }

    public int getId() {
        return id;
    }

    public ArrayList<String> getLeft() {
        return left;
    }

    public ArrayList<String> getRight() {
        return right;
    }

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        String thisTemp = "";
        for (String l : left) {
            thisTemp += l + " ";
        }
        for (String r : right) {
            thisTemp += r + " ";
        }
        thisTemp += type;
        String oTemp = "";
        for (String l : ((RewriteRule)o).left) {
            oTemp += l + " ";
        }
        for (String r : ((RewriteRule)o).right) {
            oTemp += r + " ";
        }
        oTemp += ((RewriteRule)o).type;
        return thisTemp.equals(oTemp);
    }

    @Override
    public int hashCode() {
        String temp = "";
        for (String l : left) {
            temp += l + " ";
        }
        for (String r : right) {
            temp += r + " ";
        }
        temp += type;
        return temp.hashCode();
    }

    @Override
    public String toString() {
        String l = "";
        for (String leftStr : left) {
            l += leftStr + " ";
        }
        l = l.substring(0,l.length()-1);
        String r = "";
        for (String rightStr : right) {
            r += rightStr + " ";
        }
        r = r.substring(0, r.length()-1);
        return l + "\t" + r + "\t" + type;
    }

    public int compareTo(RewriteRule rule) {
        return new Integer(this.getId()).compareTo(rule.getId());
    }

}
