package com.tle.web.wizard.page;

import java.io.Serializable;

public class WebWizardPageState implements Serializable
{
	private static final long serialVersionUID = 1;
	private boolean enabled = true;
	private boolean submitted;
	private boolean viewable;
	private boolean valid;
	private boolean showMandatory;

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isShowMandatory()
	{
		return showMandatory;
	}

	public void setShowMandatory(boolean showMandatory)
	{
		this.showMandatory = showMandatory;
	}

	public boolean isSubmitted()
	{
		return submitted;
	}

	public void setSubmitted(boolean submitted)
	{
		this.submitted = submitted;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public boolean isViewable()
	{
		return viewable;
	}

	public void setViewable(boolean viewable)
	{
		this.viewable = viewable;
	}

}
