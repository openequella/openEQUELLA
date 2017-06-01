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

package com.dytech.gui.flatter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.InsetsUIResource;

public abstract class FlatterDefaults
{
	// Colours:
	public static final ColorUIResource Black = new ColorUIResource(0, 0, 0);
	public static final ColorUIResource DarkerGray = new ColorUIResource(60, 60, 60);
	public static final ColorUIResource DarkGray = new ColorUIResource(100, 100, 100);
	public static final ColorUIResource MidGray = new ColorUIResource(150, 150, 150);
	public static final ColorUIResource LightGray = new ColorUIResource(214, 214, 214);
	public static final ColorUIResource LighterGray = new ColorUIResource(245, 245, 245);
	public static final ColorUIResource White = new ColorUIResource(255, 255, 255);

	// Default Colours:
	public static final ColorUIResource Foreground = DarkerGray;
	public static final ColorUIResource Text = DarkerGray;
	public static final ColorUIResource DisabledText = LightGray;
	public static final ColorUIResource SelectedText = White;
	public static final ColorUIResource Background = White;
	public static final ColorUIResource SelectedBackground = MidGray;
	public static final Color selectedTabColour1 = DarkGray;
	public static final Color unselectedTabColour1 = LightGray;

	// Fonts:
	public static final FontUIResource SerifFont = new FontUIResource("Serif", java.awt.Font.PLAIN, 12); //$NON-NLS-1$
	public static final FontUIResource SansSerifFont = new FontUIResource("SansSerif", // "Tahoma", //$NON-NLS-1$
		java.awt.Font.PLAIN, 11);
	public static final FontUIResource MonospaceFont = new FontUIResource("Monospace", java.awt.Font.PLAIN, 12); //$NON-NLS-1$
	public static final FontUIResource Font = SansSerifFont;

	// Borders and insets:
	public static final InsetsUIResource Margin = new InsetsUIResource(2, 2, 2, 2);
	public static final BorderUIResource LineBorder = new BorderUIResource(BorderFactory.createLineBorder(DarkGray, 1));
	public static final BorderUIResource MidLineBorder = new BorderUIResource(
		BorderFactory.createLineBorder(MidGray, 1));
	public static final BorderUIResource LightLineBorder = new BorderUIResource(BorderFactory.createLineBorder(
		LightGray, 1));
	public static final BorderUIResource LighterLineBorder = new BorderUIResource(BorderFactory.createLineBorder(
		LighterGray, 1));
	public static final BorderUIResource EmptyBorder = new BorderUIResource(BorderFactory.createEmptyBorder(2, 2, 2, 2));

