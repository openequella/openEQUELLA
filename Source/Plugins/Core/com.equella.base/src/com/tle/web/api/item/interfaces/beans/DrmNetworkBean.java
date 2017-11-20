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

package com.tle.web.api.item.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class DrmNetworkBean
{
	private String name;
	private String startAddress;
	private String endAddress;

	public DrmNetworkBean()
	{
	}

	public DrmNetworkBean(String name, String startAddress, String endAddress)
	{
		this.name = name;
		this.startAddress = startAddress;
		this.endAddress = endAddress;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getStartAddress()
	{
		return startAddress;
	}

	public void setStartAddress(String startAddress)
	{
		this.startAddress = startAddress;
	}

	public String getEndAddress()
	{
		return endAddress;
	}

	public void setEndAddress(String endAddress)
	{
		this.endAddress = endAddress;
	}
}
