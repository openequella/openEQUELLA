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

package com.tle.admin.search.searchset.virtualisation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.actions.CancelAction;
import com.tle.admin.gui.common.actions.OkAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SchemaNode;
import com.tle.admin.schema.SchemaTree;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.Schema;
import com.tle.client.gui.popup.TreeDoubleClickListener;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.client.ClientService;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class SchemaAndTargetChooserDialog
{
	private final ClientService clientService;
	private final JComboBox chooser;
	private final SchemaTree tree;
	private final SchemaModel model;
	private final JPanel panel;
	private final JDialog dialog;
	private final boolean indexedForPowersearchOnly;

	private String selectedNode;

	public SchemaAndTargetChooserDialog(final ClientService clientService, final JComponent parent,
		final boolean indexedForPowersearchOnly)
	{
		this.clientService = clientService;
		this.indexedForPowersearchOnly = indexedForPowersearchOnly;

		chooser = new JComboBox();

		model = new SchemaModel();

		tree = new SchemaTree(model, indexedForPowersearchOnly);
		tree.setEnabled(false);
		tree.addMouseListener(new TreeDoubleClickListener(tree, okAction));
		tree.addTreeSelectionListener(new TreeSelectionListener()
		{
			@Override
			public void valueChanged(TreeSelectionEvent e)
			{
				updateActions();
			}
		});

		panel = new JPanel(new MigLayout("wrap, fill", "[grow, fill]", "[][grow, fill][]"));
		panel.add(chooser);
		panel.add(new JScrollPane(tree));
		panel.add(new JButton(okAction), "split, alignx right, tag ok");
		panel.add(new JButton(cancelAction), "tag cancel");

		updateActions();

		dialog = ComponentHelper.createJDialog(parent);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(panel);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.search.searchset.virtualisation.xpathchooser.dialogtitle"));
		dialog.setModal(true);
		dialog.pack();

		ComponentHelper.ensureMinimumSize(dialog, 500, 500);
		ComponentHelper.centreOnScreen(dialog);
	}

	public String showDialog()
	{
		if( chooser.getItemCount() == 0 )
		{
			populateSchemasWorker.setComponent(panel);
			populateSchemasWorker.start();
		}

		dialog.setVisible(true);

		return selectedNode;
	}

	private void updateActions()
	{
		okAction.update();
		cancelAction.update();
	}

	private final TLEAction okAction = new OkAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SchemaNode node = (SchemaNode) tree.getLastSelectedPathComponent();
			selectedNode = node.toString();
			dialog.dispose();
		}

		@Override
		public void update()
		{
			setEnabled(false);
			if( tree.isEnabled() && !tree.isSelectionEmpty() )
			{
				SchemaNode node = (SchemaNode) tree.getLastSelectedPathComponent();
				if( !node.hasNonAttributeChildren() && (indexedForPowersearchOnly && node.isField()) )
				{
					setEnabled(true);
				}
			}
		}
	};

	private final TLEAction cancelAction = new CancelAction()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			dialog.dispose();
		}
	};

	private final GlassSwingWorker<?> populateSchemasWorker = new GlassSwingWorker<List<NameValue>>()
	{
		@Override
		public List<NameValue> construct() throws Exception
		{
			List<BaseEntityLabel> schemas = clientService.getService(RemoteSchemaService.class).listAll();
			List<NameValue> nvs = BundleCache.getNameValues(schemas);
			Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
			return nvs;
		}

		@Override
		public void finished()
		{
			chooser.addItem(new NameValue(CurrentLocale
				.get("com.tle.admin.search.searchset.virtualisation.xpathchooser.schemadropdown"), ""));
			AppletGuiUtils.addItemsToJCombo(chooser, get());

			chooser.addActionListener(schemaChoiceListener);
		}
	};

	private final ActionListener schemaChoiceListener = new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String value = ((NameValue) chooser.getSelectedItem()).getValue();
			if( Check.isEmpty(value) )
			{
				tree.setEnabled(false);
				return;
			}

			final long schemaId = Long.parseLong(value);
			GlassSwingWorker<?> worker = new GlassSwingWorker<Schema>()
			{
				@Override
				public Schema construct() throws Exception
				{
					return clientService.getService(RemoteSchemaService.class).get(schemaId);
				}

				@Override
				public void finished()
				{
					model.loadSchema(get().getDefinitionNonThreadSafe());
					tree.setEnabled(true);
				}
			};
			worker.setComponent(panel);
			worker.start();
		}
	};
}
