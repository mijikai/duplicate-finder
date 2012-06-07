import javax.swing.*;
import java.security.MessageDigest;
import java.io.File;

public class Test
{
    private static JFrame frame;
    public static void main(String[] args) throws Exception
    {
        frame = new JFrame();
        JTree tree = new JTree();
        JScrollPane pane = new JScrollPane(tree);
        DuplicateFileTree fileTree = new DuplicateFileTree();

        frame.setSize(200, 200);
        frame.add(pane);
        tree.setModel(fileTree);
        frame.setVisible(true);
        MessageDigest sha1 = MessageDigest.getInstance("sha1");
        HashUtil.hash_and_call(HashUtil.listFiles("/home/lester/Programming"), fileTree, sha1);
        fileTree.removeFile(new File("/media/windows/B/Users/lester/Documents/Programming/Java Prog/Programs/Scheme/dragon.scm"));
    }
}
