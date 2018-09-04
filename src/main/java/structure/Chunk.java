package structure;

import utils.ArrayListUtils;

import java.util.ArrayList;

/**
 * Chunk consists of a list of tokens (including opening and closing brackets) and may include internal Chunks
 * @author aylai2
 */
public class Chunk {

    /* List of internal tokens in sequential order */
    private ArrayList<MyToken> tokens;
    /* List of internal chunks in sequential order (may be empty) */
    private ArrayList<Chunk> chunks;
    /* Index of first token in the tokens list of the outermost (sentence) chunk that contains this chunk */
    private int startIdx;
    /* Index of last token in the tokens list of the outermost (sentence) chunk that tcontains this chunk */
    private int endIdx;
    /**
     * Type is "SENT" if the Chunk is a sentence, empty string otherwise
     * SENT chunks don't have their own opening/closing brackets
     */
    private final String type;
    /* Coref ID */
    private int corefId;
    /* Caption-based ID of NP or VP chunk (e.g. NP0, VP1) */
    private String id;
    /* Previous chunk */
    private Chunk prevChunk;
    /* Next chunk */
    private Chunk nextChunk;

    /**
     * Constructor using String
     * @param str Bracketed, indexed string
     */
    public Chunk(String str) {
        str = str.trim();
        if (str.equals("")) {
            tokens = new ArrayList<>();
        } else {
            constructTokens(str);
        }
        // construct sentence Chunk
        if (isSent()) {
            type = "SENT";
            // indexing starts at 0: this is the highest-level Chunk
            startIdx = 0;
            endIdx = tokens.size() - 1;
            // SENT Chunk has no coref ID, chunk ID, previous Chunk, or next Chunk
            corefId = -1;
            id = "";
            prevChunk = null;
            nextChunk = null;
            if (str.equals("")) {
                chunks = new ArrayList<>();
            } else {
                constructChunks();
            }
        }
        // construct non-sentence Chunk
        else {
            if (!str.equals("")) {
                String[] temp = str.split("\\s+");
                String[] temp2 = temp[0].split("/");
                this.type = temp2[1].substring(1, temp2[1].length());
                this.id = "";
                this.corefId = -1;
                if (temp2.length > 2) {
                    this.id = temp2[2];
                    if (this.id.contains("#")) {
                        this.id = "";
                    }
                    /*if (temp2.length > 3) {
                        this.corefId = Integer.parseInt(temp2[3]);
                    }*/
                }
            }
            else {
                tokens = new ArrayList<>();
                type = "";
                id = "";
                corefId = -1;
            }
            startIdx = 0;
            endIdx = tokens.size() - 1;
            prevChunk = null;
            nextChunk = null;
            if (str.equals("")) {
                chunks = new ArrayList<>();
            } else {
                constructChunks();
            }
        }
    }

    /**
     * Constructor from list of Tokens
     * @param tokens List of internal Tokens
     * @param startIdx (Relative to outermost chunk) start index
     * @param endIdx (Relative to outermost chunk) end index
     * @param type SENT or empty string
     * @param corefId coref ID (e.g. NP0, VP1)
     * @param chunkId chunk ID
     * @param prevChunk previous Chunk
     */
    private Chunk(ArrayList<MyToken> tokens, int startIdx, int endIdx, String type, int corefId, String chunkId, Chunk prevChunk) {
        this.type = type;
        this.tokens = tokens;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.corefId = corefId;
        this.id = chunkId;
        this.prevChunk = prevChunk;
        nextChunk = null;
        constructChunks();
    }

