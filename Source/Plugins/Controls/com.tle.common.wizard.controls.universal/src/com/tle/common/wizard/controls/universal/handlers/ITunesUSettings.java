package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * TODO: this should be in it's own plugin!
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ITunesUSettings extends UniversalSettings
{
	private static final String INSTITUTION_ID = "institutionId";

	public ITunesUSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public ITunesUSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public String getInstitutionId()
	{
		return (String) wrapped.getAttributes().get(INSTITUTION_ID);
	}

	public void setInstitutionId(String institutionId)
	{
		wrapped.getAttributes().put(INSTITUTION_ID, institutionId);
	}
}
