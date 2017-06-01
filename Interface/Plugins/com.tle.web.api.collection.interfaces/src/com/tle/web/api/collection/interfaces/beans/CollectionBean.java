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

package com.tle.web.api.collection.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

@XmlRootElement
public class CollectionBean extends BaseEntityBean
{
	private BaseEntityReference schema;
	private BaseEntityReference workflow;
	private Integer reviewPeriod;
	private CollectionSecurityBean security;
	private String filestoreId;

	public BaseEntityReference getSchema()
	{
		return schema;
	}

	public void setSchema(BaseEntityReference schema)
	{
		this.schema = schema;
	}

	public BaseEntityReference getWorkflow()
	{
		return workflow;
	}

	public void setWorkflow(BaseEntityReference workflow)
	{
		this.workflow = workflow;
	}

	@Override
	public CollectionSecurityBean getSecurity()
	{
		return security;
	}

	public void setSecurity(CollectionSecurityBean security)
	{
		this.security = security;
	}

	public Integer getReviewPeriod()
	{
		return reviewPeriod;
	}

	public void setReviewPeriod(Integer reviewPeriod)
	{
		this.reviewPeriod = reviewPeriod;
	}

	public String getFilestoreId()
	{
		return filestoreId;
	}

	public void setFilestoreId(String filestoreId)
	{
		this.filestoreId = filestoreId;
	}
}
