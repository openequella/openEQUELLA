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

package com.dytech.edge.admin.wizard;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.common.io.FileExtensionFilter;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.dytech.gui.file.FileFilterAdapter;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import com.tle.admin.Driver;
import com.tle.admin.common.gui.AbstractFileWorker;
import com.tle.admin.common.gui.actions.ExportAction;
import com.tle.admin.common.gui.actions.ImportAction;
import com.tle.admin.controls.ExportedControl;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.client.gui.popup.TreePopupListener;
import com.tle.common.Check;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.FileWorker;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginService;
import com.tle.core.remoting.RemoteItemDefinitionService;

/*
 * @dytech.jira Jira Requirement TLE-566 :
 * http://apps.dytech.com.au/jira/browse/TLX-102
 * @dytech.jira see Jira Defect TLE-600 :
 * http://apps.dytech.com.au/jira/browse/TLE-600
 */
public class WizardTree extends JPanel implements TreeSelectionListener, TreeModelListener, Changeable
{
	private static final long serialVersionUID = 1L;
	private static final String CONTROL_FILE_EXTENSION = "wzc"; //$NON-NLS-1$

	protected static final Log LOGGER = LogFactory.getLog(WizardTree.class);

	protected final ControlRepository repository;

	private WizardModel model;
	private String originalXml;

	private JTree tree;
	protected ActionListener controlListener;

	private List<TLEAction> actions;
	private ChangeDetector changeDetector;

	private final TLEAction addAction = new ControlTreeAddAction();
	private final TLEAction removeAction = new ControlTreeRemoveAction();
	private final TLEAction importAction = new ControlTreeImportAction();
	private final TLEAction exportAction = new ControlTreeExportAction();

	public WizardTree(int wizardType, ControlRepository repository)
	{
		this.repository = repository;
		controlListener = null;
		setupDefaultWizard();
		setup(wizardType);
	}

	/**
	 * @dytech.jira see Jira Defect TLE-1108 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-1108
	 */
	private void setupDefaultWizard()
	{
		model = new WizardModel(repository);
	}

