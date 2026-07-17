/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.baseentity;

import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.i18n.Lookup;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.i18n.StringLookup;
import com.tle.core.remoting.RemoteAbstractEntityService;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import net.miginfocom.swing.MigLayout;

public class EntityStagingFileViewer extends JPanel {
  private static final StringLookup strings = Lookup.lookup;

  private final RemoteAbstractEntityService<?> service;
  private final String stagingID;

  private final String rootFolder;

  // Action Buttons
  private JButton createFolder;
  private JButton uploadFile;
  private JButton downloadFile;
  private JButton delete;

  // Tree
  private JTree fileTree;
  private CustomTreeModel fileTreeModel;

  public EntityStagingFileViewer(
      EditorState<?> state, RemoteAbstractEntityService<?> service, String rootFolder) {
    this.service = service;
    this.rootFolder = rootFolder;
    this.stagingID = state.getEntityPack().getStagingID();

    // Load Swing constructs
    setupGUI();

    // Load Tree (Glass worker)
    setupTree();
  }

  private void setupGUI() {
    // Create Components
    fileTreeModel = new CustomTreeModel(new DefaultMutableTreeNode("Loading..."));
    fileTree = new JTree(fileTreeModel);
    fileTree.setRootVisible(false);
    fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    fileTree.addTreeSelectionListener(
        e -> {
          DefaultMutableTreeNode node =
              (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
          if (node == null) {
            btnEnable(true, false, true, false);
          } else {
            btnEnable(true, !node.getAllowsChildren(), true, true);
          }
        });

    // Holds all the action buttons
    setLayout(new MigLayout("insets 0, fill, wrap 2", "[fill][fill,grow]", "[][][][][][][grow]"));

    uploadFile = new JButton(s("button.upload"));
    add(uploadFile);
    uploadFile.addActionListener(uploadFileListener);

    // Add to layout
    add(new JScrollPane(fileTree), "spany 7, growy");

    downloadFile = new JButton(s("button.download"));
    add(downloadFile);
    downloadFile.addActionListener(downloadFileListener);

    add(new JSeparator());

    createFolder = new JButton(s("button.newfolder"));
    add(createFolder);
    createFolder.addActionListener(createFolderListener);

    add(new JSeparator());

    delete = new JButton(s("button.delete"));
    add(delete);
    delete.addActionListener(deleteFileFolderListener);

    // Disable all buttons
    btnEnable(true, false, true, false);
  }

  private void setupTree() {
    // Load Tree
    GlassSwingWorker<?> worker =
        new GlassSwingWorker<FileEntry>() {
          @Override
          public FileEntry construct() {
            return service.buildStagingTree(stagingID, rootFolder);
          }

          @Override
          public void finished() {
            DefaultMutableTreeNode processedTree = processStagingTree(get());
            fileTreeModel.setRoot(processedTree);
            fileTree.setModel(fileTreeModel);

            // Expands tree fully
            expandTree();
          }
        };
    worker.setComponent(this);
    worker.start();
  }

  // Processes output from enumerateTree from the file system service into a
  // tree model edible DefaultMutableTreeNode collection
  private DefaultMutableTreeNode processStagingTree(FileEntry stagingTree) {
    List<FileEntry> files = stagingTree.getFiles();

    DefaultMutableTreeNode node = new DefaultMutableTreeNode(stagingTree.getName(), true);

    DefaultMutableTreeNode child;

    for (FileEntry fileEntry : files) {
      if (fileEntry.isFolder()) {
        child = processStagingTree(fileEntry);
      } else {
        child = new DefaultMutableTreeNode(fileEntry.getName(), false);
      }
      node.add(child);
    }
    return node;
  }

  private final transient ActionListener uploadFileListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Show file chooser
          DialogResult result = DialogUtils.openDialog(getParent(), "Upload File");

          if (result.isOkayed()) {
            File file = result.getFile();

            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
              String path = rootFolder;

              // If there is a selection stick it in that container (eww)
              if (!fileTree.isSelectionEmpty()) {
                TreePath selectionPath = fileTree.getSelectionPath();
                DefaultMutableTreeNode lastNode =
                    (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

                // If the selected is a folder use it
                if (lastNode.getAllowsChildren()) {
                  path = getSelectedPath(selectionPath);
                } else {
                  // Use parent container instead
                  path = getSelectedPath(selectionPath.getParentPath());
                }
              }
              uploadFile(stagingID, path + file.getName(), in);
            } catch (IOException e1) {
              throw new RuntimeException(e1);
            }

            fileTreeModel.reload();
          }
        }
      };

  private final transient ActionListener downloadFileListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Check for selection
          if (!fileTree.isSelectionEmpty()) {
            // Get Path from tree
            TreePath selectionPath = fileTree.getSelectionPath();
            DefaultMutableTreeNode lastNode =
                (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            String path = null;

            if (!lastNode.getAllowsChildren()) {
              path = getSelectedPath(selectionPath);

              byte[] downloadedData = null;

              // Try to get file
              try {
                downloadedData = service.downloadFile(stagingID, path);
              } catch (IOException e1) {
                throw new RuntimeException(e1);
              }

              // Show save dialog with appropriate filename
              DialogResult result =
                  DialogUtils.saveDialog(getParent(), "Save File", null, lastNode.toString());

              if (result.isOkayed()) {
                File file = result.getFile();
                try (OutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
                  stream.write(downloadedData);
                } catch (IOException e1) {
                  throw new RuntimeException(e1);
                }
              }
              fileTreeModel.reload();
            }
          }
        }
      };

  private final transient ActionListener deleteFileFolderListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Check for selection
          if (!fileTree.isSelectionEmpty()) {
            TreePath selectionPath = fileTree.getSelectionPath();
            String path = getSelectedPath(selectionPath);

            // Show confirmation
            int result =
                JOptionPane.showConfirmDialog(
                    getParent(),
                    s("dialog.delete.desc"),
                    s("dialog.delete.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
              service.deleteFileFolder(stagingID, path);
              fileTreeModel.reload();
            }
          }
        }
      };

  private final transient ActionListener createFolderListener =
      new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Ask for name
          String name =
              (String)
                  JOptionPane.showInputDialog(
                      getParent(),
                      s("dialog.newfolder.desc"),
                      s("dialog.newfolder.title"),
                      JOptionPane.PLAIN_MESSAGE,
                      null,
                      null,
                      null);

          // Have they entered a name?
          if (name != null) {
            String path = rootFolder;

            // Check for selection
            if (!fileTree.isSelectionEmpty()) {
              TreePath selectionPath = fileTree.getSelectionPath();
              DefaultMutableTreeNode lastPathComponent =
                  (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

              if (lastPathComponent.getAllowsChildren()) {
                path = getSelectedPath(selectionPath);
              } else {
                path = getSelectedPath(selectionPath.getParentPath());
              }
            }
            service.createFolder(stagingID, path, name);

            fileTreeModel.reload();
          }
        }
      };

  // The whole file is sent in a single call: the REST-backed uploadFile overwrites the target
  // on each call, so it must not be invoked repeatedly with partial content for the same path.
  private void uploadFile(String staging, String filename, InputStream stream) throws IOException {
    try {
      service.uploadFile(staging, filename, stream.readAllBytes());
    } catch (IOException e) {
      try {
        // Try to remove grotesque, disfigured, incomplete, aborted
        // creations
        service.deleteFileFolder(staging, filename);
      } catch (Exception other) {
        // Forget it.
      }
      throw e;
    }
  }

  // Ghetto method to turn [root, path, leaf] into file paths
  private String getSelectedPath(TreePath selectionPath) {
    String path = selectionPath.toString();
    path = path.replace(", ", "/");
    path = path.substring(1, path.length() - 1);
    path = path + "/";

    return path;
  }

  private void btnEnable(boolean upload, boolean download, boolean createfolder, boolean del) {
    uploadFile.setEnabled(upload);
    downloadFile.setEnabled(download);
    createFolder.setEnabled(createfolder);
    delete.setEnabled(del);
  }

  private class CustomTreeModel extends DefaultTreeModel {
    @Serial private static final long serialVersionUID = -4340059723136834119L;

    public CustomTreeModel(TreeNode root) {
      super(root);
    }

    @Override
    public void reload() {
      setupTree();
    }

    @Override
    public boolean isLeaf(Object node) {
      DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
      return !treeNode.getAllowsChildren();
    }
  }

  public CustomTreeModel getFileTreeModel() {
    return fileTreeModel;
  }

  // Potentially expensive
  private void expandTree() {
    for (int i = 0; i < fileTree.getRowCount(); i++) {
      fileTree.expandRow(i);
    }
  }

  private static String s(String keyPart) {
    return strings.text("stagingfileviewer." + keyPart);
  }
}
