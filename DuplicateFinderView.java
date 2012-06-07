import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.security.MessageDigest;
import java.io.File;
import javax.swing.tree.*;


public class DuplicateFinderView extends JFrame
{
    private JTree dupFileList; // A view of the list of files containing duplicate
    private JButton selectDirButton; // Invoke FileChooser
    private JButton findDupButton; // Invoke finding duplicates in the selected path
    private JTextField dirPathField; // Field where user will enter a directory 
    private JFileChooser fc; 
    private DuplicateFileTree tree; // A tree grouping the files having the same contents
    private JButton moveFile, deleteFile;

    public DuplicateFinderView()
    {
        init_components();
        register_listeners();
        setMinimumSize(new Dimension(400, 300));
        setTitle("Duplicate Finder");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
    }

    private void init_components()
    {

        selectDirButton = new JButton("Select directory");
        findDupButton = new JButton("Find duplicate");
        dirPathField = new JTextField();
        dupFileList = new JTree();
        moveFile = new JButton("Move");
        deleteFile = new JButton("Delete");
        fc = new JFileChooser();
        tree =  new DuplicateFileTree();

        dupFileList.setModel(tree);
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);

        findDupButton.setEnabled(false);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.add(dirPathField);
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(selectDirButton);
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(findDupButton);

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(1, 2, 5, 5));
        center.setBorder(new EmptyBorder(new Insets(5, 0, 5, 0)));
        center.add(new JScrollPane(dupFileList));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(moveFile);
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(deleteFile);

        JPanel base = new JPanel();
        base.setLayout(new BorderLayout());
        base.setBorder(new EmptyBorder(new Insets(20, 20, 20, 20)));
        base.add(top, BorderLayout.NORTH);
        base.add(center, BorderLayout.CENTER);
        base.add(bottom, BorderLayout.SOUTH);

        add(base, BorderLayout.CENTER);
    }

    private void register_listeners()
    {
        selectDirButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    dirPathField.setText(fc.getSelectedFile().getPath());
                }
            }
        });

        dirPathField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)
            {
                findDupButton.setEnabled(true);
            }

            public void removeUpdate(DocumentEvent e) {
                if (e.getDocument().getLength() == 0) {
                    findDupButton.setEnabled(false);
                }
            }

            public void changedUpdate(DocumentEvent e) {}
        });

        ActionListener findDuplicate = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String path = dirPathField.getText();
                DuplicateFileTree model = new DuplicateFileTree();
                dupFileList.setModel(model);
                MessageDigest sha1 = null;
                try {
                    sha1 = MessageDigest.getInstance("sha1");
                    (new Thread(new RunHash(path, model, sha1))).start();
                } catch (Exception ex) {}
                
            }
        };

        moveFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    TreePath[] selections = dupFileList.getSelectionModel().getSelectionPaths();
                    File moveTo = new File(fc.getSelectedFile().getPath());
                    if (!moveTo.exists() && !moveTo.mkdirs()) {
                        return;
                    }
                    File file, newFile;
                    for (TreePath select : selections) {
                        file = (File)select.getLastPathComponent();
                        try {
                            newFile = new File(moveTo.getCanonicalPath() + File.separatorChar + file.getName());
                        } catch (Exception ex) { continue; }
                        file.renameTo(newFile);
                        tree.removeFile(file);
                    }
                }
            }
        });

        deleteFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                TreePath[] selections = dupFileList.getSelectionModel().getSelectionPaths();
                File[] files = new File[selections.length];
                for (int i = 0; i < selections.length; i++) {
                    files[i] = (File)selections[i].getLastPathComponent();
                }
                int res = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete selected files?");
                if (res != JOptionPane.YES_OPTION) {
                    return;
                }
                for (File file : files) {
                    file.delete();
                    tree.removeFile(file);
                }
            }
        });

        findDupButton.addActionListener(findDuplicate);
        dirPathField.addActionListener(findDuplicate);
    }

    private class RunHash implements Runnable
    {
        private String path;
        private DuplicateFileTree model;
        private MessageDigest digest;

        public RunHash(String path, DuplicateFileTree model, MessageDigest digest)
        {
            this.path = path;
            this.model = model;
            this.digest = digest;
        }

        public void run()
        {
            try {
                HashUtil.hash_and_call(HashUtil.listFiles(path), model, digest);
            } catch (Exception ex) {}
        }
    }

    public static void main(String[] args)
    {
        new DuplicateFinderView().setVisible(true);
    }
}
