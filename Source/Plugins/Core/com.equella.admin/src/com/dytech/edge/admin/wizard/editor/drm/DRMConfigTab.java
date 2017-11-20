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

package com.dytech.edge.admin.wizard.editor.drm;

import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.gui.JShuffleBox;
import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class DRMConfigTab extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final String plugPrefix = "com.dytech.edge.admin.wizard.editor.drm."; //$NON-NLS-1$
	public static final String BASIC_USAGE_KEY = "drmconfigtab.basicusage"; //$NON-NLS-1$
	public static final String USE_AND_ADAPT_KEY = "drmconfigtab.useandadapt"; //$NON-NLS-1$
	public static final String CUSTOM_PERMISSIONS_KEY = "drmconfigtab.custompermissions"; //$NON-NLS-1$

	private static final String[] USAGE_OPTIONS_KEYS = {BASIC_USAGE_KEY, USE_AND_ADAPT_KEY, CUSTOM_PERMISSIONS_KEY};

	private JShuffleBox<String> usageOptions;
	private JCheckBox acceptBeforeSummary;
	private JCheckBox previewBeforeAccepting;
	private JCheckBox ownerMustAlsoAccept;
	private JCheckBox showLicenceInComposition;

	public DRMConfigTab()
	{
		super();
		createGUI();
	}

	private void createGUI()
	{
		JLabel title = new JLabel(CurrentLocale.get(plugPrefix + "drmconfigtab.select")); //$NON-NLS-1$

		String[] usageOptionsStrings = new String[USAGE_OPTIONS_KEYS.length];
		for( int i = 0; i < USAGE_OPTIONS_KEYS.length; ++i )
		{
			usageOptionsStrings[i] = CurrentLocale.get(plugPrefix + USAGE_OPTIONS_KEYS[i]);
		}

		usageOptions = new JShuffleBox<String>(usageOptionsStrings, CurrentLocale.get(plugPrefix
			+ "drmconfigtab.available"), //$NON-NLS-1$
			CurrentLocale.get(plugPrefix + "drmconfigtab.selected")); //$NON-NLS-1$

		acceptBeforeSummary = new JCheckBox(CurrentLocale.get(plugPrefix + "drmconfigtab.requireusers")); //$NON-NLS-1$
		ownerMustAlsoAccept = new JCheckBox(CurrentLocale.get(plugPrefix + "drmconfigtab.requireowner")); //$NON-NLS-1$
		showLicenceInComposition = new JCheckBox(CurrentLocale.get(plugPrefix + "drmconfigtab.requirestudents")); //$NON-NLS-1$
		previewBeforeAccepting = new JCheckBox(CurrentLocale.get(plugPrefix + "drmconfigtab.allow")); //$NON-NLS-1$

		final int height1 = title.getPreferredSize().height;
		final int height2 = acceptBeforeSummary.getPreferredSize().height;
		final int[] rows = {height1, TableLayout.PREFERRED, height2, height2, height2, height2, TableLayout.FILL,};
		final int[] cols = {TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(title, new Rectangle(0, 0, 1, 1));
		add(usageOptions, new Rectangle(0, 1, 1, 1));
		add(acceptBeforeSummary, new Rectangle(0, 2, 1, 1));
		add(ownerMustAlsoAccept, new Rectangle(0, 3, 1, 1));
		add(showLicenceInComposition, new Rectangle(0, 4, 1, 1));
		add(previewBeforeAccepting, new Rectangle(0, 5, 1, 1));
	}

	public void load(DRMPage page)
	{
		Set<String> usages = page.getUsages();
		for( String usage : usages )
		{
			usageOptions.removeFromLeft(usage);
			usageOptions.addToRight(usage);
		}

		acceptBeforeSummary.setSelected(!page.isAllowSummary());
		ownerMustAlsoAccept.setSelected(page.isOwnerMustAccept());
		previewBeforeAccepting.setSelected(page.isAllowPreview());
		showLicenceInComposition.setSelected(page.isShowLicenceInComposition());
	}

	public void save(DRMPage page)
	{
		Set<String> usages = new HashSet<String>();
		for( String right : usageOptions.getRight() )
		{
			usages.add(right);
		}
		page.setUsages(usages);
		page.setAllowSummary(!acceptBeforeSummary.isSelected());
		page.setAllowPreview(previewBeforeAccepting.isSelected());
		page.setOwnerMustAccept(ownerMustAlsoAccept.isSelected());
		page.setShowLicenceInComposition(showLicenceInComposition.isSelected());
	}
}
