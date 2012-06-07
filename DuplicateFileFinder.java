import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Scanner;


public class DuplicateFileFinder implements HashCallback
{
    private HashMap<String, HashSet<String>> dupFilesGroup;
    private String path;

    public DuplicateFileFinder(String path)
    {
        this.dupFilesGroup = new HashMap<String, HashSet<String>>();
        this.path = path;
        try {
            HashUtil.hash_and_call(HashUtil.listFiles(path), this, MessageDigest.getInstance("SHA1"));
        } catch (NoSuchAlgorithmException ex) {
        } catch (IOException ex) {
        }
    }

    public String[][] getDuplicateFiles()
    {
        ArrayList<String[]> groupOfDup = new ArrayList<String[]>();
        Iterator<HashSet<String>> groupIter = dupFilesGroup.values().iterator();
        while (groupIter.hasNext()) {
            HashSet<String> set = groupIter.next();
            if (set.size() > 1) {
                groupOfDup.add((new ArrayList<String>(set)).toArray(new String[set.size()]));
            }
        }
        return groupOfDup.toArray(new String[groupOfDup.size()][]);

    }

    /** Group all files by their hash code. This must be only called by
     * hash_and_call */
    public void callback(String hash, String filePath)
    {
        HashSet<String> set;
        if (!dupFilesGroup.containsKey(hash)) {
            set = new HashSet<String>();
            dupFilesGroup.put(hash, set);
        } else {
            set = dupFilesGroup.get(hash);
        }

        try {
            set.add((new File(filePath)).getCanonicalPath());
        } catch (IOException ex) {
        }
    }
}
