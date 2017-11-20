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

package com.tle.admin.controls.universal;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.CustomControl;
import com.dytech.gui.workers.GlassSwingWorker;
import com.tle.admin.gui.common.CheckboxChoiceList;
import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.admin.gui.common.EmptyDynamicChoicePanel;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class UniversalControlEditor extends AbstractControlEditor<CustomControl>
{
	private static final long serialVersionUID = 1L;

	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox multiple;
	private JCheckBox maxFiles;
	private JCheckBox preview;
	private MultiTargetChooser picker;
	private CheckboxChoiceList<UniversalSettings, Extension> types;
	private SpinnerNumberModel maxFilesModel;
	private JSpinner maxFilesEdit;

	public UniversalControlEditor(final Control control, final int wizardType, final SchemaModel schema)
	{
		super(control, wizardType, schema);
		control.setEditor(this);
		setupGUI();
	}

	private void setupGUI()
	{
		setShowScripting(true);

		addSection(createDetailsSection());

		picker = WizardHelper.createMultiTargetChooser(this);
		addSection(WizardHelper.createMetaData(picker));

		addSection(createTypesSection());

		GlassSwingWorker<?> worker = new GlassSwingWorker<Collection<Extension>>()
		{
			@Override
			public Collection<Extension> construct() throws Exception
			{
				return getPluginService().getConnectedExtensions("com.tle.admin.controls.universal", "editor");
			}

			@Override
			public void finished()
			{
				types.setEnabled(true);

				final UniversalControl control = (UniversalControl) getWizardControl();
				final UniversalSettings state = new UniversalSettings(control);
				final Set<String> attachmentTypes = state.getAttachmentTypes();
				types.save(state);

				types.loadChoices(get());

				state.setAttachmentTypes(attachmentTypes);

				types.load(state);
				types.clearChanges();
				types.afterLoad(state);
			}
		};
		worker.setComponent(this);
		worker.start();
	}

	private JPanel createDetailsSection()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title"));
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description"));

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory"));
		multiple = new JCheckBox(getString("universal.multiple"));
		maxFiles = new JCheckBox(getString("maxfiles"));
		preview = new JCheckBox(getString("preview"));
		maxFilesModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		maxFilesEdit = new JSpinner(maxFilesModel);

		multiple.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();
			}
		});

		maxFiles.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateGui();
			}
		});

		JPanel details = new JPanel(new MigLayout("insets 5 5 5 5, fill, wrap 2", "[][fill,grow]"));
		details.add(titleLabel);
		details.add(title);
		details.add(descriptionLabel);
		details.add(description);

		details.add(mandatory, "span 2");
		details.add(multiple, "span 2");
		details.add(maxFiles, "gapleft 18, split 2, span 2, align left");
		details.add(maxFilesEdit, "w 60!");
		details.add(preview, "span 2");

		return details;
	}

	private JPanel createTypesSection()
	{
		JPanel typesPanel = new JPanel(new MigLayout("", "[grow]"));

		types = new CheckboxChoiceList<UniversalSettings, Extension>(
			getString("selecttypes"), 2)
		{
			@Override
			public Collection<String> getSavedChoiceIds(UniversalSettings state)
			{
				return state.getAttachmentTypes();
			}

			@Override
			public void setSavedChoiceIds(UniversalSettings state, Collection<String> choiceIds)
			{
				final Set<String> selections = new HashSet<String>();
				selections.addAll(choiceIds);
				state.setAttachmentTypes(selections);
			}

			@Override
			public String getChoiceId(Extension choice)
			{
				return choice.getId();
			}

			@Override
			public DynamicChoicePanel<UniversalSettings> getChoicePanel(Extension choice)
			{
				final Parameter configPanelParam = choice.getParameter("configPanel");
				final String configPanelClass = configPanelParam == null ? null : configPanelParam.valueAsString();

				if( Check.isEmpty(configPanelClass) )
				{
					return new EmptyDynamicChoicePanel<UniversalSettings>();
				}

				UniversalControlSettingPanel dcp = (UniversalControlSettingPanel) getPluginService().getBean(
					choice.getDeclaringPluginDescriptor(), configPanelClass);
				dcp.init((UniversalControl) getWizardControl(), getWizardType(), getSchema(), getClientService());
				return dcp;
			}

			@Override
			public String getChoiceTitle(Extension choice)
			{
				return CurrentLocale.get(choice.getParameter("nameKey").valueAsString());
			}
		};
		typesPanel.add(types, "span, growx");
		return typesPanel;
	}

	private void updateGui()
	{
		maxFilesEdit.setEnabled(maxFiles.isSelected());
		maxFiles.setEnabled(multiple.isSelected());

		if( !multiple.isSelected() )
		{
			maxFiles.setSelected(false);
			maxFilesEdit.setEnabled(false);
		}
	}

	@Override
	protected void loadControl()
	{
		final CustomControl control = getWizardControl();
		final UniversalSettings settings = new UniversalSettings(control);

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		multiple.setSelected(settings.isMultipleSelection());
		maxFiles.setSelected(settings.isMaxFilesEnabled());
		maxFiles.setEnabled(settings.isMultipleSelection());
		maxFilesEdit.setEnabled(settings.isMaxFilesEnabled());
		preview.setSelected(settings.isAllowPreviews());
		maxFilesModel.setValue(settings.getMaxFiles());
		WizardHelper.loadSchemaChooser(picker, control);

		types.load(settings);
	}

	@Override
	protected void saveControl()
	{
		final CustomControl control = getWizardControl();
		final UniversalSettings settings = new UniversalSettings(control);

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		settings.setMultipleSelection(multiple.isSelected());
		settings.setMaxFilesEnabled(maxFiles.isSelected());
		settings.setAllowPreviews(preview.isSelected());
		settings.setMaxFiles(maxFilesModel.getNumber().intValue());
		WizardHelper.saveSchemaChooser(picker, control);

		types.save(settings);
	}

	protected static String getString(String partKey, Object... params)
	{
		return CurrentLocale.get("com.tle.admin.controls.universal." + partKey, params);
	}
}
