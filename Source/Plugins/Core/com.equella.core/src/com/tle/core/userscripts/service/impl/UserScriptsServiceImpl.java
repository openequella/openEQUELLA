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

package com.tle.core.userscripts.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.Check;
import com.tle.common.EntityPack;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.LangUtils;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.userscripts.dao.UserScriptsDao;
import com.tle.core.userscripts.service.UserScriptsService;
import com.tle.core.userscripts.service.session.UserScriptEditingBean;
import com.tle.core.userscripts.service.session.UserScriptEditingSession;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@Bind(UserScriptsService.class)
@Singleton
@SecureEntity(UserScriptsService.ENTITY_TYPE)
public class UserScriptsServiceImpl
	extends
		AbstractEntityServiceImpl<UserScriptEditingBean, UserScript, UserScriptsService> implements UserScriptsService
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(UserScriptsServiceImpl.class);

	private static final String KEY_VALIDATION_NOSCRIPT = "validation.script.empty";

	@Inject
	private UserScriptsDao scriptDao;

	@Inject
	public UserScriptsServiceImpl(UserScriptsDao scriptDao)
	{
		super(Node.USER_SCRIPTS, scriptDao);
		this.scriptDao = scriptDao;
	}

	@Override
	protected void doValidation(EntityEditingSession<UserScriptEditingBean, UserScript> session, UserScript entity,
		List<ValidationError> errors)
	{
		// Nothing
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<UserScriptEditingBean, UserScript>> SESSION createSession(
		String sessionId, EntityPack<UserScript> pack, UserScriptEditingBean bean)
	{
		return (SESSION) new UserScriptEditingSession(sessionId, pack, bean);
	}

	@Override
	protected UserScriptEditingBean createEditingBean()
	{
		return new UserScriptEditingBean();
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected void populateEditingBean(UserScriptEditingBean bean, UserScript entity)
	{
		super.populateEditingBean(bean, entity);

		if( bean.getId() != 0 )
		{
			bean.setSelection(ScriptTypes.valueOf(entity.getScriptType()));
			bean.setScript(entity.getScript());
			bean.setModuleName(entity.getModuleName());
		}

	}

	@Override
	protected void doAfterImport(TemporaryFileHandle importFolder, UserScriptEditingBean bean, UserScript entity,
		ConverterParams params)
	{
		super.doAfterImport(importFolder, bean, entity, params);
		if( bean != null )
		{
			final UserScript dbUserScript = bean.getId() != 0 ? scriptDao.findById(bean.getId()) : entity;
			dbUserScript.setScriptType(bean.getSelection().toString());
			dbUserScript.setScript(bean.getScript());
			dbUserScript.setModuleName(bean.getModuleName());
			scriptDao.saveOrUpdate(dbUserScript);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(UserScript entity, boolean checkReferences)
	{
		scriptDao.delete(entity);
		EntityFile file = new EntityFile(entity);
		fileSystemService.removeFile(file, null);
		auditLogService.logEntityDeleted(entity.getId());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public EntityPack<UserScript> startEdit(UserScript entity)
	{
		return startEditInternal(entity);
	}

	@Override
	protected void populateEntity(UserScriptEditingBean bean, UserScript userScript)
	{
		super.populateEntity(bean, userScript);

		userScript.setScript(bean.getScript());
		userScript.setScriptType(bean.getSelection().toString());
		userScript.setModuleName(bean.getModuleName());
	}

	@Override
	protected void doValidationBean(UserScriptEditingBean bean, List<ValidationError> errors)
	{
		if( Check.isEmpty(bean.getScript()) )
		{
			errors.add(new ValidationError("errors.noscript", resources.getString(KEY_VALIDATION_NOSCRIPT)));
		}
	}

	@Override
	public List<UserScript> enumerateForType(ScriptTypes type)
	{
		List<UserScript> scripts = scriptDao.enumerateForType(type);
		Collections.sort(scripts, new Comparator<UserScript>()
		{
			@Override
			public int compare(UserScript o1, UserScript o2)
			{
				return LangUtils.getString(o1.getName()).compareTo(LangUtils.getString(o2.getName()));
			}

		});
		return scripts;
	}

	@Override
	public boolean executableScriptsAvailable()
	{
		return scriptDao.enumerateForType(ScriptTypes.EXECUTABLE).size() > 0;
	}

	@Override
	public boolean displayScriptsAvailable()
	{
		return scriptDao.enumerateForType(ScriptTypes.DISPLAY).size() > 0;
	}

	@Override
	public boolean isModuleNameExist(String moduleName, long id)
	{
		return scriptDao.isModuleNameExist(moduleName, id);
	}

}
