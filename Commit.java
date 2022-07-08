package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class Commit implements Serializable {
    /** y e a h .*/
    private static String a = "EEE MMM d HH:mm:ss yyyy Z";
    /** y e a h .*/
    private static SimpleDateFormat m = new SimpleDateFormat(a);
    /** y e a h .*/
    @SuppressWarnings({"unchecked", "deprecation"})
    protected static final Date INITIALDATE = new Date(1970, 0, 1);
    /** y e a h .*/
    protected String _message;
    /** y e a h .*/
    private Date _date;
    /** y e a h .*/
    private String _parent;
    /** y e a h .*/
    static final File COMMIT_FOLDER = new File(".gitlet/commits");
    /** y e a h .*/
    static final File BRANCHES = new File(".gitlet/branches");
    /** y e a h .*/
    static final File HEAD = U.join(BRANCHES, "HEAD");
    /** y e a h .*/
    static final File MASTER = U.join(BRANCHES, "master");
    /** y e a h .*/
    static final File STAGING = U.join(COMMIT_FOLDER, "staging");
    /** y e a h .*/
    static final File STAGINGRM = U.join(COMMIT_FOLDER, "stagingrm");
    /** y e a h .*/
    static final File BLOB = U.join(COMMIT_FOLDER, "blob");
    /** y e a h .*/
    static final File CWD = new File(".");
    /** y e a h .*/
    private HashMap _metadata = new HashMap<>();
    /** y e a h .*/
    private static String committedhash;
    /** y e a h .*/
    static final List<String> LISTCWD = U.plainFilenamesIn(CWD);
    /** y e a h .*/
    static final List<String> LISTSTAGES = U.plainFilenamesIn(STAGING);
    /** y e a h .*/
    static final List<String> LISTSTAGESRM = U.plainFilenamesIn(STAGINGRM);
    /** y e a h .*/
    static final List<String> LISTBRANCHES = U.plainFilenamesIn(BRANCHES);
    /** y e a h .*/
    static final File CB = U.join(BRANCHES, "curbranch");
    /** Current Working Directory. */
    static final File GITLET_FLODER = new File(".gitlet");

    @SuppressWarnings("unchecked")
    public Commit(String message, String parent) throws IOException {
        if (parent != null) {
            int jsk = STAGING.listFiles().length;
            int b = STAGINGRM.listFiles().length;
            if (jsk + b == 0) {
                System.out.println("No changes added to the commit.");
            }
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }
        _message = message;
        _date = INITIALDATE;
        _parent = parent;
        if (parent != null) {
            _date = new Date();
        }
        if (STAGING.isDirectory()) {
            File[] content = STAGING.listFiles();
            if (parent != null) {
                File parentcommit = U.join(COMMIT_FOLDER, parent);
                Commit cop = U.readObject(parentcommit, Commit.class);
                _metadata.putAll(cop._metadata);
            }

            for (int i = 0; i < content.length; i++) {
                String filename = content[i].getName();
                String shaid = U.sha1(U.readContents(content[i]));
                File pathtocontent = U.join(BLOB, shaid);
                if (!pathtocontent.exists()) {
                    Files.move(content[i].toPath(), pathtocontent.toPath());
                }
                _metadata.put(filename, pathtocontent);
                content[i].delete();
            }
        }
        if (STAGINGRM.isDirectory()) {
            File[] content = STAGINGRM.listFiles();
            for (int i = 0; i < content.length; i++) {
                String filename = content[i].getName();
                _metadata.remove(filename);
                content[i].delete();
            }
        }

        committedhash = getsha1(_date, _message, _parent, _metadata);
        if (parent != null) {
            if (CB.listFiles().length != 0) {
                File fjk = U.join(BRANCHES, CB.listFiles()[0].getName());
                movepointers(fjk);
                File b = U.join(CB, CB.listFiles()[0].getName());
                b.delete();
            } else {
                movepointers(findcurrentbranch());
            }
        }
        File newfilecommited = U.join(COMMIT_FOLDER, committedhash);
        newfilecommited.createNewFile();
    }

    @SuppressWarnings("unchecked")
    public static void init() throws IOException {
        GITLET_FLODER.mkdir();
        CWD.mkdir();
        COMMIT_FOLDER.mkdir();
        STAGING.mkdir();
        STAGINGRM.mkdir();

        BRANCHES.mkdir();
        HEAD.createNewFile();
        MASTER.createNewFile();
        BLOB.mkdir();
        CB.mkdir();


        Commit initalcommit = new Commit("initial commit", null);
        File commit1 = U.join(COMMIT_FOLDER, committedhash);
        U.writeObject(commit1, initalcommit);
        U.writeObject(HEAD, committedhash);
        U.writeObject(MASTER, committedhash);
    }

    @SuppressWarnings("unchecked")
    public static void add(String filename) throws IOException {
        File addfile = new File(filename);
        if (!addfile.exists()) {
            System.out.println("File does not exist");
        } else {
            Commit headcommit = fetchcommit("HEAD");
            File stagethis = U.join(STAGING, filename);
            stagethis.createNewFile();
            U.writeContents(stagethis, U.readContents(addfile));
            File headdataofthisfile = (File) headcommit._metadata.get(filename);
            if (headdataofthisfile != null) {
                String abdi = U.readContentsAsString(headdataofthisfile);
                String b = U.readContentsAsString(addfile);
                if (abdi.equals(b)) {
                    stagethis.delete();
                }
            }
            if (LISTSTAGESRM.contains(filename)) {
                File stagerm = U.join(STAGINGRM, filename);
                stagerm.delete();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void constructor(String msg) throws IOException {
        String parent = U.readObject(HEAD, String.class);
        Commit addcommit = new Commit(msg, parent);
        File newfile = U.join(COMMIT_FOLDER, committedhash);
        U.writeObject(newfile, addcommit);
    }

    @SuppressWarnings("unchecked")
    public static String getsha1(Date d, String sdjf, String p, HashMap map) {
        return U.sha1(U.s(d), U.s(sdjf), U.s(p), U.s(map));
    }

    @SuppressWarnings("unchecked")
    public static void movepointers(File branch) throws IOException {
        HEAD.delete();
        branch.delete();
        HEAD.createNewFile();
        branch.createNewFile();
        U.writeObject(HEAD, committedhash);
        U.writeObject(branch, committedhash);
    }


    @SuppressWarnings("unchecked")
    public static void checkout(String filename) throws IOException {
        String cdommit = U.readObject(HEAD, String.class);
        File headfile = U.join(COMMIT_FOLDER, cdommit);
        Commit headcommit = U.readObject(headfile, Commit.class);
        if (!headcommit._metadata.containsKey(filename)) {
            System.out.println("File does not exist in that commit");
            return;
        } else {
            File pathtocontent = (File) headcommit._metadata.get(filename);
            File copyto = new File(filename);
            copyto.delete();
            Files.copy(pathtocontent.toPath(), copyto.toPath());
        }
    }

    @SuppressWarnings("unchecked")
    public static void checkout(String cid, String fn) throws IOException {
        File currentidfile = U.join(COMMIT_FOLDER, cid);
        if (!currentidfile.exists()) {
            System.out.println("No commit with that id exists");
            return;
        }
        Commit idcommit = U.readObject(currentidfile, Commit.class);
        if (!idcommit._metadata.containsKey(fn)) {
            System.out.println("File does not exist in that commit");
            return;
        } else {
            File pathtocontent = (File) idcommit._metadata.get(fn);
            File copyto = new File(fn);
            copyto.delete();
            Files.copy(pathtocontent.toPath(), copyto.toPath());
        }
    }

    @SuppressWarnings("unchecked")
    public static void cksht(String cid, String fn) throws IOException {
        File currentidfile = getcommitfromshortid(cid);
        if (!currentidfile.exists()) {
            System.out.println("No commit with that id exists");
            return;
        }
        Commit idcommit = U.readObject(currentidfile, Commit.class);
        if (!idcommit._metadata.containsKey(fn)) {
            System.out.println("File does not exist in that commit");
            return;
        } else {
            File pathtocontent = (File) idcommit._metadata.get(fn);
            File copyto = new File(fn);
            copyto.delete();
            Files.copy(pathtocontent.toPath(), copyto.toPath());
        }
    }



    @SuppressWarnings("unchecked")
    public static void checkoutbranch(String branch) throws IOException {
        File pathtobranch = U.join(BRANCHES, branch);
        String headhash = U.readObject(HEAD, String.class);
        if (!pathtobranch.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        String cureentbranchcommit = U.readObject(pathtobranch, String.class);
        File commitfile = U.join(COMMIT_FOLDER, cureentbranchcommit);
        Commit currentcommit = U.readObject(commitfile, Commit.class);
        Collection<String> filename = currentcommit._metadata.keySet();

        File headcommitfile = findcurrentbranch();
        String headcommithash = U.readObject(headcommitfile, String.class);
        File heafile = U.join(COMMIT_FOLDER, headcommithash);
        Commit headcommit = U.readObject(heafile, Commit.class);

        Collection<String> filenamehead = headcommit._metadata.keySet();
        if (headcommitfile.getName().equals(branch)) {
            System.out.println("No need to checkout the current branch");
            return;
        }
        checkuntracked();

        for (String key : filename) {
            File copyfrom = (File) currentcommit._metadata.get(key);
            File filenamedir = new File(key);
            filenamedir.delete();
            Files.copy(copyfrom.toPath(), filenamedir.toPath());
        }
        File[] listOfFiles = CWD.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && !file.isDirectory()) {
                String dds = file.getName();
                if (!currentcommit._metadata.containsKey(dds)) {
                    file.delete();
                }
            }
        }

        HEAD.delete();
        HEAD.createNewFile();
        U.writeObject(HEAD, U.readObject(pathtobranch, String.class));
        STAGINGRM.delete();
        STAGINGRM.mkdir();
        STAGING.delete();
        STAGING.mkdir();
        File joined = U.join(CB, branch);
        joined.createNewFile();
    }

    @SuppressWarnings("unchecked")
    public static void rm(String filename) throws IOException {
        Commit headfile = fetchcommit("HEAD");
        File stagedfile = U.join(STAGING, filename);
        File currentdirfile = new File(filename);
        File headcontentoffile = (File) headfile._metadata.get(filename);
        if (!stagedfile.exists() && headcontentoffile == null) {
            System.out.println("No reason to remove the file");
        }
        if (stagedfile.exists()) {
            stagedfile.delete();
        }
        if (headcontentoffile != null) {
            File stagethis = U.join(STAGINGRM, filename);
            stagethis.createNewFile();
            currentdirfile.delete();
        }
    }

    @SuppressWarnings("unchecked")
    public static Commit fetchcommit(String branch) {
        File filebranch = U.join(BRANCHES, branch);
        String curcommit = U.readObject(filebranch, String.class);
        File curfile = U.join(COMMIT_FOLDER, curcommit);
        return U.readObject(curfile, Commit.class);
    }

    @SuppressWarnings("unchecked")
    public static void log() {

        String headcommithash = U.readObject(HEAD, String.class);
        File headfile = U.join(COMMIT_FOLDER, headcommithash);
        Commit headcommit = U.readObject(headfile, Commit.class);
        while (headcommit._parent != null) {
            System.out.println("===");
            System.out.println("commit " + headcommithash);
            System.out.println("Date: " + m.format(headcommit._date));
            System.out.println(headcommit._message);
            System.out.println();
            headcommithash = headcommit._parent;
            headfile = U.join(COMMIT_FOLDER, headcommit._parent);
            headcommit = U.readObject(headfile, Commit.class);

        }
        System.out.println("===");
        System.out.println("commit " + headcommithash);
        System.out.println("Date: " + m.format(headcommit._date));
        System.out.println(headcommit._message);
    }

    @SuppressWarnings("unchecked")
    public static void globallog() {
        List<String> commits = U.plainFilenamesIn(COMMIT_FOLDER);
        for (int i = 0; i < commits.size(); i++) {
            File eachcommit = U.join(COMMIT_FOLDER, commits.get(i));
            Commit commitdata = U.readObject(eachcommit, Commit.class);
            System.out.println("===");
            System.out.println("commit " + eachcommit.getName());
            System.out.println("Date: " + m.format(commitdata._date));
            System.out.println(commitdata._message);
            if (i < commits.size() - 1) {
                System.out.println();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void find(String msg) {
        List<String> commits = U.plainFilenamesIn(COMMIT_FOLDER);
        int i = 0;
        for (int j = 0; j < commits.size(); j++) {
            File eachcommit = U.join(COMMIT_FOLDER, commits.get(j));
            Commit commitdata = U.readObject(eachcommit, Commit.class);
            if (commitdata._message.equals(msg)) {
                String id = eachcommit.getName();
                System.out.println(id);
                i += 1;
            }
        }
        if (i == 0) {
            System.out.println("Found no commit with that message");
        }
    }

    @SuppressWarnings("unchecked")
    public static void statushelper() {
        System.out.println("=== Branches ===");
        ArrayList listbranchesmod = new ArrayList();
        listbranchesmod.addAll(LISTBRANCHES);
        listbranchesmod.remove("HEAD");
        Collections.sort(listbranchesmod, String.CASE_INSENSITIVE_ORDER);
        int i = 0;
        while (i < 1) {
            for (int j = 0; j < listbranchesmod.size(); j++) {
                File eb = U.join(BRANCHES, (String) listbranchesmod.get(j));
                String commitid = U.readObject(eb, String.class);
                if (commitid.equals(U.readObject(HEAD, String.class))) {
                    listbranchesmod.set(j, "*" + listbranchesmod.get(j));
                    i += 1;
                }
            }
        }
        for (int j = 0; j < listbranchesmod.size(); j++) {
            System.out.println(listbranchesmod.get(j));
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        ArrayList liststagesmod = new ArrayList();
        liststagesmod.addAll(LISTSTAGES);
        Collections.sort(liststagesmod, String.CASE_INSENSITIVE_ORDER);
        for (int j = 0; j < liststagesmod.size(); j++) {
            System.out.println(liststagesmod.get(j));
        }
        System.out.println();
    }
    @SuppressWarnings("unchecked")
    public static void status() {
        statushelper();
        ArrayList lsm = new ArrayList();
        lsm.addAll(LISTSTAGES);
        Collections.sort(lsm, String.CASE_INSENSITIVE_ORDER);
        System.out.println("=== Removed Files ===");
        ArrayList liststagesrmmod = new ArrayList();
        liststagesrmmod.addAll(LISTSTAGESRM);
        Collections.sort(liststagesrmmod, String.CASE_INSENSITIVE_ORDER);
        ArrayList tractremoval = new ArrayList();
        for (int j = 0; j < liststagesrmmod.size(); j++) {
            System.out.println(liststagesrmmod.get(j));
            tractremoval.add(j, liststagesrmmod.get(j));
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        File headcommitfile = HEAD;
        String headcommithash = U.readObject(headcommitfile, String.class);
        File heafile = U.join(COMMIT_FOLDER, headcommithash);
        Commit headcommit = U.readObject(heafile, Commit.class);
        Collection<String> filenamehead = headcommit._metadata.keySet();
        File[] listOfFiles = CWD.listFiles();
        for (File file : listOfFiles) {
            if (filenamehead.contains(file.getName())) {
                File df = (File) headcommit._metadata.get(file.getName());
                String reada = U.readContentsAsString(df);
                String readfile = U.readContentsAsString(file);
                if (!reada.equals(readfile) && !file.isDirectory()) {
                    if (!liststagesrmmod.contains(file.getName())) {
                        System.out.println(file.getName() + "(modified)");
                    }
                }
            }
        }
        for (String file : filenamehead) {
            if (!LISTCWD.contains(file)) {
                if (!tractremoval.contains(file)) {
                    System.out.println(file + "(deleted)");
                }
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (File file : listOfFiles) {
            if (file.isFile() && !file.isDirectory()) {
                String name = file.getName();
                if (!lsm.contains(name) && !filenamehead.contains(name)) {
                    System.out.println(name);
                }
            }
        }
        File[] listOfFstagingrmfiles = STAGINGRM.listFiles();
        for (File stagefile : listOfFstagingrmfiles) {
            if (stagefile.isFile()) {
                if (LISTCWD.contains(stagefile.getName())) {
                    System.out.println(stagefile.getName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static File findcurrentbranch() {
        ArrayList listbranchesmod = new ArrayList();
        listbranchesmod.addAll(LISTBRANCHES);
        listbranchesmod.remove("HEAD");
        listbranchesmod.remove("curbranch");
        for (int j = 0; j < listbranchesmod.size(); j++) {
            File eachbranch = U.join(BRANCHES, (String) listbranchesmod.get(j));
            String commitid = U.readObject(eachbranch, String.class);
            if (commitid.equals(U.readObject(HEAD, String.class))) {
                return eachbranch;
            }
        }
        throw new GitletException("no branch with head pointer??? lol");
    }

    @SuppressWarnings("unchecked")
    private static File getcommitfromshortid(String commitid) {
        String commitidfull = null;
        for (File file : COMMIT_FOLDER.listFiles()) {
            if (file.isFile()) {
                if (file.getName().contains(commitid)) {
                    commitidfull = file.getName();
                }
            }
        }
        if (commitidfull == null) {
            System.out.println("No commit with that id exists");
            return null;
        } else {
            return U.join(COMMIT_FOLDER, commitidfull);
        }
    }





    @SuppressWarnings("unchecked")
    public static void createbranch(String name) throws IOException {
        File branchcreated = U.join(BRANCHES, name);
        if (branchcreated.exists()) {
            System.out.println("A branch with that name already exists");
            return;
        }
        branchcreated.createNewFile();
        U.writeObject(branchcreated, U.readObject(HEAD, String.class));
    }

    @SuppressWarnings("unchecked")
    public static void removebranch(String name) {
        File branchremoved = U.join(BRANCHES, name);
        if (!branchremoved.exists()) {
            System.out.println("A branch with that name does not exist");
            return;
        } else if (name.equals(findcurrentbranch().getName())) {
            System.out.println("Cannot remove the current branch");
            return;
        } else {
            branchremoved.delete();
        }
    }
    @SuppressWarnings("unchecked")
    public static void reset(String commitid) throws IOException {
        File currentidfile = getcommitfromshortid(commitid);
        if (currentidfile == null) {
            return;
        }
        File[] filelist = CWD.listFiles();
        String msg = "There is an untracked file in the way;"
                + " delete it, or add and commit it first.";
        for (File file : filelist) {
            if (!file.isDirectory() && file.isFile()) {
                Commit headcommit = fetchcommit("HEAD");
                File hotf = (File) headcommit._metadata.get(file.getName());
                if (hotf == null) {
                    if (LISTSTAGES.contains(file.getName())) {
                        continue;
                    } else {
                        System.out.println(msg);
                        return;
                    }
                } else if (hotf != null) {
                    String abc = U.readContentsAsString(hotf);
                    String b = U.readContentsAsString(file);
                    if (!abc.equals(b)) {
                        System.out.println(msg);
                        return;
                    }
                }
            }
        }
        Commit currentcommit = U.readObject(currentidfile, Commit.class);
        File headcommitfile = HEAD;
        String headcommithash = U.readObject(headcommitfile, String.class);
        Collection<String> filename = currentcommit._metadata.keySet();
        for (String key : filename) {
            File copyfrom = (File) currentcommit._metadata.get(key);
            File filenamedir = new File(key);
            filenamedir.delete();
            Files.copy(copyfrom.toPath(), filenamedir.toPath());
        }
        File currbranch = findcurrentbranch();
        HEAD.delete();
        HEAD.createNewFile();
        U.writeObject(HEAD, commitid);
        currbranch.delete();
        currbranch.createNewFile();
        U.writeObject(currbranch, commitid);
        if (STAGINGRM.isDirectory()) {
            File[] content = STAGINGRM.listFiles();
            for (int i = 0; i < content.length; i++) {
                content[i].delete();
            }
        }
        if (STAGING.isDirectory()) {
            File[] content = STAGING.listFiles();
            for (int i = 0; i < content.length; i++) {
                content[i].delete();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkuntracked() {
        File headcommitfile = HEAD;
        String headcommithash = U.readObject(headcommitfile, String.class);
        File heafile = U.join(COMMIT_FOLDER, headcommithash);
        Commit headcommit = U.readObject(heafile, Commit.class);


        Collection<String> filenamehead = headcommit._metadata.keySet();
        File[] listOfFiles = CWD.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile() && !file.isDirectory()) {
                String sb = file.getName();
                if (!filenamehead.contains(sb)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete "
                            + "it, or add and commit it first.");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void merge(String branchname) {
        File curbranch = U.join(BRANCHES, branchname);
        File[] filestage = STAGING.listFiles();
        File[] filermstage = STAGINGRM.listFiles();

        if (!curbranch.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (filermstage.length != 0 || filestage.length != 0) {
            System.out.println("You have uncommitted changes.");
        }
        checkuntracked();
        if (curbranch.equals(HEAD)) {
            System.out.println("Cannot merge a branch with itself");
        }
    }
}
