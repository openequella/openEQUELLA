package com.tle.web.echo.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class EchoAttachmentData
{
	@JsonUnwrapped
	private EchoData echoData = new EchoData();

	private List<EchoPresenter> presenters;

	public EchoAttachmentData()
	{
		// Nothing
	}

	public EchoAttachmentData(EchoData echoData, List<EchoPresenter> presenters)
	{
		this.echoData = echoData;
		this.presenters = presenters;
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
