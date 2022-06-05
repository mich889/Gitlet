package gitlet;

import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {
    /** contents of blob. **/
    private String content;
    /** hash of blob. **/
    private String hashcode;
    /** file name of blob. **/
    private String fileName;

    public Blob(String name) {
        this.fileName = name;
        this.content = getContent(name);
        this.hashcode = Utils.sha1(content);
    }

    public String getContent(String name) {
        File blobFile = Utils.join(Main.cwd(), name);
        return Utils.readContentsAsString(blobFile);
    }

    public String getContent() {
        return content;
    }

    public String getHash() {
        return hashcode;
    }

    public String getFileName() {
        return fileName;
    }
}
