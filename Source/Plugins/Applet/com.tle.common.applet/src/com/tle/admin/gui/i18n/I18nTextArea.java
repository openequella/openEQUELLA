package com.tle.admin.gui.i18n;

import java.awt.Component;
import java.util.Locale;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

/**
 * @author Nicholas Read
 */
public class I18nTextArea extends I18nTextField
{
	private static final long serialVersionUID = 1L;
	private JTextArea ta;

	public I18nTextArea(Set<Locale> defaultLocales)
	{
		super(defaultLocales);
	}

	@Override
	protected JTextComponent getTextComponent()
	{
		ta = new JTextArea();
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		return ta;
	}

	public void setTextRows(int rows)
	{
		ta.setRows(rows);
	}

	@Override
	protected void initialiseLayout(String layoutConstraints, String cellConstraint, boolean addTextComponent)
	{
		super.initialiseLayout(layoutConstraints, "grow", addTextComponent); //$NON-NLS-1$
	}

	@Override
	protected Component prepareTextComponent(JTextComponent component)
	{
		return new JScrollPane(component);
	}
}