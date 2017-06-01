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

package com.tle.admin.controls.htmleditor;

import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.editor.AbstractControlEditor;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.gui.TableLayout;
import com.google.common.collect.Lists;
import com.tle.admin.Driver;
import com.tle.admin.controls.EntityShuffler;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.dynacollection.RemoteDynaCollectionService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.htmleditmce.HtmlEditMceControl;
import com.tle.core.remoting.RemoteItemDefinitionService;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.i18n.BundleCache;

/**
 * The user can specify that any selection of EQUELLA resources (made via the
 * EQUELLA selection session button in the html Editor) be restricted to such
 * collections & searches as this control allows to be chosen. Once the radio
 * button is selected to bring the shuffle-boxes into view, by default nothing
 * is chosen. An attempt to both 'activate restriction to ...' and then to chose
 * nothing is invalid and cannot be saved. Once restriction is chosen, all
 * remote repositories are invisible..
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class HtmlEditMceEditor extends AbstractControlEditor<HtmlEditMceControl>
{
	private static final long serialVersionUID = 6872094930875310377L;

	protected static final int DEFAULT_ROWS = 8;

	protected I18nTextField title;
	protected I18nTextField description;
	protected JCheckBox lazyLoad;

	private final List<EntityShuffler<HtmlEditMceControl>> restrictions = Lists.newArrayList();

	protected JCheckBox mandatory;
	protected MultiTargetChooser metadataPicker;

	public HtmlEditMceEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
		setClientService(Driver.instance().getClientService());

		setShowScripting(true);

		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title"));
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description"));

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());
		lazyLoad = new JCheckBox(CurrentLocale.get("com.tle.admin.controls.htmleditor.lazyload.label"));
		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory"));

		final int height1 = title.getPreferredSize().height;
		final int width1 = descriptionLabel.getPreferredSize().width;
		final int[] mainrows = {height1, height1, height1, height1};
		final int[] maincols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL};

		JPanel mainFeatures = new JPanel(new TableLayout(mainrows, maincols, 5, 5));

		int row = 0;
		mainFeatures.add(titleLabel, new Rectangle(0, row, 1, 1));
		mainFeatures.add(title, new Rectangle(1, row++, 2, 1));
		mainFeatures.add(descriptionLabel, new Rectangle(0, row, 1, 1));
		mainFeatures.add(description, new Rectangle(1, row++, 2, 1));
		mainFeatures.add(lazyLoad, new Rectangle(0, row++, 2, 1));
		mainFeatures.add(mandatory, new Rectangle(0, row++, 2, 1));
		addSection(mainFeatures);

		metadataPicker = WizardHelper.createMultiTargetChooser(this);
		addSection(WizardHelper.createMetaData(metadataPicker));

		JLabel headingSelectorLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.controls.htmleditor.htmlselector.label"));

		JPanel selectionFeatures = new JPanel(new MigLayout("wrap 2, insets 10 15 20 5", "[][fill, grow]"));
		selectionFeatures.add(headingSelectorLabel, "span 2, growx");

		restrictions.add(new EntityShuffler<HtmlEditMceControl>(
			"com.tle.admin.controls.htmleditor.selectedcollections.label", RemoteItemDefinitionService.class)
		{
			@Override
			protected boolean isRestricted(HtmlEditMceControl control)
			{
				return control.isRestrictCollections();
			}

			@Override
			protected Set<String> getRestrictedTo(HtmlEditMceControl control)
			{
				return control.getCollectionsUuids();
			}

			@Override
			protected void setRestricted(HtmlEditMceControl control, boolean restricted)
			{
				control.setRestrictCollections(restricted);
			}

			@Override
			protected void setRestrictedTo(HtmlEditMceControl control, Set<String> uuids)
			{
				control.setCollectionsUuids(uuids);
			}
		});
		restrictions.add(new EntityShuffler<HtmlEditMceControl>(
			"com.tle.admin.controls.htmleditor.selectedsearches.label", RemotePowerSearchService.class)
		{
			@Override
			protected boolean isRestricted(HtmlEditMceControl control)
			{
				return control.isRestrictSearches();
			}

			@Override
			protected Set<String> getRestrictedTo(HtmlEditMceControl control)
			{
				return control.getSearchUuids();
			}

			@Override
			protected void setRestricted(HtmlEditMceControl control, boolean restricted)
			{
				control.setRestrictSearches(restricted);
			}

			@Override
			protected void setRestrictedTo(HtmlEditMceControl control, Set<String> uuids)
			{
				control.setSearchUuids(uuids);
			}
		});
		restrictions.add(new EntityShuffler<HtmlEditMceControl>(
			"com.tle.admin.controls.htmleditor.selecteddynacolls.label", RemoteDynaCollectionService.class)
		{
			@Override
			protected boolean isRestricted(HtmlEditMceControl control)
			{
				return control.isRestrictDynacolls();
			}

			@Override
			protected Set<String> getRestrictedTo(HtmlEditMceControl control)
			{
				return control.getDynaCollectionsUuids();
			}

			@Override
			protected void setRestricted(HtmlEditMceControl control, boolean restricted)
			{
				control.setRestrictDynacolls(restricted);
			}

			@Override
			protected void setRestrictedTo(HtmlEditMceControl control, Set<String> uuids)
			{
				control.setDynaCollectionsUuids(uuids);
			}
		});
		restrictions.add(new EntityShuffler<HtmlEditMceControl>(
			"com.tle.admin.controls.htmleditor.selectedcontributables.label", RemoteItemDefinitionService.class)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean isRestricted(HtmlEditMceControl control)
			{
				return control.isRestrictContributables();
			}

			@Override
			protected Set<String> getRestrictedTo(HtmlEditMceControl control)
			{
				return control.getContributableUuids();
			}

			@Override
			protected void setRestricted(HtmlEditMceControl control, boolean restricted)
			{
				control.setRestrictContributables(restricted);
			}

			@Override
			protected void setRestrictedTo(HtmlEditMceControl control, Set<String> uuids)
			{
				control.setContributableUuids(uuids);
			}
		});
		for( EntityShuffler<HtmlEditMceControl> rc : restrictions )
		{
			selectionFeatures.add(rc, "span 2, growx");
		}

		addSection(selectionFeatures);
	}

	@Override
	protected void loadControl()
	{
		HtmlEditMceControl control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		lazyLoad.setSelected(control.isLazyLoad());

		mandatory.setSelected(control.isMandatory());

		for( EntityShuffler<HtmlEditMceControl> rc : restrictions )
		{
			rc.load(control);
		}
		WizardHelper.loadSchemaChooser(metadataPicker, control);
	}

	@Override
	protected void saveControl()
	{
		HtmlEditMceControl control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setLazyLoad(lazyLoad.isSelected());
		control.setMandatory(mandatory.isSelected());

		for( EntityShuffler<HtmlEditMceControl> rc : restrictions )
		{
			rc.save(control);
		}
		WizardHelper.saveSchemaChooser(metadataPicker, control);
	}
}
