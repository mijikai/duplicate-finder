import java.security.MessageDigest;
import java.io.File;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Scanner;
import java.security.DigestInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class HashUtil
{
    /** Join two string path */
    public static String joinPath(String path1, String path2)
    {
        return (new File(path1, path2)).getPath();
    }

    /**
     * List all files in the current dirctory recursively.
     */
    public static String[] listFiles(String dirPath)
    {
        Stack<String> dirPaths = new Stack<String>();
        ArrayList<String> filePaths = new ArrayList<String>();
        String[] currentContent;
        String currentPath;
        dirPaths.push(dirPath);

        while (!dirPaths.isEmpty()) {
            currentPath = dirPaths.pop();
            currentContent = (new File(currentPath)).list();
            if (currentContent == null) {
                continue;
            }
            for (String path : currentContent) {
                path = joinPath(currentPath, path);
                if ((new File(path)).isDirectory()) {
                    dirPaths.push(path);
                } else {
                    filePaths.add(path);
                }
            }
        }
        return filePaths.toArray(new String[filePaths.size()]);
    }

    /** Calculates the hexadecimal hash of file through the use of
     * hasher.
     */
    public static String digest(String path, MessageDigest hasher) throws IOException
    {
        int blen = 1024;
        byte[] bytes = new byte[blen];
        int readCount;
        FileInputStream stream = new FileInputStream(path);
        DigestInputStream digestor = new DigestInputStream(stream, hasher); 
        digestor.on(true);

        while((readCount = digestor.read(bytes)) != -1);  // read until the end of file

        byte[] hash = hasher.digest();
        String result = "";

        // Byte to hexadecimal representation
        for (int i=0; i < hash.length; i++) {
            result += Integer.toString( ( hash[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        stream.close();
        return result;
    }

    /** Calculates the hash of a file and then call the method callback from
     * HashCallback class. If the file is a directory or does not exists, it will be
     * ignore. Path can be relative or absolute path.
     */
    public static void hash_and_call(String[] filePaths, HashCallback callback, MessageDigest hasher) throws IOException
    {
        String computed_hash;
        for (String filePath : filePaths) {
            try {
                computed_hash = digest(filePath, hasher);
            } catch (FileNotFoundException ex) {
                continue;
            } 
            callback.callback(computed_hash, filePath);
        }
    }
}
