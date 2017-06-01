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

package com.tle.common.taxonomy;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public class Taxonomy extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 100)
	private String dataSourcePluginId;

	public Taxonomy()
	{
		super();
	}

	public Taxonomy(long id)
	{
		setId(id);
	}

	public String getDataSourcePluginId()
	{
		return dataSourcePluginId;
	}

	public void setDataSourcePluginId(String dataSourcePluginId)
	{
		this.dataSourcePluginId = dataSourcePluginId;
	}
}
