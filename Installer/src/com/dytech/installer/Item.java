package com.dytech.installer;

import javax.swing.AbstractButton;

public class Item
{
	protected String name;
	protected String value;
	protected boolean selected;
	protected AbstractButton button;

	public Item()
	{
		// Nothing to do here
	}

	public Item(String name, String value, boolean selected)
	{
		this.name = name;
		this.value = value;
		this.selected = selected;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setButton(AbstractButton button)
	{
		this.button = button;
	}

	public AbstractButton getButton()
	{
		return button;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
