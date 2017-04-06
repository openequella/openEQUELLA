package com.tle.core.userscripts.service.session;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.core.services.entity.EntityEditingBean;

public class UserScriptEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private ScriptTypes selection;
	private String script;
	private String moduleName;

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public ScriptTypes getSelection()
	{
		return selection;
	}

	public void setSelection(ScriptTypes selection)
	{
		this.selection = selection;
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
