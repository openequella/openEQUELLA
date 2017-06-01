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

package com.tle.admin.hierarchy;

import java.awt.Rectangle;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.hierarchy.TopicEditor.AbstractTopicEditorTab;
import com.tle.beans.NameId;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.hierarchy.HierarchyPack;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
public class DetailsTab extends AbstractTopicEditorTab
{
	private static final long serialVersionUID = 1L;

	private final EntityCache cache;

	private I18nTextField topicName;
	private I18nTextArea shortDescription;
	private I18nTextArea longDescription;

	private I18nTextField subtopicsSection;
	private I18nTextField searchResultsSection;

	private JCheckBox hideSubtopicsWithNoResults;

	private JComboBox advancedSearchSelector;

	public DetailsTab(EntityCache cache)
	{
		this.cache = cache;
	}

	@Override
	public void setup(ChangeDetector changeDetector)
	{
		JLabel topicNameLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.topicname")); //$NON-NLS-1$
		JLabel shortLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.shortdesc")); //$NON-NLS-1$
		JLabel longLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.fulldesc")); //$NON-NLS-1$
		JLabel sectionsLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.sectionnames")); //$NON-NLS-1$
		JLabel subtopicsLabel = new JLabel(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.subtopics")); //$NON-NLS-1$
		JLabel searchResultsLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.searchresults")); //$NON-NLS-1$
		JLabel advancedSearchLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.advancedsearch")); //$NON-NLS-1$

		topicName = new I18nTextField(BundleCache.getLanguages());

		subtopicsSection = new I18nTextField(BundleCache.getLanguages());
		searchResultsSection = new I18nTextField(BundleCache.getLanguages());

		shortDescription = new I18nTextArea(BundleCache.getLanguages());
		longDescription = new I18nTextArea(BundleCache.getLanguages());

		hideSubtopicsWithNoResults = new JCheckBox(
			CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.hidesubtopicswithnoresults")); //$NON-NLS-1$

		advancedSearchSelector = new JComboBox();
		advancedSearchSelector.addItem(CurrentLocale.get("com.tle.admin.hierarchy.tool.detailstab.nopowersearch")); //$NON-NLS-1$
		AppletGuiUtils.addItemsToJCombo(advancedSearchSelector, cache.getPowerSearches());

		final int width1 = 20;
		final int width2 = topicNameLabel.getPreferredSize().width - width1;
		final int width3 = Math.max(searchResultsLabel.getPreferredSize().width,
			searchResultsLabel.getPreferredSize().width) - width2;

		final int[] rows = {TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED,
				TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
				TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,};
		final int[] cols = {width1, width2, width3, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		int row = 0;
		add(topicNameLabel, new Rectangle(0, row, 2, 1));
		add(topicName, new Rectangle(2, row++, 2, 1));

		add(shortLabel, new Rectangle(0, row++, 4, 1));
		add(shortDescription, new Rectangle(1, row++, 3, 1));

		add(longLabel, new Rectangle(0, row++, 4, 1));
		add(longDescription, new Rectangle(1, row++, 3, 1));

		add(sectionsLabel, new Rectangle(0, row++, 4, 1));

		add(subtopicsLabel, new Rectangle(1, row, 2, 1));
		add(subtopicsSection, new Rectangle(3, row++, 1, 1));
		add(searchResultsLabel, new Rectangle(1, row, 2, 1));
		add(searchResultsSection, new Rectangle(3, row++, 1, 1));

		add(hideSubtopicsWithNoResults, new Rectangle(0, row++, 4, 1));

		add(advancedSearchLabel, new Rectangle(0, row++, 4, 1));
		add(advancedSearchSelector, new Rectangle(1, row++, 3, 1));

		changeDetector.watch(topicName);
		changeDetector.watch(shortDescription);
		changeDetector.watch(longDescription);
		changeDetector.watch(subtopicsSection);
		changeDetector.watch(searchResultsSection);
		changeDetector.watch(advancedSearchSelector);
	}

	@Override
	public void load(HierarchyPack pack)
	{
		HierarchyTopic topic = pack.getTopic();

		topicName.load(topic.getName());
		shortDescription.load(topic.getShortDescription());
		longDescription.load(topic.getLongDescription());

		subtopicsSection.load(topic.getSubtopicsSectionName());
		searchResultsSection.load(topic.getResultsSectionName());

		hideSubtopicsWithNoResults.setSelected(topic.isHideSubtopicsWithNoResults());

		if( topic.getAdvancedSearch() != null )
		{
			AppletGuiUtils.selectInJCombo(advancedSearchSelector, new NameId(null, topic.getAdvancedSearch().getId()),
				0);
		}
	}

	@Override
	public void save(HierarchyPack pack)
	{
		HierarchyTopic topic = pack.getTopic();

		topic.setName(topicName.save());
		topic.setShortDescription(shortDescription.save());
		topic.setLongDescription(longDescription.save());

		topic.setSubtopicsSectionName(subtopicsSection.save());
		topic.setResultsSectionName(searchResultsSection.save());

		topic.setHideSubtopicsWithNoResults(hideSubtopicsWithNoResults.isSelected());

		if( advancedSearchSelector.getSelectedIndex() == 0 )
		{
			topic.setAdvancedSearch(null);
		}
		else
		{
			long id = ((NameId) advancedSearchSelector.getSelectedItem()).getId();
			topic.setAdvancedSearch(new PowerSearch(id));
		}
	}

	@Override
	public void validation() throws EditorException
	{
		// nothing to validate
	}
}
