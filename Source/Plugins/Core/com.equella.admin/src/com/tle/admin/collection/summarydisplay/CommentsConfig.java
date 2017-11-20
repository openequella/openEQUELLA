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

package com.tle.admin.collection.summarydisplay;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

public class CommentsConfig extends AbstractOnlyTitleConfig
{
	private static enum NameDisplayType
	{
		BOTH, FIRST, LAST;
	}

	private static final String ANONYMOUSLY_COMMENTS_KEY = "ANONYMOUSLY_COMMENTS_KEY"; //$NON-NLS-1$
	private static final String DISPLAY_IDENTITY_KEY = "DISPLAY_IDENTITY_KEY"; //$NON-NLS-1$
	private static final String DISPLAY_NAME_KEY = "DISPLAY_NAME_KEY"; //$NON-NLS-1$
	private static final String SUPPRESS_USERNAME_KEY = "SUPPRESS_USERNAME_KEY"; //$NON-NLS-1$

	private JLabel text;
	private JRadioButton showIdentity;
	private JRadioButton firstAndLast;
	private JRadioButton firstOnly;
	private JRadioButton lastOnly;
	private JCheckBox suppressUsername;
	private JCheckBox allowAnonymous;
	private JRadioButton hideIdenty;
	private ChangeDetector changeDetector;

	private static final String RESOURCE_PFX = "com.tle.admin.collection.tool.summarysections.comments."; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void setup()
	{
		setLayout(new MigLayout());

		ButtonGroup displayIdentity = new ButtonGroup();
		ButtonGroup names = new ButtonGroup();

		text = new JLabel(CurrentLocale.get(RESOURCE_PFX + "text"));
		showIdentity = new JRadioButton(CurrentLocale.get(RESOURCE_PFX + "showidentity"));
		displayIdentity.add(showIdentity);
		showIdentity.addActionListener(new EnableOptions());

		firstAndLast = new JRadioButton(CurrentLocale.get(RESOURCE_PFX + "firstandlast"));
		names.add(firstAndLast);

		firstOnly = new JRadioButton(CurrentLocale.get(RESOURCE_PFX + "first"));
		names.add(firstOnly);

		lastOnly = new JRadioButton(CurrentLocale.get(RESOURCE_PFX + "last"));
		names.add(lastOnly);

		suppressUsername = new JCheckBox(CurrentLocale.get(RESOURCE_PFX + "suppressusername"));

		allowAnonymous = new JCheckBox(CurrentLocale.get(RESOURCE_PFX + "allowanonymous"));

		hideIdenty = new JRadioButton(CurrentLocale.get(RESOURCE_PFX + "hideidentity"));
		displayIdentity.add(hideIdenty);
		hideIdenty.addActionListener(new DisableOptions());

		super.setup();

		add(text, "wrap 15px, gaptop 15px");
		add(showIdentity, "wrap 5px");
		add(firstAndLast, "wrap, gapleft 15px");
		add(firstOnly, "wrap, gapleft 15px");
		add(lastOnly, "wrap 5px, gapleft 15px");
		add(suppressUsername, "wrap 8px, gapleft 15px");
		add(allowAnonymous, "wrap 10px, gapleft 15px");
		add(hideIdenty, "wrap");

		changeDetector = new ChangeDetector();
		changeDetector.watch(allowAnonymous);
		changeDetector.watch(displayIdentity);
		changeDetector.watch(names);
		changeDetector.watch(suppressUsername);
		changeDetector.watch(title);

	}

	@Override
	public boolean showTitleHelp()
	{
		return true;
	}

	@Override
	public void load(SummarySectionsConfig sectionElement)
	{
		String config = sectionElement.getConfiguration();
		super.load(sectionElement);
		if( !Check.isEmpty(config) )
		{
			PropBagEx xml = new PropBagEx(config);
			if( Boolean.parseBoolean(xml.getNode(DISPLAY_IDENTITY_KEY)) )
			{
				showIdentity.setSelected(true);
				enableOptions(true);
				suppressUsername.setSelected(Boolean.parseBoolean(xml.getNode(SUPPRESS_USERNAME_KEY)));
				allowAnonymous.setSelected(Boolean.parseBoolean(xml.getNode(ANONYMOUSLY_COMMENTS_KEY)));
				switch( NameDisplayType.valueOf(xml.getNode(DISPLAY_NAME_KEY)) )
				{
					case BOTH:
						firstAndLast.setSelected(true);
						break;
					case FIRST:
						firstOnly.setSelected(true);
						break;
					case LAST:
						lastOnly.setSelected(true);
						break;
					default:
						firstAndLast.setSelected(true);
				}
			}
			else
			{
				hideIdenty.setSelected(true);
				enableOptions(false);
			}

		}
		else
		{
			showIdentity.setSelected(true);
			firstAndLast.setSelected(true);
			suppressUsername.setSelected(true);
			allowAnonymous.setSelected(true);
		}

	}

	@SuppressWarnings("nls")
	@Override
	public void save(SummarySectionsConfig sectionElement)
	{
		PropBagEx xml = new PropBagEx("<xml/>");
		if( hideIdenty.isSelected() )
		{
			xml.setNode(DISPLAY_IDENTITY_KEY, false);
		}
		else
		{
			xml.setNode(DISPLAY_IDENTITY_KEY, true);
			xml.setNode(SUPPRESS_USERNAME_KEY, suppressUsername.isSelected());
			xml.setNode(ANONYMOUSLY_COMMENTS_KEY, allowAnonymous.isSelected());
			if( firstAndLast.isSelected() )
			{
				xml.setNode(DISPLAY_NAME_KEY, NameDisplayType.BOTH.toString());
			}
			else if( firstOnly.isSelected() )
			{
				xml.setNode(DISPLAY_NAME_KEY, NameDisplayType.FIRST.toString());
			}
			else
			{
				xml.setNode(DISPLAY_NAME_KEY, NameDisplayType.LAST.toString());
			}
		}

		sectionElement.setConfiguration(xml.toString());
		super.save(sectionElement);
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges() | super.hasDetectedChanges();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setClientService(ClientService service)
	{
		// nothing here
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// nothing here
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		// nothing here
	}

	private void enableOptions(boolean enable)
	{
		firstAndLast.setEnabled(enable);
		firstOnly.setEnabled(enable);
		lastOnly.setEnabled(enable);
		suppressUsername.setEnabled(enable);
		allowAnonymous.setEnabled(enable);
	}

	protected class EnableOptions implements ActionListener
	{
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			enableOptions(true);
		}
	}

	protected class DisableOptions implements ActionListener
	{
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			enableOptions(false);
		}
	}
}
