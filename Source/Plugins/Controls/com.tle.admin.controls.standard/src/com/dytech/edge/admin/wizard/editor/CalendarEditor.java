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

package com.dytech.edge.admin.wizard.editor;

import java.awt.Rectangle;
import java.text.ParseException;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dytech.edge.admin.wizard.ReloadHandler;
import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.control.Calendar;
import com.dytech.edge.wizard.beans.control.Calendar.DateFormat;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.dytech.gui.TableLayout;
import com.tle.admin.gui.common.DateSelector;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.MultiTargetChooser;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.applet.gui.JGroup;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.i18n.BundleCache;

@SuppressWarnings("nls")
public class CalendarEditor extends AbstractPowerSearchControlEditor<Calendar>
{
	private static final long serialVersionUID = 1L;

	// see Jira Defect TLE-1840 : http://apps.dytech.com.au/jira/browse/TLE-1840
	private static final int[] DAYS = {0, 1, 2, 3, 5, 7, 14, 28};
	// see Jira Defect TLE-2038 : http://apps.dytech.com.au/jira/browse/TLE-2038

	private MultiTargetChooser picker;
	private I18nTextField title;
	private I18nTextField description;
	private JCheckBox mandatory;
	private JCheckBox range;
	private JCheckBox reload;

	private ButtonGroup defaultButtons;
	private JComboBox dayCombo;
	private JGroup defaultGroup;
	private JRadioButton dateButton;
	private JRadioButton todayButton;
	private DateSelector dateSelector;

	private ButtonGroup formatGroup;
	private JRadioButton dayMonthYear;
	private JRadioButton monthYear;
	private JRadioButton year;