	private void setup(int wizardType)
	{
		actions = new ArrayList<TLEAction>();
		actions.add(addAction);
		actions.add(removeAction);
		actions.add(upAction);
		actions.add(downAction);
		actions.add(importAction);
		actions.add(exportAction);

		tree = new MyTree(wizardType, getModel());
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new TreePopupListener(tree, actions));
		tree.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				if( removeAction.isEnabled() && e.getKeyCode() == KeyEvent.VK_DELETE )
				{
					removeAction.actionPerformed(null);
				}
			}
		});

		JScrollPane treeScroll = new JScrollPane(tree);

		JButton addControl = new JButton(addAction);
		JButton removeControl = new JButton(removeAction);
		JButton upButton = new JTextlessButton(upAction);
		JButton downButton = new JTextlessButton(downAction);

		final int height1 = upButton.getPreferredSize().height;
		final int height2 = removeControl.getPreferredSize().height;
		final int width1 = upButton.getPreferredSize().width;
		final int width2 = removeControl.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height1, height1, TableLayout.FILL, height2};
		final int[] cols = {width1, TableLayout.FILL, width2, width2, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols, 5, 5));

		add(treeScroll, new Rectangle(1, 0, 4, 4));
		add(upButton, new Rectangle(0, 1, 1, 1));
		add(downButton, new Rectangle(0, 2, 1, 1));

		add(addControl, new Rectangle(2, 4, 1, 1));
		add(removeControl, new Rectangle(3, 4, 1, 1));

		updateButtons();

		changeDetector = new ChangeDetector();
	}

	/**
	 * Updates the buttons depending on the state..
	 */
	private void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
	}

	public void setControlListener(ActionListener listener)
	{
		controlListener = listener;
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
		originalXml = WizardHelper.getXmlForComparison(getRoot().save());
	}

	@Override
	public boolean hasDetectedChanges()
	{
		if( changeDetector.hasDetectedChanges() )
		{
			return true;
		}
		else
		{
			if( originalXml == null )
			{
				return false;
			}
			else
			{
				String newXml = WizardHelper.getXmlForComparison(getRoot().save());
				if( !originalXml.equals(newXml) )
				{
					return true;
				}
			}
		}
		return false;
	}

	// // LOADING WIZARD FROM XML FUNCTIONS
	// /////////////////////////////////////

	/**
	 * @dytech.jira see Jira Defect TLE-1108 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-1108
	 */
	public void loadItem(Object wizard)
	{
		if( wizard != null )
		{
			model = new WizardModel(repository);
			changeDetector.watch(model);
			model.loadWizard(wizard);
		}
		else
		{
			setupDefaultWizard();
		}
		tree.setModel(model);
		// setSelectedControl(model.getRootControl());

		clearChanges();
	}

	// // TREE MANIPULATION FUNCTIONS
	// ///////////////////////////////////////////

	public void clearSelection()
	{
		tree.clearSelection();
	}

	public void setSelectedControl(Control control)
	{
		tree.setSelectionPath(getModel().getTreePath(control));
	}

	public void controlChanged(Control control)
	{
		getModel().controlChanged(control);
	}

	/**
	 * @see javax.swing.JTree.addTreeSelectionListener(TreeSelectionListener)
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener)
	{
		tree.addTreeSelectionListener(listener);
	}

	/**
	 * @see javax.swing.JTree.removeTreeSelectionListener(TreeSelectionListener)
	 */
	public void removeTreeSelectionListener(TreeSelectionListener listener)
	{
		tree.removeTreeSelectionListener(listener);
	}

	public Control getLastSelectedPathComponent()
	{
		return (Control) tree.getLastSelectedPathComponent();
	}

	public WizardModel getModel()
	{
		return model;
	}

	/**
	 * @return true if a valid wizard(s) exists
	 * @dytech.jira see Jira Defect TLE-600 :
	 *              http://apps.dytech.com.au/jira/browse/TLE-600
	 */
	public boolean isThereAWizard()
	{
		return !model.getRootControl().getChildren().isEmpty();
	}

	/**
	 * Returns the root control.
	 */
	public Control getRoot()
	{
		return getModel().getRootControl();
	}

	/**
	 * Adds a child to the parent in the current model.
	 */
	public Control addChild(Control parent, ControlDefinition child)
	{
		return getModel().addControl(parent, child);
	}

	/**
	 * Adds a control to the model as if it were a page.
	 */
	public Control addPage(ControlDefinition control)
	{
		return addChild(getModel().getRootControl(), control);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(
	 * javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		updateButtons();
		tree.expandPath(e.getNewLeadSelectionPath());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		// We do not care about this event.
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.
	 * event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
		// We don't care about this event.
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
		if( e.getSource() == model )
		{
			TreePath path = e.getTreePath();
			path = path.pathByAddingChild(e.getChildren()[0]);
			tree.setSelectionPath(path);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
		if( e.getSource() == model )
		{
			tree.clearSelection();
		}
	}

	protected final void onImport()
	{
		final int confirm = JOptionPane.showConfirmDialog(this,
			CurrentLocale.get("com.tle.admin.controls.warning.import", CurrentLocale //$NON-NLS-1$
				.get("com.tle.application.name")), CurrentLocale //$NON-NLS-1$
				.get("com.tle.admin.controls.title.import"), JOptionPane.YES_NO_OPTION, //$NON-NLS-1$
			JOptionPane.WARNING_MESSAGE);

		if( confirm == JOptionPane.YES_OPTION )
		{
			DialogUtils.doOpenDialog(this,
				CurrentLocale.get("com.tle.admin.controls.title.import"), new ControlFileFilter(), //$NON-NLS-1$
				importer());
		}
	}

	private FileWorker importer()
	{
		return new AbstractFileWorker<Control>(null, "wizard/controlImport") //$NON-NLS-1$
		{
			@Override
			public Control construct() throws Exception
			{

				final String controlXml = getCollectionService(Driver.instance())
					.importControl(Files.toByteArray(file));

				final XStream xstream = new XStream();
				final Set<String> pluginIds = getPluginIds(controlXml);
				final PluginService pluginService = Driver.instance().getPluginService();

				final CompositeClassLoader loader = new CompositeClassLoader();
				for( String pluginId : pluginIds )
				{
					loader.add(pluginService.getClassLoader(pluginId));
				}
				loader.add(WizardTree.class.getClassLoader());
				xstream.setClassLoader(loader);

				return addExportedControl((ExportedControl) xstream.fromXML(controlXml),
					getLastSelectedPathComponent(), true);
			}

			@Override
			public void finished()
			{
				if( get() != null )
				{
					Driver.displayInformation(parent, CurrentLocale.get("com.tle.admin.controls.imported")); //$NON-NLS-1$
				}
				else
				{
					Driver.displayInformation(parent, CurrentLocale.get("com.tle.admin.controls.notimported")); //$NON-NLS-1$
				}
			}
		};
	}

	protected Set<String> getPluginIds(String importXml) throws Exception
	{
		Set<String> pluginIds = new HashSet<String>();
		pluginIds.addAll(getPluginIds(new PropBagEx(importXml)));
		return pluginIds;
	}

	protected Set<String> getPluginIds(PropBagEx exportedControlSubtree)
	{
		Set<String> pluginIds = new HashSet<String>();
		final String pluginId = exportedControlSubtree.getNode("pluginId"); //$NON-NLS-1$
		if( !Check.isEmpty(pluginId) )
		{
			pluginIds.add(pluginId);
		}

		for( PropBagEx subControl : exportedControlSubtree
			.iterateAll("children/com.tle.admin.controls.ExportedControl") ) //$NON-NLS-1$
		{
			pluginIds.addAll(getPluginIds(subControl));
		}
		return pluginIds;
	}

	protected Control addExportedControl(ExportedControl ctl, Control parent, boolean selectIt)
	{
		// add the control to the tree...
		Control newControl = doAddControl(repository.getDefinition(ctl.getControlTypeId()), parent, selectIt);
		if( newControl != null )
		{
			newControl.setWrappedObject(ctl.getWizardControl());

			// add children
			if( !Check.isEmpty(ctl.getChildren()) )
			{
				for( ExportedControl child : ctl.getChildren() )
				{
					addExportedControl(child, newControl, false);
				}
			}
		}
		return newControl;
	}

	public Control doAddControl(ControlDefinition newControlDef, Control parentControl, boolean selectIt)
	{
		Control newControl = null;

		// Add the new control to the tree.
		if( rootLevel(newControlDef) )
		{
			newControl = addPage(newControlDef);
		}
		else
		{
			if( parentControl == null )
			{
				Driver.displayInformation(tree, CurrentLocale.get("com.tle.admin.controls.error.noparent")); //$NON-NLS-1$
				return null;
			}
			newControl = addChild(parentControl, newControlDef);
		}

		// Select the new control.
		if( selectIt && newControl != null )
		{
			setSelectedControl(newControl);
		}
		return newControl;
	}

	protected boolean rootLevel(ControlDefinition def)
	{
		return def.hasContext(Contexts.CONTEXT_PAGES) || def.hasContext(Contexts.CONTEXT_METADATA);
	}

	protected final void onExport()
	{
		final Control control = getLastSelectedPathComponent();
		final String suggestedFilename = DialogUtils
			.getSuggestedFileName(
				getTitle(control.getWrappedObject(), CurrentLocale.get("com.tle.admin.controls.untitled")), CONTROL_FILE_EXTENSION); //$NON-NLS-1$
		DialogUtils.doSaveDialog(this, CurrentLocale.get("com.tle.admin.controls.title.export"), //$NON-NLS-1$
			new ControlFileFilter(), suggestedFilename, exporter(control));
	}

	private FileWorker exporter(final Control control)
	{
		return new AbstractFileWorker<Object>(CurrentLocale.get("com.tle.admin.controls.exported"), //$NON-NLS-1$
			CurrentLocale.get("com.tle.admin.controls.error.exporting")) //$NON-NLS-1$
		{
			@Override
			public Object construct() throws Exception
			{
				final Driver driver = Driver.instance();
				final PluginService pluginService = driver.getPluginService();

				final ExportedControl ctl = getExportedControl(control, pluginService, driver.getVersion().getFull());

				byte[] zipData = getCollectionService(driver).exportControl(new XStream().toXML(ctl));

				ByteArrayInputStream stream = new ByteArrayInputStream(zipData);
				try( OutputStream out = new FileOutputStream(file) )
				{
					ByteStreams.copy(stream, out);
				}
				return null;
			}
		};
	}

	protected static String getTitle(Object control, String defaultTitle)
	{
		if( control instanceof WizardControl )
		{
			return CurrentLocale.get(((WizardControl) control).getTitle(), defaultTitle);
		}
		else if( control instanceof DefaultWizardPage )
		{
			return CurrentLocale.get(((DefaultWizardPage) control).getTitle(), defaultTitle);
		}
		return defaultTitle;
	}

	protected ExportedControl getExportedControl(Control control, final PluginService pluginService,
		final String version)
	{
		final Object wizControl = control.getWrappedObject();
		ExportedControl ctl = new ExportedControl();
		ctl.setControlTypeId(control.getDefinition().getId());
		ctl.setWizardControl(wizControl);
		ctl.setPluginId(pluginService.getPluginIdForObject(wizControl));
		ctl.setVersion(version);

		List<Control> childControls = control.getChildren();
		if( !Check.isEmpty(childControls) )
		{
			List<ExportedControl> exportChildren = new ArrayList<ExportedControl>();
			for( Control child : childControls )
			{
				exportChildren.add(getExportedControl(child, pluginService, version));
			}
			ctl.setChildren(exportChildren);
		}

		return ctl;
	}

	protected boolean isPage(ControlDefinition def)
	{
		return def.hasContext(Contexts.CONTEXT_PAGE);
	}

	protected RemoteItemDefinitionService getCollectionService(Driver driver)
	{
		return driver.getClientService().getService(RemoteItemDefinitionService.class);
	}

	private final TLEAction upAction = new UpAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control control = getLastSelectedPathComponent();
			setSelectedControl(null);
			getModel().raiseControl(control);
			setSelectedControl(control);
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			boolean hasPrevSibling = control != null && control.getPreviousSibling() != null;

			if( control == null || WizardHelper.isMetadata(control) )
			{
				setEnabled(false);
			}
			else if( WizardHelper.isPage(control) || WizardHelper.isGroupItem(control) )
			{
				setEnabled(hasPrevSibling);
			}
			else
			{
				Control parent = control.getParent();
				if( parent != null )
				{
					Control prevPage = parent.getPreviousSibling();
					boolean prevPageValid = prevPage != null && WizardHelper.isStandardPage(prevPage);

					setEnabled(hasPrevSibling || prevPageValid);
				}
			}
		}
	};

	private final TLEAction downAction = new DownAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control control = getLastSelectedPathComponent();
			setSelectedControl(null);
			getModel().lowerControl(control);
			setSelectedControl(control);
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			Control nextSibling = control == null ? null : control.getNextSibling();
			boolean hasNextSibling = nextSibling != null;

			if( control == null || WizardHelper.isMetadata(control) )
			{
				setEnabled(false);
			}
			else if( WizardHelper.isPage(control) || WizardHelper.isGroupItem(control) )
			{
				setEnabled(hasNextSibling && !WizardHelper.isMetadata(nextSibling));
			}
			else
			{
				Control parent = control.getParent();
				if( parent != null )
				{
					Control nextPage = parent.getNextSibling();
					boolean nextPageValid = nextPage != null && WizardHelper.isStandardPage(nextPage);

					setEnabled(hasNextSibling || nextPageValid);
				}
			}
		}
	};

	/**
	 * A slightly customised tree.
	 * 
	 * @author Nicholas Read
	 */
	private class MyTree extends JTree
	{
		private static final long serialVersionUID = 1L;

		public MyTree(int wizardType, WizardModel model)
		{
			super(model);

			// Some basic properties
			setEditable(true);
			setShowsRootHandles(true);
			setRootVisible(wizardType == WizardHelper.WIZARD_TYPE_CONTRIBUTION);
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			// Set the renderer and editor
			CellRenderer renderer = new CellRenderer(repository);
			CellEditor editor = new CellEditor(repository, this, renderer);

			setCellRenderer(renderer);
			setCellEditor(editor);
		}
	}

	/**
	 * Renderers cells containing Controls.
	 * 
	 * @author Nicholas Read
	 */
	private static class CellRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private final ControlRepository repository;

		/**
		 * Constructs a new WizardTreeCellRenderer.
		 */
		public CellRenderer(ControlRepository repository)
		{
			this.repository = repository;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			Control control = (Control) value;
			setIcon(repository.getIcon(control.getControlClass(), control.isScripted()));
			return this;
		}
	}

	/**
	 * Edits cells containing Controls.
	 * 
	 * @author Nicholas Read
	 */
	private static class CellEditor extends DefaultTreeCellEditor implements CellEditorListener
	{
		private final ControlRepository repository;
		private Control currentControl;

		/**
		 * Constructs a new WizardTreeCellEditor.
		 */
		public CellEditor(ControlRepository repository, JTree tree, DefaultTreeCellRenderer renderer)
		{
			super(tree, renderer);
			this.repository = repository;
			addCellEditorListener(this);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
			boolean leaf, int row)
		{
			Component comp = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

			currentControl = (Control) value;
			editingIcon = repository.getIcon(currentControl.getControlClass(), currentControl.isScripted());

			return comp;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.CellEditorListener#editingStopped(javax.swing.event
		 * .ChangeEvent)
		 */
		@Override
		public void editingStopped(ChangeEvent e)
		{
			if( currentControl != null )
			{
				currentControl.setCustomName((String) getCellEditorValue());
				currentControl = null;
			}
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * javax.swing.event.CellEditorListener#editingCanceled(javax.swing.
		 * event.ChangeEvent)
		 */
		@Override
		public void editingCanceled(ChangeEvent e)
		{
			// We don't care about this event.
		}
	}

	protected class ControlTreeAddAction extends AddAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( controlListener != null )
			{
				ActionEvent fakeEvent = new ActionEvent(WizardTree.this, 0, null);
				controlListener.actionPerformed(fakeEvent);
			}
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			setEnabled(control == null ? !tree.isRootVisible() : control.allowsChildren());
		}
	}

	protected class ControlTreeRemoveAction extends RemoveAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control control = getLastSelectedPathComponent();
			boolean hasChildren = !control.getChildren().isEmpty();

			ControlDefinition def = control.getDefinition();
			StringBuilder message = new StringBuilder();
			if( def.hasContext(Contexts.CONTEXT_PAGES) )
			{
				message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.selectedpage")); //$NON-NLS-1$
				if( hasChildren )
				{
					message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.allcontrolspage")); //$NON-NLS-1$
				}
			}
			else if( WizardHelper.isGroup(control) )
			{
				message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.selectedgroup")); //$NON-NLS-1$
				if( hasChildren )
				{
					message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.allcontrolgroups")); //$NON-NLS-1$
				}
			}
			else if( def.hasContext(Contexts.CONTEXT_GROUP) )
			{
				message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.selecteditem")); //$NON-NLS-1$
				if( hasChildren )
				{
					message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.allcontrolitem")); //$NON-NLS-1$
				}
			}
			else
			{
				message.append(CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.selectedcontrol")); //$NON-NLS-1$
			}

			final int confirm = JOptionPane.showConfirmDialog(WizardTree.this, message.toString(),
				CurrentLocale.get("com.dytech.edge.admin.wizard.wizardtree.remove"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

			if( confirm == JOptionPane.YES_OPTION )
			{
				getModel().removeControl(control);
			}
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			setEnabled(control != null && control.isRemoveable());
		}
	}

	protected class ControlTreeExportAction extends ExportAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			onExport();
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			setEnabled(control != null);
		}
	}

	protected class ControlTreeImportAction extends ImportAction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			onImport();
		}

		@Override
		public void update()
		{
			Control control = getLastSelectedPathComponent();
			setEnabled(control == null || control.allowsChildren());
		}
	}

	public static class ControlFileFilter extends FileFilterAdapter
	{
		@SuppressWarnings("nls")
		public ControlFileFilter()
		{
			super(new FileExtensionFilter(CONTROL_FILE_EXTENSION), CurrentLocale
				.get("com.tle.admin.controls.wizardcontrolfiles"));
		}
	}
}
