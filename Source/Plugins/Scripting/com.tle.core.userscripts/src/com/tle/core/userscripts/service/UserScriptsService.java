package com.tle.core.userscripts.service;

import java.util.List;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.userscripts.service.session.UserScriptEditingBean;

public interface UserScriptsService extends AbstractEntityService<UserScriptEditingBean, UserScript>
{
	@SuppressWarnings("nls")
	public static final String ENTITY_TYPE = "USER_SCRIPTS";

	List<UserScript> enumerateForType(ScriptTypes type);

	boolean executableScriptsAvailable();

	boolean displayScriptsAvailable();

	boolean isModuleNameExist(String moduleName, long id);
}
