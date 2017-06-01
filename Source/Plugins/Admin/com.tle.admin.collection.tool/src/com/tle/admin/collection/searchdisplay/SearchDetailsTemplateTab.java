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

package com.tle.admin.collection.searchdisplay;

import java.awt.Component;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.EditorException;
import com.tle.admin.itemdefinition.AbstractItemdefTab;
import com.tle.admin.itemdefinition.DisplayNodeList;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SearchDetails;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class SearchDetailsTemplateTab extends AbstractItemdefTab
{
	public enum AttachmentDisplayOption
	{
		STRUCTURED, THUMBNAILS;
	}

	private DisplayNodeList displayNodeList;
	private ChangeDetector changeDetector;
	private SchemaModel schemaModel;

	private ButtonGroup attlistgroup;
	private JRadioButton structured;
	private JRadioButton thumbnails;
	private JLabel description;
	private JCheckBox disableThumbnail;
	private JLabel displayInfo;
	private ButtonGroup standardGroup;
	private JRadioButton standardOpen;
	private JRadioButton standardClosed;
	private ButtonGroup integrationGroup;
	private JRadioButton integrationOpen;
	private JRadioButton integrationClosed;

	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void load()
	{
		SearchDetails searchDetails = state.getEntity().getSearchDetails();
		if( searchDetails != null )
		{
			List<DisplayNode> displayNodes = searchDetails.getDisplayNodes();
			if( !Check.isEmpty(displayNodes) )
			{
				displayNodeList.load(displayNodes);
			}

			String displayType = searchDetails.getAttDisplay();
			if( !Check.isEmpty(displayType) )
			{
				AttachmentDisplayOption display = AttachmentDisplayOption.valueOf(displayType);
				if( display != null )
				{
					switch( display )
					{
						case STRUCTURED:
							structured.setSelected(true);
							break;
						case THUMBNAILS:
							thumbnails.setSelected(true);
							break;
					}
				}
			}
			else
			{
				structured.setSelected(true);
			}
			boolean disable = searchDetails.isDisableThumbnail();
			if( disable )
			{
				disableThumbnail.setSelected(true);
			}
			else
			{
				disableThumbnail.setSelected(false);

			}
			if( searchDetails.isStandardOpen() )
			{
				standardOpen.setSelected(true);
			}
			else
			{
				standardClosed.setSelected(true);
			}
			if( searchDetails.isIntegrationOpen() )
			{
				integrationOpen.setSelected(true);
			}
			else
			{
				integrationClosed.setSelected(true);
			}

		}
		else
		{
			structured.setSelected(true);
			standardClosed.setSelected(true);
			integrationClosed.setSelected(true);
		}
	}

	@Override
	public void save()
	{
		ItemDefinition itemdef = state.getEntity();
		SearchDetails searchDetails = new SearchDetails();
		List<DisplayNode> displayNodes = displayNodeList.save();
		if( !displayNodes.isEmpty() )
		{
			searchDetails.setDisplayNodes(displayNodes);
		}
		if( structured.isSelected() )
		{
			searchDetails.setAttDisplay(AttachmentDisplayOption.STRUCTURED.name());
		}
		if( thumbnails.isSelected() )
		{
			searchDetails.setAttDisplay(AttachmentDisplayOption.THUMBNAILS.name());
		}
		searchDetails.setDisableThumbnail(disableThumbnail.isSelected());
		searchDetails.setIntegrationOpen(integrationOpen.isSelected());
		searchDetails.setStandardOpen(standardOpen.isSelected());
		itemdef.setSearchDetails(searchDetails);
	}

	@Override
	public void validation() throws EditorException
	{
		List<DisplayNode> displayNodes = displayNodeList.save();

		if( displayNodes != null )
		{
			for( DisplayNode displayNode : displayNodes )
			{
				LanguageBundle titleBundle = displayNode.getTitle();
				String node = displayNode.getNode();
				String type = displayNode.getType();
				boolean notTheCaseTheyAllEmpty = !(titleBundle == null && Check.isEmpty(node) && Check.isEmpty(type));
				boolean incomplete = titleBundle == null || Check.isEmpty(node) || Check.isEmpty(type);
				// If everything's empty we ignore it (it may be being deleted)
				// but it any of the 3 critical values is present we require
				// that they all are
				if( notTheCaseTheyAllEmpty && incomplete )
				{
					throw new EditorException(
						CurrentLocale
							.get("com.tle.admin.collection.tool.searchdetailstemplatetab.validation.incomplete"));
				}
			}
		}
	}

	@Override
	public void setSchemaModel(SchemaModel schemaModel)
	{
		this.schemaModel = schemaModel;
	}

	@Override
	public String getTitle()
	{
		return s("title");
	}

	@Override
	@SuppressWarnings("deprecation")
	public void init(Component parent)
	{
		displayNodeList = new DisplayNodeList(schemaModel, false);
		description = new JLabel("<html>" + s("attviewtype.description"));
		disableThumbnail = new JCheckBox(s("attviewtype.disablethumbnails"));
		attlistgroup = new ButtonGroup();
		structured = new JRadioButton(s("attviewtype.structured"));
		thumbnails = new JRadioButton(s("attviewtype.thumbnail"));
		attlistgroup.add(structured);
		attlistgroup.add(thumbnails);
		displayInfo = new JLabel("<html>" + s("attviewtype.display.info"));
		standardOpen = new JRadioButton(s("attviewtype.display.open"));
		standardClosed = new JRadioButton(s("attviewtype.display.closed"));
		standardGroup = new ButtonGroup();
		standardGroup.add(standardOpen);
		standardGroup.add(standardClosed);
		integrationGroup = new ButtonGroup();
		integrationOpen = new JRadioButton(s("attviewtype.display.open"));
		integrationClosed = new JRadioButton(s("attviewtype.display.closed"));
		integrationGroup.add(integrationOpen);
		integrationGroup.add(integrationClosed);

		setLayout(new MigLayout("wrap", "[fill,grow]"));
		add(disableThumbnail);
		add(new JLabel(s("attviewtype.disablethumbnails.help")), "gap i*2.3");
		add(new JSeparator(), "gaptop 10");

		add(description, "gaptop 10");
		add(structured);
		add(new JLabel("<html>" + s("attviewtype.structured.info")), "gap i*2.3");
		add(thumbnails);
		add(new JLabel("<html>" + s("attviewtype.thumbnail.info")), "gap i*2.3");
		add(new JSeparator(), "gaptop 10");

		add(displayInfo, "gaptop 10");
		add(new JLabel("<html>" + s("attviewtype.display.standard")), "gap i*2, width 200!, split 3");
		add(standardOpen, "width 80!");
		add(standardClosed);
		add(new JLabel("<html>" + s("attviewtype.display.integration")), "gap i*2, width 200!, split 3");
		add(integrationOpen, "width 80!");
		add(integrationClosed);
		add(new JLabel(s("attviewtype.display.help")));
		add(new JSeparator(), "gaptop 10");
		add(displayNodeList);

		changeDetector = new ChangeDetector();
		changeDetector.watch(displayNodeList);
	}

	private String s(String key)
	{
		return CurrentLocale.get("com.tle.admin.itemdefinition.searchdetailstemplatetab." + key);
	}
}
