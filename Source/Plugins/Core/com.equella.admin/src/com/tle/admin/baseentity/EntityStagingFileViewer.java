/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.workers.GlassSwingWorker;
import com.google.common.base.Throwables;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.remoting.RemoteAbstractEntityService;

/**
 * @author Andrew Gibb
 */
@SuppressWarnings({"nls", "serial"})
public class EntityStagingFileViewer extends JPanel
{
	private static final int UPLOAD_CHUNK_SIZE = 1048576;

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

	public EntityStagingFileViewer(EditorState<?> state, RemoteAbstractEntityService<?> service, String rootFolder)
	{
		this.service = service;
		this.rootFolder = rootFolder;
		this.stagingID = state.getEntityPack().getStagingID();

		// Load Swing constructs
		setupGUI();

		// Load Tree (Glass worker)
		setupTree();
	}

	private void setupGUI()
	{
		// Create Components
		fileTreeModel = new CustomTreeModel(new DefaultMutableTreeNode("Loading..."));
		fileTree = new JTree(fileTreeModel);
		fileTree.setRootVisible(false);
		fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileTree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
				if( node == null )
				{
					btnEnable(true, false, true, false);
				}
				else
				{
					if( !node.getAllowsChildren() )
					{
						btnEnable(true, true, true, true);
					}
					else
					{
						btnEnable(true, false, true, true);
					}
				}
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

	private void setupTree()
	{
		// Load Tree
		GlassSwingWorker<?> worker = new GlassSwingWorker<FileEntry>()
		{
			@Override
			public FileEntry construct()
			{
				return service.buildStagingTree(stagingID, rootFolder);
			}

			@Override
			public void finished()
			{
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
	private DefaultMutableTreeNode processStagingTree(FileEntry stagingTree)
	{
		List<FileEntry> files = stagingTree.getFiles();

		DefaultMutableTreeNode node = new DefaultMutableTreeNode(stagingTree.getName(), true);

		DefaultMutableTreeNode child;

		for( FileEntry fileEntry : files )
		{
			if( fileEntry.isFolder() )
			{
				child = processStagingTree(fileEntry);
			}
			else
			{
				child = new DefaultMutableTreeNode(fileEntry.getName(), false);
			}
			node.add(child);
		}
		return node;
	}

	private final transient ActionListener uploadFileListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Show file chooser
			DialogResult result = DialogUtils.openDialog(getParent(), "Upload File");

			if( result.isOkayed() )
			{
				File file = result.getFile();

				try( BufferedInputStream in = new BufferedInputStream(new FileInputStream(file)) )
				{
					String path = rootFolder;

					// If there is a selection stick it in that container (eww)
					if( !fileTree.isSelectionEmpty() )
					{
						TreePath selectionPath = fileTree.getSelectionPath();
						DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

						// If the selected is a folder use it
						if( lastNode.getAllowsChildren() )
						{
							path = getSelectedPath(selectionPath);
						}
						else
						{
							// Use parent container instead
							path = getSelectedPath(selectionPath.getParentPath());
						}
					}
					uploadFile(stagingID, path + file.getName(), in);
				}
				catch( IOException e1 )
				{
					throw Throwables.propagate(e1);
				}

				fileTreeModel.reload();
			}
		}
	};

	private final transient ActionListener downloadFileListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Check for selection
			if( !fileTree.isSelectionEmpty() )
			{
				// Get Path from tree
				TreePath selectionPath = fileTree.getSelectionPath();
				DefaultMutableTreeNode lastNode = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
				String path = null;

				if( !lastNode.getAllowsChildren() )
				{
					path = getSelectedPath(selectionPath);

					byte[] downloadedData = null;

					// Try to get file
					try
					{
						downloadedData = service.downloadFile(stagingID, path);
					}
					catch( IOException e1 )
					{
						throw Throwables.propagate(e1);
					}

					// Show save dialog with appropriate filename
					DialogResult result = DialogUtils.saveDialog(getParent(), "Save File", null, lastNode.toString());

					if( result.isOkayed() )
					{
						File file = result.getFile();
						try( OutputStream stream = new BufferedOutputStream(new FileOutputStream(file)) )
						{
							stream.write(downloadedData);
						}
						catch( IOException e1 )
						{
							throw Throwables.propagate(e1);
						}
					}
					fileTreeModel.reload();
				}
			}
		}
	};