	public static final BorderUIResource CMidLineBorder = new BorderUIResource(BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(MidGray, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	public static final BorderUIResource CLightLineBorder = new BorderUIResource(BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(LightGray, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
	public static final BorderUIResource CLighterLineBorder = new BorderUIResource(BorderFactory.createCompoundBorder(
		BorderFactory.createLineBorder(LighterGray, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));

	// ToolTip:
	public static final BorderUIResource ToolTipBorder = LineBorder;
	public static final Color ToolTipForeground = Black;
	public static final Color ToolTipBackground = LightGray;

	// Icons:
	public static final IconUIResource CheckIcon = new IconUIResource(new FlatterIcons.BoxIcon(12, 12, DarkGray,
		DarkGray));

	public abstract static class Button
	{
		public static final ColorUIResource BackgroundNormal = FlatterDefaults.Background;
		public static final ColorUIResource BackgroundPressed = FlatterDefaults.LightGray;
		public static final ColorUIResource BackgroundActive = FlatterDefaults.LighterGray;
		public static final ColorUIResource TextNormal = FlatterDefaults.Text;
		public static final ColorUIResource TextPressed = FlatterDefaults.Text;
		public static final ColorUIResource TextActive = FlatterDefaults.Text;
		public static final ColorUIResource TextDisabled = FlatterDefaults.DisabledText;
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final BorderUIResource Border = FlatterDefaults.CMidLineBorder;
	}

	public abstract static class CheckBox
	{
		public static final ColorUIResource BackgroundNormal = FlatterDefaults.Background;
		public static final ColorUIResource BackgroundPressed = FlatterDefaults.LightGray;
		public static final ColorUIResource BackgroundActive = FlatterDefaults.LighterGray;
		public static final ColorUIResource TextNormal = FlatterDefaults.Text;
		public static final ColorUIResource TextPressed = FlatterDefaults.Text;
		public static final ColorUIResource TextActive = FlatterDefaults.Text;
		public static final ColorUIResource TextDisabled = FlatterDefaults.DisabledText;
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final Integer TextIconGap = Integer.valueOf(8);

		public static final int IconHeight = 12;
		public static final int IconWidth = 12;
		public static final ColorUIResource IconColorEC = DarkGray; // Enabled
																	// and
																	// Checked
		public static final ColorUIResource IconColorDC = LightGray; // Disabled
																		// and
																		// Checked
		public static final ColorUIResource IconColorEUC = DarkGray; // Enabled
																		// and
																		// Unchecked
		public static final ColorUIResource IconColorDUC = LightGray; // Disabled
																		// and
																		// Unchecked

		public static final IconUIResource IconChecked = new IconUIResource(new FlatterIcons.BoxChecked(IconHeight,
			IconWidth, IconColorEC, IconColorDC));

		public static final IconUIResource IconUnchecked = new IconUIResource(new FlatterIcons.BoxUnchecked(IconHeight,
			IconWidth, IconColorEUC, IconColorDUC));
	}

	public abstract static class CheckBoxMenuItem
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.LighterGray;

		public static final ColorUIResource SelectionBackground = FlatterDefaults.MidGray;

		public static final int IconHeight = FlatterDefaults.CheckBox.IconHeight;
		public static final int IconWidth = FlatterDefaults.CheckBox.IconWidth;
		public static final ColorUIResource IconColorEC = FlatterDefaults.CheckBox.IconColorEC; // Enabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorDC = FlatterDefaults.CheckBox.IconColorDC; // Disabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorEUC = FlatterDefaults.CheckBox.IconColorEUC; // Enabled
																									// and
																									// Unchecked
		public static final ColorUIResource IconColorDUC = FlatterDefaults.CheckBox.IconColorDUC; // Disabled
																									// and
																									// Unchecked

		public static final IconUIResource IconChecked = FlatterDefaults.CheckBox.IconChecked;
		public static final IconUIResource IconUnchecked = FlatterDefaults.CheckBox.IconUnchecked;
		public static final IconUIResource CheckIcon = FlatterDefaults.CheckIcon;

		public static final BorderUIResource Border = FlatterDefaults.LighterLineBorder;
	}

	public abstract static class ComboBox
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.SelectedBackground;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource ForegroundDisabled = FlatterDefaults.DisabledText;
		public static final ColorUIResource ForegroundSelected = FlatterDefaults.White;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
		public static final Integer Width = Integer.valueOf(16);
	}

	public abstract static class Label
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.Background;
	}

	public abstract static class List
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource ForegroundSelected = FlatterDefaults.SelectedText;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.SelectedBackground;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
	}

	public abstract static class Menu
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.LighterGray;
		public static final ColorUIResource ForegroundSelected = FlatterDefaults.SelectedText;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.SelectedBackground;
		public static final BorderUIResource Border = null;// FlatterDefaults.LightLineBorder;

		public static final Integer SubmenuPopupOffsetX = Integer.valueOf(0);
		public static final Integer SubmenuPopupOffsetY = Integer.valueOf(-1);
	}

