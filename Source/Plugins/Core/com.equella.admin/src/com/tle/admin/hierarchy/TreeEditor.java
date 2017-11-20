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

package com.tle.admin.hierarchy;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.dytech.devlib.PropBagEx;
import com.tle.common.beans.exception.ValidationError;
import com.dytech.gui.workers.GlassSwingWorker;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.tle.admin.Driver;
import com.tle.admin.common.gui.actions.ExportAction;
import com.tle.admin.common.gui.actions.ImportAction;
import com.tle.admin.common.gui.tree.AbstractTreeEditorTree;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.beans.hierarchy.HierarchyTreeNode;
import com.tle.common.Check;
import com.tle.common.LazyTreeNode.ChildrenState;
import com.tle.common.hierarchy.RemoteHierarchyService;
import com.tle.common.hierarchy.RemoteHierarchyService.ExportStatus;
import com.tle.common.hierarchy.RemoteHierarchyService.ImportStatus;
import com.tle.common.i18n.CurrentLocale;

/**
 * The <code>TreeEditor</code> provides a self contained component for
 * displaying and editing a hierarchy of <code>Topic</code> nodes. This allows
 * for moving Topics up and down the tree, plus creating new topics (children or
 * siblings), and removing them.
 * 
 * @author Nicholas Read
 * @created 4 June 2003
 */
@SuppressWarnings("nls")
public class TreeEditor extends AbstractTreeEditorTree<HierarchyTreeNode>
{
	private static final Object INHERIT_CONSTRAINTS = new Object();

	private final RemoteHierarchyService hierarchyService;

	public TreeEditor(RemoteHierarchyService hierarchyService, boolean canAddRootTopics)
	{
		super(canAddRootTopics);

		Check.checkNotNull(hierarchyService);

		this.hierarchyService = hierarchyService;

		setup();
	}

	@Override
	protected void setupAdditionalActions(List<TLEAction> actions)
	{
		actions.add(importAction);
		actions.add(exportAction);
	}

	@Override
	public boolean canEdit(HierarchyTreeNode node)
	{
		while( node != null )
		{
			if( node.isGrantedEditTopic() )
			{
				return true;
			}
			node = (HierarchyTreeNode) node.getParent();
		}
		return false;
	}

	@Override
	protected HierarchyTreeNode createNode()
	{
		HierarchyTreeNode htn = new HierarchyTreeNode();
		htn.setGrantedEditTopic(true);
		return htn;
	}

