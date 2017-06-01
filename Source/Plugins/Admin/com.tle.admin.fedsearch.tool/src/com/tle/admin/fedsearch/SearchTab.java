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

package com.tle.admin.fedsearch;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.file.FileFilterAdapter;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.baseentity.JEntityFileUpload;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.SearchSettings;
import com.tle.beans.search.XmlBasedSearchSettings;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemoteSchemaService;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class SearchTab extends BaseEntityTab<FederatedSearch> implements AbstractDetailsTab<FederatedSearch>
{
	public static final int TIMEOUT_START = 1;
	public static final int TIMEOUT_END = 120;
	public static final int TIMEOUT_DEFAULT = 30;

	private SearchPlugin<?> plugin;

	protected JPanel panel;
	protected JComboBox collections;
	protected JCheckBox disabled;

	private SpinnerNumberModel timeoutModel;
	private SingleUserSelector owner;
	protected I18nTextField nameField;
	protected I18nTextField descriptionField;

	// XML based searches
	protected JComboBox transforms;
	private JEntityFileUpload displayXslt;
	protected Map<NameValue, List<String>> transformsPerCollection = new HashMap<NameValue, List<String>>();

	@Override
	public final void init(Component parent)
	{
		nameField = new I18nTextField(BundleCache.getLanguages());
		descriptionField = new I18nTextField(BundleCache.getLanguages());
		timeoutModel = new SpinnerNumberModel(TIMEOUT_DEFAULT, TIMEOUT_START, TIMEOUT_END, 1);

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));

		collections = new JComboBox();
		transforms = new JComboBox();
		displayXslt = new JEntityFileUpload(adminService, s("browse"));
		displayXslt.setFileFilter(FileFilterAdapter.XSLT());
		disabled = new JCheckBox(s("disabled"));

		panel = new JPanel(new MigLayout("insets 5px 5px 0 0, wrap 2", "[fill][:640:800, fill]", "top"));

		panel.add(new JLabel(s("name")));
		panel.add(nameField);
		panel.add(new JLabel(s("description")));
		panel.add(descriptionField);
		panel.add(new JLabel(s("timeout")));
		panel.add(new JSpinner(timeoutModel));
		panel.add(new JLabel(s("owner")));
		panel.add(owner);
		panel.add(new JLabel(s("collection")));
		panel.add(collections);
		panel.add(new JLabel(s("transform")));
		panel.add(transforms);
		panel.add(new JLabel(s("displayxslt")));
		panel.add(displayXslt);
		panel.add(disabled, "skip");
		panel.add(new JSeparator(), "span 2, gapbottom 20");

		plugin.setClientService(clientService);
		plugin.setPanel(panel);
		plugin.initGUI();

		setLayout(new MigLayout("insets 0, fill"));

		JScrollPane sp = new JScrollPane(panel);
		sp.setViewportBorder(null);
		getComponent().add("grow", sp);
	}

	private static String s(String keyPart)
	{
		return CurrentLocale.get("com.tle.admin.search.searchtab." + keyPart);
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		nameField.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		final FederatedSearch fedSearch = state.getEntity();

		final SearchSettings settings = plugin.newInstance();
		settings.load(fedSearch);

		nameField.load(fedSearch.getName());
		descriptionField.load(fedSearch.getDescription());
		plugin.loadSettings(state.getEntityPack(), settings);
		owner.setUserId(fedSearch.getOwner());
		timeoutModel.setValue(settings.getTimeout());
		disabled.setSelected(fedSearch.isDisabled());

		// FIXME:
		if( settings instanceof XmlBasedSearchSettings )
		{
			displayXslt.load(state.getEntityPack(), ((XmlBasedSearchSettings) settings).getDisplayXslt());
		}

		GlassSwingWorker<?> worker = new GlassSwingWorker<List<NameValue>>()
		{
			@Override
			public List<NameValue> construct() throws Exception
			{
				List<BaseEntityLabel> cols = clientService.getService(RemoteItemDefinitionService.class).listAll();
				List<NameValue> nvs = BundleCache.getNameUuidValues(cols);
				Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
				return nvs;
			}

			@Override
			public void finished()
			{
				AppletGuiUtils.addItemsToJCombo(collections, get());
				// FIXME:
				if( settings instanceof XmlBasedSearchSettings )
				{
					loadImportSettings((XmlBasedSearchSettings) settings);
				}

				updateTransforms(settings, true);

				collections.addItemListener(new ItemListener()
				{
					@Override
					public void itemStateChanged(ItemEvent e)
					{
						updateTransforms(settings, false);
					}
				});
			}
		};
		worker.setComponent(collections);
		worker.start();
	}

	@Override
	public void save()
	{
		final FederatedSearch fedSearch = state.getEntity();

		final SearchSettings settings = plugin.newInstance();
		settings.setTimeout(timeoutModel.getNumber().intValue());
		settings.setName(nameField.save());
		settings.setDescription(descriptionField.save());

		settings.setCollectionUuid(((NameValue) collections.getSelectedItem()).getValue());
		// FIXME:
		if( settings instanceof XmlBasedSearchSettings )
		{
			XmlBasedSearchSettings xsettings = (XmlBasedSearchSettings) settings;
			xsettings.setSchemaInputTransform((String) transforms.getSelectedItem());
			xsettings.setDisplayXslt(displayXslt.save());
		}

		settings.setDisabled(disabled.isSelected());

		plugin.saveSettings(settings);
		fedSearch.setOwner(owner.getUser().getUniqueID());
		settings.save(fedSearch);
	}

	@Override
	public String getTitle()
	{
		return "Details";
	}

	@Override
	public void validation() throws EditorException
	{
		if( nameField.isCompletelyEmpty() )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.search.searchtab.entername"));
		}
		if( owner.getUser() == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.search.searchtab.noowner"));
		}
		plugin.validation();
	}

	public void setPlugin(SearchPlugin<?> plugin)
	{
		this.plugin = plugin;
	}

	private void updateTransforms(final SearchSettings settings, final boolean clearAfterwards)
	{
		final NameValue selcol = (NameValue) collections.getSelectedItem();

		GlassSwingWorker<?> worker = new GlassSwingWorker<List<String>>()
		{
			@Override
			public List<String> construct() throws Exception
			{
				List<String> tpc = transformsPerCollection.get(selcol);
				if( tpc == null )
				{
					long schemaId = clientService.getService(RemoteItemDefinitionService.class)
						.getSchemaIdForCollectionUuid(selcol.getValue());
					tpc = clientService.getService(RemoteSchemaService.class).getImportSchemaTypes(schemaId);
					transformsPerCollection.put(selcol, tpc);
				}
				return tpc;
			}

			@Override
			public void finished()
			{
				String selectedTransform = (String) transforms.getSelectedItem();
				transforms.removeAllItems();
				if( selectedTransform == null && settings instanceof XmlBasedSearchSettings )
				{
					selectedTransform = ((XmlBasedSearchSettings) settings).getSchemaInputTransform();
				}
				AppletGuiUtils.addItemsToJCombo(transforms, get());
				AppletGuiUtils.selectInJCombo(transforms, selectedTransform, 0);

				if( clearAfterwards )
				{
					clearChanges();
				}
			}
		};
		worker.setComponent(transforms);
		worker.start();
	}

	private void clearChanges()
	{
		// Slightly ghetto? JFakePanel crap
		super.panel.clearChanges();
	}

	public void loadImportSettings(XmlBasedSearchSettings settings)
	{
		String collectionUuid = settings.getCollectionUuid();
		if( !Check.isEmpty(collectionUuid) )
		{
			AppletGuiUtils.selectInJCombo(collections, new NameValue(null, collectionUuid), 0);

			AppletGuiUtils.selectInJCombo(transforms, settings.getSchemaInputTransform(), 0);
		}
	}
}
