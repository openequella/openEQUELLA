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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = GenericFileBean.class)
public class GenericFileBean extends AbstractExtendableBean
{
	public static final String TYPE = "generic";

	private String filename;
	private GenericFileBean parent;

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	public GenericFileBean getParent()
	{
		return parent;
	}

	public void setParent(GenericFileBean parent)
	{
		this.parent = parent;
	}
}
