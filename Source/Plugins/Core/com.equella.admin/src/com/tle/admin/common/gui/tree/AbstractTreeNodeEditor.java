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
import com.tle.core.plugins.AbstractPluginService;

/**
 * @author Nicholas Read
 */
public abstract class AbstractTreeNodeEditor extends JPanel
{
	private static final long serialVersionUID = 1L;

	protected final ChangeDetector changeDetector = new ChangeDetector();

	protected abstract LazyTreeNode getUpdatedNode();

	protected abstract void save() throws Exception;

	protected abstract void validation() throws EditorException;

	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(getKey(key));
	}

	protected String getKey(String key)
	{
		return KEY_PFX+key;
	}
	@SuppressWarnings("nls")
	public void doSave()
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<Object>()
		{
			@Override
			public Object construct() throws EditorException, Exception
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
