package com.dytech.gui.flatter;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class FlatterRadioButtonUI extends FlatterCheckBoxUI
{
	private static final FlatterRadioButtonUI m_buttonUI = new FlatterRadioButtonUI();

	public FlatterRadioButtonUI()
	{
		// Nothing to do here
	}

	public static ComponentUI createUI(JComponent c)
	{
		return m_buttonUI;
	}

	@Override
	public synchronized void installUI(JComponent c)
	{
		super.installUI(c);

		mBackgroundNormal = UIManager.getColor("RadioButton.background");
		mBackgroundPressed = UIManager.getColor("RadioButton.backgroundPressed");
		mBackgroundActive = UIManager.getColor("RadioButton.backgroundActive");
		mTextNormal = UIManager.getColor("RadioButton.textNormal");
		mTextPressed = UIManager.getColor("RadioButton.textPressed");
		mTextActive = UIManager.getColor("RadioButton.textActive");
		mTextDisabled = UIManager.getColor("RadioButton.textDisabled");
		mIconChecked = UIManager.getIcon("RadioButton.iconChecked");
		mIconUnchecked = UIManager.getIcon("RadioButton.iconUnchecked");
		mIconPressedChecked = UIManager.getIcon("RadioButton.iconPressedChecked");
		mIconPressedUnchecked = UIManager.getIcon("RadioButton.iconPressedUnchecked");

		c.setBackground(mBackgroundNormal);
		c.addMouseListener(this);
	}
}
