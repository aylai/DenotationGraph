package structure;

/**
 * Token for word or bracket inside a chunk
 * @author aylai2
 */
public class MyToken {

    private int id;
    private String tokenStr; /* either bracket ("[" or "]") or the word */
    private tokenType tType;
    private MyToken prevToken;
    private MyToken nextToken;

    /* word variables */
    private String posTag;

    /* bracket variables */
    private String chunkId; /* NP0 or VP0 */
    private int corefId;
    private String chunkType;

    /* create word Token */
    MyToken(int id, String str, String posTag, tokenType tType, MyToken prevToken) {
        this.id = id;
        this.tokenStr = str;
        this.posTag = posTag;
        this.tType = tType;
        chunkType = "";
        chunkId = "";
        this.prevToken = prevToken;
    }

    /* create bracket token */
    MyToken(int id, String str, String chunkType, String chunkId, int corefId, tokenType tType, MyToken prevToken) {
        this.id = id;
        this.tokenStr = str; // bracket
        posTag = "";
        this.tType = tType;
        this.chunkType = chunkType;
        this.corefId = corefId;
        this.chunkId = chunkId;
        this.prevToken = prevToken;
    }

    void setNextToken(MyToken t) {
        nextToken = t;
    }

    void setPrevToken(MyToken t) {
        prevToken = t;
    }

    public MyToken getNextToken() {
        return nextToken;
    }

    public MyToken getPrevToken() {
        return prevToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getStr() {
        return tokenStr;
    }

    public void setStr(String s) {
        tokenStr = s;
    }

    int getCorefId() {
        return corefId;
    }

    public String getPos() {
        return posTag;
    }

    public void setPos(String pos) {
        posTag = pos;
    }

    boolean isOpenBracket() {
        return tType == tokenType.BRACKET && tokenStr.equals("[");
    }

    boolean isCloseBracket() {
        return tType == tokenType.BRACKET && tokenStr.equals("]");
    }

    public boolean isWord() {
        return tType == tokenType.WORD;
    }

    String getChunkType() {
        return chunkType;
    }

    public void setChunkType(String t) {
        chunkType = t;
    }

    String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String s) {
        chunkId = s;
    }

    @Override
    public boolean equals(Object o) {
        MyToken other = (MyToken) o;
        if (other.id != id || !other.tokenStr.equals(tokenStr))
            return false;
        if (tType == tokenType.BRACKET && other.tType == tokenType.BRACKET) {
            return chunkType.equals(other.chunkType)
                    && chunkId.equals(other.chunkId)
                    && corefId == other.corefId;
        }
        return tType == tokenType.WORD && other.tType == tokenType.WORD && posTag.equals(other.posTag);
    }

    @Override
    public int hashCode() {
        if (tType == tokenType.BRACKET) {
            return id * tokenStr.hashCode() * chunkType.hashCode() * chunkId.hashCode() * corefId;
        }
        return id * tokenStr.hashCode() * posTag.hashCode();
    }

    @Override
    public String toString() {
        String temp = "";
        if (tType == tokenType.BRACKET) {
            temp += id + "/" + tokenStr;
            if (!chunkType.equals("")) {
                temp += chunkType;
            }
            if (!chunkId.equals("")) {
                temp += "/" + chunkId;
            }
            if (corefId != -1) {
                temp += "/" + corefId;
            }
        }
        else if (tType == tokenType.WORD) {
            return id + "/" + tokenStr + "/" + posTag;
        }
        return temp;
    }

    String toStringNoIndex() {
        String temp = "";
        if (tType == tokenType.BRACKET) {
            temp += tokenStr;
            if (!chunkType.equals("")) {
                temp += chunkType;
            }
        }
        else if (tType == tokenType.WORD) {
            return tokenStr;
        }
        return temp;
    }
}

enum tokenType {
    BRACKET,
    WORD
}