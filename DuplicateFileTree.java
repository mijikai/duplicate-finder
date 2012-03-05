import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeModelListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import javax.swing.event.TreeModelEvent;

public class DuplicateFileTree implements TreeModel, HashCallback
{
    private HashMap<String, Integer> hashToArrayIndGroup;
    private HashMap<String, Integer> hashToArrayIndNoDup;
    private GroupList<FileGroup> groups;
    private ArrayList<FileGroup> noDups;
    private ArrayList<TreeModelListener> listeners;

    public DuplicateFileTree()
    {
        hashToArrayIndGroup = new HashMap<String, Integer>();
        hashToArrayIndNoDup = new HashMap<String, Integer>();
        groups = new GroupList<FileGroup>();
        noDups = new ArrayList<FileGroup>();
        listeners = new ArrayList<TreeModelListener>();
    }

    public Object getChild(Object parent, int index)
    {
        if (parent == groups) {
            return groups.get(index);
        } else if (parent instanceof FileGroup) {
            return ((FileGroup)parent).fileAt(index);
        }
        return null;
    }

    public int getChildCount(Object parent)
    {
        if (parent == groups) {
            return groups.size();
        } else if (parent instanceof FileGroup) {
            return ((FileGroup)parent).size();
        }
        return 0;
    }

    public int getIndexOfChild(Object parent, Object child)
    {
        if (parent == groups) {
            return groups.indexOf(child);
        } else if (parent instanceof FileGroup) {
            return ((FileGroup)parent).indexOf((File)child);
        }
        return -1;
    }

    public Object getRoot()
    {
        return groups;
    }

    public boolean isLeaf(Object node)
    {
        if (node == groups || node instanceof FileGroup && groups.contains(node)) {
           return false;
        }

        return true;
    }

    public void removeFile(File file)
    {
        for (FileGroup gr : groups) {
            if (gr.contains(file)) {
                gr.removeFile(file);
                fireTreeChanged(gr, REMOVE);
                fireTreeChanged(gr, CHANGE);
            }
        }
    }

    public void valueForPathChanged(TreePath path, Object newValue)
    {
        System.out.println(path);
    }

    public void addTreeModelListener(TreeModelListener l)
    {
        listeners.add(l);
    }

    public void removeTreeModelListener(TreeModelListener l)
    {
        listeners.remove(l);
    }

    public void callback(String hash, String filePath)
    {
        int i;
        Integer indDup = hashToArrayIndGroup.get(hash);  // index of the files with the same hash
        Integer indNoDup;
        File file = new File(filePath);
        FileGroup fileGroup = null;
        try {
            file = new File(file.getCanonicalPath());
        } catch (IOException ex) { }

        if (indDup == null) {    // There are no duplicate found
            indNoDup = hashToArrayIndNoDup.get(hash);
            if (indNoDup == null) {  // The hash is new so add it into consideration
                fileGroup = new FileGroup(hash);
                fileGroup.addFile(hash, file);

                noDups.add(fileGroup);

                i = noDups.indexOf(fileGroup);
                hashToArrayIndNoDup.put(hash, i);

                fireTreeChanged(fileGroup, INSERT);
            } else { // There is a matching file so remove it from consideration to the duplicate files
                i = indNoDup.intValue();
                fileGroup = noDups.get(i);
                fileGroup.addFile(hash, file);

                if (fileGroup.size() == 1) { // The file may be a symlink so do not continue
                    return;
                }

                groups.add(fileGroup);

                noDups.remove(i);
                hashToArrayIndNoDup.remove(hash);

                i = groups.indexOf(fileGroup);
                hashToArrayIndGroup.put(hash, i);

                fireTreeChanged(fileGroup, STRUCTURE);
            }
        } else {  // there are matching files
            i = indDup.intValue();
            fileGroup = groups.get(i);
            fileGroup.addFile(hash, file);

            fireTreeChanged(fileGroup, INSERT);
        }

    }

    protected final int INSERT = 0;
    protected final int REMOVE = 1;
    protected final int CHANGE = 2;
    protected final int STRUCTURE = 3;

    protected void fireTreeChanged(FileGroup group, int changeType)
    {
        TreeModelEvent e = null;
        switch(changeType) {

        case INSERT:
            e = new TreeModelEvent(this, new Object[] {groups, group});
            for (TreeModelListener tml : listeners) {
                tml.treeNodesInserted(e);
            }
            break;

        case REMOVE:
            e = new TreeModelEvent(this, new Object[] {groups, group});
            for (TreeModelListener tml : listeners) {
                tml.treeNodesRemoved(e);
            }
            break;

        case CHANGE:
            e = new TreeModelEvent(this, new Object[] {groups, group});
            for (TreeModelListener tml : listeners) {
                tml.treeNodesChanged(e);
            }
            break;

        case STRUCTURE:
            e = new TreeModelEvent(this, new Object[] {groups});
            for (TreeModelListener tml : listeners) {
                tml.treeStructureChanged(e);
            }
            break;
        } 
    }

    private class GroupList<E> extends ArrayList<E>
    {
        public String toString()
        {
            return "Duplicate Files";
        }
    }
}
