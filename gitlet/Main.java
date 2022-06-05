package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Michelle Chen
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    private static File _cwd = new File(".");

    public static void main(String... args) throws IOException {
        Repo r = new Repo();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        } else if (args[0].equals("init")) {
            r.init();
        } else if (!(Utils.join(cwd(), ".gitlet").exists())) {
            System.out.println("Not in an initialized Gitlet directory.");
        } else if (args[0].equals("add")) {
            r.add(args[1]);
        } else if (args[0].equals("commit")) {
            if (!(args[1].equals(""))) {
                r.commit(args[1], null);
            } else {
                System.out.println("Please enter a commit message.");
            }
        } else if (args[0].equals("checkout")) {
            checkout(r, args);
        } else if (args[0].equals("rm")) {
            r.rm(args[1]);
        } else if (args[0].equals("log")) {
            r.log();
        } else if (args[0].equals("global-log")) {
            r.globalLog();
        } else if (args[0].equals("find")) {
            r.find(args[1]);
        } else if (args[0].equals("status")) {
            r.status();
        } else if (args[0].equals("branch")) {
            r.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            r.rmBranch(args[1]);
        } else if (args[0].equals("reset")) {
            r.reset(args[1]);
        } else if (args[0].equals("merge")) {
            r.merge(args[1]);
        } else if (args[0].equals("add-remote")) {
            r.addRemote(args[1], args[2]);
        } else if (args[0].equals("rm-remote")) {
            r.rmRemote(args[1]);
        } else if (args[0].equals("push")) {
            r.push(args[1], args[2]);
        } else if (args[0].equals("fetch")) {
            r.fetch(args[1], args[2]);
        } else if (args[0].equals("pull")) {
            r.pull(args[1], args[2]);
        } else {
            System.out.println("No command with that name exists.");
        }
    }

    public static File cwd() {
        return _cwd;
    }

    public static void checkout(Repo r, String...args) {
        if (args[1].equals("--")) {
            r.checkoutOne(args[2]);
        } else if (args.length > 2) {
            if (args[2].equals("--")) {
                r.checkoutTwo(args[1], args[3]);
                return;
            }
            System.out.println("Incorrect operands.");
        } else {
            r.checkoutThree(args[1]);
        }
    }

}
