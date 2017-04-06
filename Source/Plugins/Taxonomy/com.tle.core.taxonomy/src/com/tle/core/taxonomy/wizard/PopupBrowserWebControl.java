package com.tle.core.taxonomy.wizard;

import javax.inject.Inject;

import com.tle.core.guice.Bind;

@Bind
class PopupBrowserWebControl extends AbstractPopupBrowserWebControl
{
	@Inject
	public PopupBrowserWebControl(PopupBrowserDialog popupBrowserDialog)
	{
		super(popupBrowserDialog);
	}
}