	public abstract static class MenuBar
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.LighterGray;
		public static final ColorUIResource ForegroundSelected = FlatterDefaults.SelectedText;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.SelectedBackground;
		public static final BorderUIResource Border = new BorderUIResource(new com.dytech.gui.LineBorder(MidGray, 0, 0,
			1, 0));
		// FlatterDefaults.LightLineBorder;
	}

	public abstract static class MenuItem
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.LighterGray;
		public static final BorderUIResource Border = FlatterDefaults.LighterLineBorder;
		public static final ColorUIResource AcceleratorForeground = FlatterDefaults.DarkGray;
	}

	public abstract static class Panel
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.Background;
	}

	public abstract static class PopupMenu
	{
		public static final BorderUIResource Border = FlatterDefaults.MidLineBorder;
	}

	public abstract static class RadioButton
	{
		public static final ColorUIResource BackgroundNormal = FlatterDefaults.CheckBox.BackgroundNormal;
		public static final ColorUIResource BackgroundPressed = FlatterDefaults.CheckBox.BackgroundPressed;
		public static final ColorUIResource BackgroundActive = FlatterDefaults.CheckBox.BackgroundActive;
		public static final ColorUIResource TextNormal = FlatterDefaults.CheckBox.TextNormal;
		public static final ColorUIResource TextPressed = FlatterDefaults.CheckBox.TextPressed;
		public static final ColorUIResource TextActive = FlatterDefaults.CheckBox.TextActive;
		public static final ColorUIResource TextDisabled = FlatterDefaults.CheckBox.TextDisabled;
		public static final FontUIResource Font = FlatterDefaults.CheckBox.Font;
		public static final Integer TextIconGap = FlatterDefaults.CheckBox.TextIconGap;

		public static final int IconHeight = FlatterDefaults.CheckBox.IconHeight;
		public static final int IconWidth = FlatterDefaults.CheckBox.IconWidth;
		public static final ColorUIResource IconColorEC = FlatterDefaults.CheckBox.IconColorEC; // Enabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorDC = FlatterDefaults.CheckBox.IconColorDC; // Disabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorEUC = FlatterDefaults.CheckBox.IconColorEUC; // Enabled
																									// and
																									// Unchecked
		public static final ColorUIResource IconColorDUC = FlatterDefaults.CheckBox.IconColorDUC; // Disabled
																									// and
																									// Unchecked

		public static final IconUIResource IconChecked = FlatterDefaults.CheckBox.IconChecked;
		public static final IconUIResource IconUnchecked = FlatterDefaults.CheckBox.IconUnchecked;
	}

	public abstract static class RadioButtonMenuItem
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource Background = FlatterDefaults.LighterGray;

		public static final ColorUIResource SelectionBackground = FlatterDefaults.MidGray;

		public static final int IconHeight = FlatterDefaults.CheckBox.IconHeight;
		public static final int IconWidth = FlatterDefaults.CheckBox.IconWidth;
		public static final ColorUIResource IconColorEC = FlatterDefaults.CheckBox.IconColorEC; // Enabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorDC = FlatterDefaults.CheckBox.IconColorDC; // Disabled
																								// and
																								// Checked
		public static final ColorUIResource IconColorEUC = FlatterDefaults.CheckBox.IconColorEUC; // Enabled
																									// and
																									// Unchecked
		public static final ColorUIResource IconColorDUC = FlatterDefaults.CheckBox.IconColorDUC; // Disabled
																									// and
																									// Unchecked

		public static final IconUIResource IconChecked = FlatterDefaults.CheckBox.IconChecked;
		public static final IconUIResource IconUnchecked = FlatterDefaults.CheckBox.IconUnchecked;

		public static final BorderUIResource Border = FlatterDefaults.LighterLineBorder;

		public static final IconUIResource CheckIcon = FlatterDefaults.CheckIcon;
	}

	public abstract static class ScrollBar
	{
		public static final ColorUIResource Track = FlatterDefaults.DarkGray;
		public static final ColorUIResource Thumb = FlatterDefaults.White;
		public static final ColorUIResource Arrow = FlatterDefaults.White;
		public static final Integer Width = Integer.valueOf(16);
	}

	public abstract static class ScrollPane
	{
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
	}

	public abstract static class Separator
	{
		public static final ColorUIResource Foreground = FlatterDefaults.MidGray;
		public static final ColorUIResource Background = FlatterDefaults.Background;
	}

	public abstract static class Spinner
	{
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
	}

	public abstract static class TabbedPane
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource TabBackgroundSelected = FlatterDefaults.Background;
		public static final ColorUIResource TabBackgroundNormal = FlatterDefaults.LightGray;
		public static final ColorUIResource TabForegroundSelected = FlatterDefaults.Foreground;
		public static final ColorUIResource TabForegroundNormal = FlatterDefaults.Foreground;
		public static final InsetsUIResource TabInsets = new InsetsUIResource(10, 10, 10, 10);
	}

	public abstract static class Table
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource Foreground = FlatterDefaults.Foreground;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.LightGray;
		public static final ColorUIResource ForegroundSelected = FlatterDefaults.White;
		public static final ColorUIResource Grid = FlatterDefaults.MidGray;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
		public static final ColorUIResource CellBackgroundSelected = FlatterDefaults.LighterGray;
		public static final ColorUIResource CellForegroundSelected = FlatterDefaults.White;
	}

	public abstract static class TableHeader
	{
		public static final FontUIResource Font = FlatterDefaults.Font;
		public static final ColorUIResource Background = FlatterDefaults.MidGray;
		public static final ColorUIResource Foreground = FlatterDefaults.White;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
	}

	public abstract static class TextField
	{
		public static final FontUIResource Font = FlatterDefaults.MonospaceFont;
		public static final ColorUIResource Background = FlatterDefaults.Background;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.SelectedBackground;
		public static final ColorUIResource TextNormal = FlatterDefaults.Text;
		public static final ColorUIResource TextDisabled = FlatterDefaults.DisabledText;
		public static final ColorUIResource TextSelected = FlatterDefaults.SelectedText;
		public static final BorderUIResource Border = FlatterDefaults.LineBorder;
	}

	public abstract static class TitledBorder
	{
		public static final FontUIResource Font = FlatterDefaults.Label.Font;
	}

	public abstract static class PasswordField
	{
		public static final FontUIResource Font = FlatterDefaults.TextField.Font;
		public static final ColorUIResource Background = FlatterDefaults.TextField.Background;
		public static final ColorUIResource BackgroundSelected = FlatterDefaults.TextField.BackgroundSelected;
		public static final ColorUIResource TextNormal = FlatterDefaults.TextField.TextNormal;
		public static final ColorUIResource TextDisabled = FlatterDefaults.TextField.TextDisabled;
		public static final ColorUIResource TextSelected = FlatterDefaults.TextField.TextSelected;
		public static final BorderUIResource Border = FlatterDefaults.TextField.Border;
	}
}