	public CalendarEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);
	}

	@Override
	protected void loadControl()
	{
		Calendar control = getWizardControl();

		title.load(control.getTitle());
		description.load(control.getDescription());
		mandatory.setSelected(control.isMandatory());
		reload.setSelected(control.isReload());

		// Range will not exist for powersearches.
		if( range != null )
		{
			range.setSelected(control.isRange());
		}

		if( control.getFormat() != null )
		{
			switch( control.getFormat() )
			{
				case DMY:
					dayMonthYear.setSelected(true);
					break;
				case MY:
					monthYear.setSelected(true);
					break;
				case Y:
					year.setSelected(true);
					break;
				default:
					dayMonthYear.setSelected(true);

			}
		}

		WizardHelper.loadSchemaChooser(picker, control);

		loadDefaultValue(control);

		super.loadControl();
	}

	@Override
	protected void saveControl()
	{
		Calendar control = getWizardControl();

		control.setTitle(title.save());
		control.setDescription(description.save());
		control.setMandatory(mandatory.isSelected());
		control.setReload(reload.isSelected());

		ButtonModel selectedFormat = formatGroup.getSelection();
		if( selectedFormat == dayMonthYear.getModel() )
		{
			control.setFormat(DateFormat.DMY);
		}
		else if( selectedFormat == monthYear.getModel() )
		{
			control.setFormat(DateFormat.MY);
		}
		else
		{
			control.setFormat(DateFormat.Y);
		}

		// Range should always be true if this is an advanced search
		boolean isPowerSearch = getWizardType() == WizardHelper.WIZARD_TYPE_POWERSEARCH;
		control.setRange(isPowerSearch || range.isSelected());

		WizardHelper.saveSchemaChooser(picker, control);

		saveDefaultValue(control);

		super.saveControl();
	}

	@Override
	protected void setupGUI()
	{
		setShowScripting(true);

		picker = WizardHelper.createMultiTargetChooser(this);

		addSection(createDetails());
		addSection(WizardHelper.createMetaData(picker));

		super.setupGUI();
	}

	private JComponent createDetails()
	{
		JLabel titleLabel = new JLabel(CurrentLocale.get("wizard.controls.title"));
		JLabel descriptionLabel = new JLabel(CurrentLocale.get("wizard.controls.description"));

		title = new I18nTextField(BundleCache.getLanguages());
		description = new I18nTextField(BundleCache.getLanguages());

		JComponent dateSelection = generateDefaultDate();

		final String dateLangPfx = "com.tle.admin.controls.standard.date.format";
		formatGroup = new ButtonGroup();

		JLabel formatLabel = new JLabel(CurrentLocale.get(dateLangPfx));
		dayMonthYear = new JRadioButton(CurrentLocale.get(dateLangPfx + ".full"));
		formatGroup.add(dayMonthYear);
		monthYear = new JRadioButton(CurrentLocale.get(dateLangPfx + ".month"));
		formatGroup.add(monthYear);
		year = new JRadioButton(CurrentLocale.get(dateLangPfx + ".year"));
		formatGroup.add(year);
		dayMonthYear.setSelected(true);// default

		mandatory = new JCheckBox(CurrentLocale.get("wizard.controls.mandatory"));

		reload = new JCheckBox(CurrentLocale.get("wizard.controls.reload"));
		reload.addActionListener(new ReloadHandler(reload));

		final int width1 = formatLabel.getPreferredSize().width;
		final int height1 = title.getPreferredSize().height;
		final int height2 = dateSelection.getPreferredSize().height;
		final int height3 = dayMonthYear.getPreferredSize().height;

		final int[] rows = {height1, height1, height2, height1, height3, height3, height3, height1, height1,
				TableLayout.PREFERRED,};
		final int[] cols = {width1, TableLayout.FILL,};

		JPanel all = new JPanel(new TableLayout(rows, cols, 5, 5));

		int row = -1;
		all.add(titleLabel, new Rectangle(0, ++row, 1, 1));
		all.add(title, new Rectangle(1, row, 1, 1));

		all.add(descriptionLabel, new Rectangle(0, ++row, 1, 1));
		all.add(description, new Rectangle(1, row, 1, 1));

		all.add(dateSelection, new Rectangle(0, ++row, 2, 1));
		// TODO: format so it doesn't look so ugly
		all.add(formatLabel, new Rectangle(0, ++row, 1, 1));
		all.add(dayMonthYear, new Rectangle(1, ++row, 1, 1));
		all.add(monthYear, new Rectangle(1, ++row, 1, 1));
		all.add(year, new Rectangle(1, ++row, 1, 1));

		all.add(mandatory, new Rectangle(0, ++row, 2, 1));

		all.add(reload, new Rectangle(0, ++row, 2, 1));

		if( getWizardType() != WizardHelper.WIZARD_TYPE_POWERSEARCH )
		{
			range = new JCheckBox(CurrentLocale.get("wizard.controls.timerange"));
			all.add(range, new Rectangle(0, ++row, 2, 1));
		}

		return all;
	}

	private JComponent generateDefaultDate()
	{
		defaultButtons = new ButtonGroup();
		defaultGroup = new JGroup(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.defaultdate"));

		dateButton = new JRadioButton();
		todayButton = new JRadioButton();
		defaultButtons.add(todayButton);
		defaultButtons.add(dateButton);
		dateButton.setSelected(true);

		dayCombo = new JComboBox(getDayNames());

		dateSelector = new DateSelector();

		final int width1 = todayButton.getPreferredSize().width;
		final int height1 = dateSelector.getPreferredSize().height;
		final int height2 = title.getPreferredSize().height;

		final int[] rows = {height1, height2};
		final int[] cols = {width1, TableLayout.FILL, TableLayout.DOUBLE_FILL};

		defaultGroup.setInnerLayout(new TableLayout(rows, cols, 5, 5));
		int row = -1;

		defaultGroup.addInner(dateButton, new Rectangle(0, ++row, 1, 1));
		defaultGroup.addInner(dateSelector, new Rectangle(1, row, 2, 1));

		defaultGroup.addInner(todayButton, new Rectangle(0, ++row, 1, 1));
		defaultGroup.addInner(dayCombo, new Rectangle(1, row, 1, 1));

		defaultGroup.setSelected(false);
		return defaultGroup;
	}

	private void loadDefaultValue(Calendar control)
	{
		String defaultValue = null;

		// Check if there are any items.
		boolean hasDefault = !control.getItems().isEmpty();
		if( hasDefault )
		{
			WizardControlItem item = control.getItems().get(0);
			defaultValue = item.getValue();

			hasDefault = defaultValue != null && defaultValue.length() > 0;
		}

		if( hasDefault )
		{
			try
			{
				int value = Integer.parseInt(defaultValue);
				int index = -1;
				for( int i = 0; index < 0 && i < DAYS.length; i++ )
				{
					if( DAYS[i] == value )
					{
						index = i;
					}
				}
				dayCombo.setSelectedIndex(index);
				defaultButtons.setSelected(todayButton.getModel(), true);
			}
			catch( Exception e )
			{
				try
				{
					UtcDate d = UtcDate.conceptualDate(defaultValue);
					dateSelector.setDate(d.toDate());
				}
				catch( ParseException ex )
				{
					// Legacy, ISO_MIDNIGHT
					try
					{
						UtcDate d = new UtcDate(defaultValue, Dates.ISO_MIDNIGHT);
						dateSelector.setDate(d.toDate());
					}
					catch( ParseException ex2 )
					{
						// We just leave it in the display format
					}
				}
				defaultButtons.setSelected(dateButton.getModel(), true);
			}
		}
		defaultGroup.setSelected(hasDefault);
	}

	private void saveDefaultValue(Calendar control)
	{
		WizardControlItem item = new WizardControlItem();
		if( defaultGroup.isSelected() )
		{
			if( defaultButtons.getSelection().equals(dateButton.getModel()) )
			{
				if( dateSelector.getDate() != null )
				{
					item.setValue(new UtcDate(dateSelector.getDate()).format(Dates.ISO_DATE_ONLY));
				}
			}
			else
			{
				item.setValue(Integer.toString(DAYS[dayCombo.getSelectedIndex()]));
			}
		}

		control.getItems().clear();
		control.getItems().add(item);

		// We need to add the item again for ranges
		if( range != null && range.isSelected() )
		{
			control.getItems().add(item);
		}
	}

	public static String[] getDayNames()
	{
		return new String[]{CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today1"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today2"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today3"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today5"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today1wk"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today2wk"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.editor.calendareditor.today1mth")};
	}
}