	@Override
	protected boolean preAddNewNode(HierarchyTreeNode parent, HierarchyTreeNode newNode, Map<Object, Object> params)
	{
		if( !parent.isRoot() && JOptionPane.showConfirmDialog(this,
			getString("treeeditor.constraints"),
			getString("treeeditor.inheritconstraints"),
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION )
		{
			params.put(INHERIT_CONSTRAINTS, INHERIT_CONSTRAINTS);
		}

		return true;
	}

	@Override
	protected void doAddNewNode(HierarchyTreeNode parent, HierarchyTreeNode newNode, Map<Object, Object> params)
	{
		String newName = newNode.getName();
		boolean inheritConstraints = params.containsKey(INHERIT_CONSTRAINTS);

		long newId = 0;
		if( parent.isRoot() )
		{
			newId = hierarchyService.addToRoot(newName, inheritConstraints);
		}
		else
		{
			newId = hierarchyService.add(parent, newName, inheritConstraints);
		}
		newNode.setId(newId);
	}

	@Override
	protected void doDelete(HierarchyTreeNode node)
	{
		hierarchyService.delete(node);
	}

	@Override
	protected List<HierarchyTreeNode> doListNodes(HierarchyTreeNode parent)
	{
		if( hierarchyService == null )
		{
			return null;
		}
		return hierarchyService.listTreeNodes(parent.getId());
	}

	@Override
	protected void doMove(HierarchyTreeNode node, HierarchyTreeNode parent, int position)
	{
		hierarchyService.move(node.getId(), parent.getId(), position);
	}

	private final TLEAction exportAction = new ExportAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final HierarchyTreeNode node = getSelectedNode();

			File dest = askForDestinationFile(TreeEditor.this, node.getName(), "xml");
			if( dest != null )
			{
				final boolean exportSecurity = JOptionPane.showConfirmDialog(TreeEditor.this,
					getString("treeeditor.export"),
					getString("treeeditor.expsecurity"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

				final ProgressDialog pd = ProgressDialog.showProgress(TreeEditor.this, "Exporting...");

				writeFile(TreeEditor.this, dest, new ExportSerialiser()
				{
					@Override
					public String getSerialisedForm() throws Exception
					{
						String taskId = hierarchyService.exportTopic(node, exportSecurity);

						try
						{
							while( true )
							{
								try
								{
									Thread.sleep(1000);
								}
								catch( InterruptedException ex )
								{
									// Don't care
								}

								ExportStatus status = hierarchyService.getExportStatus(taskId);
								String url = status.getDownloadUrl();
								if( url != null )
								{
									return Resources.toString(new URL(url), Charsets.UTF_8);
								}
								else
								{
									pd.setProgressSafely(status.getDone(), status.getTotal());
								}
							}
						}
						finally
						{
							pd.closeDialog();
						}
					}
				});
			}
		}

		@Override
		public void update()
		{
			setEnabled(tree.getSelectionCount() == 1 && canEdit(getSelectedNode()));
		}
	};

	private final TLEAction importAction = new ImportAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			final HierarchyTreeNode parent = getSelectedNode();

			final PropBagEx content = askForXmlImport(TreeEditor.this);
			if( content == null )
			{
				return;
			}

			boolean importSecurity = false;
			if( JOptionPane.showConfirmDialog(TreeEditor.this,
				getString("treeeditor.import"),
				getString("treeeditor.impsecurity"),
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION )
			{
				importSecurity = true;
			}

			importTopic(content.toString(), parent, false, importSecurity);
		}

		@Override
		public void update()
		{
			setEnabled(tree.getSelectionCount() == 1 && canEdit(getSelectedNode()) || canAddRootNodes());
		}
	};

	private void importTopic(final String content, final HierarchyTreeNode parent, final boolean newids,
		final boolean importSecurity)
	{
		final ProgressDialog pd = ProgressDialog.showProgress(this, "Importing...");

		GlassSwingWorker<ValidationError> worker = new GlassSwingWorker<ValidationError>()
		{
			@Override
			public ValidationError construct() throws Exception
			{
				final String taskId;
				if( parent == null || parent.isRoot() )
				{
					taskId = hierarchyService.importRootTopic(content, newids, importSecurity);
				}
				else
				{
					taskId = hierarchyService.importTopic(content, parent, newids, importSecurity);
				}

				while( true )
				{
					try
					{
						Thread.sleep(1000);
					}
					catch( InterruptedException ex )
					{
						// Don't care
					}

					ImportStatus status = hierarchyService.getImportStatus(taskId);
					if( status.isError() )
					{
						return status.getError();
					}
					else if( status.isFinished() )
					{
						return null;
					}
					else
					{
						pd.setProgressSafely(status.getDone(), status.getTotal());
					}
				}
			}

			@Override
			protected void afterConstruct()
			{
				SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						pd.closeDialog();
					}
				});
			}

			@Override
			public void finished()
			{
				ValidationError error = get();
				if( error == null )
				{
					HierarchyTreeNode rootTopic = getRootNode();
					rootTopic.setChildrenState(ChildrenState.UNLOADED);
					loadChildren(rootTopic);

					Driver.displayInformation(getComponent(),
						CurrentLocale.get("com.tle.admin.gui.common.tree.editor.importsuccessful"));
				}
				else if( !newids && error.getField().equals("uuid") )
				{
					int i = JOptionPane.showConfirmDialog(TreeEditor.this,
						CurrentLocale.get("com.tle.admin.gui.common.tree.editor.importsametopic"),
						CurrentLocale.get("com.tle.admin.gui.common.tree.editor.importsametopictitle"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if( i == JOptionPane.YES_OPTION )
					{
						importTopic(content, parent, true, importSecurity);
					}
				}
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.tree.editor.importerror"));
				getException().printStackTrace();
			}
		};
		worker.setComponent(TreeEditor.this);
		worker.start();
	}
}
