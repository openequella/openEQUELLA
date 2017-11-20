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

import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_KEY;
import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_STRUCTURED;
import static com.tle.common.collection.AttachmentConfigConstants.DISPLAY_MODE_THUMBNAIL;
import static com.tle.common.collection.AttachmentConfigConstants.METADATA_TARGET;
import static com.tle.common.collection.AttachmentConfigConstants.SHOW_FULLSCREEN_LINK_KEY;
import static com.tle.common.collection.AttachmentConfigConstants.SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.miginfocom.swing.MigLayout;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.ChangeDetector;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class AttachmentsSectionConfig extends AbstractOnlyTitleConfig
{
	private static final long serialVersionUID = 1L;

	private JCheckBox showFull;
	private JCheckBox showFullNewWindow;
	private JRadioButton modeStructured;
	private JRadioButton modeThumbnail;
	private MultiTargetChooser metadataTarget;

	private SchemaModel schemaModel;

	private ChangeDetector changeDetector;

	@Override
	public void setup()
	{
		showFull = new JCheckBox(s("showfullscreen"));
		showFullNewWindow = new JCheckBox(s("showfullscreen.newwindow"));
		modeStructured = new JRadioButton(s("displaymode.structured.title"));
		modeThumbnail = new JRadioButton(s("displaymode.thumbnail.title"));
		metadataTarget = new MultiTargetChooser(schemaModel, "");

		ButtonGroup bg = new ButtonGroup();
		bg.add(modeStructured);
		bg.add(modeThumbnail);

		changeDetector = new ChangeDetector();
		changeDetector.watch(showFull);
		changeDetector.watch(showFullNewWindow);
		changeDetector.watch(modeStructured);
		changeDetector.watch(modeThumbnail);
		changeDetector.watch(metadataTarget);

		setLayout(new MigLayout("wrap", "[]"));

		super.setup();

		add(new JLabel(s("fullscreen")), "gaptop 2u");
		add(showFull, "gap 2i");
		add(showFullNewWindow, "gap 2i");

		add(new JLabel(s("displaymode")), "gaptop 2u");

		add(modeStructured, "gap i*2");
		add(new JLabel("<html>" + s("displaymode.structured.description")), "gap i*4");

		add(modeThumbnail, "gap i*2");
		add(new JLabel("<html>" + s("displaymode.thumbnail.description")), "gap i*4");

		add(new JLabel(s("restrict")), "gaptop 2u, grow");
		add(new JLabel("<html>" + s("restrict.help")), "gap 2i");
		add(metadataTarget, "grow, gap 2i");
	}

	@Override
	public boolean showTitleHelp()
	{
		return true;
	}

	@Override
	public void load(SummarySectionsConfig element)
	{
		String config = element.getConfiguration();
		super.load(element);
		if( !Check.isEmpty(config) )
		{
			PropBagEx xml = new PropBagEx(config);
			showFull.setSelected(xml.isNodeTrue(SHOW_FULLSCREEN_LINK_KEY));
			showFullNewWindow.setSelected(xml.isNodeTrue(SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY));
			(xml.getNode(DISPLAY_MODE_KEY).equals(DISPLAY_MODE_THUMBNAIL) ? modeThumbnail : modeStructured)
				.setSelected(true);
			metadataTarget.setTargets(xml.getNodeList(METADATA_TARGET));
		}
		else
		{
			showFull.setSelected(true);
			showFullNewWindow.setSelected(false);
			modeStructured.setSelected(true);
		}
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		PropBagEx xml = new PropBagEx();
		xml.setNode(SHOW_FULLSCREEN_LINK_KEY, showFull.isSelected());
		xml.setNode(SHOW_FULLSCREEN_LINK_NEW_WINDOW_KEY, showFullNewWindow.isSelected());
		xml.setNode(DISPLAY_MODE_KEY, modeThumbnail.isSelected() ? DISPLAY_MODE_THUMBNAIL : DISPLAY_MODE_STRUCTURED);
		xml.deleteAll(METADATA_TARGET);
		for( String target : metadataTarget.getTargets() )
		{
			xml.createNode(METADATA_TARGET, target);
		}

		element.setConfiguration(xml.toString());
		super.save(element);
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		this.schemaModel = model;
	}

	@Override
	public void setClientService(ClientService service)
	{
		// Nothing to do here
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// Nothing to do hereF
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

	private static final String s(String keypart)
	{
		return CurrentLocale.get("com.tle.admin.collection.tool.summarysections.attachments." + keypart);
	}
}
