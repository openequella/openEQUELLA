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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.gui.common.JAdminSpinner;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

public class BasicConfig extends AbstractOnlyTitleConfig implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private static String SHOW_OWNER_KEY = "owner"; //$NON-NLS-1$
	private static String TITLE_LENGTH_KEY = "title"; //$NON-NLS-1$
	private static String DESCRIPTION_LENGTH_KEY = "description"; //$NON-NLS-1$

	private ChangeDetector changeDetector;
	private JAdminSpinner maxLengthTitle;
	private JAdminSpinner maxLengthDesc;
	private JCheckBox limitTitle;
	private JCheckBox limitDesc;

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges() | super.hasDetectedChanges();
	}

	@Override
	public void setup()
	{
		setLayout(new MigLayout());
		super.setup();

		limitDesc = new JCheckBox(
			CurrentLocale.get("com.tle.admin.collection.tool.summarysections.basic.limitdesclabel")); //$NON-NLS-1$
		maxLengthDesc = new JAdminSpinner(355, 10, 10000, 5);

		limitTitle = new JCheckBox(
			CurrentLocale.get("com.tle.admin.collection.tool.summarysections.basic.limittitlelabel")); //$NON-NLS-1$
		maxLengthTitle = new JAdminSpinner(200, 10, 10000, 5);

		limitDesc.addActionListener(this);
		limitTitle.addActionListener(this);

		add(limitTitle, "wrap"); //$NON-NLS-1$
		add(new JLabel(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.basic.maxlabel")), //$NON-NLS-1$
			"split 2"); //$NON-NLS-1$
		add(maxLengthTitle, "wrap"); //$NON-NLS-1$

		add(limitDesc, "wrap"); //$NON-NLS-1$
		add(new JLabel(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.basic.maxlabel")), //$NON-NLS-1$
			"split 2"); //$NON-NLS-1$
		add(maxLengthDesc, "wrap"); //$NON-NLS-1$

		changeDetector = new ChangeDetector();
		changeDetector.watch(maxLengthDesc);
		changeDetector.watch(limitDesc);
		changeDetector.watch(maxLengthTitle);
		changeDetector.watch(limitTitle);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(SummarySectionsConfig element)
	{
		super.load(element);
		if( element.getConfiguration() != null )
		{
			try
			{
				XStream xstream = new XStream();
				HashMap<String, String> fromXML = (HashMap<String, String>) xstream.fromXML(element.getConfiguration());

				boolean desc = fromXML.containsKey(DESCRIPTION_LENGTH_KEY);
				if( desc )
				{
					maxLengthDesc.set(fromXML.get(DESCRIPTION_LENGTH_KEY), 355);
				}
				limitDesc.setSelected(desc);
				maxLengthDesc.setEnabled(desc);

				boolean title = fromXML.containsKey(TITLE_LENGTH_KEY);
				if( title )
				{
					maxLengthTitle.set(fromXML.get(TITLE_LENGTH_KEY), 200);
				}
				limitTitle.setSelected(title);
				maxLengthTitle.setEnabled(title);
			}
			catch( StreamException e )
			{
				limitDesc.setSelected(false);
				maxLengthDesc.setEnabled(false);

				limitTitle.setSelected(false);
				maxLengthTitle.setEnabled(false);
			}
		}
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		HashMap<String, String> settings = new HashMap<String, String>();
		settings.put(SHOW_OWNER_KEY, "true"); //$NON-NLS-1$
		if( limitDesc.isSelected() )
		{
			settings.put(DESCRIPTION_LENGTH_KEY, maxLengthDesc.getCurrentValue());
		}

		if( limitTitle.isSelected() )
		{
			settings.put(TITLE_LENGTH_KEY, maxLengthTitle.getCurrentValue());
		}

		XStream xstream = new XStream();
		String toXML = xstream.toXML(settings);
		element.setConfiguration(toXML);
		super.save(element);
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// Ignore
	}

	@Override
	public void setClientService(ClientService service)
	{
		// Ignore
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == limitDesc )
		{
			if( limitDesc.isSelected() )
			{
				maxLengthDesc.setEnabled(true);
			}
			else
			{
				maxLengthDesc.setEnabled(false);
			}
		}
		else if( e.getSource() == limitTitle )
		{
			if( limitTitle.isSelected() )
			{
				maxLengthTitle.setEnabled(true);
			}
			else
			{
				maxLengthTitle.setEnabled(false);
			}
		}
	}
}
