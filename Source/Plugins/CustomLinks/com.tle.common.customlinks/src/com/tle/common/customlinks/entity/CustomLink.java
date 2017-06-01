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

package com.tle.common.customlinks.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.i18n.CurrentLocale;

@Entity
@AccessType("field")
public class CustomLink extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(nullable = false)
	@Lob
	private String url;

	@Column(nullable = false)
	private int order;

	public CustomLink()
	{
		super();
	}

	public CustomLink(long id)
	{
		setId(id);
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public int getOrder()
	{
		return order;
	}

	@Override
	@SuppressWarnings("nls")
	public String toString()
	{
		return CurrentLocale.get(getName(), "") + " - " + url;
	}
}
