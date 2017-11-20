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

package com.tle.admin.itemdefinition;

import java.awt.Component;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.gui.common.JAdminSpinner;
import com.tle.admin.gui.common.ListWithViewInterface;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.admin.schema.SingleTargetChooser;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.common.NameValue;
import com.tle.common.applet.gui.AppletGuiUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

/**
 * Created on Dec 4, 2003
 * 
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class DisplayNodePanel extends JPanel implements ListWithViewInterface<DisplayNode>
{
	private static final long serialVersionUID = 1L;
	private final SchemaModel model;
	private final boolean extrasVisible;

	private SingleTargetChooser picker;
	private I18nTextField title;
	private JTextField splitter;
	private JComboBox type;
	private JComboBox mode;
	private ChangeDetector changeDetector;
	private JAdminSpinner truncateLength;

	public DisplayNodePanel(final SchemaModel model, final boolean extrasVisible)
	{
		this.model = model;
		this.extrasVisible = extrasVisible;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
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
	public void addNameListener(final KeyListener listener)
	{
		title.addKeyListener(listener);
	}

	@Override
	public void save(final DisplayNode element)
	{
		LanguageBundle titleBundle = title.save();
		String pickerTarget = picker.getTarget();
		String typeValue = type.getSelectedItem() != null ? ((NameValue) type.getSelectedItem()).getValue() : null;

		element.setTitle(titleBundle);
		element.setNode(pickerTarget);
		element.setSplitter(splitter.getText());
		element.setTruncateLength(truncateLength.getIntValue());
		element.setType(typeValue);
		if( mode.isVisible() && mode.getSelectedItem() != null )
		{
			element.setMode(((NameValue) mode.getSelectedItem()).getValue());
		}
	}

	@Override
	public void load(final DisplayNode element)
	{
		title.load(element.getTitle());
		picker.setTarget(element.getNode());
		splitter.setText(element.getSplitter());
		final Integer trunc = element.getTruncateLength();
		truncateLength.set((trunc == null ? 355 : trunc), 0);
		if( mode.isVisible() )
		{
			AppletGuiUtils.selectInJCombo(mode, new NameValue(null, element.getMode()));
		}
		AppletGuiUtils.selectInJCombo(type, new NameValue(null, element.getType()));
	}

	@Override
	public void setup()
	{
		final JLabel titleLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.title"));
		final JLabel displayLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.mode"));
		final JLabel targetLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.target"));
		final JLabel typeLabel = new JLabel(CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.type"));
		final JLabel splitterLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.splitter"));
		final JLabel truncateLabel = new JLabel(
			CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel.truncate"));

		title = new I18nTextField(BundleCache.getLanguages());
		splitter = new JTextField();
		mode = new JComboBox(getNameList("single", "double"));
		type = new JComboBox(getNameList("text", "date", "html", "url"));
		picker = new SingleTargetChooser(model, null);
		truncateLength = new JAdminSpinner(0, 0, Integer.MAX_VALUE, 1);


		setLayout(new MigLayout("wrap 2", "[fill][fill,grow]"));

		add(titleLabel);
		add(title);

		add(splitterLabel);
		add(splitter);

		add(targetLabel);
		add(picker);

		add(typeLabel);
		add(type);

		add(displayLabel);
		add(mode);

		add(truncateLabel);
		add(truncateLength);

		changeDetector = new ChangeDetector();
		changeDetector.watch(title);
		changeDetector.watch(splitter);
		changeDetector.watch(mode);
		changeDetector.watch(type);
		changeDetector.watch(picker);
		changeDetector.watch(truncateLength);

		if( !extrasVisible )
		{
			displayLabel.setVisible(false);
			mode.setVisible(false);
		}
	}

	private static NameValue[] getNameList(final String... values)
	{
		final List<NameValue> nvs = new ArrayList<NameValue>(values.length);
		for( final String value : values )
		{
			nvs.add(new NameValue(CurrentLocale.get("com.tle.admin.itemdefinition.displaynodepanel." + value), value));
		}
		return nvs.toArray(new NameValue[nvs.size()]);
	}
}
