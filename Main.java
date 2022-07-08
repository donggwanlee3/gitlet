package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author David Lee
 */
public class Main {
    /** Current Working Directory. */
    static final File CWD = new File(".");
    /** Current Working Directory. */
    static final File GITLET_FLODER = new File(".gitlet");

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            return;
        }
        switch (args[0]) {
        case "init":
            if (GITLET_FLODER.exists()) {
                System.out.println("Gitlet version-control system "
                        + "already exists in the curent directory.");
                return;
            }
            Commit.init();
            break;
        case "add":
            Commit.add(args[1]);
            break;
        case "commit":
            Commit.constructor(args[1]);
            break;
        case "rm":
            Commit.rm(args[1]);
            break;
        case "log":
            Commit.log();
            break;
        case "global-log":
            Commit.globallog();
            break;
        case "find":
            Commit.find(args[1]);
            break;
        case "status":
            if (!GITLET_FLODER.exists()) {
                System.out.println("Not in an initialized Gitlet directory.");
                break;
            }
            Commit.status();
            break;
        case "checkout":
            helper(args);
            break;
        case "branch":
            Commit.createbranch(args[1]);
            break;
        case "rm-branch":
            Commit.removebranch(args[1]);
            break;
        case "reset":
            Commit.reset(args[1]);
            break;
        case "merge":
            Commit.merge(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
            break;
        }
        return;
    }

    /**
     * Prints out MESSAGE and exits with error code -1.
     * Note:
     *     The functionality for erroring/exit codes is different within Gitlet
     *     so DO NOT use this as a reference.
     *     Refer to the spec for more information.
     * @param message message to print
     */
    public static void exitWithError(String message) {
        if (message != null && !message.equals("")) {
            System.out.println(message);
        }
        System.exit(-1);
    }

    public static void helper(String[] args) throws IOException {
        if (args.length == 2) {
            Commit.checkoutbranch(args[1]);
        } else if (args.length == 3) {
            Commit.checkout(args[2]);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            if (args[1].length() == 8) {
                Commit.cksht(args[1], args[3]);
            } else {
                Commit.checkout(args[1], args[3]);
            }
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
}


