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

package com.tle.common.userscripts.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

@Entity
@AccessType("field")
public final class UserScript extends BaseEntity
{
	@SuppressWarnings("nls")
	public static final String ENTITY_TYPE = "SCRIPT";

	@Column(length = 16, nullable = false)
	private String scriptType;
	@Lob
	private String script;
	@Column(length = 64)
	@Index(name = "userScriptModuleIndex")
	private String moduleName;

	public String getScriptType()
	{
		return scriptType;
	}

	public void setScriptType(String scriptType)
	{
		this.scriptType = scriptType;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	public void setModuleName(String moduleName)
	{
		this.moduleName = moduleName;
	}
}
