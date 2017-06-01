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
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DRMPage.Container;
import com.dytech.edge.wizard.beans.DRMPage.Contributor;
import com.dytech.gui.TableLayout;
import com.tle.common.applet.gui.JGroup;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class DRMRightsTab extends JPanel
{
	private static final long serialVersionUID = 1L;
	private SectorPanel sector;
	private AttributionPanel attribution;
	private AcceptancePanel acceptance;

	public DRMRightsTab()
	{
		super();
		createGUI();
	}

	private void createGUI()
	{
		sector = new SectorPanel();
		attribution = new AttributionPanel();
		acceptance = new AcceptancePanel();

		final int height1 = sector.getPreferredSize().height;
		final int height2 = attribution.getPreferredSize().height;
		final int height3 = acceptance.getPreferredSize().height;
		final int[] rows = {height1, height2, height3, TableLayout.FILL};
		final int[] cols = {TableLayout.FILL};

		setLayout(new TableLayout(rows, cols, 5, 5));
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		add(sector, new Rectangle(0, 0, 1, 1));
		add(attribution, new Rectangle(0, 1, 1, 1));
		add(acceptance, new Rectangle(0, 2, 1, 1));
	}

	public void load(DRMPage page)
	{
		Container container = page.getContainer();
		Contributor contributor = page.getContributor();
		if( container.isPurpose() )
		{
			sector.restrictedToEducation();
		}
		else if( contributor.isSector() )
		{
			sector.selectableByContributor();
		}

		if( page.isAttribution() )
		{
			attribution.attributionRequired();
		}
		else if( contributor.isAttribution() )
		{
			attribution.selectableByContributor();
		}
		attribution.enforceAttribution(page.isAttributionIsEnforced());

		if( page.getRemark() != null )
		{
			acceptance.termsRequired(page.getRemark());
		}
		else if( contributor.isTerms() )
		{
			acceptance.selectableByContributor();
		}
	}

	public void save(DRMPage page)
	{
		if( sector.isRestrictedToEducation() )
		{
			page.getContainer().setPurpose(true);
			page.getContributor().setSector(false);
		}
		else if( sector.isSelectableByContributor() )
		{
			page.getContributor().setSector(true);
			page.getContainer().setPurpose(false);
		}

		page.setAttribution(attribution.isAttributionRequired());
		page.getContributor().setAttribution(attribution.isSelectableByContributor());
		page.setAttributionIsEnforced(attribution.isAttributionEnforced());

		page.setRemark(null);
		page.getContributor().setTerms(false);
		if( acceptance.areTermsRequired() )
		{
			page.setRemark(acceptance.getTerms());
		}
		else if( acceptance.isSelectableByContributor() )
		{
			page.getContributor().setTerms(true);
		}
	}

	private static final class SectorPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final JRadioButton restrict;
		private final JRadioButton selectable;

		public SectorPanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.restrictwarning"), false); //$NON-NLS-1$

			restrict = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.restrictedu"), true); //$NON-NLS-1$
			selectable = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.contributor")); //$NON-NLS-1$

			ButtonGroup group = new ButtonGroup();
			group.add(restrict);
			group.add(selectable);

			final int height = restrict.getPreferredSize().height;
			final int[] rows = {height, height};
			final int[] cols = {TableLayout.FILL};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(restrict, new Rectangle(0, 0, 1, 1));
			addInner(selectable, new Rectangle(0, 1, 1, 1));

			setSelected(false);
		}

		public boolean isRestrictedToEducation()
		{
			return isSelected() && restrict.isSelected();
		}

		public boolean isSelectableByContributor()
		{
			return isSelected() && selectable.isSelected();
		}

		public void restrictedToEducation()
		{
			restrict.setSelected(true);
			setSelected(true);
		}

		public void selectableByContributor()
		{
			selectable.setSelected(true);
			setSelected(true);
		}
	}

	private static final class AttributionPanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final JRadioButton required;
		private final JRadioButton selectable;
		private final JCheckBox enforce;

		public AttributionPanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.attribution"), false); //$NON-NLS-1$

			required = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.require"), true); //$NON-NLS-1$
			selectable = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.selects")); //$NON-NLS-1$
			enforce = new JCheckBox(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.enforce"), true); //$NON-NLS-1$

			ButtonGroup group = new ButtonGroup();
			group.add(required);
			group.add(selectable);

			final int height = required.getPreferredSize().height;
			final int[] rows = {height, height, height,};
			final int[] cols = {TableLayout.FILL,};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(required, new Rectangle(0, 0, 1, 1));
			addInner(selectable, new Rectangle(0, 1, 1, 1));
			addInner(enforce, new Rectangle(0, 2, 1, 1));

			setSelected(false);
		}

		public boolean isAttributionRequired()
		{
			return isSelected() && required.isSelected();
		}

		public boolean isSelectableByContributor()
		{
			return isSelected() && selectable.isSelected();
		}

		public boolean isAttributionEnforced()
		{
			return isSelected() && enforce.isSelected();
		}

		public void attributionRequired()
		{
			setSelected(true);
			required.setSelected(true);
		}

		public void selectableByContributor()
		{
			setSelected(true);
			selectable.setSelected(true);
		}

		public void enforceAttribution(boolean b)
		{
			enforce.setSelected(b);
		}
	}

	private static final class AcceptancePanel extends JGroup
	{
		private static final long serialVersionUID = 1L;
		private final JRadioButton required;
		private final JRadioButton selectable;
		private final JTextArea terms;

		public AcceptancePanel()
		{
			super(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.acceptance"), false); //$NON-NLS-1$

			JLabel termsLabel = new JLabel(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.terms")); //$NON-NLS-1$

			required = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.requireall"), true); //$NON-NLS-1$
			selectable = new JRadioButton(
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drm.drmrightstab.allow")); //$NON-NLS-1$

			ButtonGroup group = new ButtonGroup();
			group.add(required);
			group.add(selectable);

			required.addActionListener(this);
			selectable.addActionListener(this);

			terms = new JTextArea();
			terms.setWrapStyleWord(true);
			terms.setLineWrap(true);

			JScrollPane termsScroll = new JScrollPane(terms);

			final int height1 = required.getPreferredSize().height;
			final int height2 = termsLabel.getPreferredSize().height;
			final int height3 = height1 * 6;
			final int[] rows = {height1, height2, height3, height1};
			final int[] cols = {15, TableLayout.FILL};

			setInnerLayout(new TableLayout(rows, cols, 5, 5));
			addInner(required, new Rectangle(0, 0, 2, 1));
			addInner(termsLabel, new Rectangle(1, 1, 1, 1));
			addInner(termsScroll, new Rectangle(1, 2, 1, 1));
			addInner(selectable, new Rectangle(0, 3, 2, 1));

			setSelected(false);
		}

		public boolean areTermsRequired()
		{
			return isSelected() && required.isSelected();
		}

		public boolean isSelectableByContributor()
		{
			return isSelected() && selectable.isSelected();
		}

		public String getTerms()
		{
			return terms.getText();
		}

		public void termsRequired(String text)
		{
			required.setSelected(true);
			terms.setText(text);
			setSelected(true);
		}

		public void selectableByContributor()
		{
			selectable.setSelected(true);
			setSelected(true);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( e.getSource() == required || e.getSource() == selectable )
			{
				updateTerms();
			}
			else
			{
				super.actionPerformed(e);
			}
		}

		@Override
		public void setSelected(boolean enabled)
		{
			super.setSelected(enabled);
			updateTerms();
		}

		private void updateTerms()
		{
			terms.setEnabled(required.isSelected() && isSelected());
		}
	}
}
