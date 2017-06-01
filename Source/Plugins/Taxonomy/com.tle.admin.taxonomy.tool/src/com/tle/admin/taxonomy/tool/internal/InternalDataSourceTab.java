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

package com.tle.admin.taxonomy.tool.internal;

import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;

import com.dytech.gui.Changeable;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.common.gui.tree.AbstractTreeEditor;
import com.tle.admin.common.gui.tree.AbstractTreeEditorTree;
import com.tle.admin.common.gui.tree.AbstractTreeNodeEditor;
import com.tle.common.Pair;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.RemoteTaxonomyService;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.common.taxonomy.terms.RemoteTermService;
import com.tle.core.plugins.PluginService;

@SuppressWarnings("nls")
public class InternalDataSourceTab extends JPanel implements Changeable
{
	private final ClientService clientService;
	private final PluginService pluginService;

	private AbstractTreeEditor<TermTreeNode> tree;
	private Taxonomy taxonomy;

	public InternalDataSourceTab(ClientService clientService, PluginService pluginService)
	{
		super(new GridLayout(1, 1));

		this.clientService = clientService;
		this.pluginService = pluginService;

		add(new JLabel("<html><b>" + s("mustbesaved"), SwingConstants.CENTER));

		addComponentListener(ensureTaxonomySavedListener);
	}

	@Override
	public void clearChanges()
	{
		// Nothing to do here
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return false;
	}

	public void load(Taxonomy state)
	{
		this.taxonomy = state;
		attemptToShowFullUi();
	}

	public void save(Taxonomy state)
	{
		this.taxonomy = state;
	}

	public void afterSave()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				attemptToShowFullUi();
			}
		});
	}

	private final transient ComponentListener ensureTaxonomySavedListener = new ComponentAdapter()
	{
		@Override
		public void componentShown(ComponentEvent e)
		{
			attemptToShowFullUi();
		}
	};

	private void attemptToShowFullUi()
	{
		if( tree != null )
		{
			return;
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<Boolean>()
		{
			private Map<String, Pair<String, String>> predefinedTermDataKeys;

			@Override
			public Boolean construct() throws Exception
			{
				boolean valid = taxonomy != null
					&& clientService.getService(RemoteTaxonomyService.class).identifyByUuid(taxonomy.getUuid()) != 0;
				if( valid )
				{
					predefinedTermDataKeys = new HashMap<String, Pair<String, String>>();
					for( Extension ext : pluginService.getConnectedExtensions("com.tle.admin.taxonomy.tool",
						"predefinedTermDataKey") )
					{
						final String key = ext.getParameter("key").valueAsString();
						final String name = CurrentLocale.get(ext.getParameter("name").valueAsString());
						final String desc = CurrentLocale.get(ext.getParameter("description").valueAsString());

						predefinedTermDataKeys.put(key, new Pair<String, String>(name, desc));
					}
				}
				return valid;
			}

			@Override
			public void finished()
			{
				if( !get() )
				{
					return;
				}

				final JPanel p = InternalDataSourceTab.this;

				// Remove this listener and clear the panel
				p.removeComponentListener(ensureTaxonomySavedListener);
				p.removeAll();

				final RemoteTermService termService = clientService.getService(RemoteTermService.class);

				tree = new AbstractTreeEditor<TermTreeNode>()
				{
					@Override
					protected AbstractTreeEditorTree<TermTreeNode> createTree()
					{
						return new TermTree(taxonomy, true, termService);
					}

					@Override
					protected AbstractTreeNodeEditor createEditor(TermTreeNode node)
					{
						return new TermEditor(termService, predefinedTermDataKeys, taxonomy, node);
					}
				};

				p.setLayout(new MigLayout("wrap 1, fill"));
				p.add(new JLabel("<html>" + s("immediatechanges")));
				p.add(tree, "push, grow");

				p.revalidate();
				p.repaint();
			}
		};
		worker.setComponent(InternalDataSourceTab.this);
		worker.start();
	}

	static String s(String keyEnd)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.internal.tab." + keyEnd);
	}
}
