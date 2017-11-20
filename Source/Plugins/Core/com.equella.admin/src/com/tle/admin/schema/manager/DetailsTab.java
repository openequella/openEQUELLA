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

package com.tle.admin.schema.manager;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.dytech.gui.TableLayout;
import com.tle.admin.baseentity.BaseEntityEditor.AbstractDetailsTab;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.gui.EditorException;
import com.tle.admin.gui.i18n.I18nTextArea;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.beans.entity.Schema;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.recipientselector.SingleUserSelector;
import com.tle.core.remoting.RemoteUserService;
import com.tle.i18n.BundleCache;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class DetailsTab extends BaseEntityTab<Schema> implements AbstractDetailsTab<Schema>
{
	private final SchemaModel model;

	private I18nTextField name;
	private I18nTextArea description;
	private SingleUserSelector owner;
	private SingleTargetChooser nameXpathChooser;
	private SingleTargetChooser descriptionXpathChooser;

	public DetailsTab(SchemaModel model)
	{
		this.model = model;
	}

	@Override
	public void init(Component parent)
	{
		setupGUI();
	}

	@Override
	public String getTitle()
	{
		return CurrentLocale.get("com.tle.admin.schema.manager.detailstab.title");
	}

	private void setupGUI()
	{
		JLabel nameLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.name"));
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.desc"));
		JLabel ownerLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.owner"));
		JLabel nameXpathLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.path"));
		JLabel descriptionXpathLabel = new JLabel(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.descpath"));

		descriptionLabel.setVerticalAlignment(SwingConstants.TOP);

		name = new I18nTextField(BundleCache.getLanguages());
		nameLabel.setLabelFor(name);

		description = new I18nTextArea(BundleCache.getLanguages());
		descriptionLabel.setLabelFor(description);

		owner = new SingleUserSelector(clientService.getService(RemoteUserService.class));
		ownerLabel.setLabelFor(owner);

		nameXpathChooser = new SingleTargetChooser(model, null);
		descriptionXpathChooser = new SingleTargetChooser(model, null);
		nameXpathLabel.setLabelFor(nameXpathChooser);
		descriptionXpathLabel.setLabelFor(descriptionXpathChooser);

		final int height1 = descriptionXpathChooser.getPreferredSize().height;
		final int ownerHeight = owner.getPreferredSize().height;
		final int width1 = descriptionXpathLabel.getPreferredSize().width;

		final int[] rows = {height1, height1 * 3, ownerHeight, height1, height1, TableLayout.FILL,};
		final int[] cols = {width1, TableLayout.DOUBLE_FILL, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));

		add(nameLabel, new Rectangle(0, 0, 1, 1));
		add(name, new Rectangle(1, 0, 1, 1));

		add(descriptionLabel, new Rectangle(0, 1, 1, 1));
		add(description, new Rectangle(1, 1, 1, 1));

		add(ownerLabel, new Rectangle(0, 2, 1, 1));
		add(owner, new Rectangle(1, 2, 1, 1));

		add(nameXpathLabel, new Rectangle(0, 3, 1, 1));
		add(nameXpathChooser, new Rectangle(1, 3, 1, 1));

		add(descriptionXpathLabel, new Rectangle(0, 4, 1, 1));
		add(descriptionXpathChooser, new Rectangle(1, 4, 1, 1));

		// Make sure things are readonly.
		if( state.isReadonly() )
		{
			name.setEnabled(false);
			description.setEnabled(false);
			owner.setEnabled(false);
			nameXpathChooser.setEnabled(false);
			descriptionXpathChooser.setEnabled(false);
		}
	}

	@Override
	public void addNameListener(KeyListener listener)
	{
		name.addKeyListener(listener);
	}

	@Override
	public void load()
	{
		Schema schema = state.getEntity();

		name.load(schema.getName());
		description.load(schema.getDescription());
		owner.setUserId(schema.getOwner());
		nameXpathChooser.setTarget(schema.getItemNamePath());
		descriptionXpathChooser.setTarget(schema.getItemDescriptionPath());
	}

	@Override
	public void save()
	{
		Schema schema = state.getEntity();

		schema.setName(name.save());
		schema.setDescription(description.save());
		schema.setOwner(owner.getUser().getUniqueID());
		schema.setItemNamePath(nameXpathChooser.getTarget());
		schema.setItemDescriptionPath(descriptionXpathChooser.getTarget());
	}

	@Override
	public void validation() throws EditorException
	{
		if( name.isCompletelyEmpty() )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.supplyname"));
		}

		if( nameXpathChooser.getTarget().length() == 0 )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.chooseforname"));
		}

		if( descriptionXpathChooser.getTarget().length() == 0 )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.choosefordesc"));
		}

		if( model.getNode(nameXpathChooser.getTarget()) == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.choosenewname"));
		}

		if( model.getNode(descriptionXpathChooser.getTarget()) == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.choosenewdesc"));
		}

		if( owner.getUser() == null )
		{
			throw new EditorException(CurrentLocale.get("com.tle.admin.schema.manager.detailstab.noowner"));
		}
	}
}