    /**
     * Evaluates whether the inner tokens are enclosed by an outer set of brackets
     * or need to be treated as a sequence of chunks (sentence)
     * @return false if an outer set of brackets encloses all inner chunks; true otherwise
     */
    private boolean isSent() {
        int level = 0;
        for (int i = 0; i < tokens.size(); i++) {
            MyToken t = tokens.get(i);
            if (t.isOpenBracket()) {
                level++;
            }
            else if (t.isCloseBracket()) {
                level--;
            }
            if (level == 0 && i < tokens.size()-1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Construct ArrayList of Tokens from original chunk string
     * @param str Bracketed, indexed String
     */
    private void constructTokens(String str) {
        tokens = new ArrayList<>();
        String[] toks = str.split("\\s+");
        MyToken prevTok = null;
        for (String tok : toks) {
            String[] temp = tok.split("/");
            int id = Integer.parseInt(temp[0]);
            if (tok.contains("]")) { // closing bracket
                // create Token
                MyToken t = new MyToken(id, "]", "", "", -1, tokenType.BRACKET, prevTok);
                // link to other Tokens
                if (prevTok != null) {
                    prevTok.setNextToken(t);
                }
                prevTok = t;
                tokens.add(t);
            }
            else if (tok.contains("[")) { // opening bracket
                String type = temp[1].substring(1); // remove bracket
                int corefId = -1;
                String chunkId = "";
                if (temp.length > 2) {
                    chunkId = temp[2];
                }
                if (chunkId.contains("#")) { // actually a coref id
                    chunkId = "";
                }
                /*if (temp.length > 3) {
                    corefId = Integer.parseInt(temp[3]);
                }*/
                // create Token
                MyToken t = new MyToken(id, "[", type, chunkId, corefId, tokenType.BRACKET, prevTok);
                // link to other Tokens
                if (prevTok != null) {
                    prevTok.setNextToken(t);
                }
                prevTok = t;
                tokens.add(t);
            }
            else { // word
                String word = temp[1];
                String pos = temp[2];
                // create Token
                MyToken t = new MyToken(id, word, pos, tokenType.WORD, prevTok);
                // link to other Tokens
                if (prevTok != null) {
                    prevTok.setNextToken(t);
                }
                prevTok = t;
                tokens.add(t);
            }
        }
    }

    /**
     * Construct ArrayList of chunks from list of tokens
     */
    private void constructChunks() {
        chunks = new ArrayList<>();
        Chunk prevChunk = null;
        int chunkStartIdx = -1;
        ArrayList<MyToken> tokensTemp = new ArrayList<>();
        String chunkType = "";
        String chunkId = "";
        int chunkCorefId = -1;
        int level = 0;
        if (!type.equals("SENT")) {
            level = -1; // non-SENT chunks will start+end with their own chunk brackets
        }
        boolean containsChunks = containsChunk(tokens);
        for (int i = 0; i < tokens.size(); i++) {
            MyToken t = tokens.get(i);
            if (type.equals("SENT") || i > 0) {
                tokensTemp.add(t); // add this token to list of tokens in this chunk
            }
            if (t.isOpenBracket()) { // store all information for this chunk
                level++; // entering a new chunk
                if (level == 1) { // top-level internal chunk: record this string
                    chunkStartIdx = i + startIdx;
                    chunkType = t.getChunkType();
                    chunkId = t.getChunkId();
                    chunkCorefId = t.getCorefId();
                }
            }
            else if (t.isCloseBracket()) { // create chunk and remove info from ArrayLists
                level--; // leaving a chunk
                if (level == 0) { // we just completed a top-level internal chunk: create it and add to list of internal chunks
                    int chunkEndIdx = i + startIdx;
                    Chunk temp = new Chunk(tokensTemp, chunkStartIdx, chunkEndIdx, chunkType, chunkCorefId, chunkId, prevChunk);
                    if (prevChunk != null) {
                        prevChunk.setNextChunk(temp);
                    }
                    chunks.add(temp);
                    prevChunk = temp;
                    tokensTemp = new ArrayList<>();
                }
            }
            else if (level == 0 && (type.equals("SENT") || containsChunks)) { // stray token at top-level
                tokensTemp = new ArrayList<>();
                String type = t.getPos();
                ArrayList<MyToken> temp = new ArrayList<>();
                temp.add(t);
                Chunk c = new Chunk(temp, i + startIdx, i + startIdx, type, -1, type, prevChunk);
                if (prevChunk != null) {
                    prevChunk.setNextChunk(c);
                }
                chunks.add(c);
                prevChunk = c;
            }
        }
    }

    /**
     * Checks if there is an internal chunk to be processed in this list of tokens
     * @param tokens List of Tokens we want to check
     * @return True if there is more than one open bracket "[" token in the list
     */
    private boolean containsChunk(ArrayList<MyToken> tokens) {
        int numOpenBrackets = 0;
        for (MyToken t : tokens) {
            if (t.isOpenBracket()) {
                numOpenBrackets++;
            }
        }
        return numOpenBrackets > 1;
    }

    /* access variables of this Chunk */

    public String getId() {
        return id;
    }

    public int getStartIdx() {
        return startIdx;
    }

    public int getEndIdx() {
        return endIdx;
    }

    public String getType() {
        return type;
    }

    public Chunk getNextChunk() {
        return nextChunk;
    }

    public Chunk getPrevChunk() {
        return prevChunk;
    }

    public ArrayList<MyToken> getTokens() {
        return tokens;
    }

    public ArrayList<Chunk> getChunks() {
        return chunks;
    }

    public int getTokenLength() {
        return tokens.size();
    }

    /* set variables of this chunk */

    private void setStartIdx(int i) {
        startIdx = i;
    }

    private void setEndIdx(int i) {
        endIdx = i;
    }

    private void setNextChunk(Chunk c) {
        nextChunk = c;
    }

    private void setPrevChunk(Chunk c) {
        prevChunk = c;
    }

    /**
     * Returns internal tokens (not including opening and closing brackets)
     * @return ArrayList of internal Tokens
     */
    public ArrayList<MyToken> getInnerTokens() {
        ArrayList<MyToken> list = new ArrayList<>(getTokens());
        if (list.size() < 3) {
            return list;
        }
        if (!type.equals("SENT")) {
            list.remove(0);
            list.remove(list.size() - 1);
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        Chunk other = (Chunk)o;
        if (other.tokens.size() != tokens.size())
            return false;
        for (int i = 0; i < tokens.size(); ++i)
            if (!tokens.get(i).equals(other.tokens.get(i)))
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (MyToken t : tokens)
            hash *= t.hashCode();
        return hash;
    }

    /* true if there are no chunks or tokens before this chunk */

    /**
     * Check if another chunk is the first internal chunk
     * @param c Internal chunk
     * @return True if c is the first chunk inside this chunk
     */
    public boolean isFirstChunk(Chunk c) {
        return (chunks.size() == 0 && c.toString().equals(this.toString())) || c.equals(chunks.get(0));
    }

    /**
     * Check if another chunk is the last internal chunk
     * @param c Internal chunk
     * @return True if c is the last chunk inside this chunk
     */
    public boolean isLastChunk(Chunk c) {
        return (chunks.size() == 0 && c.toString().equals(this.toString())) || c.equals(chunks.get(chunks.size() - 1));
    }

    /**
     * Return the internal chunk that has the specified start index (might be inside an internal chunk)
     * @param idx Token index (relative to outermost chunk)
     * @return Chunk that starts at the token index, or null if no chunk starts exactly at that token index
     */
    Chunk getChunkStartsAtToken(int idx) {
        for (Chunk c : chunks) {
            if (c.getStartIdx() == idx) {
                return c;
            }
            else if (c.getStartIdx() <= idx && c.getEndIdx() >= idx) { // token is inside this chunk
                if (c.chunks.size() > 0) {
                    return c.getChunkStartsAtToken(idx);
                }
                else {
                    return null;
                }
            }
        }
        if (idx == this.startIdx) {
            return this;
        }
        return null;
    }

    /**
     * Return the position (index of tokens list) of this Token
     * @param t Token whose position we want to find
     * @return Index of tokens list where Token t is located
     */
    public int getTokenPosition(MyToken t) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).equals(t)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return the internal chunk that contains this position (index of tokens list)
     * @param pos Token position
     * @return Internal Chunk
     */
    private Chunk getChunkContainsPosition(int pos) {
        for (Chunk c : chunks) {
            if (c.startIdx <= pos && c.endIdx >= pos) {
                return c;
            }
        }
        return null;
    }

    /**
     * Return the internal chunk that contains Token with this ID (might be inside an internal chunk)
     * @param tId ID of Token we want to find
     * @return Chunk that contains the Token with this ID
     */
    public Chunk getChunkContainsTokenId(int tId) {
        int pos = -1;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getId() == tId) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            return null;
        }
        return getChunkContainsPosition(pos);
    }

    /**
     * Get token that precedes this chunk
     * @return Previous Token
     */
    public MyToken getPrevToken() {
        if (prevChunk != null) {
            return prevChunk.getLastToken();
        }
        return null;
    }

    /**
     * Get token that follows this chunk
     * @return Next Token
     */
    public MyToken getNextToken() {
        if (nextChunk != null) {
            return nextChunk.getFirstToken();
        }
        return null;
    }

    /**
     * Get first token of this chunk
     * @return First Token
     */
    public MyToken getFirstToken() {
        if (tokens.size() > 0) {
            return tokens.get(0);
        }
        return null;
    }

    public Chunk getLastChunk() {
        if (chunks.size() > 0) {
            return chunks.get(chunks.size() - 1);
        }
        return null;
    }

    public boolean containsToken(MyToken t) {
        for (MyToken temp : tokens) {
            if (t.equals(temp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this is the first token in this chunk
     * @param t Token we want to check
     * @return True if t is the first token in tokens list; false otherwise
     */
    public boolean isFirstToken(MyToken t) {
        return tokens.get(0) == t;
    }

    /**
     * Get last token of this chunk
     * @return Last Token
     */
    public MyToken getLastToken() {
        return tokens.get(tokens.size() - 1);
    }

    /**
     * Returns token at this position in tokens list (starts at index 0)
     * @param idx Index in tokens list we want to check
     * @return Token at index
     */
    public MyToken getTokenAtPosition(int idx) {
        if (tokens.size() == 0) {
            return null;
        }
        return tokens.get(idx);
    }

    /**
     * Returns ArrayList of tokens represented as Strings (index/word/pos)
     * (including opening and closing brackets)
     * @return ArrayList of String representation of tokens
     */
    public ArrayList<String> toStringList() {
        ArrayList<String> list = new ArrayList<>();
        for (MyToken t : tokens) {
            list.add(t.toString());
        }
        return list;
    }

    /**
     * Returns ArrayList of tokens represented as Strings (index/word/pos)
     * (without opening and closing brackets)
     * @return ArrayList of String representation of tokens
     */
    public ArrayList<String> toStringListInner() {
        ArrayList<String> list = this.toStringList();
        if (!type.equals("SENT")) {
            list.remove(0);
            list.remove(list.size() - 1);
        }
        return list;
    }

    /**
     * returns ArrayList of Tokens IDs, including opening and closing brackets
     * @return ArrayList of Token IDs
     */
    public ArrayList<Integer> toIds() {
        ArrayList<Integer> list = new ArrayList<>();
        for (MyToken t : tokens) {
            list.add(t.getId());
        }
        return list;
    }

    /**
     * Return all chunks contains in this chunk (including their internal chunks)
     * @return ArrayList of internal Chunks
     */
    public ArrayList<Chunk> getAllChunks() {
        ArrayList<Chunk> chunkList = new ArrayList<>();
        for (Chunk innerChunk : chunks) {
            chunkList.add(innerChunk);
            chunkList.addAll(innerChunk.getAllChunks());
        }
        return chunkList;
    }

    /**
     * Returns bare string of all tokens in this Chunk (no IDs or POS tags)
     * Note that this function removes words that are repeated as last word of NPM chunk and first word of following NPH chunk
     * @return String of all token Strings concatenated
     */
    public String toBareString() {
        ArrayList<String> temp = new ArrayList<>();
        // CC (or RB?) chunk
        if (startIdx == endIdx) {
            return tokens.get(0).getStr();
        }
        String prevType = "";
        String thisType = "";
        String lastWordNPM = "";
        int chunkIdx = 0;
        for (int i = 0; i < tokens.size(); i++) {
            chunkIdx++;
            MyToken t = tokens.get(i);
            // check for NPM chunk
            Chunk currentChunk = getChunkStartsAtToken(i+startIdx);
            if (currentChunk != null) {
                chunkIdx = 0;
                prevType = thisType;
                thisType = currentChunk.getType();
            }
            if (thisType.equals("NPM") && !t.getStr().equals("]")) {
                lastWordNPM = t.toStringNoIndex(); // last word in NPM chunk
            }
            if (thisType.equals("NPH") && prevType.equals("NPM") && chunkIdx == 1 && t.toStringNoIndex().equals(lastWordNPM) && tokens.size() > 2) {
                // remove NPM word (except if string is just "word word" - we want to eliminate this node later)
                temp.remove(temp.size()-1);
            }
            if (t.getStr().equals("[") || t.getStr().equals("]")) {
                continue;
            }
            temp.add(t.getStr());
        }
        return ArrayListUtils.stringListToString(temp, " ");
    }

    /**
     * Returns head noun (assuming this chunk is [EN [NP [NPH ] ] ])
     * @return Bare string representation of head noun
     */
    public String getChunkHead() {
        ArrayList<String> heads = new ArrayList<>();
        if (type.equals("EN")) {
            for (Chunk c : chunks) {
                if (c.getType().equals("NP")) {
                    // add this chunk's words to list of head words
                    c.getChunks().stream().filter(c2 -> c2.getType().equals("NPH")).forEach(c2 -> { // add this chunk's words to list of head words
                        String headChunk = c2.toBareString();
                        heads.add(headChunk);
                    });
                }
            }
        }
        String headsStr = "";
        // concatenate multiple heads with "/" (e.g. X of Y)
        for (String h : heads) {
            headsStr += h + "/";
        }
        if (headsStr.length() > 0) {
            headsStr = headsStr.substring(0, headsStr.length() - 1);
        }
        return headsStr;
    }

    /**
     * Drop a sequence of tokens and replace with new tokens (two sequences can be different lengths)
     * @param startIdx Index of beginning of token sequence
     * @param endIdx Index of end of token sequence
     * @param newTokens Array of new Tokens as Strings (idx/word/pos)
     */
    public void replaceTokens(int startIdx, int endIdx, String[] newTokens) {
        // remove tokens
        for (int i = startIdx; i <= endIdx; i++) {
            removeToken(tokens.get(startIdx));
        }
        // add tokens
        int idx = startIdx;
        for (String token : newTokens) {
            String[] temp = token.split("/");
            addToken(idx, Integer.parseInt(temp[0]), temp[1], temp[2]);
            idx++;
        }
    }

    /**
     * Create new Token and add to tokens list in this chunk (and all internal chunks) at specific position
     * @param idx Index of tokens list where we want to insert token
     * @param tId ID of new Token
     * @param str String of new Token
     * @param pos POS of new Token
     */
    private void addToken(int idx, int tId, String str, String pos) {
        int chunkIdx = idx;
        if (startIdx != 0) { // chunk index doesn't start at 0
            chunkIdx = idx - startIdx;
        }
        // create new Token
        MyToken prevTok = null;
        if (idx > 0) {
            prevTok = tokens.get(chunkIdx).getPrevToken();
        }
        MyToken t = new MyToken(tId, str, pos, tokenType.WORD, prevTok);
        // update token pointers
        MyToken nextTok;
        if (prevTok != null) {
            nextTok = prevTok.getNextToken();
            prevTok.setNextToken(t);
        }
        else {
            nextTok = tokens.get(0);
        }
        tokens.add(chunkIdx, t);
        t.setNextToken(nextTok);
        if (nextTok != null) {
            nextTok.setPrevToken(t);
        }
        // update current chunk
        endIdx = endIdx + 1;
        // find internal chunk
        if (chunks.size() > 0) {
            Chunk inner = getChunkContainsPosition(idx);
            if (inner != null) {
                inner.addToken(idx, t);
            }
        }
        // update following chunk indices
        ArrayList<Chunk> allChunks = getAllChunks();
        allChunks.stream().filter(c -> c.getStartIdx() >= idx).forEach(c -> {
            c.setStartIdx(c.getStartIdx() + 1);
            c.setEndIdx(c.getEndIdx() + 1);
        });
    }

    /**
     * Add existing Token to tokens list in this chunk (and all internal chunks) at specified position
     * @param idx Index of tokens list where we want to insert token
     * @param t Token to add
     */
    private void addToken(int idx, MyToken t) {
        int chunkIdx = idx;
        if (startIdx != 0) { // chunk index doesn't start at 0
            chunkIdx = idx - startIdx;
        }
        tokens.add(chunkIdx, t);
        endIdx = endIdx + 1;
        // find internal chunk
        if (chunks.size() > 0) {
            Chunk inner = getChunkContainsPosition(idx);
            if (inner != null) {
                inner.addToken(idx, t);
            }
        }
    }

    /**
     * Remove Token from this chunk (and all internal chunks)
     * @param t Token to remove
     */
    public void removeToken(MyToken t) {
        // update token pointers
        int chunkIdx = tokens.indexOf(t);
        int idx = startIdx + chunkIdx;
        MyToken prevTok = t.getPrevToken();
        MyToken nextTok = t.getNextToken();
        if (prevTok != null) {
            prevTok.setNextToken(nextTok);
        }
        if (nextTok != null) {
            nextTok.setPrevToken(prevTok);
        }
        // remove token
        tokens.remove(t);
        // remove token from internal chunk
        if (chunks.size() > 0) {
            Chunk inner = getChunkContainsPosition(idx);
            if (inner != null) {
                inner.removeToken(idx);
            }
        }
        // update following chunk indices
        ArrayList<Chunk> allChunks = getAllChunks();
        allChunks.stream().filter(c -> c.getStartIdx() >= idx).forEach(c -> {
            c.setStartIdx(c.getStartIdx() - 1);
            c.setEndIdx(c.getEndIdx() - 1);
        });
        endIdx--;
    }

    /**
     * Remove the token at the specified index from this chunk (and all internal chunks)
     * @param idx index of Token to remove
     */
    private void removeToken(int idx) {
        int chunkIdx = idx;
        if (startIdx != 0) { // chunk index doesn't start at 0
            chunkIdx = idx - startIdx;
        }
        tokens.remove(chunkIdx);
        endIdx = endIdx - 1;
        // find internal chunk
        if (chunks.size() > 0) {
            Chunk inner = getChunkContainsPosition(idx);
            if (inner != null) {
                inner.removeToken(idx);
            }
        }
    }

    /**
     * Remove an internal chunk from this chunk (and possibly from other internal chunks)
     * @param cTemp Internal chunk to remove
     */
    public void removeChunk(Chunk cTemp, boolean sent) {
        // check if c is inside this chunk
        if (cTemp.getStartIdx() < startIdx || cTemp.getEndIdx() > endIdx) {
            return;
        }
        // get index of chunk
        // idx may be -1 because c is not in the top-level chunks
        int idx = chunks.indexOf(cTemp);
        Chunk c = cTemp;
        if (idx != -1) {
            c = chunks.get(idx);
        }
        // get relevant indices
        int removeStartIdx = c.getStartIdx();
        int removeChunkStartIdx = removeStartIdx - startIdx;
        int removeEndIdx = c.getEndIdx();
        int removeChunkEndIdx = removeEndIdx - startIdx;
        int chunkLen = c.endIdx - c.startIdx + 1;
        // update token pointers
        if (idx != -1) {
            MyToken prevTok = c.getFirstToken().getPrevToken();
            MyToken nextTok = c.getLastToken().getNextToken();
            if (prevTok != null) {
                prevTok.setNextToken(nextTok);
            }
            if (nextTok != null) {
                nextTok.setPrevToken(prevTok);
            }
        }
        // remove tokens (from this chunk only)
        for (int i = removeChunkEndIdx; i >= removeChunkStartIdx; i--) {
            tokens.remove(i);
        }
        // update chunk pointers (only if chunk is in this level)
        if (idx != -1) {
            Chunk prevChunk = c.getPrevChunk();
            Chunk nextChunk = c.getNextChunk();
            if (prevChunk != null) {
                prevChunk.setNextChunk(nextChunk);
            }
            if (nextChunk != null) {
                nextChunk.setPrevChunk(prevChunk);
            }
            // remove chunk
            chunks.remove(idx);
        }
        if (idx == -1) {
            // find inner chunk that contains Chunk c
            if (chunks.size() != 0) {
                for (Chunk inner : chunks) {
                    inner.removeChunk(c, false);
                }
            }
        }
        //if (idx != -1 || sent) {
        if (idx != -1) {
            // update following chunk indices
            ArrayList<Chunk> allChunks = getAllChunks();
            allChunks.stream().filter(temp -> temp.getEndIdx() > removeEndIdx).forEach(temp -> {
                temp.setStartIdx(temp.getStartIdx() - chunkLen);
                temp.setEndIdx(temp.getEndIdx() - chunkLen);
            });
        }
        else if (sent) {
            // update following chunk indices
            ArrayList<Chunk> allChunks = getAllChunks();
            allChunks.stream().filter(temp -> temp.getStartIdx() > removeEndIdx).forEach(temp -> {
                temp.setStartIdx(temp.getStartIdx() - chunkLen);
                temp.setEndIdx(temp.getEndIdx() - chunkLen);
            });
        }
        // update this chunk's indices
        endIdx = endIdx - chunkLen;
    }

    /**
     * Remove internal tokens (not opening/closing brackets) from specified chunk (and internal chunks)
     * @param c Chunk whose internal tokens we want to remove
     * @param top True if this is the outermost chunk (do additional index bookkeeping); false otherwise
     */
    public void removeChunkKeepBrackets(Chunk c, boolean top) {
        // check if c is inside this chunk
        if (!(c.getStartIdx() >= startIdx && c.getEndIdx() <= endIdx)) {
            return;
        }
        // get index of chunk
        // idx may be -1 because c is not in the top-level chunks
        int idx = chunks.indexOf(c);
        // get relevant indices
        int cStartIdx = c.getStartIdx();
        int chunkStartIdx = cStartIdx - startIdx;
        int cEndIdx = c.getEndIdx();
        int chunkEndIdx = cEndIdx - startIdx;
        if (idx != -1) {
            // update token pointers
            MyToken prevTok = c.getFirstToken();
            MyToken nextTok = c.getLastToken();
            prevTok.setNextToken(nextTok);
            nextTok.setPrevToken(prevTok);
            // update this chunk
            // empty inner chunks
            c.chunks = new ArrayList<>();
            // remove tokens
            int numTokensRemoved = 0;
            for (int i = c.tokens.size()-2; i > 0; i--) {
                c.tokens.remove(i);
                numTokensRemoved++;
            }
            c.setEndIdx(c.endIdx - numTokensRemoved);
        }
        // remove tokens (except for start and end brackets)
        int numTokensRemoved = 0;
        for (int i = chunkEndIdx - 1; i > chunkStartIdx; i--) {
            tokens.remove(i);
            numTokensRemoved++;
        }
        // update this chunk
        setEndIdx(endIdx - numTokensRemoved);
        if (idx == -1) {
            // find inner chunk that contains Chunk c
            if (chunks.size() != 0) {
                for (Chunk inner : chunks) {
                    inner.removeChunkKeepBrackets(c, false);
                }
            }
        }
        if (top) {
            // update following chunk indices
            ArrayList<Chunk> allChunks = getAllChunks();
            for (Chunk temp : allChunks) {
                if (temp.getStartIdx() > cEndIdx) {
                    temp.setStartIdx(temp.getStartIdx() - numTokensRemoved);
                    temp.setEndIdx(temp.getEndIdx() - numTokensRemoved);
                }
            }
        }
    }

    @Override
    public String toString() {
        String temp = "";
        for (MyToken t : tokens) {
            temp += t.toString() + " ";
        }
        if (temp.length() > 0) {
            temp = temp.substring(0, temp.length() - 1);
        }
        return temp;
    }

    /**
     * Returns String representation of Chunk with Token indices removed
     * Used for chunk text file comparison across captions
     * @return String representation of Chunk without indices
     */
    String toStringNoIndex() {
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<Boolean> foundWord = new ArrayList<>();
        for (MyToken t : tokens) {
            if (t.isOpenBracket()) {
                foundWord.add(false);
                temp.add(t.toStringNoIndex());
            } else if (t.isCloseBracket()) {
                if (foundWord.size() > 0 && !foundWord.get(foundWord.size() - 1)) {
                    foundWord.remove(foundWord.size() - 1);
                    temp.remove(temp.size() - 1);
                } else {
                    if (foundWord.size() > 0) {
                        foundWord.remove(foundWord.size() - 1);
                    }
                    temp.add(t.toStringNoIndex());
                }
            } else { // word token
                temp.add(t.toStringNoIndex());
                for (int j = 0; j < foundWord.size(); j++) {
                    foundWord.set(j, true);
                }
            }
        }
        return ArrayListUtils.stringListToString(temp, " ");
    }


}
