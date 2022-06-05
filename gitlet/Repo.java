package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Stack;

@SuppressWarnings("unchecked")
public class Repo {
    /** path to .gitlet folder. **/
    private File gitlet = Utils.join(Main.cwd(), ".gitlet");
    /** path to blobs folder. **/
    private File blobFolder = Utils.join(gitlet, "blobs");
    /** path to commit folder. **/
    private File commitFolder = Utils.join(gitlet, "commits");
    /** path to stage (treemap) file. **/
    private File stage = Utils.join(gitlet, "stageToAdd");
    /** path to removal staging (arraylist) file. **/
    private File stageRM = Utils.join(gitlet, "stageToRemove");
    /** path to branches (treemap) file. **/
    private File branches = Utils.join(gitlet, "branches");
    /** path to head Commit (string) file. **/
    private File head = Utils.join(gitlet, "head");
    /** path that stores the branch name of the head commit. **/
    private File headBranch = Utils.join(gitlet, "currentBranch");
    /** path to commitIDs, a map of all the IDs. **/
    private File commitIDs = Utils.join(gitlet, "commitIDs");
    /** error msg. **/
    private String e = "There is an untracked file in the way; "
            + "delete it, or add and commit it first.";

    public void init() throws IOException {
        if (gitlet.exists()) {
            String o = "A Gitlet version-control system already "
                    + "exists in the current directory.";
            System.out.println(o);
        } else {
            gitlet.mkdir();
            blobFolder.mkdir();
            commitFolder.mkdir();
            Commit initialCommit = new Commit();
            File initial = Utils.join(commitFolder, initialCommit.getHash());
            initial.createNewFile();
            Utils.writeObject(initial, initialCommit);
            head.createNewFile();
            String h = initialCommit.getHash();
            Utils.writeObject(head, h);
            branches.createNewFile();
            TreeMap<String, String> b = new TreeMap<>();
            b.put("master", initialCommit.getHash());
            Utils.writeObject(branches, b);
            headBranch.createNewFile();
            Utils.writeObject(headBranch, "master");
            stage.createNewFile();
            Utils.writeObject(stage, new TreeMap<String, String>());
            stageRM.createNewFile();
            Utils.writeObject(stageRM, new ArrayList<String>());
            commitIDs.createNewFile();
            Utils.writeObject(commitIDs, new ArrayList<String>());
        }
    }

    public void add(String fileName) throws IOException {
        File blobFile = Utils.join(Main.cwd(), fileName);
        if (!blobFile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            File pathToCurrCommit = Utils.join(commitFolder,
                    Utils.readObject(head, String.class));
            Commit currCommit = Utils.readObject
                    (pathToCurrCommit, Commit.class);
            if (currCommit.getBlobs().get(fileName) != null) {
                File path = Utils.join(blobFolder,
                        currCommit.getBlobs().get(fileName));
                String content = Utils.readObject
                        (path, Blob.class).getContent();
                if (Utils.readContentsAsString(blobFile).
                        equals(content)) {
                    ArrayList<String> rmStage =
                            Utils.readObject(stageRM, ArrayList.class);
                    if (rmStage.contains(fileName)) {
                        rmStage.remove(fileName);
                        Utils.writeObject(stageRM, rmStage);
                    }
                    return;
                }
            }
            Blob blob = new Blob(fileName);
            File blobCopy = Utils.join(blobFolder, blob.getHash());
            blobCopy.createNewFile();
            Utils.writeObject(blobCopy, blob);
            TreeMap currStage = Utils.readObject(stage, TreeMap.class);
            currStage.put(blob.getFileName(), blob.getHash());
            Utils.writeObject(stage, currStage);
        }
    }

