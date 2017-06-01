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

package com.tle.admin.taxonomy.wizard;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractPowerSearchControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.admin.gui.common.EmptyDynamicChoicePanel;
import com.tle.admin.gui.common.RadioButtonChoiceList;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.applet.gui.AppletGuiUtils.BetterGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.RemoteTaxonomyService;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.wizard.TermSelectorControl;
import com.tle.common.taxonomy.wizard.TermSelectorControl.TermStorageFormat;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class TermSelectorEditor extends AbstractPowerSearchControlEditor<TermSelectorControl>
{
	private static final long serialVersionUID = 1L;

	private I18nTextField title;
	private I18nTextField description;
	private JComboBox taxonomies;
	private JCheckBox mandatory;
	private JCheckBox multiple;
	private JCheckBox addterms;
	private BetterGroup<JRadioButton, TermStorageFormat> termStorageGroup;
	private BetterGroup<JRadioButton, SelectionRestriction> restrictionGroup;
	private RadioButtonChoiceList<TermSelectorControl, Extension> displayTypes;
	private MultiTargetChooser picker;

	private RemoteTaxonomyService taxonomyService;

	public TermSelectorEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	public void init()
	{
		taxonomyService = getClientService().getService(RemoteTaxonomyService.class);
	}

	@Override
	protected void loadControl()
	{
		TermSelectorControl control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		multiple.setSelected(control.isAllowMultiple());
		addterms.setSelected(control.isAllowAddTerms());
		termStorageGroup.selectButtonByValue(control.getTermStorageFormat());
		restrictionGroup.selectButtonByValue(control.getSelectionRestriction());
		displayTypes.load(control);

		WizardHelper.loadSchemaChooser(picker, control);

		selectTaxonomy(control.getSelectedTaxonomy());

		super.loadControl();
	}

	@Override
	protected void saveControl()
	{
		TermSelectorControl control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setSelectedTaxonomy(getSelectedTaxonomy().getValue());
		control.setMandatory(mandatory.isSelected());
		control.setAllowMultiple(multiple.isSelected());
		control.setAllowAddTerms(getWizardType() == WizardHelper.WIZARD_TYPE_CONTRIBUTION && addterms.isEnabled()
			&& addterms.isSelected());
		control.setTermStorageFormat(termStorageGroup.getSelectedValue());
		control.setSelectionRestriction(restrictionGroup.getSelectedValue());

		displayTypes.save(control);

		WizardHelper.saveSchemaChooser(picker, control);

		super.saveControl();
	}

	@Override
	protected void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createMultiTargetChooser(this);

		addSection(setupDetailsSection());
		addSection(WizardHelper.createMetaData(picker));

		super.setupGUI();
	}

	private JPanel setupDetailsSection()
	{
		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		taxonomies = new JComboBox();
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory"));
		multiple = new JCheckBox(s("multiple"));
		addterms = new JCheckBox(s("addterm"));

		final boolean isPower = (getWizardType() == WizardHelper.WIZARD_TYPE_POWERSEARCH);

		JRadioButton storageFull = new JRadioButton(s(isPower ? "usage.full" : "storage.full"), true);
		JRadioButton storageLeaf = new JRadioButton(s(isPower ? "usage.leaf" : "storage.leaf"));

		termStorageGroup = new BetterGroup<JRadioButton, TermStorageFormat>(true, storageFull,
			TermStorageFormat.FULL_PATH, storageLeaf, TermStorageFormat.LEAF_ONLY);

		JRadioButton restrictionTopLevel = new JRadioButton(s("restriction.toplevel"));
		JRadioButton restrictionLeaf = new JRadioButton(s("restriction.leaf"), true);
		JRadioButton restrictionNone = new JRadioButton(s("restriction.none"));

		restrictionGroup = new BetterGroup<JRadioButton, SelectionRestriction>(true, restrictionTopLevel,
			SelectionRestriction.TOP_LEVEL_ONLY, restrictionLeaf, SelectionRestriction.LEAF_ONLY, restrictionNone,
			SelectionRestriction.UNRESTRICTED);

		displayTypes = new RadioButtonChoiceList<TermSelectorControl, Extension>()
		{
			@Override
			public void setSavedChoiceId(TermSelectorControl state, String choiceId)
			{
				state.setDisplayType(choiceId);
			}

			@Override
			public String getSavedChoiceId(TermSelectorControl state)
			{
				return state.getDisplayType();
			}

			@Override
			public String getChoiceTitle(Extension choice)
			{
				return CurrentLocale.get(choice.getParameter("nameKey").valueAsString());
			}

			@Override
			@SuppressWarnings("unchecked")
			public DynamicChoicePanel<TermSelectorControl> getChoicePanel(Extension choice)
			{
				final Parameter configPanelParam = choice.getParameter("configPanel");
				final String configPanelClass = configPanelParam == null ? null : configPanelParam.valueAsString();

				if( Check.isEmpty(configPanelClass) )
				{
					return new EmptyDynamicChoicePanel<TermSelectorControl>();
				}
				else
				{
					DynamicChoicePanel<TermSelectorControl> dcp = (DynamicChoicePanel<TermSelectorControl>) getPluginService()
						.getBean(choice.getDeclaringPluginDescriptor(), configPanelClass);
					return dcp;
				}
			}

			@Override
			public String getChoiceId(Extension choice)
			{
				return choice.getId();
			}
		};

		// We want to make sure that control settings are saved, even when we're
		// reloading the list of choices, so ensure we save and load all states.
		displayTypes.setRemoveStateForNonSelectedChoices(false);
		displayTypes.setLoadStateForNonSelectedChoices(true);

		JPanel all = new JPanel(new MigLayout("wrap 2", "[][grow]"));
		all.add(new JLabel(CurrentLocale.get("wizard.controls.title")), "align label");
		all.add(title, "growx");
		all.add(new JLabel(CurrentLocale.get("wizard.controls.description")), "align label");
		all.add(description, "growx");
		all.add(new JLabel(s("taxonomy")), "align label");
		all.add(taxonomies, "");

		all.add(mandatory, "span");
		all.add(multiple, "span");
		if( getWizardType() == WizardHelper.WIZARD_TYPE_CONTRIBUTION )
		{
			all.add(addterms, "span");
		}

		all.add(new JLabel(s(isPower ? "usage" : "storage")), "span");
		all.add(storageFull, "span, gapleft 40px");
		all.add(storageLeaf, "span, gapleft 40px");

		all.add(new JLabel(s("restriction")), "span");
		all.add(restrictionTopLevel, "span, gapleft 40px");
		all.add(restrictionLeaf, "span, gapleft 40px");
		all.add(restrictionNone, "span, gapleft 40px");

		all.add(new JLabel(s("displaytypes")), "span");
		all.add(displayTypes, "span, gapleft 40px, growx");

		taxonomies.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				taxonomySelectionEvent();
			}
		});

		return all;
	}

	private void taxonomySelectionEvent()
	{
		final String uuid = getSelectedTaxonomy().getValue();

		if( Check.isEmpty(uuid) )
		{
			addterms.setEnabled(false);
			displayTypes.setEnabled(false);
		}
		else
		{
			GlassSwingWorker<?> worker = new GlassSwingWorker<List<Extension>>()
			{
				private boolean supportsTermAddition;
				private boolean supportsTermBrowsing;
				private boolean supportsTermSearching;

				@Override
				public List<Extension> construct() throws Exception
				{
					supportsTermAddition = taxonomyService.supportsTermAddition(uuid);
					supportsTermBrowsing = taxonomyService.supportsTermBrowsing(uuid);
					supportsTermSearching = taxonomyService.supportsTermSearching(uuid);

					// Filter the extension list to only include supported
					// display types for this above settings
					List<Extension> rv = new ArrayList<Extension>();
					for( Extension ext : getPluginService().getConnectedExtensions("com.tle.admin.taxonomy.tool",
						"displayType") )
					{
						if( (ext.getParameter("supportsBrowsing").valueAsBoolean() && supportsTermBrowsing)
							|| (ext.getParameter("supportsSearching").valueAsBoolean() && supportsTermSearching) )
						{
							rv.add(ext);
						}
					}
					return rv;
				}

				@Override
				public void finished()
				{
					addterms.setEnabled(supportsTermAddition);
					displayTypes.setEnabled(true);

					// Ensure that we keep any configuaration state between
					// choice reloads. Much nicer for the end-user.
					final TermSelectorControl state = getWizardControl();

					// We need to maintain the displayType property ourselves,
					// otherwise it will be deleted by the "save" invocation
					// below when no choices are loaded.
					final String displayType = state.getDisplayType();
					displayTypes.save(state);

					displayTypes.loadChoices(get());

					state.setDisplayType(displayType);
					displayTypes.load(state);
					displayTypes.clearChanges();
				}
			};
			worker.setComponent(this);
			worker.start();
		}
	}

	private NameValue getSelectedTaxonomy()
	{
		return (NameValue) taxonomies.getSelectedItem();
	}

	private String s(String keyEnd)
	{
		return CurrentLocale.get("com.tle.admin.taxonomy.tool.wizard.termselector." + keyEnd);
	}

	private void selectTaxonomy(final String taxUuid)
	{
		GlassSwingWorker<?> worker = new GlassSwingWorker<List<NameValue>>()
		{
			@Override
			public List<NameValue> construct() throws Exception
			{
				if( taxonomies.getModel().getSize() > 0 )
				{
					return null;
				}
				else
				{
					List<BaseEntityLabel> ts = taxonomyService.listAll();
					List<NameValue> nvs = BundleCache.getNameUuidValues(ts);
					Collections.sort(nvs, Format.NAME_VALUE_COMPARATOR);
					nvs.add(0, new NameValue(s("taxonomy.pleasechoose"), null));
					return nvs;
				}
			}

			@Override
			public void finished()
			{
				if( get() != null )
				{
					for( NameValue nv : get() )
					{
						taxonomies.addItem(nv);
					}
				}

				if( taxUuid != null )
				{
					AppletGuiUtils.selectInJCombo(taxonomies, new NameValue(null, taxUuid));
				}
			}
		};
		worker.setComponent(taxonomies);
		worker.start();
	}
}
