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