    public void commit(String commitMsg, String p2) throws IOException {
        TreeMap<String, String> s =
                Utils.readObject(stage, TreeMap.class);
        ArrayList<String> removeStage =
                Utils.readObject(stageRM, ArrayList.class);
        if (s.size() == 0 && removeStage.size() == 0) {
            System.out.println("No changes added to the commit.");
        }
        String h = Utils.readObject(head, String.class);
        File headCommit = Utils.join(commitFolder, h);
        Commit headCommitCopy =
                Utils.readObject(headCommit, Commit.class);
        headCommitCopy.setMsg(commitMsg);
        headCommitCopy.setTimestamp();
        headCommitCopy.addBlobs(s);
        headCommitCopy.setParent2(null);
        for (String a: removeStage) {
            if (headCommitCopy.getBlobs().containsKey(a)) {
                headCommitCopy.getBlobs().remove(a);
            }
        }
        headCommitCopy.setParent(h);
        if (p2 != null) {
            headCommitCopy.setParent2(p2);
        }
        Utils.writeObject(stage, new TreeMap<String, String>());
        Utils.writeObject(stageRM, new ArrayList<String>());
        File newCommit =
                Utils.join(commitFolder, headCommitCopy.getHash());
        newCommit.createNewFile();
        Utils.writeObject(newCommit, headCommitCopy);
        ArrayList<String> listOfIDs =
                Utils.readObject(commitIDs, ArrayList.class);
        listOfIDs.add(headCommitCopy.getHash());
        Utils.writeObject(commitIDs, listOfIDs);
        Utils.writeObject(head, headCommitCopy.getHash());
        TreeMap<String, String> currbranches =
                Utils.readObject(branches, TreeMap.class);
        String currbranch = Utils.readObject(headBranch, String.class);
        currbranches.replace(currbranch, Utils.readObject(head, String.class));
        Utils.writeObject(branches, currbranches);
    }

