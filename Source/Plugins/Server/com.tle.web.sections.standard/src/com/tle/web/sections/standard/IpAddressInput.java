package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlIpAddressInputState;

public class IpAddressInput extends AbstractValueStateComponent<HtmlIpAddressInputState, JSValueComponent>
{

	public IpAddressInput()
	{
		super(RendererConstants.IP);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return setupState(info, new HtmlIpAddressInputState());
	}

	public String getValue(SectionInfo info)
	{
		return getState(info).getValue();
	}

	public void setValue(SectionInfo info, String value)
	{
		getState(info).setValue(value);
	}

	@Override
	protected String getBookmarkStringValue(HtmlIpAddressInputState state)
	{
		return state.getValue();
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		// nah mate
	}
}
