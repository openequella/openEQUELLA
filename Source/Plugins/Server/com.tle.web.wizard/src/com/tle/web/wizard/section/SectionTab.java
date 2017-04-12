package com.tle.web.wizard.section;

import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;

public class SectionTab
{
	private Label name;

	private boolean enabled;
	private boolean current;
	private boolean invalid;
	private String sectionName;
	private String data;
	private JSHandler onclick;
	private SectionTabable tabSection;

	public SectionTab(SectionTabable section, Label name, String data)
	{
		this.name = name;
		this.tabSection = section;
		this.sectionName = section.getSectionId();
		this.data = data;
		this.enabled = true;
	}

	public boolean getClickable()
	{
		return enabled && !current;
	}

	public Label getName()
	{
		return name;
	}

	public String getInvalidString()
	{
		if( invalid )
		{
			return " *"; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isCurrent()
	{
		return current;
	}

	public void setCurrent(boolean current)
	{
		this.current = current;
	}

	public boolean isInvalid()
	{
		return invalid;
	}

	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public JSHandler getOnclick()
	{
		return onclick;
	}

	public void setOnclick(JSHandler onclick)
	{
		this.onclick = onclick;
	}

	public String getSectionName()
	{
		return sectionName;
	}

	public SectionTabable getTabSection()
	{
		return tabSection;
	}

	public String getHref()
	{
		return "#"; //$NON-NLS-1$
	}

	public String getUniqueId()
	{
		return sectionName + '_' + data;
	}
}