    public void checkoutOne(String fileName) {
        String headFile = Utils.readObject(head, String.class);
        Commit headCommit = Utils.readObject
                (Utils.join(commitFolder, headFile), Commit.class);
        String sha = headCommit.getSha(fileName);
        File blobpath = Utils.join(blobFolder, sha);
        if (blobpath.exists()) {
            Blob headBlob = Utils.readObject(blobpath, Blob.class);
            Utils.writeContents(Utils.join(Main.cwd(), fileName),
                    headBlob.getContent());
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    public void checkoutTwo(String commitID, String fileName) {
        String id;
        if (commitID.length() < Utils.UID_LENGTH) {
            id = searchID(commitID);
        } else {
            id = commitID;
        }
        File commitPath = Utils.join(commitFolder, id);
        if (!(commitPath.exists())) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit newCommit = Utils.readObject(commitPath, Commit.class);
        String sha = newCommit.getSha(fileName);
        if (sha == null || !(Utils.join(blobFolder, sha).exists())) {
            System.out.println("File does not exist in that commit.");
        } else {
            Blob newBlob = Utils.readObject
                    (Utils.join(blobFolder, sha), Blob.class);
            Utils.writeContents(Utils.join
                    (Main.cwd(), fileName), newBlob.getContent());
        }
    }

    public void checkoutThree(String branchName) {
        TreeMap<String, String> allBranches =
                Utils.readObject(branches, TreeMap.class);
        if (!allBranches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(Utils.readObject(headBranch, String.class))) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File pathToCurrCommit = Utils.join(commitFolder,
                Utils.readObject(head, String.class));
        TreeMap<String, String> currBlobs = Utils.readObject
                (pathToCurrCommit, Commit.class).getBlobs();
        String checkedOutBranchHash = allBranches.get(branchName);
        Commit checkedOutCommit = Utils.readObject
                (Utils.join(commitFolder, checkedOutBranchHash), Commit.class);
        TreeMap<String, String> checkedOutBlobs = checkedOutCommit.getBlobs();
        for (Map.Entry<String, String> entry: checkedOutBlobs.entrySet()) {
            if (Utils.join(Main.cwd(), entry.getKey()).exists()
                    && !(currBlobs.containsKey(entry.getKey()))) {
                System.out.println(e);
                System.exit(0);
            }
            Blob b = Utils.readObject(Utils.join
                    (blobFolder, entry.getValue()), Blob.class);
            Utils.writeContents(Utils.join
                    (Main.cwd(), entry.getKey()), b.getContent());
        }
        for (Map.Entry<String, String> entry: currBlobs.entrySet()) {
            if (!(checkedOutBlobs.containsKey(entry.getKey()))) {
                Utils.restrictedDelete(Utils.join(Main.cwd(), entry.getKey()));
            }
        }
        TreeMap<String, String> clearedStage =
                Utils.readObject(stage, TreeMap.class);
        clearedStage.clear();
        Utils.writeObject(stage, clearedStage);
        Utils.writeObject(head, checkedOutCommit.getHash());
        Utils.writeObject(headBranch, branchName);
    }

    public void rm(String fileName) {
        TreeMap<String, String> stageAddition =
                Utils.readObject(stage, TreeMap.class);
        File pathToCurrCommit = Utils.join(commitFolder,
                Utils.readObject(head, String.class));
        Commit currCommit = Utils.readObject(pathToCurrCommit, Commit.class);
        if (stageAddition.containsKey(fileName)) {
            stageAddition.remove(fileName);
            Utils.writeObject(stage, stageAddition);
        } else if (currCommit.getBlobs().containsKey(fileName)) {
            ArrayList<String> removeStage =
                    Utils.readObject(stageRM, ArrayList.class);
            removeStage.add(fileName);
            Utils.writeObject(stageRM, removeStage);
            Utils.restrictedDelete(Utils.join(Main.cwd(), fileName));
        } else {
            System.out.println("No reason to remove the file.");
        }
    }

    public void log() {
        String s = Utils.readObject(head, String.class);
        File headCommitPath = Utils.join(commitFolder, s);
        Commit currCommit = Utils.readObject(headCommitPath, Commit.class);
        while (currCommit != null) {
            System.out.println(currCommit);
            if (currCommit.getP() == null) {
                break;
            } else {
                File parentPath = Utils.join(commitFolder, currCommit.getP());
                currCommit = Utils.readObject(parentPath, Commit.class);
            }
        }
    }

    public void globalLog() {
        List<String> allFiles = Utils.plainFilenamesIn(commitFolder);
        for (String s: allFiles) {
            System.out.println(
                    Utils.readObject
                            (Utils.join(commitFolder, s), Commit.class));
        }
    }

    public void find(String msg) {
        boolean flag = false;
        List<String> allFiles = Utils.plainFilenamesIn(commitFolder);
        for (String file: allFiles) {
            Commit c = Utils.readObject
                    (Utils.join(commitFolder, file), Commit.class);
            if (c.getMsg().equals(msg)) {
                System.out.println(c.getHash());
                flag = true;
            }
        }
        if (!flag) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        Commit currCommit = Utils.readObject(Utils.join(commitFolder,
                Utils.readObject(head, String.class)), Commit.class);
        List<String> allFilesInCWD = Utils.plainFilenamesIn(Main.cwd());
        System.out.println("=== Branches ===");
        String currBranch = Utils.readObject(headBranch, String.class);
        TreeMap<String, String> allBranches =
                Utils.readObject(branches, TreeMap.class);
        for (String key: allBranches.keySet()) {
            if (key.equals(currBranch)) {
                System.out.println("*" + key);
            } else {
                System.out.println(key);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        TreeMap<String, String> sa = Utils.readObject(stage, TreeMap.class);
        for (String fileName: sa.keySet()) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> stageRemove =
                Utils.readObject(stageRM, ArrayList.class);
        for (String fileName: stageRemove) {
            System.out.println(fileName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> lst = new ArrayList<>();
        for (Map.Entry<String, String> s: currCommit.getBlobs().entrySet()) {
            if (!(Utils.join(Main.cwd(), s.getKey()).exists())) {
                if (!(stageRemove.contains(s.getKey()))) {
                    lst.add(s.getKey() + " (deleted)");
                }
            } else if (!(Utils.readObject
                            (Utils.join(blobFolder, s.getValue()), Blob.class)
                    .getContent()).equals(Utils.readContentsAsString
                    (Utils.join(Main.cwd(), s.getKey())))) {
                if (!(sa.containsKey(s.getValue()))) {
                    lst.add(s.getKey() + " (modified)");
                }
            }
        }
        Collections.sort(lst);
        for (String s: lst) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (String file: allFilesInCWD) {
            if (!(sa.containsKey(file))
                    && !(currCommit.getBlobs().containsKey(file))) {
                System.out.println(file);
            }
        }
    }

    public void branch(String branchName) {
        TreeMap<String, String> currBranches =
                Utils.readObject(branches, TreeMap.class);
        if (currBranches.containsKey(branchName)) {
            String msg = "A branch with that name already exists.";
            System.out.println(msg);
        } else {
            File pathToCurrHead = Utils.join(commitFolder,
                    Utils.readObject(head, String.class));
            Commit currCommit = Utils.readObject(pathToCurrHead, Commit.class);
            currBranches.put(branchName, currCommit.getHash());
            Utils.writeObject(branches, currBranches);
        }
    }

    public void rmBranch(String branchName) {
        TreeMap<String, String> newBranches =
                Utils.readObject(branches, TreeMap.class);
        if (!(newBranches.containsKey(branchName))) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (branchName.equals
                (Utils.readObject(headBranch, String.class))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        newBranches.remove(branchName);
        Utils.writeObject(branches, newBranches);
    }

    public void reset(String commitID) {
        File cPath = Utils.join(commitFolder, commitID);
        if (!(cPath.exists())) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Commit c = Utils.readObject(cPath, Commit.class);
        Commit currCommit = Utils.readObject
                (Utils.join(commitFolder,
                        Utils.readObject(head, String.class)), Commit.class);
        for (String currFileName: currCommit.getBlobs().keySet()) {
            if (!(c.getBlobs().containsKey(currFileName))) {
                Utils.restrictedDelete(Utils.join(Main.cwd(), currFileName));
            }
        }
        for (String fileName: c.getBlobs().keySet()) {
            if (Utils.join(Main.cwd(), fileName).exists()
                    && !(currCommit.getBlobs().containsKey(fileName))) {
                System.out.println(e);
                System.exit(0);
            }
            checkoutTwo(commitID, fileName);
        }
        Utils.writeObject(head, c.getHash());
        Utils.writeObject(stage, new TreeMap<String, String>());
        TreeMap<String, String> currBranches =
                Utils.readObject(branches, TreeMap.class);
        currBranches.replace(Utils.readObject(headBranch,
                String.class), c.getHash());
        Utils.writeObject(branches, currBranches);
    }

    public void merge(String givenBranch) throws IOException {
        File f = Utils.join(commitFolder, Utils.readObject(head, String.class));
        Commit headCommit = Utils.readObject(f, Commit.class);
        boolean flag = checkMergeErrors(givenBranch, headCommit);
        if (flag) {
            return;
        }
        TreeMap<String, String> allBranches =
                Utils.readObject(branches, TreeMap.class);
        String latestGivenCommit = allBranches.get(givenBranch);
        Commit givenCommit = Utils.readObject
                (Utils.join(commitFolder, latestGivenCommit), Commit.class);
        ArrayList<String> visitedCurr = search(headCommit.getHash());
        ArrayList<String> visitedGiven = search(givenCommit.getHash());
        Commit splitCommit = null;
        for (String vg: visitedCurr) {
            if (visitedGiven.contains(vg)) {
                splitCommit = Utils.readObject
                        (Utils.join(commitFolder, vg), Commit.class);
                break;
            }
        }
        ArrayList<String> allFiles =
                getAllFiles(givenCommit, headCommit, splitCommit);
        if (splitCommit.getHash().equals(givenCommit.getHash())) {
            String msg = "Given branch is an ancestor of the current branch.";
            System.out.println(msg);
            return;
        } else if (splitCommit.getHash().equals(headCommit.getHash())) {
            checkoutThree(givenBranch);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        findmerge(allFiles, splitCommit, headCommit, givenCommit);
        String h = Utils.readObject(headBranch, String.class);
        String msg = "Merged " + givenBranch + " into " + h + ".";
        TreeMap<String, String> t = Utils.readObject(branches, TreeMap.class);
        File cfile = Utils.join(commitFolder, t.get(givenBranch));
        Commit p2 = Utils.readObject(cfile, Commit.class);
        commit(msg, p2.getHash());
    }
    public boolean checkMergeErrors(String branch, Commit currCommit) {
        boolean flag = false;
        int size = Utils.readObject(stageRM, ArrayList.class).size();
        if (Utils.readObject(stage, TreeMap.class).size() != 0 || size != 0) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        for (String file : Utils.plainFilenamesIn(Main.cwd())) {
            if (!(currCommit.getBlobs().containsKey(file))) {
                TreeMap<String, String> sha =
                        Utils.readObject(branches, TreeMap.class);
                File f = Utils.join(commitFolder, sha.get(branch));
                Commit c = Utils.readObject(f, Commit.class);
                if (c.getBlobs().containsKey(file)) {
                    System.out.println(e);
                    System.exit(0);
                    flag = true;
                }
            }
        }
        String hBranch = Utils.readObject(headBranch, String.class);
        if (!(Utils.readObject(branches, TreeMap.class).containsKey(branch))) {
            System.out.println("A branch with that name does not exist.");
            flag = true;
        } else if (branch.equals(hBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            flag = true;
        }
        return flag;
    }

    public String mergeConflict(String currContent, String givenContent) {
        String result = "<<<<<<< HEAD\n";
        if (currContent.equals("null")) {
            result += "=======";
        } else {
            result += currContent + "=======\n";
        }
        if (givenContent.equals("null")) {
            result += ">>>>>>>\n";
        } else {
            result += givenContent + ">>>>>>>\n";
        }
        return result;
    }

    public void changeDirectory(String contents, String f, String sha) {
        boolean flag = true;
        if (contents.equals("null")) {
            flag = false;
        }
        if (flag) {
            Utils.writeContents(Utils.join(Main.cwd(), f), contents);
            TreeMap<String, String> t = Utils.readObject(stage, TreeMap.class);
            t.put(f, sha);
            Utils.writeObject(stage, t);
        } else {
            Utils.restrictedDelete(Utils.join(Main.cwd(), f));
            ArrayList<String> t = Utils.readObject(stageRM, ArrayList.class);
            t.add(f);
            Utils.writeObject(stageRM, t);
        }
    }

    public String searchID(String shortUID) {
        ArrayList<String> allIDs = Utils.readObject(commitIDs, ArrayList.class);
        for (String id: allIDs) {
            if (id.substring(0, shortUID.length()).equals(shortUID)) {
                return id;
            }
        }
        return "No ID found";
    }

    public ArrayList<String> search(String s) {
        ArrayList<String> result = new ArrayList<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.add(s);
        while (!queue.isEmpty()) {
            File cPath = Utils.join(commitFolder, queue.remove());
            Commit c = Utils.readObject(cPath, Commit.class);
            if (!result.contains(c.getHash())) {
                result.add(c.getHash());
                if (c.getP() != null && (!(result.contains(c.getP())))) {
                    queue.add(c.getP());
                }
                if (c.getP2() != null && (!(result.contains(c.getP2())))) {
                    queue.add(c.getP2());
                }
            }
        }
        return result;
    }
    public ArrayList<String> getAllFiles(Commit a, Commit b, Commit c) {
        ArrayList<String> result = new ArrayList<>();
        for (String file: a.getBlobs().keySet()) {
            result.add(file);
        }
        for (String file: b.getBlobs().keySet()) {
            if (!(result.contains(file))) {
                result.add(file);
            }
        }
        for (String file: c.getBlobs().keySet()) {
            if (!(result.contains(file))) {
                result.add(file);
            }
        }
        return result;
    }

    public void findmerge(ArrayList<String> allFiles,
                          Commit splitCommit,
                          Commit headCommit,
                          Commit givenCommit) throws IOException {
        for (String file: allFiles) {
            String split;
            String curr;
            String other;
            if (splitCommit.getBlobs().containsKey(file)) {
                split = Utils.readObject
                        (Utils.join(blobFolder,
                                splitCommit.getBlobs().get(file)), Blob.class)
                        .getContent();
            } else {
                split = "null";
            }
            if (headCommit.getBlobs().containsKey(file)) {
                curr = Utils.readObject(Utils.join(blobFolder,
                        headCommit.getBlobs().get(file)),
                        Blob.class).getContent();
            } else {
                curr = "null";
            }
            if (givenCommit.getBlobs().containsKey(file)) {
                other = Utils.readObject(Utils.join(blobFolder,
                        givenCommit.getBlobs().get(file)),
                        Blob.class).getContent();
            } else {
                other = "null";
            }
            if (split.equals(curr) && !(curr.equals(other))) {
                changeDirectory(other, file, givenCommit.getBlobs().get(file));
            } else if (split.equals(other) && !(curr.equals(split))) {
                changeDirectory(curr, file, headCommit.getBlobs().get(file));
            } else if (split.equals("null") && other.equals("null")) {
                changeDirectory(curr, file, headCommit.getBlobs().get(file));
            } else if (split.equals("null") && curr.equals("null")) {
                changeDirectory(other, file, givenCommit.getBlobs().get(file));
            } else if (split.equals(curr) && other.equals("null")) {
                changeDirectory(other, file, null);
            } else if (split.equals(other) && curr.equals("null")) {
                break;
            } else if (!(split.equals(other)) && !(split.equals(curr))) {
                if (other.equals(curr)) {
                    break;
                } else {
                    System.out.println("Encountered a merge conflict.");
                    String txt = mergeConflict(curr, other);
                    Utils.writeContents(Utils.join(Main.cwd(), file), txt);
                    add(file);
                }
            }
        }
    }

    public void addRemote(String name, String path) throws IOException {
        List<String> allFiles = Utils.plainFilenamesIn(gitlet);
        for (String f: allFiles) {
            if (f.equals(name)) {
                System.out.println("A remote with that name already exists.");
                return;
            }
        }
        String f = path.replace("/", java.io.File.separator);
        File file = new File(f);
        File remote = Utils.join(gitlet, name);
        remote.createNewFile();
        Utils.writeObject(remote, file);
    }

    public void rmRemote(String name) {
        File f = Utils.join(gitlet, name);
        if (!(f.exists())) {
            System.out.println(
                    "A remote with that name does not exist.");
            return;
        } else {
            f.delete();
        }
    }

    public void push(String remoteName,
                     String remoteBranchName) throws IOException {
        File remote = Utils.readObject
                (Utils.join(gitlet, remoteName), File.class);
        if (!(remote.exists())) {
            System.out.println("Remote directory not found.");
            return;
        }
        File remoteCommitsFolder = Utils.join(remote, "commits");
        File remoteBlobsFolder = Utils.join(remote, "blobs");
        TreeMap<String, String> remoteBranch =
                Utils.readObject(Utils.join(remote, "branches"), TreeMap.class);
        String rHead = Utils.readObject
                (Utils.join(remote, "head"), String.class);
        Commit rhCommit = Utils.readObject(Utils.join
                (remoteCommitsFolder, rHead), Commit.class);
        if (!(remoteBranch.containsKey(remoteBranchName))) {
            remoteBranch.put(remoteBranchName, rhCommit.getHash());
            return;
        }
        Commit currCommit = Utils.readObject(
                Utils.join(commitFolder, Utils.readObject
                        (head, String.class)), Commit.class);
        Stack<String> commitHistory = new Stack<>();
        while (currCommit != null) {
            commitHistory.push(currCommit.getHash());
            if (currCommit.getP() == null) {
                break;
            } else {
                currCommit = Utils.readObject(Utils.join
                        (commitFolder, currCommit.getP()), Commit.class);
            }
        }
        if (!(commitHistory.contains(rhCommit.getHash()))) {
            System.out.println(
                    "Please pull down remote changes before pushing.");
            return;
        } else {
            Commit headCommit =
                    Utils.readObject(Utils.join(commitFolder, Utils.readObject
                            (head, String.class)), Commit.class);
            regularPushCommands(commitHistory,
                    remoteCommitsFolder, remoteBlobsFolder);
            remoteBranch.replace(remoteBranchName, headCommit.getHash());
            Utils.writeObject(Utils.join(remote, "branches"), remoteBranch);
            Utils.writeObject(Utils.join(remote, "head"), headCommit.getHash());
        }
    }

    public void fetch(String remoteName,
                      String remoteBranchName) throws IOException {
        File remote = Utils.readObject
                (Utils.join(gitlet, remoteName), File.class);
        if (!(remote.exists())) {
            System.out.println("Remote directory not found.");
            return;
        }
        TreeMap<String, String> currBranch =
                Utils.readObject(branches, TreeMap.class);
        Commit currCommit = Utils.readObject(Utils.join(commitFolder,
                        Utils.readObject(head, String.class)),
                Commit.class);
        TreeMap<String, String> remoteBranch =
                Utils.readObject(Utils.join(remote, "branches"), TreeMap.class);
        if (!(remoteBranch.containsKey(remoteBranchName))) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        if (!(currBranch.containsKey(remoteBranchName))) {
            currBranch.put(remoteBranchName, currCommit.getHash());
            Utils.writeObject(branches, currBranch);
            return;
        }
        File remoteCommitsFolder = Utils.join(remote, "commits");
        File remoteBlobsFolder = Utils.join(remote, "blobs");
        String rHead = remoteBranch.get(remoteBranchName);
        Commit rhCommit = Utils.readObject
                (Utils.join(remoteCommitsFolder, rHead), Commit.class);
        currBranch.put((remoteName + "/" + remoteBranchName),
                rhCommit.getHash());
        Utils.writeObject(branches, currBranch);
        ArrayList<Commit> rCommitHistory = new ArrayList<>();
        while (rhCommit != null) {
            rCommitHistory.add(rhCommit);
            if (rhCommit.getP() == null) {
                break;
            } else {
                rhCommit = Utils.readObject(Utils.join
                        (commitFolder, rhCommit.getP()), Commit.class);
            }
        }
        for (Commit c: rCommitHistory) {
            File cFile = Utils.join(commitFolder, c.getHash());
            if (!(cFile.exists())) {
                cFile.createNewFile();
                Utils.writeObject(cFile, c);
                for (String blobHash: c.getBlobs().values()) {
                    File bFile = Utils.join(blobFolder, blobHash);
                    if (!(bFile).exists()) {
                        bFile.createNewFile();
                        Utils.writeObject(bFile, Utils.readObject(Utils.join
                                (remoteBlobsFolder, blobHash), Blob.class));
                    }
                }
            }
        }
    }

    public void pull(String remoteName,
                     String remoteBranchName) throws IOException {
        fetch(remoteName, remoteBranchName);
        String fetchedBranch = remoteName + "/" + remoteBranchName;
        merge(fetchedBranch);
    }

    public void regularPushCommands(Stack<String> commitHistory,
                                    File remoteCommitsFolder,
                                    File remoteBlobsFolder) throws IOException {
        for (String c: commitHistory) {
            File cFile = Utils.join(remoteCommitsFolder, c);
            if (!(cFile.exists())) {
                cFile.createNewFile();
                Utils.writeObject(cFile, Utils.readObject
                        (Utils.join(commitFolder, c), Commit.class));
                for (String blobHash : Utils.readObject
                                (Utils.join(commitFolder, c), Commit.class)
                        .getBlobs().values()) {
                    File bFile = Utils.join(remoteBlobsFolder, blobHash);
                    if (!(bFile).exists()) {
                        bFile.createNewFile();
                        Utils.writeObject(bFile, Utils.readObject(Utils.join
                                (blobFolder, blobHash), Blob.class));
                    }
                }
            }
        }
    }
}
