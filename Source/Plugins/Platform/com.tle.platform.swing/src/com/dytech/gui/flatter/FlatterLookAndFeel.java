/*
 * Copyright 2019 Apereo
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

package com.dytech.gui.flatter;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.dytech.gui.JShuffleBox;
import com.dytech.gui.VerticalFlowLayout;

public class FlatterLookAndFeel extends MetalLookAndFeel
{
	/*
	 * The following lines exist to help out class dependency tools.
	 */
	public static final FlatterButtonUI dependsGrabber01 = new FlatterButtonUI();
	public static final FlatterCheckBoxUI dependsGrabber02 = new FlatterCheckBoxUI();
	public static final FlatterComboBoxUI dependsGrabber03 = new FlatterComboBoxUI();
	public static final FlatterRadioButtonUI dependsGrabber04 = new FlatterRadioButtonUI();
	public static final FlatterScrollBarUI dependsGrabber05 = new FlatterScrollBarUI();
	public static final FlatterSeparatorUI dependsGrabber06 = new FlatterSeparatorUI();
	public static final FlatterTabbedPaneUI dependsGrabber07 = new FlatterTabbedPaneUI();
	public static final FlatterSpinnerUI dependsGrabber08 = new FlatterSpinnerUI();

	public static final String ID = "Flatter";
	public static final String NAME = "Dytech Flatter L&F";
	public static final String DESCRIPTION = "A complete revision of the original com.dytech.gui.Flat L&F";
	private Object[] defaults;
	public static final List<String> listData = Collections.unmodifiableList(Arrays.asList("one", "two", "three",
		"four", "five", "six", "seven", "eight", "nine", "ten", "Fifty Two Thousand!"));

	public FlatterLookAndFeel()
	{
		// Nothing to do here
	}

	@Override
	public String getID()
	{
		return ID;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public String getDescription()
	{
		return DESCRIPTION;
	}

	@Override
	public boolean isSupportedLookAndFeel()
	{
		return true;
	}

	@Override
	public boolean isNativeLookAndFeel()
	{
		return false;
	}

	@Override
	public boolean getSupportsWindowDecorations()
	{
		return false;
	}

	@Override
	protected void initComponentDefaults(UIDefaults table)
	{
		super.initComponentDefaults(table);

		Object[] defaultSettings = {"Button.background", FlatterDefaults.Button.BackgroundNormal,
				"Button.backgroundPressed",
				FlatterDefaults.Button.BackgroundPressed, "Button.backgroundActive",
				FlatterDefaults.Button.BackgroundActive, "Button.textNormal", FlatterDefaults.Button.TextNormal,
				"Button.textPressed", FlatterDefaults.Button.TextPressed, "Button.textActive",
				FlatterDefaults.Button.TextActive, "Button.textDisabled", FlatterDefaults.Button.TextDisabled,
				"Button.font", FlatterDefaults.Button.Font, "Button.border", FlatterDefaults.Button.Border,

				"CheckBox.font", FlatterDefaults.CheckBox.Font, "CheckBox.background",
				FlatterDefaults.CheckBox.BackgroundNormal, "CheckBox.backgroundPressed",
				FlatterDefaults.CheckBox.BackgroundPressed, "CheckBox.backgroundActive",
				FlatterDefaults.CheckBox.BackgroundActive, "CheckBox.textNormal", FlatterDefaults.CheckBox.TextNormal,
				"CheckBox.textActive", FlatterDefaults.CheckBox.TextActive, "CheckBox.textPressed",
				FlatterDefaults.CheckBox.TextPressed, "CheckBox.textDisabled", FlatterDefaults.CheckBox.TextDisabled,
				"CheckBox.iconChecked", FlatterDefaults.CheckBox.IconChecked, "CheckBox.iconUnchecked",
				FlatterDefaults.CheckBox.IconUnchecked, "CheckBox.iconPressedChecked",
				FlatterDefaults.CheckBox.IconChecked, "CheckBox.iconPressedUnchecked",
				FlatterDefaults.CheckBox.IconUnchecked, "CheckBox.textIconGap", FlatterDefaults.CheckBox.TextIconGap,

				"CheckBoxMenuItem.font", FlatterDefaults.CheckBoxMenuItem.Font, "CheckBoxMenuItem.background",
				FlatterDefaults.CheckBoxMenuItem.Background, "CheckBoxMenuItem.foreground",
				FlatterDefaults.CheckBoxMenuItem.Foreground, "CheckBoxMenuItem.border",
				FlatterDefaults.CheckBoxMenuItem.Border, "CheckBoxMenuItem.selectionBackground",
				FlatterDefaults.CheckBoxMenuItem.SelectionBackground, "CheckBoxMenuItem.checkIcon",
				FlatterDefaults.CheckBoxMenuItem.CheckIcon,

				"ComboBox.font", FlatterDefaults.ComboBox.Font, "ComboBox.background",
				FlatterDefaults.ComboBox.Background, "ComboBox.foreground", FlatterDefaults.ComboBox.Foreground,
				"ComboBox.border", FlatterDefaults.ComboBox.Border, "ComboBox.disabledBackground",
				FlatterDefaults.ComboBox.Background, "ComboBox.disabledForeground",
				FlatterDefaults.ComboBox.ForegroundDisabled, "ComboBox.selectedBackground",
				FlatterDefaults.ComboBox.BackgroundSelected, "ComboBox.selectedForeground",
				FlatterDefaults.ComboBox.ForegroundSelected, "ComboBox.selectionBackground",
				FlatterDefaults.ComboBox.BackgroundSelected, "ComboBox.selectionForeground",
				FlatterDefaults.ComboBox.ForegroundSelected, "ComboBox.width",
				FlatterDefaults.ComboBox.Width,

				"Label.font",
				FlatterDefaults.Label.Font,
				"Label.background",
				FlatterDefaults.Label.Background,
				"Label.foreground",
				FlatterDefaults.Label.Foreground,

				"List.background",
				FlatterDefaults.List.Background,
				"List.foreground",
				FlatterDefaults.List.Foreground,
				"List.font",
				FlatterDefaults.List.Font,
				"List.selectionForeground",
				FlatterDefaults.List.ForegroundSelected,
				"List.selectionBackground",
				FlatterDefaults.List.BackgroundSelected,
				"List.focusCellHighlightBorder",
				FlatterDefaults.List.Border,

				"Menu.background",
				FlatterDefaults.Menu.Background,
				"Menu.foreground",
				FlatterDefaults.Menu.Foreground,
				"Menu.selectionBackground",
				FlatterDefaults.Menu.BackgroundSelected,
				"Menu.selectionForeground",
				FlatterDefaults.Menu.ForegroundSelected,
				"Menu.font",
				FlatterDefaults.Menu.Font,
				"Menu.border",
				FlatterDefaults.Menu.Border,

				"Menu.submenuPopupOffsetX",
				FlatterDefaults.Menu.SubmenuPopupOffsetX,
				"Menu.submenuPopupOffsetY",
				FlatterDefaults.Menu.SubmenuPopupOffsetY,

				"MenuBar.background",
				FlatterDefaults.MenuBar.Background,
				"MenuBar.foreground",
				FlatterDefaults.MenuBar.Foreground,
				"MenuBar.font",
				FlatterDefaults.MenuBar.Font,
				"MenuBar.border",
				FlatterDefaults.MenuBar.Border,

				"MenuItem.background",
				FlatterDefaults.MenuBar.Background,
				"MenuItem.foreground",
				FlatterDefaults.MenuBar.Foreground,
				"MenuItem.selectionBackground",
				FlatterDefaults.Menu.BackgroundSelected,
				"MenuItem.selectionForeground",
				FlatterDefaults.Menu.ForegroundSelected,
				"MenuItem.font",
				FlatterDefaults.MenuBar.Font,
				"MenuItem.border",
				FlatterDefaults.MenuItem.Border,
				"MenuItem.acceleratorForeground",
				FlatterDefaults.MenuItem.AcceleratorForeground,
				// "MenuItem.margin", new Ins

				"PopupMenu.border", FlatterDefaults.PopupMenu.Border,

				"Panel.font", FlatterDefaults.Panel.Font, "Panel.background", FlatterDefaults.Panel.Background,
				"Panel.foreground", FlatterDefaults.Panel.Foreground,

				"PasswordField.font", FlatterDefaults.TextField.Font, "PasswordField.background",
				FlatterDefaults.TextField.Background, "PasswordField.selectionBackground",
				FlatterDefaults.TextField.BackgroundSelected, "PasswordField.foreground",
				FlatterDefaults.TextField.TextNormal, "PasswordField.inactiveForeground",
				FlatterDefaults.TextField.TextDisabled, "PasswordField.selectionForeground",
				FlatterDefaults.TextField.TextSelected, "PasswordField.border", FlatterDefaults.TextField.Border,

				"RadioButton.font", FlatterDefaults.RadioButton.Font, "RadioButton.background",
				FlatterDefaults.RadioButton.BackgroundNormal, "RadioButton.backgroundPressed",
				FlatterDefaults.RadioButton.BackgroundPressed, "RadioButton.backgroundActive",
				FlatterDefaults.RadioButton.BackgroundActive, "RadioButton.textNormal",
				FlatterDefaults.RadioButton.TextNormal, "RadioButton.textActive",
				FlatterDefaults.RadioButton.TextActive, "RadioButton.textPressed",
				FlatterDefaults.RadioButton.TextPressed, "RadioButton.textDisabled",
				FlatterDefaults.RadioButton.TextDisabled, "RadioButton.iconChecked",
				FlatterDefaults.RadioButton.IconChecked, "RadioButton.iconUnchecked",
				FlatterDefaults.RadioButton.IconUnchecked, "RadioButton.iconPressedChecked",
				FlatterDefaults.RadioButton.IconChecked, "RadioButton.iconPressedUnchecked",
				FlatterDefaults.RadioButton.IconUnchecked, "RadioButton.textIconGap",
				FlatterDefaults.RadioButton.TextIconGap,

				"RadioButtonMenuItem.font", FlatterDefaults.RadioButtonMenuItem.Font, "RadioButtonMenuItem.background",
				FlatterDefaults.RadioButtonMenuItem.Background, "RadioButtonMenuItem.foreground",
				FlatterDefaults.RadioButtonMenuItem.Foreground, "RadioButtonMenuItem.selectionBackground",
				FlatterDefaults.RadioButtonMenuItem.SelectionBackground, "RadioButtonMenuItem.border",
				FlatterDefaults.RadioButtonMenuItem.Border, "RadioButtonMenuItem.checkIcon",
				FlatterDefaults.RadioButtonMenuItem.CheckIcon,

				"ScrollBar.track", FlatterDefaults.ScrollBar.Track, "ScrollBar.thumb", FlatterDefaults.ScrollBar.Thumb,
				"ScrollBar.arrow", FlatterDefaults.ScrollBar.Arrow, "ScrollBar.width", FlatterDefaults.ScrollBar.Width,

				"ScrollPane.background", FlatterDefaults.ScrollPane.Background, "ScrollPane.foreground",
				FlatterDefaults.ScrollPane.Foreground, "ScrollPane.border", FlatterDefaults.ScrollPane.Border,
				"Viewport.background", FlatterDefaults.ScrollPane.Background,

				"Separator.foreground", FlatterDefaults.Separator.Foreground, "Separator.background",
				FlatterDefaults.Separator.Background,

				"Spinner.border", FlatterDefaults.Spinner.Border,

				"TabbedPane.font", FlatterDefaults.TabbedPane.Font,

				"Table.font", FlatterDefaults.Table.Font, "Table.background", FlatterDefaults.Table.Background,
				"Table.foreground", FlatterDefaults.Table.Foreground, "Table.gridColor", FlatterDefaults.Table.Grid,
				"Table.scrollPaneBorder", FlatterDefaults.Table.Border, "Table.selectionBackground",
				FlatterDefaults.Table.BackgroundSelected, "Table.selectionForeground",
				FlatterDefaults.Table.ForegroundSelected, "Table.focusCellBackground",
				FlatterDefaults.Table.CellBackgroundSelected, "Table.focusCellForeground",
				FlatterDefaults.Table.CellForegroundSelected, "Table.focusCellHighlightBorder",
				FlatterDefaults.Table.Border,

				"TableHeader.background", FlatterDefaults.TableHeader.Background, "TableHeader.foreground",
				FlatterDefaults.TableHeader.Foreground, "TableHeader.font", FlatterDefaults.TableHeader.Font,
				"TableHeader.cellBorder", FlatterDefaults.TableHeader.Border,

				"TextField.font", FlatterDefaults.TextField.Font, "TextField.background",
				FlatterDefaults.TextField.Background, "TextField.inactiveBackground",
				FlatterDefaults.TextField.Background, "TextField.selectionBackground",
				FlatterDefaults.TextField.BackgroundSelected, "TextField.foreground",
				FlatterDefaults.TextField.TextNormal, "TextField.inactiveForeground",
				FlatterDefaults.TextField.TextDisabled, "TextField.selectionForeground",
				FlatterDefaults.TextField.TextSelected, "TextField.border", FlatterDefaults.TextField.Border,

				"TitledBorder.font", FlatterDefaults.Font,

				"OptionPane.background", FlatterDefaults.Background,

				"ToolTip.border", FlatterDefaults.ToolTipBorder, "ToolTip.background",
				FlatterDefaults.ToolTipBackground, "ToolTip.foreground", FlatterDefaults.ToolTipForeground,};

		table.putDefaults(defaultSettings);
	}

	@Override
	protected void initClassDefaults(UIDefaults table)
	{
		super.initClassDefaults(table);

		final String packageName = "com.dytech.gui.flatter.";
		final String buttonClass = packageName + "FlatterButtonUI";
		final String checkBoxClass = packageName + "FlatterCheckBoxUI";
		final String comboBoxClass = packageName + "FlatterComboBoxUI";
		final String radioButtonClass = packageName + "FlatterRadioButtonUI";
		final String scrollBarClass = packageName + "FlatterScrollBarUI";
		final String separatorClass = packageName + "FlatterSeparatorUI";
		final String tabbedPaneClass = packageName + "FlatterTabbedPaneUI";
		final String spinnerClass = packageName + "FlatterSpinnerUI";

		try
		{
			if( defaults == null )
			{
				defaults = new Object[]{"ButtonUI", buttonClass, buttonClass, Class.forName(buttonClass), "CheckBoxUI",
						checkBoxClass, checkBoxClass, Class.forName(checkBoxClass), "ComboBoxUI", comboBoxClass,
						comboBoxClass, Class.forName(comboBoxClass), "RadioButtonUI", radioButtonClass,
						radioButtonClass, Class.forName(radioButtonClass), "ScrollBarUI", scrollBarClass,
						scrollBarClass, Class.forName(scrollBarClass), "SeparatorUI", separatorClass, separatorClass,
						Class.forName(separatorClass), "PopupMenuSeparatorUI", separatorClass, separatorClass,
						Class.forName(separatorClass), "TabbedPaneUI", tabbedPaneClass, "SpinnerUI", spinnerClass,
						spinnerClass, Class.forName(spinnerClass), tabbedPaneClass, Class.forName(tabbedPaneClass),};
			}
			table.putDefaults(defaults);
		}
		catch( ClassNotFoundException ex )
		{
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception
	{
		// JFrame.setDefaultLookAndFeelDecorated(true);
		UIManager.setLookAndFeel(new FlatterLookAndFeel());

		JPanel panel = new JPanel(new VerticalFlowLayout(false, false));

		JButton button1 = new JButton("Enabled button");
		panel.add(button1);
		button1.setToolTipText("This is button1");

		JButton button2 = new JButton("Disabled button");
		button2.setEnabled(false);
		panel.add(button2);

		JCheckBox checkbox1 = new JCheckBox("Enabled check box");
		panel.add(checkbox1);

		JCheckBox checkbox2 = new JCheckBox("Disabled check box");
		checkbox2.setEnabled(false);
		panel.add(checkbox2);

		JComboBox combobox1 = new JComboBox(listData.toArray());
		panel.add(combobox1);

		JComboBox combobox2 = new JComboBox(listData.toArray());
		combobox2.setEnabled(false);
		panel.add(combobox2);

		JLabel label = new JLabel("A label");
		panel.add(label);

		JList list = new JList(listData.toArray());
		JScrollPane scrollpane = new JScrollPane(list);
		panel.add(scrollpane);

		JShuffleBox<?> shuffleBox = new JShuffleBox<Object>(listData.toArray());
		shuffleBox.setPreferredSize(new Dimension(300, 150));
		panel.add(shuffleBox);

		JPasswordField passwordfield1 = new JPasswordField(10);
		passwordfield1.setText("pas");
		panel.add(passwordfield1);

		JPasswordField passwordfield2 = new JPasswordField(10);
		passwordfield2.setText("password");
		passwordfield2.setEnabled(false);
		panel.add(passwordfield2);

		JRadioButton radiobutton1 = new JRadioButton("Enabled radio button");
		panel.add(radiobutton1);

		JRadioButton radiobutton2 = new JRadioButton("Disabled radio button");
		radiobutton2.setEnabled(false);
		panel.add(radiobutton2);

		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(300, 20));
		panel.add(separator);

		JSpinner spinner = new JSpinner();
		separator.setPreferredSize(new Dimension(50, 50));
		panel.add(spinner);

		JTextField textfield1 = new JTextField("Active textfield", 10);
		panel.add(textfield1);

		JTextField textfield2 = new JTextField("Inactive textfield", 10);
		textfield2.setEnabled(false);
		panel.add(textfield2);

		JTextField textfield3 = new JTextField("Non-editable textfield", 10);
		textfield3.setEditable(false);
		panel.add(textfield3);

		JMenuBar menubar = new JMenuBar();

		JMenu menu1 = new JMenu("File");
		menu1.setMnemonic('f');
		menubar.add(menu1);

		menu1.add(new JMenuItem("New..."));
		menu1.add(new JMenuItem("Save..."));
		menu1.addSeparator();
		menu1.add(new JMenuItem("Exit"));

		JMenu menu2 = new JMenu("Edit");
		menu2.setMnemonic('e');
		menubar.add(menu2);

		menu2.add(new JMenuItem("Cut"));
		menu2.add(new JMenuItem("Copy"));
		menu2.add(new JMenuItem("Paste"));
		menu2.add(new JSeparator());

		JMenu menu3 = new JMenu("Preferences");
		menu2.setMnemonic('f');
		menu2.add(menu3);

		JCheckBoxMenuItem micb = new JCheckBoxMenuItem("Check 1");
		micb.setSelected(true);
		menu3.add(new JRadioButtonMenuItem("Radio 1"));
		menu3.add(new JRadioButtonMenuItem("Radio 2"));
		menu3.add(micb);

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("one", new JLabel("page 1"));
		tabs.add("two", new JLabel("page 2"));
		tabs.setMinimumSize(new Dimension(200, 100));
		tabs.setMaximumSize(new Dimension(200, 100));
		tabs.setPreferredSize(new Dimension(200, 100));
		panel.add(tabs);

		panel.setBorder(new TitledBorder("Titled border"));

		JFrame frame = new JFrame("Flatter L&F Test");
		frame.getContentPane().add(panel);
		frame.setJMenuBar(menubar);
		frame.setBounds(200, 20, 700, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		JOptionPane.showMessageDialog(frame, "Information", spinner.getUI().getClass().getName(),
			JOptionPane.INFORMATION_MESSAGE);
		JOptionPane.showMessageDialog(frame, "Question", "Question", JOptionPane.QUESTION_MESSAGE);
		JOptionPane.showMessageDialog(frame, "Warning", "Warning", JOptionPane.WARNING_MESSAGE);
		JOptionPane.showMessageDialog(frame, "Error", "Error", JOptionPane.ERROR_MESSAGE);
	}
}