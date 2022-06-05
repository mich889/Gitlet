package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

public class Commit implements Serializable {
    /** msg of commit. **/
    private String msg;
    /** time of commit. **/
    private Date timestamp;
    /** original parent of commit. **/
    private String parent;
    /** merge parent of commit. **/
    private String parent2;
    /** blobs of commit. **/
    private TreeMap<String, String> blobs;

    public Commit() {
        this.msg = "initial commit";
        this.timestamp = new Date(0);
        List<String> newCommit = new ArrayList<>();
        newCommit.add(msg);
        newCommit.add(timestamp.toString());
        this.blobs = new TreeMap<>();
    }

    public Commit(String m, String p) {
        this.msg = m;
        this.timestamp = new Date();
        this.parent = p;
        this.blobs = new TreeMap<>();
    }

    public void addBlobs(TreeMap<String, String> stagingArea) {
        blobs.putAll(stagingArea);
    }
    public String getSha(String filename) {
        return blobs.get(filename);
    }

    public String getP() {
        return parent;
    }

    public String getP2() {
        return parent2; }

    public String getMsg() {
        return msg; }

    public String getHash() {
        return Utils.sha1(Utils.serialize(this));
    }

    public void setMsg(String message) {
        this.msg = message;
    }

    public void setTimestamp() {
        this.timestamp = new Date();
    }

    public void setParent(String s) {
        this.parent = s;
    }

    public void setParent2(String s) {
        parent2 = s;
    }

    public TreeMap<String, String> getBlobs() {
        return blobs;
    }

    @Override
    public String toString() {
        String result = "===\n";
        result += "commit " + Utils.sha1(Utils.serialize(this)) + "\n";
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        result += "Date: " + formatter.format(timestamp) + "\n";
        result += msg + "\n";
        return result;
    }

}