	private final transient ActionListener deleteFileFolderListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Check for selection
			if( !fileTree.isSelectionEmpty() )
			{
				TreePath selectionPath = fileTree.getSelectionPath();
				String path = getSelectedPath(selectionPath);

				// Show confirmation
				int result = JOptionPane.showConfirmDialog(getParent(), s("dialog.delete.desc"),
					s("dialog.delete.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

				if( result == JOptionPane.YES_OPTION )
				{
					service.deleteFileFolder(stagingID, path);
					fileTreeModel.reload();
				}
			}
		}
	};

	private final transient ActionListener createFolderListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Ask for name
			String name = (String) JOptionPane.showInputDialog(getParent(), s("dialog.newfolder.desc"),
				s("dialog.newfolder.title"), JOptionPane.PLAIN_MESSAGE, null, null, null);

			// Have they entered a name?
			if( name != null )
			{
				String path = rootFolder;

				// Check for selection
				if( !fileTree.isSelectionEmpty() )
				{
					TreePath selectionPath = fileTree.getSelectionPath();
					DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectionPath
						.getLastPathComponent();

					if( lastPathComponent.getAllowsChildren() )
					{
						path = getSelectedPath(selectionPath);
					}
					else
					{
						path = getSelectedPath(selectionPath.getParentPath());
					}
				}
				service.createFolder(stagingID, path, name);

				fileTreeModel.reload();
			}
		}
	};

	// Stolen from Driver
	private void uploadFile(String staging, String filename, InputStream stream) throws IOException
	{
		try
		{
			byte[] bytes = new byte[UPLOAD_CHUNK_SIZE];
			int read = 0;
			int offset = 0;
			while( (read = stream.read(bytes, 0, UPLOAD_CHUNK_SIZE)) > 0 )
			{
				byte[] data = bytes;

				// hack: uploadFile doesn't take length param,
				// which is fine, we don't want to send unused bytes over the
				// wire...
				if( read < UPLOAD_CHUNK_SIZE )
				{
					data = new byte[read];
					System.arraycopy(bytes, 0, data, 0, read);
				}

				service.uploadFile(staging, filename, data);
				offset += read;
			}
		}
		catch( IOException e )
		{
			try
			{
				// Try to remove grotesque, disfigured, incomplete, aborted
				// creations
				service.deleteFileFolder(staging, filename);
			}
			catch( Exception other )
			{
				// Forget it.
			}
			throw e;
		}
	}

	// Ghetto method to turn [root, path, leaf] into file paths
	private String getSelectedPath(TreePath selectionPath)
	{
		String path = selectionPath.toString();
		path = path.replace(", ", "/");
		path = path.substring(1, path.length() - 1);
		path = path + "/";

		return path;
	}

	private void btnEnable(boolean upload, boolean download, boolean createfolder, boolean del)
	{
		uploadFile.setEnabled(upload);
		downloadFile.setEnabled(download);
		createFolder.setEnabled(createfolder);
		delete.setEnabled(del);
	}

	private class CustomTreeModel extends DefaultTreeModel
	{
		private static final long serialVersionUID = -4340059723136834119L;

		public CustomTreeModel(TreeNode root)
		{
			super(root);
		}

		@Override
		public void reload()
		{
			setupTree();
		}

		@Override
		public boolean isLeaf(Object node)
		{
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
			return !treeNode.getAllowsChildren();
		}
	}

	public CustomTreeModel getFileTreeModel()
	{
		return fileTreeModel;
	}

	// Potentially expensive
	private void expandTree()
	{
		for( int i = 0; i < fileTree.getRowCount(); i++ )
		{
			fileTree.expandRow(i);
		}
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.tools.stagingfileviewer." + keyPart);
	}

}
