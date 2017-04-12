package com.tle.admin.common.gui.tree;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.Driver;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.common.actions.SaveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.common.LazyTreeNode;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public abstract class AbstractTreeNodeEditor extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected final ChangeDetector changeDetector = new ChangeDetector();

	protected abstract LazyTreeNode getUpdatedNode();

	protected abstract void save();

	protected abstract void validation() throws EditorException;

	@SuppressWarnings("nls")
	public void doSave()
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws EditorException
			{
				validation();
				save();
				return null;
			}

			@Override
			public void finished()
			{
				changeDetector.clearChanges();

				final LazyTreeNode updatedNode = getUpdatedNode();
				for( TreeNodeChangeListener l : listenerList.getListeners(TreeNodeChangeListener.class) )
				{
					l.nodeSaved(updatedNode);
				}

				Driver.displayInformation(getComponent(),
					CurrentLocale.get("com.tle.admin.gui.common.tree.nodeeditor.savesuccessful"));
			}

			@Override
			public void exception()
			{
				Driver.displayInformation(getComponent(), getException().getMessage());
			}
		};
		worker.setComponent(AbstractTreeNodeEditor.this);
		worker.start();
	}

	public final void addNodeChangeListener(TreeNodeChangeListener l)
	{
		listenerList.add(TreeNodeChangeListener.class, l);
	}

	public final void removeNodeChangeListener(TreeNodeChangeListener l)
	{
		listenerList.remove(TreeNodeChangeListener.class, l);
	}

	public final boolean hasChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	protected final TLEAction createSaveAction()
	{
		return new SaveAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e)
			{
				doSave();
			}
		};
	}
}
