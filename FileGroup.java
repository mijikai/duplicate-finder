import java.util.LinkedHashSet;
import java.io.File;
import java.util.Iterator;

public class FileGroup
{
    private String hash;
    private LinkedHashSet<File> files;
    private File head;

    public FileGroup(String hash)
    {
        this.hash = hash;
        files = new LinkedHashSet<File>();
    }

    private updateHead(File file, boolean isAdd)
    {
        if (isAdd) {
            if (files.isEmpty() || files.toArray()[0].equals(file)) {
                head = file;
            }
        } else if (head != null && head.equals(file)) {
            if (files.isEmpty()) {
                head = null;
            } else {
                head = files.toArray()[0];
            }
        }
    }

    public boolean addFile(String hash, File file)
    {
        if (this.hash != null && this.hash.equals(hash)) {
            updateHead(file, true);
            return files.add(file);
        }
        return false;
    }

    public boolean removeFile(File file)
    {
        boolean result = files.remove(file);
        updateHead(file, false);
        return result;
    }

    public boolean contains(File file)
    {
        return files.contains(file);
    }

    public File fileAt(int index)
    {
        return (File)files.toArray()[index];
    }

    public int indexOf(File file)
    {
        int ind = 0;
        Iterator<File> iter = files.iterator();
        File other;
        while (iter.hasNext()) {
            other = iter.next();
            if (file.equals(other)) {
                return ind;
            }
            ind++;
        }
        return -1;
    }

    public String toString()
    {
        if (hash == null || hash.equals("")) {
            return "<Group Unknown>";
        }

        if (files.isEmpty()) {
            return "<No Files>";
        }

        return head.getName();
    }

    public int size()
    {
        return files.size();
    }
}
