/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
