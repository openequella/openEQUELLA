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

package com.tle.admin.controls.resource.universal;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.collect.Lists;
import com.tle.admin.Driver;
import com.tle.admin.controls.EntityShuffler;
import com.tle.admin.controls.universal.UniversalControlSettingPanel;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.dynacollection.RemoteDynaCollectionService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.resource.ResourceSettings;
import com.tle.common.wizard.controls.resource.ResourceSettings.AllowedSelection;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.core.remoting.RemoteAbstractEntityService;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemotePowerSearchService;

@SuppressWarnings("nls")
public class ResourceSettingsPanel extends UniversalControlSettingPanel
{
	private final JComboBox<RelationType> relationType;
	private final JComboBox<ASItem> allowSelection;
	private final List<EntityShuffler<ResourceSettings>> restrictions = Lists.newArrayList();

	public ResourceSettingsPanel()
	{
		super();

		JLabel allowSelectionLabel = new JLabel(
			getString("allowSelection.title"));
		JLabel relationTypeLabel = new JLabel(getString("relationType.title"));

		allowSelection = new JComboBox<>();
		allowSelection.addItem(new ASItem("anything", AllowedSelection.ANYTHING));
		allowSelection.addItem(new ASItem("both", AllowedSelection.ITEMS_AND_ATTACHMENTS));
		allowSelection.addItem(new ASItem("items", AllowedSelection.ITEMS));
		allowSelection.addItem(new ASItem("attachments", AllowedSelection.ATTACHMENTS));
		allowSelection.addItem(new ASItem("packages", AllowedSelection.PACKAGES));

		relationType = new JComboBox<>();
		AppletGuiUtils.addItemsToJCombo(relationType, getRelationTypes());
		relationType.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				updateFromRelationType();
			}
		});

		restrictions.add(new RestrictEntities(getKey("restrict.collections"),
			RemoteItemDefinitionService.class, ResourceSettings.KEY_RESTRICT_COLLECTIONS));
		restrictions.add(new RestrictEntities(getKey("restrict.powersearches"),
			RemotePowerSearchService.class, ResourceSettings.KEY_RESTRICT_POWERSEARCHES));
		restrictions.add(new RestrictEntities(getKey("restrict.dynacollection"),
			RemoteDynaCollectionService.class, ResourceSettings.KEY_RESTRICT_DYNACOLLECTION));
		restrictions.add(new RestrictEntities(getKey("restrict.contribution"),
			RemoteItemDefinitionService.class, ResourceSettings.KEY_RESTRICT_CONTRIBUTION));

		add(relationTypeLabel);
		add(relationType);

		add(allowSelectionLabel);
		add(allowSelection);

		for( EntityShuffler<ResourceSettings> rc : restrictions )
		{
			add(rc, "span 2, growx");
		}

		updateFromRelationType();
	}

	protected void updateFromRelationType()
	{
		RelationType type = (RelationType) relationType.getSelectedItem();

		AllowedSelection ls = type.getLockSelection();
		allowSelection.setEnabled(ls == null);
		if( ls != null )
		{
			AppletGuiUtils.selectInJCombo(allowSelection, new ASItem(null, ls));
		}
	}

	@Override
	protected String getTitleKey()
	{
		return getKey("rescontrol.settings.title");
	}

	@Override
	public void load(UniversalSettings state)
	{
		final ResourceSettings settings = new ResourceSettings(state);

		AppletGuiUtils.selectInJCombo(allowSelection, new ASItem(null, settings.getAllowedSelection()));
		AppletGuiUtils.selectInJCombo(relationType, new RelationType(settings.getRelationType()));
		for( EntityShuffler<ResourceSettings> rc : restrictions )
		{
			rc.load(settings);
		}
	}

	@Override
	public void removeSavedState(UniversalSettings state)
	{
		// Nothing to do here
	}

	@Override
	public void save(UniversalSettings state)
	{
		final ResourceSettings settings = new ResourceSettings(state);

		settings.setAllowedSelection(((ASItem) allowSelection.getSelectedItem()).getSecond());
		settings.setRelationType(((RelationType) relationType.getSelectedItem()).getValue());
		for( EntityShuffler<ResourceSettings> rc : restrictions )
		{
			rc.save(settings);
		}
	}

	private List<RelationType> getRelationTypes()
	{
		List<RelationType> results = new ArrayList<RelationType>();

		results.add(new RelationType(getKey("relationType.general"), null));

		for( Extension ext : Driver.instance().getPluginService()
			.getConnectedExtensions("com.tle.common.wizard.controls.resource", "relationTypes") )
		{
			results.add(new RelationType(ext));
		}

		return results;
	}

	private class ASItem extends Pair<String, AllowedSelection>
	{
		private static final long serialVersionUID = 1L;

		public ASItem(String key, AllowedSelection as)
		{
			super(key == null ? null : CurrentLocale.get(getKey("allowSelection.") + key), as);
		}

		@Override
		public boolean checkFields(Pair<String, AllowedSelection> rhs)
		{
			return Objects.equals(rhs.getSecond(), getSecond());
		}
	}

	private static class RelationType extends NameValue
	{
		private static final long serialVersionUID = 1L;
		private AllowedSelection lockSelection;

		public RelationType(String nameKey, String value)
		{
			super(CurrentLocale.get(nameKey), value);
		}

		public RelationType(String value)
		{
			this(null, value);
		}

		public RelationType(Extension ext)
		{
			this(ext.getParameter("nameKey").valueAsString(), ext.getParameter("value").valueAsString());

			Parameter p2 = ext.getParameter("lockSelectTypeToValue");
			if( p2 != null )
			{
				lockSelection = AllowedSelection.valueOf(p2.valueAsString());
			}
		}

		public AllowedSelection getLockSelection()
		{
			return lockSelection;
		}
	}

	private static class RestrictEntities extends EntityShuffler<ResourceSettings>
	{
		private final String storageKey;

		public RestrictEntities(String textKey, Class<? extends RemoteAbstractEntityService<?>> serviceClass,
			String storageKey)
		{
			super(textKey, serviceClass);
			this.storageKey = storageKey;
		}

		@Override
		protected boolean isRestricted(ResourceSettings control)
		{
			return control.isRestricted(storageKey);
		}

		@Override
		protected Set<String> getRestrictedTo(ResourceSettings control)
		{
			return control.getRestrictedTo(storageKey);
		}

		@Override
		protected void setRestricted(ResourceSettings control, boolean restricted)
		{
			control.setRestricted(storageKey, restricted);
		}

		@Override
		protected void setRestrictedTo(ResourceSettings control, Set<String> uuids)
		{
			control.setRestrictedTo(storageKey, uuids);
		}
	}
}
