package com.tle.web.echo.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class EchoAttachmentBean extends EquellaAttachmentBean
{
	@JsonUnwrapped
	private EchoData echoData = new EchoData();

	private List<EchoPresenter> presenters;

	@Override
	public String getRawAttachmentType()
	{
		return "custom/echo";
	}

	public List<EchoPresenter> getPresenters()
	{
		return presenters;
	}

	public void setPresenters(List<EchoPresenter> presenters)
	{
		this.presenters = presenters;
	}

	public EchoData getEchoData()
	{
		return echoData;
	}

	public void setEchoData(EchoData echoData)
	{
		this.echoData = echoData;
	}
}
