package com.tle.core.taxonomy.wizard;

import javax.inject.Inject;

import com.tle.core.guice.Bind;

@Bind
class WidePopupBrowserWebControl extends AbstractPopupBrowserWebControl
{
	@Inject
	public WidePopupBrowserWebControl(WidePopupBrowserDialog popupBrowserDialog)
	{
		super(popupBrowserDialog);
	}
}
