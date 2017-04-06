package com.tle.web.wizard.controls;

public abstract class AbstractSimpleWebControl extends AbstractWebControl<WebControlModel>
{

	@Override
	public final Class<WebControlModel> getModelClass()
	{
		return WebControlModel.class;
	}

}
