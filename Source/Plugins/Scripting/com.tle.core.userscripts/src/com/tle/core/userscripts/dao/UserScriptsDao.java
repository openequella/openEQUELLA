package com.tle.core.userscripts.dao;

import java.util.List;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.dao.AbstractEntityDao;

public interface UserScriptsDao extends AbstractEntityDao<UserScript>
{

	List<UserScript> enumerateForType(ScriptTypes type);

	boolean isModuleNameExist(String moduleName, long id);

}
